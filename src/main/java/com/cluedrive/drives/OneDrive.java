package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.IllegalPathException;
import com.cluedrive.onedrive.response.ChildrenList;
import com.cluedrive.onedrive.response.DriveMetadata;
import com.cluedrive.onedrive.response.Item;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Tamas on 2015-10-01.
 */
public class OneDrive implements ClueDrive {
    private String accessToken;
    private String driveId = null;
    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<CResource> list(CPath path) throws ClueException {
        if(driveId == null) {
            setDefaultDrive();
        }
        ResponseEntity<ChildrenList> response = restTemplate.getForEntity(
                "https://api.onedrive.com/v1.0/drive/items/root:"+ path.toString() +":/children?access_token=" + accessToken +
                        "&select=name,size,createdDateTime,lastModifiedDateTime,folder,file",
                ChildrenList.class);
        return response.getBody().getValue().stream().map(item -> {
            CPath resourcePath = CPath.create(path, item.getName());
            if(item.getFile() != null) {
                return new CFile(resourcePath, item.getSize(), item.getLastModifiedDateTime());
            }else {
                return new CDirectory(resourcePath);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void setClient(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void createFolder(CPath path) {

    }

    public void setDefaultDrive() {
        ResponseEntity<DriveMetadata> response = restTemplate.getForEntity("https://api.onedrive.com/v1.0/drive?access_token=" + accessToken, DriveMetadata.class);
        driveId = response.getBody().getId();
    }
}
