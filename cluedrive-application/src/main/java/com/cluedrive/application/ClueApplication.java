package com.cluedrive.application;

import com.cluedrive.commons.*;
import com.cluedrive.drives.DropBoxDrive;
import com.cluedrive.drives.GoogleDrive;
import com.cluedrive.drives.OneDrive;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.IllegalPathException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tamas on 2015-11-16.
 */

public class ClueApplication implements Serializable {
    private static final Path setupOrigin = Paths.get(new JFileChooser().getFileSystemView().getDefaultDirectory().getParentFile().getAbsolutePath() + File.separator + "ClueDrive" + File.separator + "setupData.obj");
    private static Path localRootPath;
    private String localRootPathAsString;
    private List<AppDrive> myDrives = new ArrayList<>();
    private transient ClueDrive tmpDrive = null;
    private static MainWindow mainWindow;
    public static CPath currentPath;
    public static AppDrive currentDrive;
    public static java.util.Deque<CFolder> currentFolder;
    public static CPath basePath;
    private static List<CResourceUI> selected;

    private int roundRobinDrive;


    public ClueApplication() {
        localRootPath = Paths.get(new JFileChooser().getFileSystemView().getDefaultDirectory().getAbsolutePath() + File.separator + "ClueDrive local files");
        initialize();

    }
    private void initialize() {
        try {
            basePath = CPath.create("/Cloud");
            currentPath = basePath;
            currentDrive = null;
            currentFolder = new ArrayDeque<>();
            selected = new ArrayList<>();
        } catch (IllegalPathException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                ClueApplication application = null;
                if(Files.exists(setupOrigin)) {
                    try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(setupOrigin.toFile()))) {
                        application = (ClueApplication) inputStream.readObject();
                        localRootPath = Paths.get(application.localRootPathAsString);
                    }
                } else {
                    Files.createDirectories(setupOrigin.getParent());
                    application = new ClueApplication();
                    application.persist();
                }
                if( ! Files.exists(localRootPath)) {
                    Files.createDirectories(localRootPath);
                }
                application.initialize();
                // Create previously registered drives;
                application.myDrives.forEach(appDrive -> appDrive.getDrive().initialize());
                application.myDrives.parallelStream().forEach(appDrive -> {
                    try {
                        appDrive.setAccountInfo(appDrive.getDrive().getAccountInfo());
                    } catch (ClueException e) {
                        JOptionPane.showMessageDialog(mainWindow,
                                "Your accessToken for " + appDrive.getDrive().getProvider() + " timed out. Delete that drive and authenticate again.",
                                "Expired accessToken",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
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
                int counter = 0;
                String credentialsPath = new JFileChooser().getFileSystemView().getDefaultDirectory().getParentFile().getAbsolutePath() + java.io.File.separator + "ClueDrive" + java.io.File.separator + "credentials";
                while(Files.exists(Paths.get(credentialsPath + counter))) {
                    counter++;
                }
                tmpDrive = new GoogleDrive(Paths.get(credentialsPath + counter));
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
        try {
            tmpDrive.finishAuth(accessToken);
            CFolder folder = tmpDrive.createFolder(tmpDrive.getRootFolder(), basePath.getLeaf());
            AppDrive appDrive = new AppDrive(tmpDrive, folder);
            appDrive.setAccountInfo(tmpDrive.getAccountInfo());
            myDrives.add(appDrive);
        } catch (ClueException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Token is not valid, please try again.",
                    "Unsuccessful authorization",
                    JOptionPane.ERROR_MESSAGE);
        }
        mainWindow.refreshDrivePane();
        mainWindow.refreshResourcePane();

        persist();
    }

    public static Path getLocalRootPath() {
        return localRootPath;
    }

    public void setLocalRootPath(Path localRootPath) {
        ClueApplication.localRootPath = localRootPath;
        this.localRootPathAsString = localRootPath.toString();
        persist();
    }

    public List<AppDrive> getMyDrives() {
        return myDrives;
    }

    public void setMyDrives(List<AppDrive> myDrives) {
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

    public static void refreshMainPanel() {
        mainWindow.refreshAddressPane();
        mainWindow.refreshResourcePane();
    }

    public static void stepInFolder(CFolder folder) {
        currentFolder.addLast(folder);
        currentPath = folder.getRemotePath();
    }

    public static void stepOutFolder() {
        currentFolder.pollLast();
        if(currentFolder.isEmpty()) {
            currentPath = basePath;
            currentDrive = null;
        } else {
            currentPath = currentFolder.peekLast().getRemotePath();
        }
    }

    public void createFolder(String folderName) {
        AppDrive selectedDrive = currentDrive;
        CFolder parentFolder = currentFolder.peekLast();
        if(currentDrive == null) {
            selectedDrive = myDrives.get(roundRobinDrive);
            roundRobinDrive++;
            if(roundRobinDrive >= myDrives.size()) {
                roundRobinDrive = 0;
            }
            parentFolder = selectedDrive.getRootFolder();

        }
        try {
            selectedDrive.getDrive().createFolder(parentFolder, folderName);
        } catch (ClueException e) {
            e.printStackTrace();
        }
    }

    public static void addSelected(CResourceUI resource) {
        if(selected.isEmpty()) {
            mainWindow.invertShowRemoveSelectionLabel();
        }
        selected.add(resource);
    }

    public static void removeSelected(CResourceUI resource) {
        selected.remove(resource);
        if(selected.isEmpty()) {
            mainWindow.invertShowRemoveSelectionLabel();
        }
    }

    public static void emptySelected() {
        selected = new ArrayList<>();
    }

    public static boolean deleteSelectedResources() {
        if(selected.isEmpty()) {
            return false;
        }
        selected.forEach(resourceUI -> {
            try {
                resourceUI.getHolder().getDrive().delete(resourceUI.getResource());
            } catch (ClueException e) {
                e.printStackTrace();
            }
        });
        emptySelected();
        return true;
    }

    public void uploadItem(Path item) {
        AppDrive selectedDrive = currentDrive;
        CFolder parentFolder = currentFolder.peekLast();
        if(currentDrive == null) {
            selectedDrive = myDrives.get(roundRobinDrive);
            roundRobinDrive++;
            if(roundRobinDrive >= myDrives.size()) {
                roundRobinDrive = 0;
            }
            parentFolder = selectedDrive.getRootFolder();

        }
        try {
            selectedDrive.getDrive().uploadFile(parentFolder, item);
        } catch (ClueException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
