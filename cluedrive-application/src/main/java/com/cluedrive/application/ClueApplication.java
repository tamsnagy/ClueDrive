package com.cluedrive.application;

import com.cluedrive.commons.ClueDrive;
import com.cluedrive.commons.ClueDriveProvider;
import com.cluedrive.drives.DropBoxDrive;
import com.cluedrive.drives.GoogleDrive;
import com.cluedrive.drives.OneDrive;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tamas on 2015-11-16.
 */

public class ClueApplication implements Serializable {
    private static final Path setupOrigin = Paths.get(new JFileChooser().getFileSystemView().getDefaultDirectory().getParentFile().getAbsolutePath() + File.separator + "ClueDrive" + File.separator + "setupData.obj");
    private transient Path localRootPath;
    private String localRootPathAsString;
    private List<ClueDrive> myDrives = new ArrayList<>();
    private transient ClueDrive tmpDrive = null;
    private static MainWindow mainWindow;

    public ClueApplication() {
        localRootPath = Paths.get(new JFileChooser().getFileSystemView().getDefaultDirectory().getAbsolutePath() + File.separator + "ClueDrive local files");
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                ClueApplication application = null;
                if(Files.exists(setupOrigin)) {
                    try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(setupOrigin.toFile()))) {
                        application = (ClueApplication) inputStream.readObject();
                        application.localRootPath = Paths.get(application.localRootPathAsString);
                    }
                } else {
                    Files.createDirectories(setupOrigin.getParent());
                    application = new ClueApplication();
                    application.persist();
                }
                if( ! Files.exists(application.localRootPath)) {
                    Files.createDirectories(application.localRootPath);
                }
                // Create previously registered drives;
                application.myDrives.forEach(com.cluedrive.commons.ClueDrive::initialize);
                createAndShowGUI(application);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public void persist() {
        try(ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(setupOrigin.toFile()))) {
            localRootPathAsString = localRootPath.toString();
            outputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createAndShowGUI(ClueApplication application) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
        mainWindow = MainWindow.getInstance(application);
        mainWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainWindow.setVisible(true);
    }

    public void addDriveCandidate(ClueDriveProvider provider) {
        switch (provider) {
            case GOOGLE:
                tmpDrive = new GoogleDrive();
                break;
            case ONEDRIVE:
                tmpDrive = new OneDrive();
                break;
            case DROPBOX:
                tmpDrive = new DropBoxDrive();
                break;
        }
        String urlString = tmpDrive.startAuth();
        if(urlString != null) {
            try {
                Desktop.getDesktop().browse(new URI(urlString));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void addAccessTokenToTmpDrive(String accessToken) {
        tmpDrive.setAccessToken(accessToken);
        myDrives.add(tmpDrive);

        //TODO: check if token is real
        mainWindow.refreshDrivePane();

        persist();
    }

    public Path getLocalRootPath() {
        return localRootPath;
    }

    public void setLocalRootPath(Path localRootPath) {
        this.localRootPath = localRootPath;
        this.localRootPathAsString = localRootPath.toString();
        persist();
    }

    public List<ClueDrive> getMyDrives() {
        return myDrives;
    }

    public void setMyDrives(List<ClueDrive> myDrives) {
        this.myDrives = myDrives;
        persist();
    }

    public static Path getSetupOrigin() {
        return setupOrigin;
    }

    public String getLocalRootPathAsString() {
        return localRootPathAsString;
    }

    public void setLocalRootPathAsString(String localRootPathAsString) {
        this.localRootPathAsString = localRootPathAsString;
    }


}
