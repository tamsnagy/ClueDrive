package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.onedrive.request.CreateFolderRequest;
import com.cluedrive.onedrive.response.ChildrenList;
import com.cluedrive.onedrive.response.CreateFolderResponse;
import com.cluedrive.onedrive.response.DriveMetadata;
import com.cluedrive.onedrive.response.Item;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Tamas on 2015-10-01.
 */
public class OneDrive extends ClueDrive {
    private RestTemplate restTemplate = new RestTemplate();
    public static final String URI_BASE = "https://api.onedrive.com/v1.0/drive/special/approot";
    private static final String LIST_FILTERS = "name,size,createdDateTime,lastModifiedDateTime,folder,file";
    private URLUtility url;
    private HttpHeaders headers;
    private ObjectMapper MAPPER;

    public OneDrive() {
        headers = new HttpHeaders();
        MAPPER = new ObjectMapper();
        MAPPER.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public List<CResource> list(CPath path) throws ClueException {

        ResponseEntity<ChildrenList> response;
        HttpEntity entity = new HttpEntity(headers);
        if(path.isRootPath()) {
            response = restTemplate.exchange(
                    url.base().route("children").filter(LIST_FILTERS).toString(),
                    HttpMethod.GET,
                    entity,
                    ChildrenList.class
            );
        } else {
            response = restTemplate.exchange(
                    url.base().segment(path).route("children").filter(LIST_FILTERS).toString(),
                    HttpMethod.GET,
                    entity,
                    ChildrenList.class
            );
        }
        return response.getBody().getValue().stream().map(item -> {
            CPath resourcePath = CPath.create(path, item.getName());
            if(item.getFile() != null) {
                return new CFile(resourcePath, item.getSize(), item.getLastModifiedDateTime());
            }else {
                return new CFolder(resourcePath);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void setToken(String accessToken) {
        this.accessToken = accessToken;
        url = new URLUtility(URI_BASE);
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
    }

    @Override
    public CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException {
        try {
            HttpEntity<byte[]> entity = new HttpEntity<>(MAPPER.writeValueAsBytes(new CreateFolderRequest(folderName)), headers);
            ResponseEntity<CreateFolderResponse> response = restTemplate.exchange(
                    url.base().segment(parentFolder.getRemotePath(), folderName).query("nameConflict", "rename").toString(),
                    HttpMethod.PUT,
                    entity,
                    CreateFolderResponse.class);
            return new CFolder(CPath.create(parentFolder.getRemotePath(), response.getBody().getName()));
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public CFolder getRootFolder() throws ClueException {
        return null;
    }

    @Override
    public CFile uploadFile(CFolder remotePath, Path localPath) throws ClueException, FileNotFoundException {
        return null;
    }

    @Override
    public void delete(CResource resource) throws ClueException {
        HttpEntity entity = new HttpEntity(headers);
        restTemplate.exchange(
                url.base().segment(resource.getRemotePath()).toString(),
                HttpMethod.DELETE,
                entity,
                Item.class
        );
    }

    @Override
    public CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException {
        return null;
    }

}
