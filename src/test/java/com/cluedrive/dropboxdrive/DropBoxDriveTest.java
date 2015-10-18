package com.cluedrive.dropboxdrive;

import com.cluedrive.commons.CFolder;
import com.cluedrive.commons.CPath;
import com.cluedrive.commons.ClueDrive;
import com.cluedrive.drives.DropBoxDrive;

import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.IllegalPathException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

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

}
