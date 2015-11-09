package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.NotExistingPathException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Tamas on 2015-10-01.
 */
public class GoogleDrive implements ClueDrive {
    private Drive client;
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

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
                if(FOLDER_MIME_TYPE.equals(file.getMimeType())) {
                    responseList.add(new CFolder(fileName, file.getId()));
                } else {
                    Date date = (file.getModifiedDate() == null) ? null : new Date(file.getModifiedDate().getValue());
                    long size = 0;
                    if(file.keySet().contains("fileSize")) {
                        size = file.getFileSize();
                    }
                    responseList.add(new CFile(fileName, file.getId(), size, date));
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
    public CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException {
        File body = new File();
        body.setTitle(folderName);
        body.setMimeType(FOLDER_MIME_TYPE);
        body.setParents(Arrays.asList(new ParentReference().setId(parentFolder.getId())));

        try {
            File folder = client.files().insert(body).execute();
            return new CFolder(CPath.create(parentFolder.getRemotePath(), folder.getTitle()), folder.getId());
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public CFolder getRootFolder() throws ClueException {
        try {
            String rootId = client.about().get().execute().getRootFolderId();
            File file = client.files().get(rootId).execute();
            return new CFolder(CPath.create("/"), file.getId());
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public CFile uploadFile(CFolder remotePath, Path localPath) throws ClueException, FileNotFoundException {
        File body = new File();
        String fileName = localPath.getFileName().toString();
        body.setTitle(fileName);
        body.setParents(Arrays.asList(new ParentReference().setId(remotePath.getId())));
        try {
            String mimeType = Files.probeContentType(localPath);
            body.setMimeType(mimeType);
            FileContent mediaContent = new FileContent(mimeType, localPath.toFile());

            File uploadedFile = client.files().insert(body, mediaContent).execute();

            Date date = (uploadedFile.getModifiedDate() == null) ? null : new Date(uploadedFile.getModifiedDate().getValue());
            long size = 0;
            if(uploadedFile.keySet().contains("fileSize")) {
                size = uploadedFile.getFileSize();
            }
            return new CFile(CPath.create(remotePath.getRemotePath(), fileName), uploadedFile.getId(), size, date);
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public void delete(CResource resource) throws ClueException {

    }

    @Override
    public CFile downloadFile(CPath remotePath, Path localPath) throws ClueException {
        return null;
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
