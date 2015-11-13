package com.cluedrive.onedrive;

import com.cluedrive.ClueDriveTest;
import com.cluedrive.commons.*;
import com.cluedrive.drives.OneDrive;
import com.cluedrive.exception.ClueException;
import com.cluedrive.onedrive.request.CreateFolderRequest;
import com.cluedrive.onedrive.response.ChildrenList;
import com.cluedrive.onedrive.response.CreateFolderResponse;
import com.cluedrive.onedrive.response.Item;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Tamas on 2015-10-11.
 */
public class OneDriveTest extends ClueDriveTest{
    private static RestTemplate restTemplate;
    private static URLUtility url;
    private static final String LIST_FILTERS = "id,name,size,createdDateTime,lastModifiedDateTime,folder,file";

    //TODO: remove
    private static boolean login = false;

     @Override
    protected void format() throws ClueException {
        CFolder baseFolderCandidate = null;
         try {
             HttpHeaders headers = new HttpHeaders();
             headers.set("Authorization", "Bearer " +drive.getToken());
             headers.set("Content-Type", "application/json");
             HttpEntity entity = new HttpEntity(headers);
             String requestUrl = url.base().route("children").simpleString();
             System.out.println("token: " + drive.getToken());
             System.out.println("url: " + requestUrl);
             ResponseEntity<ChildrenList> rootChildren = restTemplate.exchange(
                     requestUrl,
                     HttpMethod.GET,
                     entity,
                     ChildrenList.class
                     );

            for (Item rootItem : rootChildren.getBody().getValue()) {
                if (rootItem.getFolder() != null && BASE_FOLDER_NAME.equals(rootItem.getName())) {
                    ResponseEntity<ChildrenList> removableItems = restTemplate.getForEntity(
                            url.base().segment(baseFolder.getRemotePath()).route("children").filter(LIST_FILTERS).toString(),
                            ChildrenList.class);
                    baseFolderCandidate = new CFolder(CPath.create("/" + BASE_FOLDER_NAME));
                    for (Item removableItem : removableItems.getBody().getValue()) {
                        restTemplate.delete(url.base()
                                .segment(CPath.create(baseFolder.getRemotePath(), removableItem.getName()))
                                .toString());
                    }
                    continue;
                }
            }
            if (baseFolderCandidate == null) {

                    HttpHeaders headers2 = new HttpHeaders();
                    headers2.set("Authorization", "Bearer " +drive.getToken());
                    headers2.set("Content-Type", "application/json");
                 ObjectMapper MAPPER = new ObjectMapper();
                MAPPER.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
                    HttpEntity<byte[]> entity2 = new HttpEntity<>(MAPPER.writeValueAsBytes(new CreateFolderRequest(BASE_FOLDER_NAME)), headers2);
                    System.out.println(headers2);
                    //CookieRestTemplate cookieRestTemplate = new CookieRestTemplate(Arrays.asList("MSFPC=ID=7927d2139610224eb935bbce8dd3d1b7&CS=3&LV=201509&V=1", "_ga=GA1.2.1425413201.1442182321"));
                    ResponseEntity<CreateFolderResponse> response2 = restTemplate.exchange(
                            requestUrl,
                            HttpMethod.POST,
                            entity2,
                            CreateFolderResponse.class);

                baseFolderCandidate = new CFolder(CPath.create("/"+BASE_FOLDER_NAME));
            }
         } catch (HttpClientErrorException e) {
             System.out.println(e.getStatusCode());
             System.out.println(e.getMessage());
             System.out.println(e.getResponseBodyAsString());
             throw new ClueException(e);
         } catch (JsonMappingException e) {
             e.printStackTrace();
         } catch (JsonGenerationException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         baseFolder = baseFolderCandidate;
    }

    @Override
    protected void driveSpecificSetup() throws IOException {
        Properties properties = new Properties();
        InputStream config = OneDriveTest.class.getClassLoader().getResourceAsStream("config.properties");
        properties.load(config);
        restTemplate = new RestTemplate();
        if(login) {
            login = false;
            String clientId = properties.getProperty("microsoftOnedrive.clientId");
            String scope = "wl.signin%20onedrive.readwrite";
            try {
                Desktop.getDesktop().browse(new URI(new StringBuilder()
                        .append("https://login.live.com/oauth20_authorize.srf?client_id=")
                        .append(clientId)
                        .append("&scope=")
                        .append(scope)
                        .append("&response_type=token&redirect_uri=https://login.live.com/oauth20_desktop.srf")
                        .toString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
           /* ResponseEntity<String> response = restTemplate.getForEntity(new StringBuilder()
                            .append("https://login.live.com/oauth20_authorize.srf?client_id=")
                            .append(clientId)
                            .append("&scope=")
                            .append(scope)
                            .append("&response_type=token&redirect_uri=https://login.live.com/oauth20_desktop.srf")
                            .toString(),
                    String.class);
            System.out.println("Login Response: " + response);*/
        }

        properties.load(config);
        String accessToken = properties.getProperty("microsoftOnedrive.accessToken");
        drive = new OneDrive();
        drive.setToken(accessToken);

        url = new URLUtility(OneDrive.URI_BASE, accessToken);
    }

    @Override
    protected void listSetup() throws ClueException {
        /*CreateFolderResponse response = */restTemplate.put(
                url.base().segment(baseFolder.getRemotePath(), "folder1").query("nameConflict", "replace").toString(),
                new CreateFolderRequest("folder1")/*,
                CreateFolderResponse.class*/);
    }
}
