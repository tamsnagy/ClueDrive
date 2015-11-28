package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.NotExistingPathException;
import com.cluedrive.exception.UnAuthorizedException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * ClueDrive for accessing Google's cloud storage.
 */
public class GoogleDrive extends ClueDrive {
    public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private String setupOrigin;
    private transient Drive client;

    /**
     * Creates a drive which is able to start auth flow.
     */
    public GoogleDrive() {
        provider = ClueDriveProvider.GOOGLE;
        this.setupOrigin = new JFileChooser().getFileSystemView().getDefaultDirectory().getParentFile().getAbsolutePath() + java.io.File.separator + "ClueDrive" + java.io.File.separator + "credentials";
    }

    /**
     * Used for unit tests.
     * @param client the client to be set.
     */
    public GoogleDrive(Drive client) {
        provider = ClueDriveProvider.GOOGLE;
        this.client = client;
        this.setupOrigin = new JFileChooser().getFileSystemView().getDefaultDirectory().getParentFile().getAbsolutePath() + java.io.File.separator + "ClueDrive" + java.io.File.separator + "credentials";
    }

    /**
     * Creates a drive which is able to start auth flow.
     * @param setupPath path where credentials can be saved.
     */
    public GoogleDrive(Path setupPath) {
        provider = ClueDriveProvider.GOOGLE;
        setupOrigin = setupPath.toString();
    }

    @Override
    public String startAuth()  throws UnAuthorizedException{
        try {
            // Load client secrets.
            InputStream in = this.getClass().getResourceAsStream("/google_client_secret.json");
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, jsonFactory, clientSecrets, Arrays.asList(DriveScopes.DRIVE))
                    .setDataStoreFactory(new FileDataStoreFactory(Paths.get(setupOrigin).toFile()))
                    .setAccessType("offline")
                    .build();

            Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
            client = new Drive.Builder(
                    httpTransport, jsonFactory, credential)
                    .setApplicationName("ClueDrive")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new UnAuthorizedException(e);
        }
        return null;
    }

    @Override
    public void initialize() throws UnAuthorizedException {
        startAuth();
    }

    @Override
    public CAccountInfo getAccountInfo() throws ClueException {
        try {
            About about = client.about().get().execute();
            return new CAccountInfo(about.getUser().getDisplayName(), about.getQuotaBytesTotal());
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public List<CResource> list(CPath path) throws ClueException {
        List<CResource> responseList = new ArrayList<>();
        try {

            // Access the wanted folder, from root.
            String rootId = client.about().get().execute().getRootFolderId();
            Map<String, File> children = listChildren(rootId);
            if (!"/".equals(path.toString())) {
                String[] pathParts = path.toString().split("/");

                for (int i = 1; i < pathParts.length; i++) {
                    if (!children.containsKey(pathParts[i])) {
                        throw new NotExistingPathException(path);
                    }
                    children = listChildren(children.get(pathParts[i]).getId());
                }
            }

            // convert Google's response to common CResources
            for (File file : children.values()) {
                CPath fileName = CPath.create(path, file.getTitle());
                if (FOLDER_MIME_TYPE.equals(file.getMimeType())) {
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
    public void setAccessToken(String accessToken) {

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
            if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
                HttpResponse response = client.getRequestFactory().buildGetRequest(
                        new GenericUrl(file.getDownloadUrl())
                ).execute();
                try (InputStream inputStream = response.getContent();
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

    public Drive getClient() {
        return client;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods.

    /**
     * Lists children for of a resource identified by it's id.
     * @param id the id of the parent.
     * @return Map of id-google's file key-value pairs.
     * @throws ClueException Problem between client and provider.
     */
    private Map<String, File> listChildren(String id) throws ClueException {
        Map<String, File> children = new HashMap<>();
        try {
            for (ChildReference childRef : client.children().list(id).execute().getItems()) {
                File tmp = client.files().get(childRef.getId()).execute();
                children.put(tmp.getTitle(), tmp);
            }
            return children;
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    /**
     * Helps googles file to transform to CFile
     * @param googleFile google's file format
     * @param remoteFolder parent folder on cloud.
     * @param localPath local path on file system.
     * @return CFile created from given parameters.
     */
    private CFile createCFile(File googleFile, CPath remoteFolder, Path localPath) {
        Date date = (googleFile.getModifiedDate() == null) ? null : new Date(googleFile.getModifiedDate().getValue());
        long size = 0;
        if (googleFile.keySet().contains("fileSize")) {
            size = googleFile.getFileSize();
        }
        CFile cFile = new CFile(CPath.create(remoteFolder, googleFile.getTitle()), googleFile.getId(), size, date);
        cFile.setLocalPath(localPath);
        return cFile;
    }
}
