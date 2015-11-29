package com.cluedrive;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.IllegalPathException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by Tamas on 2015-11-11.
 */
public abstract class ClueDriveTest {
    protected static final String BASE_FOLDER_NAME = "ClueDriveTest";
    protected ClueDrive drive;
    protected CFolder baseFolder;

    public ClueDriveTest() {
        try {
            baseFolder = new CFolder(CPath.create("/" + BASE_FOLDER_NAME));
        } catch (IllegalPathException e) {
            System.err.println("Tests cannot run. Base folder is not set up correctly");
        }
    }

    /**
     * Checks if folder with name BASE_FOLDER_NAME exists at cloud root. If not creates one.
     * If folder existed with that name, than deletes everything from that folder.
     * @throws ClueException Problem between client and provider.
     */
    protected abstract void format() throws ClueException;

    /**
     * Runs before format. Must Set up anything required specially by that provider, before executing format.
     * @throws IOException
     */
    protected abstract void driveSpecificSetup() throws IOException;

    /**
     * Must create two folder in the BASE_FOLDER_NAME folder.
     * One of the two folder must be empty. The other one must contain an other folder.
     * @throws ClueException Problem between client and provider.
     */
    protected abstract void listSetup() throws ClueException;


    /**
     * Prepares cloud for testing.
     * @throws IOException
     * @throws ClueException Problem between client and provider.
     */
    @Before
    public void setup() throws IOException, ClueException {
        driveSpecificSetup();
        format();
    }

    /**
     * Tries to access account info, from provider. Does not depend on other test.
     * @throws ClueException Problem between client and provider.
     */
    @Test
    public void testGetAccountInfo() throws ClueException {
        CAccountInfo accountInfo = drive.getAccountInfo();
        assertNotNull(accountInfo);
        assertNotNull(accountInfo.getName());
        assertNotEquals(0, accountInfo.getTotal());
    }

    /**
     * Checks for 2 folder in the BASE_FOLDER, one should contain an other folder too.
     * @throws ClueException Problem between client and provider.
     */
    @Test
    public void testList() throws ClueException {
        listSetup();
        //Test listing.
        List<CResource> resources = drive.list(baseFolder.getRemotePath());
        assertEquals(2, resources.size());
        int subFolder1 = drive.list(resources.get(0).getRemotePath()).size();
        int subFolder2 = drive.list(resources.get(1).getRemotePath()).size();
        assertTrue((0 == subFolder1 && 1 == subFolder2) ||
                (1 == subFolder1 && 0 == subFolder2));
    }

    /**
     * Tries to create a folder.
     * @throws ClueException Problem between client and provider.
     */
    @Test
    public void testCreateFolder() throws ClueException {
        CFolder folder = drive.createFolder(baseFolder, "banana");
        assertEquals("/" + BASE_FOLDER_NAME + "/banana", folder.getRemotePath().toString());
    }

    /**
     * Tries to delete a folder previously created.
     * Depends on folder creation.
     * @throws ClueException Problem between client and provider.
     */
    @Test
    public void testDeleteFolder() throws ClueException {
        CFolder cFolder = drive.createFolder(baseFolder, "delete");
        assertEquals(1, drive.list(baseFolder.getRemotePath()).size());
        drive.delete(cFolder);
        assertEquals(0, drive.list(baseFolder.getRemotePath()).size());
    }

    /**
     * Tries to delete a file.
     * Depends on folder creation and file upload.
     * @throws ClueException Problem between client and provider.
     * @throws FileNotFoundException
     */
    @Test
    public void testDeleteFile() throws ClueException, FileNotFoundException {
        CFolder cFolder = drive.createFolder(baseFolder, "upload");
        Path uploadThis = Paths.get("build/resources/test/test.txt");
        CFile cFile = drive.uploadFile(cFolder, uploadThis);
        assertEquals(uploadThis, cFile.getLocalPath());
        assertEquals("/" + BASE_FOLDER_NAME + "/upload/test.txt", cFile.getRemotePath().toString());
        drive.delete(cFile);
        assertEquals(0, drive.list(cFolder.getRemotePath()).size());
    }

    /**
     * Tries to upload a file.
     * Depends on folder creation.
     * @throws ClueException Problem between client and provider.
     * @throws FileNotFoundException
     */
    @Test
    public void testFileUpload() throws ClueException, FileNotFoundException {
        CFolder cFolder = drive.createFolder(baseFolder, "upload");
        Path uploadThis = Paths.get("build/resources/test/test.txt");
        CFile cFile = drive.uploadFile(cFolder, uploadThis);
        assertEquals(uploadThis, cFile.getLocalPath());
        assertEquals("/" + BASE_FOLDER_NAME + "/upload/test.txt", cFile.getRemotePath().toString());
    }

    /**
     * Tries to download a file.
     * Depends on folder creation, file upload.
     * @throws ClueException Problem between client and provider.
     * @throws FileNotFoundException
     */
    @Test
    public void testFileDownload() throws ClueException, FileNotFoundException {
        CFolder cFolder = drive.createFolder(baseFolder, "download");
        Path uploadThis = Paths.get("build/resources/test/test.txt");
        CFile cFile = drive.uploadFile(cFolder, uploadThis);
        assertEquals(uploadThis, cFile.getLocalPath());
        assertEquals("/" + BASE_FOLDER_NAME + "/download/test.txt", cFile.getRemotePath().toString());

        Path destination = Paths.get("build/resources/test/downloaded.txt");

        CFile downloadedFile = drive.downloadFile(cFile, destination);

        assertEquals(cFile.getRemotePath(), downloadedFile.getRemotePath());
        assertEquals(destination, downloadedFile.getLocalPath());
        assertTrue(Files.exists(downloadedFile.getLocalPath()));
    }

}
