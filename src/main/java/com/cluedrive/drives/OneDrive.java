package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.onedrive.request.CreateFileRequest;
import com.cluedrive.onedrive.request.CreateFolderRequest;
import com.cluedrive.onedrive.response.ChildrenList;
import com.cluedrive.onedrive.response.CreateFolderResponse;
import com.cluedrive.onedrive.response.Item;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
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
    private HttpHeaders jsonHeaders;
    private ObjectMapper MAPPER;

    public OneDrive() {
        jsonHeaders = new HttpHeaders();
        MAPPER = new ObjectMapper();
        MAPPER.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public List<CResource> list(CPath path) throws ClueException {

        ResponseEntity<ChildrenList> response;
        HttpEntity entity = new HttpEntity(jsonHeaders);
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
        jsonHeaders.set("Authorization", "Bearer " + accessToken);
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException {
        try {
            HttpEntity<byte[]> entity = new HttpEntity<>(MAPPER.writeValueAsBytes(new CreateFolderRequest(folderName)), jsonHeaders);
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
    public CFile uploadFile(CFolder remoteFolder, Path localPath) throws ClueException, FileNotFoundException {
        try {
            String fileName = localPath.getFileName().toString();

            HttpHeaders multipartHeader = new HttpHeaders();
            multipartHeader.set("Authorization", "Bearer " + accessToken);
            multipartHeader.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> fileEntity;
            try(InputStream inputStream = new FileInputStream(localPath.toFile());
                ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder()) {
                int read;
                int length = 0;
                byte[] bytes = new byte[4096];
                while ((read = inputStream.read(bytes)) != -1) {
                    byteArrayBuilder.write(bytes, length, length + read);
                    length += read;
                }
                fileEntity = new HttpEntity<>(byteArrayBuilder.toByteArray(), multipartHeader);
            }
            // Uploading the files content. It creates the item based on the name.
            ResponseEntity<Item> fileResponse = restTemplate.exchange(
                    url.base().segment(remoteFolder.getRemotePath(), fileName).route("content").query("nameConflict", "rename").toString(),
                    HttpMethod.PUT,
                    fileEntity,
                    Item.class);

            CFile returnFile = new CFile(CPath.create(remoteFolder.getRemotePath(), fileResponse.getBody().getName()),
                    fileResponse.getBody().getSize(), fileResponse.getBody().getLastModifiedDateTime());
            returnFile.setLocalPath(localPath);
            return returnFile;
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public void delete(CResource resource) throws ClueException {
        HttpEntity entity = new HttpEntity(jsonHeaders);
        restTemplate.exchange(
                url.base().segment(resource.getRemotePath()).toString(),
                HttpMethod.DELETE,
                entity,
                Item.class
        );
    }

    @Override
    public CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException {
        HttpHeaders rangeHeader = new HttpHeaders();
        rangeHeader.set("Authorization", "Bearer " + accessToken);
        //rangeHeader.set("Range", "bytes=0-1023");
        HttpEntity entity = new HttpEntity(rangeHeader);
        ResponseEntity<Item> fileResponse = restTemplate.exchange(
                url.base().segment(remoteFile.getRemotePath()).route("content").toString(),
                HttpMethod.GET,
                entity,
                Item.class);
        return null;
    }

}
