package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.NotExistingPathException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Tamas on 2015-10-01.
 */
public class GoogleDrive extends ClueDrive {
    private Drive client;
    public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

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
                    responseList.add(createCFile(file, fileName, null));
                }
            }

        } catch (IOException e) {
            throw new ClueException(e);
        }
        return responseList;
    }

    @Override
    public void setToken(String accessToken) {

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
    public CFile uploadFile(CFolder remoteFolder, Path localPath) throws ClueException, FileNotFoundException {
        File body = new File();
        String fileName = localPath.getFileName().toString();
        body.setTitle(fileName);
        body.setParents(Arrays.asList(new ParentReference().setId(remoteFolder.getId())));
        try {
            String mimeType = Files.probeContentType(localPath);
            body.setMimeType(mimeType);
            FileContent mediaContent = new FileContent(mimeType, localPath.toFile());

            File uploadedFile = client.files().insert(body, mediaContent).execute();

            return createCFile(uploadedFile, remoteFolder.getRemotePath(), localPath);
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public void delete(CResource resource) throws ClueException {
        try {
            client.files().delete(resource.getId()).execute();
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException {
        try {
            File file = client.files().get(remoteFile.getId()).execute();
            if(file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
                HttpResponse response = client.getRequestFactory().buildGetRequest(
                        new GenericUrl(file.getDownloadUrl())
                ).execute();
                try(InputStream inputStream = response.getContent();
                    FileOutputStream outputStream = new FileOutputStream(localPath.toFile())) {
                    int read;
                    byte[] bytes = new byte[4096];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                }
                return createCFile(file, remoteFile.getRemotePath().getParent(), localPath);
            } else {
                throw new NotExistingPathException(remoteFile.getRemotePath());
            }
        } catch (IOException e) {
            throw new ClueException(e);
        }
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

    private CFile createCFile(File googleFile, CPath remoteFolder, Path localPath) {
        Date date = (googleFile.getModifiedDate() == null) ? null : new Date(googleFile.getModifiedDate().getValue());
        long size = 0;
        if(googleFile.keySet().contains("fileSize")) {
            size = googleFile.getFileSize();
        }
        CFile cFile = new CFile(CPath.create(remoteFolder, googleFile.getTitle()), googleFile.getId(), size, date);
        cFile.setLocalPath(localPath);
        return cFile;
    }

    public Drive getClient() {
        return client;
    }
}
