package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.NotExistingPathException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;

import java.io.IOException;
import java.util.*;

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
        List<CResource> responseList = new ArrayList<>();
        try {

            // Access the wanted folder, from root.
            String rootId = client.about().get().execute().getRootFolderId();
            Map<String, File> children = listChildren(rootId);
            if(! "/".equals(path.toString())) {
                String[] pathParts = path.toString().split("/");

                for(int i = 1; i < pathParts.length; i++) {
                    if(! children.containsKey(pathParts[i])) {
                        throw new NotExistingPathException(path);
                    }
                    children = listChildren(children.get(pathParts[i]).getId());
                }
            }

            // convert Google's response to common CResources
            for (File file : children.values()) {
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
            }

        } catch (IOException e) {
            throw new ClueException(e);
        }
        return responseList;
    }

    @Override
    public void setClient(String accessToken) {

    }

    @Override
    public void createFolder(CPath path) {

    }

    private Map<String, File> listChildren(String id) throws ClueException {
        Map<String, File> children = new HashMap<>();
        try {
            for(ChildReference childRef : client.children().list(id).execute().getItems()) {
                File tmp = client.files().get(childRef.getId()).execute();
                children.put(tmp.getTitle(), tmp);
            }
            return children;
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }
}
