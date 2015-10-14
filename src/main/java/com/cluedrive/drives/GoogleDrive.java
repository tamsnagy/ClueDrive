package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Tamas on 2015-10-01.
 */
public class GoogleDrive implements ClueDrive {
    private Drive client;

    public GoogleDrive(Drive client) {
        this.client = client;
    }

    @Override
    public List<CResource> list(CPath path) throws ClueException {
        ChildList result = null;
        try {
            String rootId = client.about().get().execute().getRootFolderId();
            result = client.children().list(rootId).execute();
        } catch (IOException e) {
            throw new ClueException(e);
        }
        List<ChildReference> children = result.getItems();
        List<CResource> responseList = new ArrayList<>();
        if (children == null || children.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (ChildReference ch : children) {
                try {
                    File file = client.files().get(ch.getId()).execute();
                    CPath fileName = CPath.create(path, file.getTitle());
                    if("application/vnd.google-apps.folder".equals(file.getMimeType())) {
                        responseList.add(new CDirectory(fileName));
                    } else {
                        Date date = (file.getModifiedDate() == null) ? null : new Date(file.getModifiedDate().getValue());
                        long size = 0;
                        if(file.keySet().contains("fileSize")) {
                             size = file.getFileSize();
                        }
                        responseList.add(new CFile(fileName, size, date));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseList;
    }

    @Override
    public void setClient(String accessToken) {

    }

    @Override
    public void createFolder(CPath path) {

    }
}
