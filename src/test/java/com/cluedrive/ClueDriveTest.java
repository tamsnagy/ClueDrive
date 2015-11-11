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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Tamas on 2015-11-11.
 */
public abstract class ClueDriveTest {
    protected ClueDrive drive;

    protected final CPath basePath;

    public ClueDriveTest() throws IllegalPathException {
        basePath = CPath.create("/ClueDriveTest");
    }

    protected abstract void format() throws ClueException;

    protected abstract void driveSpecificSetup() throws IOException;

    protected abstract void listSetup() throws ClueException;



    @Before
    public void setup() throws IOException, ClueException {
        driveSpecificSetup();
        format();
    }

    @Test
    public void testList() throws ClueException {
        listSetup();
        //Test listing.
        List<CResource> resources = drive.list(basePath);
        assertEquals(2, resources.size());
        int subFolder1 = drive.list(resources.get(0).getRemotePath()).size();
        int subFolder2 = drive.list(resources.get(1).getRemotePath()).size();
        assertTrue( (0 == subFolder1 && 1 == subFolder2) ||
                (1 == subFolder1 && 0 == subFolder2));
    }

    @Test
    public void testCreateFolder() throws ClueException {
        CFolder folder = drive.createFolder(new CFolder(CPath.create(basePath, "apple/pear")), "banana");
        assertEquals("/ClueDriveTest/apple/pear/banana", folder.getRemotePath().toString());
    }

    @Test
    public void testDelete() throws ClueException {
        CFolder cFolder = drive.createFolder(new CFolder(basePath), "delete");
        assertEquals(1, drive.list(basePath).size());
        drive.delete(cFolder);
        assertEquals(0, drive.list(basePath).size());
    }

    @Test
    public void testFileUpload() throws ClueException, FileNotFoundException {
        CFolder cFolder = drive.createFolder(new CFolder(basePath), "upload");
        Path uploadThis = Paths.get("build/resources/test/test.txt");
        CFile cFile = drive.uploadFile(cFolder, uploadThis);
        assertEquals(uploadThis, cFile.getLocalPath());
        assertEquals("/ClueDriveTest/upload/test.txt", cFile.getRemotePath().toString());
    }

    @Test
    public void testFileDownload() throws ClueException, FileNotFoundException {
        CFolder cFolder = drive.createFolder(new CFolder(CPath.create("/")), "download");
        Path uploadThis = Paths.get("build/resources/test/test.txt");
        CFile cFile = drive.uploadFile(cFolder, uploadThis);
        assertEquals(uploadThis, cFile.getLocalPath());
        assertEquals("/download/test.txt", cFile.getRemotePath().toString());

        Path destination = Paths.get("build/resources/test/downloaded.txt");

        CFile downloadedFile = drive.downloadFile(cFile.getRemotePath(), destination);

        assertEquals(cFile.getRemotePath(), downloadedFile.getRemotePath());
        assertEquals(destination, downloadedFile.getLocalPath());
        assertTrue(Files.exists(downloadedFile.getLocalPath()));
    }

}
