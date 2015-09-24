package com.cluedrive.dropboxdrive;

import com.cluedrive.commons.ClueDrive;
import com.cluedrive.commons.DropBoxDrive;

import com.cluedrive.exception.ClueException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
        System.out.println(drive.list("/"));
        System.out.println(drive.list("/alma"));
    }

    @Test
    public void testCreateFolder() {
        drive.createFolder("/alma/korte");
        drive.createFolder("/korte/szilva");

    }

}
