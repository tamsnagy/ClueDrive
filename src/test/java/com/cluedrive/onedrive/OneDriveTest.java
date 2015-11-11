package com.cluedrive.onedrive;

import com.cluedrive.ClueDriveTest;
import com.cluedrive.commons.*;
import com.cluedrive.drives.OneDrive;
import com.cluedrive.exception.ClueException;
import com.cluedrive.onedrive.request.CreateFolderRequest;
import com.cluedrive.onedrive.response.ChildrenList;
import com.cluedrive.onedrive.response.CreateFolderResponse;
import com.cluedrive.onedrive.response.Item;
import org.junit.BeforeClass;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Tamas on 2015-10-11.
 */
public class OneDriveTest extends ClueDriveTest{
    private ClueDrive drive;
    private static RestTemplate restTemplate;
    private static URLUtility url;
    private static final String LIST_FILTERS = "id,name,size,createdDateTime,lastModifiedDateTime,folder,file";

     @Override
    protected void format() throws ClueException {
        OneDrive oDrive = ((OneDrive) drive);
        if (oDrive.getDriveId() == null) {
            oDrive.setDefaultDrive();
        }
        CFolder baseFolderCandidate = null;
        ResponseEntity<ChildrenList> rootChildren = restTemplate.getForEntity(
                url.base().toString(),
                ChildrenList.class);
        for (Item rootItem : rootChildren.getBody().getValue()) {
            if (rootItem.getFolder() != null && BASE_FOLDER_NAME.equals(rootItem.getName())) {
                ResponseEntity<ChildrenList> removableItems = restTemplate.getForEntity(
                        url.base().route("children").filter(LIST_FILTERS).toString(),
                        ChildrenList.class);
                baseFolderCandidate = new CFolder(CPath.create("/" + BASE_FOLDER_NAME), rootItem.getId());
                for (Item removableItem : removableItems.getBody().getValue()) {
                    restTemplate.delete(url.base().segment(removableItem.getId()).toString());
                }
                continue;
            }
        }
        if (baseFolderCandidate == null) {
            ResponseEntity<Item> root = restTemplate.getForEntity(
                    url.base().toString(),
                    Item.class);
            ResponseEntity<CreateFolderResponse> response = restTemplate.postForEntity(
                    url.base().route("children").toString(),
                    new CreateFolderRequest(BASE_FOLDER_NAME),
                    CreateFolderResponse.class);
            baseFolderCandidate = new CFolder(CPath.create("/"+BASE_FOLDER_NAME), response.getBody().getId());
        }
        baseFolder = baseFolderCandidate;
    }

    @Override
    protected void driveSpecificSetup() throws IOException {
        Properties properties = new Properties();
        InputStream config = OneDriveTest.class.getClassLoader().getResourceAsStream("config.properties");
        properties.load(config);
        restTemplate = new RestTemplate();
        String clientId = properties.getProperty("microsoftOnedrive.clientId");
        String scope = "wl.signin wl.offline_access onedrive.readwrite onedrive.appfolder";
        ResponseEntity<String> response = restTemplate.getForEntity(new StringBuilder()
                        .append("https://login.live.com/oauth20_authorize.srf?client_id=")
                        .append(clientId)
                        .append("&scope=")
                        .append(scope)
                        .append("&response_type=token&redirect_uri=https://login.live.com/oauth20_desktop.srf")
                        .toString(),
                String.class);
        System.out.println("Login Response: " + response);

        String accessToken = properties.getProperty("microsoftOnedrive.accessToken");
        drive = new OneDrive();
        drive.setClient(accessToken);

        url = new URLUtility(OneDrive.URI_BASE, accessToken);
    }

    @Override
    protected void listSetup() throws ClueException {

    }
}
