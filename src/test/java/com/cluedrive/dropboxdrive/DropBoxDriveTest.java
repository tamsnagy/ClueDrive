package com.cluedrive.dropboxdrive;

import com.cluedrive.commons.CFile;
import com.cluedrive.commons.CFolder;
import com.cluedrive.commons.CPath;
import com.cluedrive.commons.ClueDrive;
import com.cluedrive.drives.DropBoxDrive;

import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.IllegalPathException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tamas on 2015-09-24.
 */
public class DropBoxDriveTest {
    private ClueDrive drive;

    @Before
    public void setup() throws IOException {
        Properties properties = new Properties();
        InputStream config = getClass().getClassLoader().getResourceAsStream("config.properties");
        properties.load(config);
        String accessToken = properties.getProperty("dropBox.token");
        drive = new DropBoxDrive();
        drive.setClient(accessToken);
    }

    @Test
    public void testList() throws ClueException {
        System.out.println(drive.list(CPath.create("/")));
    }

    @Test
    public void testCreateFolder() throws ClueException {
        CFolder folder = drive.createFolder(new CFolder(CPath.create("/korte/szilva")), "alma");
        assertEquals("/korte/szilva/alma", folder.getRemotePath().toString());
    }

    @Test
    public void testDelete() throws ClueException {
        CFolder cFolder = drive.createFolder(new CFolder(CPath.create("/")), "delete");
        drive.delete(cFolder);
    }

    @Test
    public void testFileUpload() throws ClueException, FileNotFoundException {
        CFolder cFolder = drive.createFolder(new CFolder(CPath.create("/")), "upload");
        Path uploadThis = Paths.get("build/resources/test/test.txt");
        CFile cFile = drive.uploadFile(cFolder, uploadThis);
        assertEquals(uploadThis, cFile.getLocalPath());
        assertEquals("/upload/test.txt", cFile.getRemotePath().toString());
        drive.delete(cFolder);
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

        drive.delete(cFolder);
    }

}
