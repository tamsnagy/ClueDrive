package com.cluedrive.onedrive;

import com.cluedrive.ClueDriveTest;
import com.cluedrive.commons.*;
import com.cluedrive.drives.OneDrive;
import com.cluedrive.exception.ClueException;
import com.cluedrive.onedrive.request.CreateFolderRequest;
import com.cluedrive.onedrive.response.ChildrenList;
import com.cluedrive.onedrive.response.CreateFolderResponse;
import com.cluedrive.onedrive.response.Item;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by Tamas on 2015-10-11.
 */
public class OneDriveTest extends ClueDriveTest{
    private static RestTemplate restTemplate;
    private static URLUtility url;
    private static final String LIST_FILTERS = "id,name,size,createdDateTime,lastModifiedDateTime,folder,file";

    private ObjectMapper MAPPER;
    private HttpHeaders headers;

    public OneDriveTest() {
        MAPPER = new ObjectMapper();
        MAPPER.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        headers = new HttpHeaders();
        restTemplate = new RestTemplate();
    }

    @Override
    protected void format() throws ClueException {
        CFolder baseFolderCandidate = null;
         try {
             HttpEntity entity = new HttpEntity(headers);
             String requestUrl = url.base().route("children").toString();
             ResponseEntity<ChildrenList> rootChildren = restTemplate.exchange(
                     requestUrl,
                     HttpMethod.GET,
                     entity,
                     ChildrenList.class
                     );
            for (Item rootItem : rootChildren.getBody().getValue()) {
                if (rootItem.getFolder() != null && BASE_FOLDER_NAME.equals(rootItem.getName())) {
                    ResponseEntity<ChildrenList> removableItems = restTemplate.exchange(
                            url.base().segment(baseFolder.getRemotePath()).route("children").filter(LIST_FILTERS).toString(),
                            HttpMethod.GET,
                            entity,
                            ChildrenList.class);
                    baseFolderCandidate = new CFolder(CPath.create("/" + BASE_FOLDER_NAME));
                    for (Item removableItem : removableItems.getBody().getValue()) {
                        restTemplate.exchange(
                                url.base().segment(CPath.create(baseFolder.getRemotePath(), removableItem.getName())).toString(),
                                HttpMethod.DELETE,
                                entity,
                                Item.class
                        );
                    }
                    continue;
                }
            }
            if (baseFolderCandidate == null) {
                    HttpEntity<byte[]> entity2 = new HttpEntity<>(MAPPER.writeValueAsBytes(new CreateFolderRequest(BASE_FOLDER_NAME)), headers);
                    ResponseEntity<CreateFolderResponse> response2 = restTemplate.exchange(
                            requestUrl,
                            HttpMethod.POST,
                            entity2,
                            CreateFolderResponse.class);

                baseFolderCandidate = new CFolder(CPath.create("/"+response2.getBody().getName()));
            }
         } catch (HttpClientErrorException e) {
             System.out.println(e.getStatusCode());
             System.out.println(e.getMessage());
             System.out.println(e.getResponseBodyAsString());
             throw new ClueException(e);
         } catch (IOException e) {
             throw new ClueException(e);
         }
         baseFolder = baseFolderCandidate;
    }

    @Override
    protected void driveSpecificSetup() throws IOException {
        Properties properties = new Properties();
        try(InputStream config = new FileInputStream(Paths.get("build/resources/test/config.properties").toFile())) {
            properties.load(config);
        }

        String accessToken = properties.getProperty("microsoftOnedrive.accessToken");
        drive = new OneDrive();
        drive.setAccessToken(accessToken);

        url = new URLUtility(OneDrive.URI_BASE);

        headers.set("Authorization", "Bearer " + drive.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    protected void listSetup() throws ClueException {
        try {
            String folder1 = "folder1", folder2 = "folder2", folder3 = "folder3";
            HttpEntity<byte[]> entity1 = new HttpEntity<>(MAPPER.writeValueAsBytes(new CreateFolderRequest(folder1)), headers);
            HttpEntity<byte[]> entity2 = new HttpEntity<>(MAPPER.writeValueAsBytes(new CreateFolderRequest(folder2)), headers);
            HttpEntity<byte[]> entity3 = new HttpEntity<>(MAPPER.writeValueAsBytes(new CreateFolderRequest(folder3)), headers);
            restTemplate.exchange(
                    url.base().segment(baseFolder.getRemotePath(), folder1).query("nameConflict", "rename").toString(),
                    HttpMethod.PUT,
                    entity1,
                    CreateFolderResponse.class);
            CFolder cFolder2 = new CFolder(CPath.create(baseFolder.getRemotePath(), folder2));
            restTemplate.exchange(
                    url.base().segment(baseFolder.getRemotePath(), folder2).query("nameConflict", "rename").toString(),
                    HttpMethod.PUT,
                    entity2,
                    CreateFolderResponse.class);
            restTemplate.exchange(
                    url.base().segment(cFolder2.getRemotePath(), folder3).query("nameConflict", "rename").toString(),
                    HttpMethod.PUT,
                    entity3,
                    CreateFolderResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
