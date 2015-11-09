package com.cluedrive.dropboxdrive;

import com.cluedrive.commons.*;
import com.cluedrive.drives.DropBoxDrive;

import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.IllegalPathException;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tamas on 2015-09-24.
 */
public class DropBoxDriveTest {
    private ClueDrive drive;

    private void format() throws DbxException {
        DbxClient dbxClient = ((DropBoxDrive)drive).getClient();
        for(DbxEntry children: dbxClient.getMetadataWithChildren("/").children) {
            dbxClient.delete(children.path);
        }
    }

    @Before
    public void setup() throws IOException, ClueException, DbxException {
        Properties properties = new Properties();
        InputStream config = getClass().getClassLoader().getResourceAsStream("config.properties");
        properties.load(config);
        String accessToken = properties.getProperty("dropBox.token");
        drive = new DropBoxDrive();
        drive.setClient(accessToken);
        format();
    }

    @Test
    public void testList() throws ClueException, DbxException {
        //Init with dbxClient
        DbxClient dbxClient = ((DropBoxDrive) drive).getClient();
        dbxClient.createFolder("/folder1");
        dbxClient.createFolder("/folder2/folder3");

        //Test listing.
        List<CResource> resources = drive.list(CPath.create("/"));
        assertEquals(2, resources.size());
        int subFolder1 = drive.list(resources.get(0).getRemotePath()).size();
        int subFolder2 = drive.list(resources.get(1).getRemotePath()).size();
        assertTrue( (0 == subFolder1 && 1 == subFolder2) ||
                    (1 == subFolder1 && 0 == subFolder2));

    }

    @Test
    public void testCreateFolder() throws ClueException {
        CFolder folder = drive.createFolder(new CFolder(CPath.create("/apple/pear")), "banana");
        assertEquals("/apple/pear/banana", folder.getRemotePath().toString());
    }

    @Test
    public void testDelete() throws ClueException {
        CFolder cFolder = drive.createFolder(new CFolder(CPath.create("/")), "delete");
        assertEquals(1, drive.list(CPath.create("/")).size());
        drive.delete(cFolder);
        assertEquals(0, drive.list(CPath.create("/")).size());
    }

    @Test
    public void testFileUpload() throws ClueException, FileNotFoundException {
        CFolder cFolder = drive.createFolder(new CFolder(CPath.create("/")), "upload");
        Path uploadThis = Paths.get("build/resources/test/test.txt");
        CFile cFile = drive.uploadFile(cFolder, uploadThis);
        assertEquals(uploadThis, cFile.getLocalPath());
        assertEquals("/upload/test.txt", cFile.getRemotePath().toString());
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
