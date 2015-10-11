package com.cluedrive.onedrive;

import com.cluedrive.commons.CPath;
import com.cluedrive.commons.CResource;
import com.cluedrive.commons.ClueDrive;
import com.cluedrive.drives.OneDrive;
import com.cluedrive.exception.ClueException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Tamas on 2015-10-11.
 */
public class OneDriveTest {
    private ClueDrive drive;
    private static RestTemplate restTemplate;

    @BeforeClass
    public static void authClient() throws IOException {
        Properties properties = new Properties();
        InputStream config = OneDriveTest.class.getClassLoader().getResourceAsStream("config.properties");
        properties.load(config);
        restTemplate = new RestTemplate();
        String clientId = properties.getProperty("microsoftOnedrive.clientId");
        String scope = "onedrive.readwrite";
        ResponseEntity<String> response = restTemplate.getForEntity(new StringBuilder()
                    .append("https://login.live.com/oauth20_authorize.srf?client_id=")
                    .append(clientId)
                    .append("&scope=")
                    .append(scope)
                    .append("&response_type=token&redirect_uri=https://login.live.com/oauth20_desktop.srf")
                    .toString(),
            String.class);
        System.out.println("Login Response: " + response);
    }

    @Before
    public void setupClient() throws IOException {
        Properties properties = new Properties();
        InputStream config = OneDriveTest.class.getClassLoader().getResourceAsStream("config.properties");
        properties.load(config);
        drive = new OneDrive();
        drive.setClient(properties.getProperty("microsoftOnedrive.accessToken"));
    }

    @Test
    public void testDefaultDrive() throws ClueException {
        drive.list(CPath.create("/AAF")).forEach(System.out::println);
    }
}
