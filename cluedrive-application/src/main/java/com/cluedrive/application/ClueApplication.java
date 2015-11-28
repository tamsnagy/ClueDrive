package com.cluedrive.application;

import com.cluedrive.commons.CFolder;
import com.cluedrive.commons.CPath;
import com.cluedrive.commons.ClueDrive;
import com.cluedrive.commons.ClueDriveProvider;
import com.cluedrive.drives.DropBoxDrive;
import com.cluedrive.drives.GoogleDrive;
import com.cluedrive.drives.OneDrive;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.IllegalPathException;
import com.cluedrive.exception.UnAuthorizedException;

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
 * Model of sample application.
 * The purpose of this application is to show an example usage of ClueDriveAPI.
 */
public class ClueApplication implements Serializable {
    /**
     * Path on File system where application saves serialized configurations of the application like:
     *   + Registered drives and accessTokens for them.
     *   + Local folders path as a string.
     *   + RoundRobin coefficient.
     */
    private static final Path setupOrigin = Paths.get(new JFileChooser().getFileSystemView().getDefaultDirectory().getParentFile().getAbsolutePath() + File.separator + "ClueDrive" + File.separator + "setupData.obj");
    /**
     * The remote path of which child resources are shown at application.
     */
    public static CPath currentPath;
    /**
     * The Drive which holds the currentPath. If currentPath is at the applications rootPath, than this field is null,
     * upload and new folder should choose drive based on round robin strategy.
     */
    public static AppDrive currentDrive;
    /**
     * Dequeue used as stack of current Folders. Helps in navigation.
     */
    public static java.util.Deque<CFolder> currentFolder;
    /**
     * The Path shown as root to the application.
     */
    public static CPath basePath;
    /**
     * Link to the main View of the application.
     */
    private static MainWindow mainWindow;
    /**
     * List of selected resourceUI elements.
     */
    private static List<CResourceUI> selected;
    /**
     * Helper field for adding new drive.
     */
    private transient ClueDrive tmpDrive = null;
    /**
     * Path on the File system, pointing at the local root folder. Where files are saved before opening.
     */
    private static Path localRootPath;
    /**
     * localRootPaths string representation used for serialization purposes.
     */
    private String localRootPathAsString;
    /**
     * List of registered drives.
     */
    private List<AppDrive> myDrives = new ArrayList<>();
    /**
     * RoundRobin coefficient. Its value is from [0, myDrives.size).
     */
    private int roundRobinCoefficient;

    /**
     * Main function of the clueDrive example application.
     * @param args Arguments are not used.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClueApplication application = null;
                if (Files.exists(setupOrigin)) {
                    try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(setupOrigin.toFile()))) {
                        application = (ClueApplication) inputStream.readObject();
                        localRootPath = Paths.get(application.localRootPathAsString);
                    }
                } else {
                    Files.createDirectories(setupOrigin.getParent());
                    application = new ClueApplication();
                    application.persist();
                }
                if (!Files.exists(localRootPath)) {
                    Files.createDirectories(localRootPath);
                }
                application.initialize();
                // Create previously registered drives;
                application.myDrives.forEach(appDrive -> {
                    try {
                        appDrive.getDrive().initialize();
                    } catch (UnAuthorizedException e) {
                        JOptionPane.showMessageDialog(mainWindow,
                                "Something unexpected happened, please check help",
                                "Internal error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
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
                JOptionPane.showMessageDialog(mainWindow,
                        "Something unexpected happened, plese check help",
                        "Internal error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Returns local root path.
     * @return localRootPath
     */
    public static Path getLocalRootPath() {
        return localRootPath;
    }

    /**
     * Sets local root path and persists it's value.
     * @param localRootPath localRootPath to be saved.
     */
    public void setLocalRootPath(Path localRootPath) {
        ClueApplication.localRootPath = localRootPath;
        this.localRootPathAsString = localRootPath.toString();
        persist();
    }

    /**
     * Refreshed whole mainPanel of View.
     */
    public static void refreshMainPanel() {
        mainWindow.refreshAddressPane();
        mainWindow.refreshResourcePane();
    }

    /**
     * Handles step in into a folder.
     * @param folder the folder to open.
     */
    public static void stepInFolder(CFolder folder) {
        currentFolder.addLast(folder);
        currentPath = folder.getRemotePath();
    }

    /**
     * Steps out of current folder.
     */
    public static void stepOutFolder() {
        currentFolder.pollLast();
        if (currentFolder.isEmpty()) {
            currentPath = basePath;
            currentDrive = null;
        } else {
            currentPath = currentFolder.peekLast().getRemotePath();
        }
    }

    /**
     * Adds resourceUI to selectedResources list.
     * @param resource The resource to add to selected list.
     */
    public static void addSelected(CResourceUI resource) {
        if (selected.isEmpty()) {
            mainWindow.invertShowRemoveSelectionLabel();
        }
        selected.add(resource);
    }

    /**
     * Removes resourceUI from selectedResources list.
     * @param resource resource to be removed from selected list.
     */
    public static void removeSelected(CResourceUI resource) {
        selected.remove(resource);
        if (selected.isEmpty()) {
            mainWindow.invertShowRemoveSelectionLabel();
        }
    }

    /**
     * Empties the selectedResources list.
     */
    public static void emptySelected() {
        selected = new ArrayList<>();
    }

    /**
     * Deletes all selected resources from their holder drive.
     * @return True if at least one element was deleted. False if there were no selected elements.
     */
    public static boolean deleteSelectedResources() {
        if (selected.isEmpty()) {
            return false;
        }
        selected.forEach(resourceUI -> {
            try {
                resourceUI.getHolder().getDrive().delete(resourceUI.getResource());
            } catch (ClueException e) {
                JOptionPane.showMessageDialog(mainWindow,
                        "Something unexpected happened, please check help",
                        "Internal error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        emptySelected();
        return true;
    }

    /**
     * Starts authorization flow to the selected cloud provider.
     * @param provider Type of the provider.
     */
    public void addDriveCandidate(ClueDriveProvider provider) {
        switch (provider) {
            case GOOGLE:
                int counter = 0;
                String credentialsPath = new JFileChooser().getFileSystemView().getDefaultDirectory().getParentFile().getAbsolutePath() + java.io.File.separator + "ClueDrive" + java.io.File.separator + "credentials";
                while (Files.exists(Paths.get(credentialsPath + counter))) {
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
        String urlString = null;
        try {
            urlString = tmpDrive.startAuth();
            if (urlString != null) {
                Desktop.getDesktop().browse(new URI(urlString));
            }
        }catch (UnAuthorizedException | URISyntaxException | IOException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Something unexpected happened, please check help",
                    "Internal error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Finishes authorization flow of a drive. Asks for its root folder and account information.
     * @param accessToken The accessToken, or authorization code returned from cloud provider.
     */
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

    /**
     * Creates new folder. If currentPath is appRoot, than drives are selected based on round robin strategy.
     * @param folderName new folders name.
     */
    public void createFolder(String folderName) {
        AppDrive selectedDrive = currentDrive;
        CFolder parentFolder = currentFolder.peekLast();
        if (currentDrive == null) {
            selectedDrive = myDrives.get(roundRobinCoefficient);
            roundRobinCoefficient++;
            if (roundRobinCoefficient >= myDrives.size()) {
                roundRobinCoefficient = 0;
            }
            parentFolder = selectedDrive.getRootFolder();

        }
        try {
            selectedDrive.getDrive().createFolder(parentFolder, folderName);
        } catch (ClueException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Something unexpected happened, please check help",
                    "Internal error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Uploads item to the current folder. If currentPath is appRoot, than it gets uploaded to a registered drives root,
     * the drive is selected with round robin strategy.
     * @param item The path to the item which needs to be uploaded.
     */
    public void uploadItem(Path item) {
        AppDrive selectedDrive = currentDrive;
        CFolder parentFolder = currentFolder.peekLast();
        if (currentDrive == null) {
            selectedDrive = myDrives.get(roundRobinCoefficient);
            roundRobinCoefficient++;
            if (roundRobinCoefficient >= myDrives.size()) {
                roundRobinCoefficient = 0;
            }
            parentFolder = selectedDrive.getRootFolder();

        }
        try {
            selectedDrive.getDrive().uploadFile(parentFolder, item);
        } catch (ClueException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Something unexpected happened, please check help",
                    "Internal error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Selected file not found, please try again.",
                    "File not found",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * List of registered drives.
     * @return registered drives.
     */
    public List<AppDrive> getMyDrives() {
        return myDrives;
    }

    /**
     * Sets the registered drives. Serialization purposes.
     * @param myDrives Drive list to be set.
     */
    public void setMyDrives(List<AppDrive> myDrives) {
        this.myDrives = myDrives;
        persist();
    }

    /**
     * Returns localRootPath as a string. Serialization purposes.
     * @return local root path.
     */
    public String getLocalRootPathAsString() {
        return localRootPathAsString;
    }

    /**
     * Sets local root path. Serialization purposes.
     * @param localRootPathAsString path to be set.
     */
    public void setLocalRootPathAsString(String localRootPathAsString) {
        this.localRootPathAsString = localRootPathAsString;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // private methods.

    /**
     * Creates Application when no previously serialized configuration is found.
     */
    private ClueApplication() {
        localRootPath = Paths.get(new JFileChooser().getFileSystemView().getDefaultDirectory().getAbsolutePath() + File.separator + "ClueDrive local files");
        initialize();

    }

    /**
     * Creates the View to the model. shows the main window.
     * @param application The Model for the application.
     */
    private static void createAndShowGUI(ClueApplication application) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Something unexpected happened, please check help",
                    "Internal error",
                    JOptionPane.ERROR_MESSAGE);
        }
        mainWindow = MainWindow.getInstance(application);
        mainWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainWindow.setVisible(true);
    }

    /**
     * Initializes Application model, without the serialized data.
     */
    private void initialize() {
        try {
            basePath = CPath.create("/Cloud");
            currentPath = basePath;
            currentDrive = null;
            currentFolder = new ArrayDeque<>();
            selected = new ArrayList<>();
        } catch (IllegalPathException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Something unexpected happened, please check help",
                    "Internal error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Persists Application model.
     */
    private void persist() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(setupOrigin.toFile()))) {
            localRootPathAsString = localRootPath.toString();
            outputStream.writeObject(this);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Something unexpected happened, plese check help",
                    "Internal error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
