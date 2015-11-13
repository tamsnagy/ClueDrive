package com.cluedrive.dropboxdrive;

import com.cluedrive.ClueDriveTest;
import com.cluedrive.commons.*;
import com.cluedrive.drives.DropBoxDrive;

import com.cluedrive.exception.ClueException;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tamas on 2015-09-24.
 */
public class DropBoxDriveTest  extends ClueDriveTest {

    protected void format() throws ClueException {
        try {
            DbxClient dbxClient = ((DropBoxDrive) drive).getClient();
            for (DbxEntry children : dbxClient.getMetadataWithChildren("/").children) {
                dbxClient.delete(children.path);
            }
        } catch (DbxException e) {
            throw new ClueException(e);
        }
    }

    protected void driveSpecificSetup() throws IOException {
        Properties properties = new Properties();
        InputStream config = getClass().getClassLoader().getResourceAsStream("config.properties");
        properties.load(config);
        String accessToken = properties.getProperty("dropBox.token");
        drive = new DropBoxDrive();
        drive.setToken(accessToken);
    }

    protected void listSetup() throws ClueException {
        try {
            DbxClient dbxClient = ((DropBoxDrive) drive).getClient();
            dbxClient.createFolder(CPath.create(baseFolder.getRemotePath(), "folder1").toString());
            dbxClient.createFolder(CPath.create(baseFolder.getRemotePath(), "folder2/folder3").toString());
        } catch (DbxException e) {
            throw new ClueException(e);
        }
    }

}
