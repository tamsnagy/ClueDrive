package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.onedrive.request.CreateFolderRequest;
import com.cluedrive.onedrive.response.ChildrenList;
import com.cluedrive.onedrive.response.CreateFolderResponse;
import com.cluedrive.onedrive.response.DriveMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Tamas on 2015-10-01.
 */
public class OneDrive implements ClueDrive {
    private String accessToken;
    private String driveId = null;
    private RestTemplate restTemplate = new RestTemplate();
    public static final String URI_BASE = "https://api.onedrive.com/v1.0/drive/special/approot";
    private static final String LIST_FILTERS = "id,name,size,createdDateTime,lastModifiedDateTime,folder,file";
    private URLUtility url;

    @Override
    public List<CResource> list(CPath path) throws ClueException {
        if(driveId == null) {
            setDefaultDrive();
        }
        ResponseEntity<ChildrenList> response = restTemplate.getForEntity(
                url.base().route("children").filter(LIST_FILTERS).toString(),
                ChildrenList.class);
        return response.getBody().getValue().stream().map(item -> {
            CPath resourcePath = CPath.create(path, item.getName());
            if(item.getFile() != null) {
                return new CFile(resourcePath, item.getId(), item.getSize(), item.getLastModifiedDateTime());
            }else {
                return new CFolder(resourcePath, item.getId());
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void setClient(String accessToken) {
        this.accessToken = accessToken;
        url = new URLUtility(URI_BASE, accessToken);
    }

    @Override
    public CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException {
        if(driveId == null) {
            setDefaultDrive();
        }
        /*CreateFolderResponse response = restTemplate.postForObject(
                url.base().route("drive").route("items").segment(parentFolder.getId())
                        .route("children").toString(),
                new CreateFolderRequest(folderName),
                CreateFolderResponse.class);
        return new CFolder(CPath.create(parentFolder.getRemotePath(), folderName), response.getId());*/
        return null;
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

    }

    @Override
    public CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException {
        return null;
    }

    public void setDefaultDrive() {
        ResponseEntity<DriveMetadata> response = restTemplate.getForEntity("https://api.onedrive.com/v1.0/drive?access_token=" + accessToken, DriveMetadata.class);
        driveId = response.getBody().getId();
    }

    public String getDriveId() {
        return driveId;
    }


}
