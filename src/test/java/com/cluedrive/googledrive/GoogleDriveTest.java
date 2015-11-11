package com.cluedrive.googledrive;

import com.cluedrive.ClueDriveTest;
import com.cluedrive.commons.CFolder;
import com.cluedrive.commons.CPath;
import com.cluedrive.commons.CResource;
import com.cluedrive.drives.GoogleDrive;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.IllegalPathException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by Tamas on 2015-10-01.
 */
public class GoogleDriveTest  extends ClueDriveTest{
    /** Application name. */
    private static final String APPLICATION_NAME =
            "Drive API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/drive-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart. */
    private static final List<String> SCOPES =
            Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public GoogleDriveTest() throws IllegalPathException {
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                GoogleDriveTest.class.getResourceAsStream("/google_client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    @Override
    protected void format() throws ClueException {
        Drive gDrive = ((GoogleDrive)drive).getClient();
        try {
            CFolder baseFolderCandidate = null;
            String rootId = gDrive.about().get().execute().getRootFolderId();
            for(ChildReference childRef : gDrive.children().list(rootId).execute().getItems()) {
                File tmp = gDrive.files().get(childRef.getId()).execute();
                if(BASE_FOLDER_NAME.equals(tmp.getTitle())) {
                    // /ClueDriveTest folder exists on gDrive. Its id must be saved and it should be emptied
                    baseFolderCandidate = new CFolder(CPath.create("/" + BASE_FOLDER_NAME), tmp.getId());
                    for(ChildReference removableChildRef : gDrive.children().list(tmp.getId()).execute().getItems()) {
                        gDrive.files().delete(removableChildRef.getId()).execute();
                    }
                    continue;
                }
            }
            // /ClueDriveTest folder was not fount on gDrive it must be created.
            if(baseFolderCandidate == null) {
                File body = new File();
                body.setTitle(BASE_FOLDER_NAME);
                body.setMimeType(GoogleDrive.FOLDER_MIME_TYPE);
                body.setParents(Arrays.asList(new ParentReference().setId(rootId)));
                try {
                    File folder = gDrive.files().insert(body).execute();
                    baseFolderCandidate = new CFolder(CPath.create("/" + BASE_FOLDER_NAME), folder.getId());
                } catch (IOException e) {
                    throw new ClueException(e);
                }
            }
            baseFolder = baseFolderCandidate;
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    protected void driveSpecificSetup() throws IOException {
        Credential credential = authorize();
        Drive gDrive = new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        drive = new GoogleDrive(gDrive);
    }

    @Override
    protected void listSetup() throws ClueException {
        File body1 = new File();
        body1.setTitle("folder1");
        body1.setMimeType(GoogleDrive.FOLDER_MIME_TYPE);
        body1.setParents(Arrays.asList(new ParentReference().setId(baseFolder.getId())));
        File body2 = new File();
        body2.setTitle("folder2");
        body2.setMimeType(GoogleDrive.FOLDER_MIME_TYPE);
        body2.setParents(Arrays.asList(new ParentReference().setId(baseFolder.getId())));
        try {
            Drive gDrive = ((GoogleDrive)drive).getClient();
            gDrive.files().insert(body1).execute();
            File folder2 = gDrive.files().insert(body2).execute();

            File body3 = new File();
            body3.setTitle("folder3");
            body3.setMimeType(GoogleDrive.FOLDER_MIME_TYPE);
            body3.setParents(Arrays.asList(new ParentReference().setId(folder2.getId())));
            gDrive.files().insert(body3).execute();
        } catch (IOException e) {
            throw new ClueException(e);
        }
    }
}
