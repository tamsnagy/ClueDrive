package com.cluedrive.application;

import com.cluedrive.commons.PropertiesUtility;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.NotExistingPathException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Main JFrame of the ClueApplication. The base View of the Model.
 */
public class MainWindow extends JFrame {
    /**
     * Singleton instance
     */
    private static MainWindow instance = null;
    /**
     * The label of erase selections.
     */
    private JLabel removeSelectionIcon;
    /**
     * The model attached to View
     */
    private ClueApplication model;
    /**
     * Panel which holds the registered drives data.
     */
    private JPanel drivesPanel;
    /**
     * Panel which holds tha addressBar and ResourcePanel.
     */
    private JPanel mainPanel;
    /**
     * Panel which holds the representation of resources uploaded to drives.
     */
    private JPanel resourcePanel;
    /**
     * Panel which holds action labels and information of the resource tree.
     */
    private JPanel addressPanel;
    /**
     * Icon used to add new file.
     */
    private ImageIcon iconAdd;
    /**
     * Icon used to delete selected resources.
     */
    private ImageIcon iconRemove;
    /**
     * Icon used to show something is in progress.
     */
    private ImageIcon iconLoad;
    /**
     * Icon used ro go up in resource tree.
     */
    private ImageIcon iconBack;
    /**
     * Icon used to create new folder/
     */
    private ImageIcon iconNewFolder;
    /**
     * Icon used to erase selection.
     */
    private ImageIcon iconRemoveSelection;
    /**
     * Holder of drives and resources panes.
     */
    private JSplitPane splitPane;
    /**
     * Colors used to differ drives on UI.
     */
    private java.util.List<Color> colorList = Arrays.asList(
            new Color(141, 206, 234),
            new Color(234, 176, 144),
            new Color(117, 234, 35),
            new Color(234, 232, 70),
            new Color(168, 87, 234),
            new Color(234, 85, 63)
    );

    /**
     * Returns singleton instance of the MainWindow.
     *
     * @param model The application model on which View and controllers are based.
     * @return singleton instance of the MainWindow.
     */
    public static MainWindow getInstance(ClueApplication model) {
        if (instance == null) {
            instance = new MainWindow(model);
        }
        return instance;
    }

    /**
     * Model getter
     *
     * @return model of application.
     */
    public ClueApplication getModel() {
        return model;
    }

    /**
     * Re initializes the drives panel.
     */
    public void refreshDrivePane() {
        splitPane.setLeftComponent(initializeDrivePane());
    }

    /**
     * Re initializes the resources panel.
     */
    public void refreshResourcePane() {
        splitPane.setRightComponent(initializeMainPanel());
    }

    /**
     * Re initializes the address panel.
     */
    public void refreshAddressPane() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        initializeAddressPanel();

        mainPanel.add(addressPanel);
        JScrollPane scrollPane = new JScrollPane(resourcePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(500, 400));
        mainPanel.add(scrollPane);
        if (model.getMyDrives().isEmpty()) {
            disableComponents(mainPanel);
        }
        splitPane.setRightComponent(mainPanel);
    }

    /**
     * Inverts the remove selection icon and text labels.
     */
    public void invertShowRemoveSelectionLabel() {
        removeSelectionIcon.setVisible(!removeSelectionIcon.isVisible());
        addressPanel.repaint();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Private methods

    /**
     * Loads icons, creates main window according to the model.
     *
     * @param model The model of the MVC.
     */
    private MainWindow(ClueApplication model) {
        iconAdd = new ImageIcon("cluedrive-application/build/resources/main/images/add.png");
        iconRemove = new ImageIcon("cluedrive-application/build/resources/main/images/remove.png");
        ImageIcon iconFolder = new ImageIcon("cluedrive-application/build/resources/main/images/folder.png");
        ImageIcon iconFile = new ImageIcon("cluedrive-application/build/resources/main/images/file.png");
        iconLoad = new ImageIcon("cluedrive-application/build/resources/main/images/load.gif");
        iconBack = new ImageIcon("cluedrive-application/build/resources/main/images/back.png");
        iconNewFolder = new ImageIcon("cluedrive-application/build/resources/main/images/new_folder.png");
        ImageIcon iconTick = new ImageIcon("cluedrive-application/build/resources/main/images/tick.png");
        iconRemoveSelection = new ImageIcon("cluedrive-application/build/resources/main/images/remove_selection.png");
        ImageIcon iconDrive = new ImageIcon("cluedrive-application/build/resources/main/images/drive48.png");
        ImageIcon iconDropbox = new ImageIcon("cluedrive-application/build/resources/main/images/dropbox48.png");
        ImageIcon iconOnedrive = new ImageIcon("cluedrive-application/build/resources/main/images/onedrive48.png");

        DriveChooserFrame.iconDrive = iconDrive;
        DriveChooserFrame.iconDropbox = iconDropbox;
        DriveChooserFrame.iconOneDrive = iconOnedrive;
        DriveChooserFrame.iconLoad = iconLoad;

        CResourceUI.iconFile = iconFile;
        CResourceUI.iconFolder = iconFolder;
        CResourceUI.iconTick = iconTick;

        this.model = model;
        setTitle("ClueDrive Application");
        setSize(1100, 650);
        setMinimumSize(new Dimension(700, 400));
        setLocationRelativeTo(null);

        setJMenuBar(initializedMenuBar());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, initializeDrivePane(), initializeMainPanel());
        this.add(splitPane);
    }

    /**
     * Initializes drives panel. Which shows what kind of drives are registered.
     *
     * @return The scrollable panel of drives.
     */
    private JScrollPane initializeDrivePane() {
        drivesPanel = new JPanel();
        drivesPanel.setBackground(new Color(253, 253, 254));
        drivesPanel.setLayout(new BoxLayout(drivesPanel, BoxLayout.PAGE_AXIS));

        // Total size panel
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(200, 40));
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createRigidArea(new Dimension(30, 40)));
        JLabel label = new JLabel("<html><b>Total size:</b></html>");
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setHorizontalTextPosition(SwingConstants.LEADING);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(10, 40)));
        long bytes = 0;
        for (AppDrive drive : model.getMyDrives()) {
            bytes += drive.getAccountInfo().getTotal();
        }
        double GBytes = bytes / 1024.0 / 1024 / 1024; //GB
        label = new JLabel(String.format("<html><b>%.2f</b></html>", GBytes));
        label.setAlignmentY(CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(10, 40)));
        label = new JLabel("<html><b>GB</b></html>");
        label.setAlignmentY(CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(10, 40)));

        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(Box.createHorizontalGlue());
        panel.add(Box.createRigidArea(new Dimension(10, 40)));
        drivesPanel.add(panel);

        // Add new Drive panel

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createRigidArea(new Dimension(5, 40)));
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(iconAdd);
        panel.add(iconLabel);
        panel.setBackground(new Color(180, 229, 29));
        label = new JLabel("Add new Drive for more space");
        label.setForeground(new Color(236, 23, 31));
        label.setHorizontalTextPosition(SwingConstants.LEADING);
        panel.add(label);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startAddNewDrive();
            }
        });
        drivesPanel.add(panel);

        // List of registered drive

        int color = 0;
        for (AppDrive drive : model.getMyDrives()) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.add(Box.createRigidArea(new Dimension(20, 40)));
            panel.setBackground(colorList.get(color));
            color++;
            if (color == colorList.size()) {
                color = 0;
            }
            label = new JLabel(drive.getDrive().getProvider().toString());
            label.setHorizontalTextPosition(SwingConstants.LEADING);
            panel.add(label);
            panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
            panel.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(Box.createHorizontalGlue());
            label = new JLabel(drive.getAccountInfo().getName());
            panel.add(label);
            panel.add(Box.createRigidArea(new Dimension(20, 40)));
            drivesPanel.add(panel);
        }
        drivesPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(drivesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(200, 400));
        return scrollPane;
    }

    /**
     * Creates The main panel with addressPanel, and Resources panel.
     * @return The mainPanel
     */
    private JPanel initializeMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        initializeAddressPanel();
        JScrollPane scrollResourcePane = initializeResourcePane();

        mainPanel.add(addressPanel);
        mainPanel.add(scrollResourcePane);
        if (model.getMyDrives().isEmpty()) {
            disableComponents(mainPanel);
        }

        return mainPanel;
    }

    /**
     * Initializes address panel. Contains the mouse listeners of action icons.
     */
    private void initializeAddressPanel() {
        addressPanel = new JPanel();
        addressPanel.setAlignmentY(CENTER_ALIGNMENT);
        addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.LINE_AXIS));

        addressPanel.add(Box.createRigidArea(new Dimension(20, 20)));

        // Go a level up in directory tree

        JLabel label = new JLabel(iconBack);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("Go Up");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (ClueApplication.currentPath.equals(ClueApplication.basePath)) {
                    return;
                }
                ClueApplication.stepOutFolder();
                refreshResourcePane();
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        // Create new folder

        label = new JLabel(iconNewFolder);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("New Folder");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String folderName = JOptionPane.showInputDialog(instance, "New folders name:", "New folder", JOptionPane.QUESTION_MESSAGE);
                if (folderName != null && !"".equals(folderName)) {
                    model.createFolder(folderName);
                    refreshResourcePane();
                }
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        // Add new item to current folder.

        label = new JLabel(iconAdd);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("Add item");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.setApproveButtonText("Upload");
                int response = fileChooser.showOpenDialog(instance);
                if (response == JFileChooser.APPROVE_OPTION) {
                    new SwingWorker<Void, Integer>() {
                        /**
                         * Uploads selected file.
                         */
                        @Override
                        protected Void doInBackground() throws Exception {
                            model.uploadItem(fileChooser.getSelectedFile().toPath());
                            publish(0);
                            return null;
                        }

                        /**
                         * Adds loaded resources to resource panel.
                         *
                         * @param chunks Dummy value
                         */
                        @Override
                        protected void process(final List<Integer> chunks) {
                            refreshResourcePane();
                        }
                    }.execute();

                }
            }

        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        //Delete items if there is any selected one.

        label = new JLabel(iconRemove);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("Delete selected items");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (ClueApplication.deleteSelectedResources()) {
                    removeSelectionIcon.setVisible(false);
                    refreshResourcePane();
                }
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        // Show the location on local drive.

        label = new JLabel(" " + ClueApplication.getLocalRootPath().toString() + "  ");
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
        label.setToolTipText("Offline files root directory on your computer");
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        // Show the current path on cloud.

        label = new JLabel(ClueApplication.currentPath.toString());
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("Path on cloud");
        addressPanel.add(label);

        addressPanel.add(Box.createHorizontalGlue());

        // Erase selection icon and label.

        removeSelectionIcon = new JLabel("Erase selection", iconRemoveSelection, SwingConstants.CENTER);
        removeSelectionIcon.setVisible(false);
        removeSelectionIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Component component : resourcePanel.getComponents()) {
                    ((CResourceUI) component).hideSelected();
                }
                ClueApplication.emptySelected();
                invertShowRemoveSelectionLabel();
            }
        });

        addressPanel.add(removeSelectionIcon);

        addressPanel.add(Box.createRigidArea(new Dimension(5, 5)));
    }

    /**
     * Initializes resource pane. Lists resources from current path.
     *
     * @return The scrollable pane with resources, or a loading panel.
     */
    private JScrollPane initializeResourcePane() {
        resourcePanel = new JPanel();
        resourcePanel.setBackground(Color.WHITE);
        resourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        resourcePanel.setPreferredSize(new Dimension(480, 380));
        SwingWorker worker = new SwingWorker<Void, CResourceUI>() {
            /**
             * Loads all resources according to ClueApplication currentPath.
             */
            @Override
            protected Void doInBackground() throws Exception {
                if (ClueApplication.currentDrive == null) {
                    //current path is the root, every drive must be asked for its root resources.
                    model.getMyDrives().parallelStream().forEach(drive -> {
                        try {
                            publish(drive.getDrive().list(ClueApplication.currentPath).parallelStream().map(resource -> new CResourceUI(resource, drive))
                                            .toArray(CResourceUI[]::new)
                            );
                        } catch (NotExistingPathException e) {
                            JOptionPane.showMessageDialog(instance,
                                    "<html>" + ClueApplication.currentPath.toString() + " was removed from " + drive.getDrive().getProvider() +".<br/>Please follow helps reset drives section.</html>",
                                    "Cloud folder changed unexpectedly",
                                    JOptionPane.ERROR_MESSAGE);
                        } catch (ClueException e) {
                            JOptionPane.showMessageDialog(instance,
                                    "Something unexpected happened, plese check help",
                                    "Internal error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } else {
                    // only the owner of current path needs to be asked for its resources.
                    publish(ClueApplication.currentDrive.getDrive().list(ClueApplication.currentPath).parallelStream().map(resource -> new CResourceUI(resource, ClueApplication.currentDrive))
                            .toArray(CResourceUI[]::new));
                }
                return null;
            }

            /**
             * Adds loaded resources to resource panel.
             * @param chunks Dummy value
             */
            @Override
            protected void process(final List<CResourceUI> chunks) {
                chunks.forEach(resourcePanel::add);
                mainPanel = new JPanel();
                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

                initializeAddressPanel();

                mainPanel.add(addressPanel);
                JScrollPane scrollPane = new JScrollPane(resourcePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setMinimumSize(new Dimension(500, 400));
                mainPanel.add(scrollPane);
                splitPane.setRightComponent(mainPanel);
            }
        };
        JPanel loadPanel = new JPanel();
        loadPanel.setPreferredSize(new Dimension(480, 380));
        loadPanel.setVisible(true);
        loadPanel.setLayout(new BorderLayout());
        JLabel label = new JLabel(iconLoad);
        loadPanel.add(label, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(loadPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(500, 400));
        worker.execute();
        return scrollPane;

    }

    /**
     * Initializes the menu bar.
     *
     * @return The menu bar.
     */
    private JMenuBar initializedMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Add Drive");
        menuItem.addActionListener(actionEvent ->
                        startAddNewDrive()
        );
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Set Local Directory");
        menuItem.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setCurrentDirectory(ClueApplication.getLocalRootPath().toFile());
            if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this, "Set as local home directory")) {
                model.setLocalRootPath(fileChooser.getSelectedFile().toPath());
                refreshAddressPane();
            }
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Refresh");
        menuItem.addActionListener(actionEvent -> {
            refreshDrivePane();
            refreshResourcePane();
        });
        fileMenu.add(menuItem);


        fileMenu.addSeparator();
        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener(actionEvent -> this.dispose());
        fileMenu.add(menuItem);


        JMenu aboutMenu = new JMenu("About");

        menuItem = new JMenuItem("GitHUB");
        menuItem.addActionListener(actionEvent -> {
                    String gitHUB = PropertiesUtility.applicationProperty("gitHUB");
                    try {
                        java.awt.Desktop.getDesktop().browse(
                                new URI(gitHUB));
                    } catch (IOException | URISyntaxException e) {
                        JOptionPane.showMessageDialog(instance, "<html><a href=\"" + gitHUB + "\">" + gitHUB + "</a></html>");
                    }
                }
        );
        aboutMenu.add(menuItem);

        menuItem = new JMenuItem("Help");
        menuItem.addActionListener(actionEvent -> {
                    String help = PropertiesUtility.applicationProperty("help");
                    try {
                        java.awt.Desktop.getDesktop().browse(
                                new URI(help));
                    } catch (IOException | URISyntaxException e) {
                        JOptionPane.showMessageDialog(instance, "<html><a href=\"" + help + "\">" + help + "</a></html>");
                    }
                }
        );
        aboutMenu.add(menuItem);

        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        return menuBar;
    }

    /**
     * Begins the adding new drive flow.
     */
    private void startAddNewDrive() {
        new DriveChooserFrame(instance);
    }

    /**
     * Disables all subComponents of a container recursively.
     * @param container the container with disabled components.
     */
    private void disableComponents(Container container) {
        for (Component component : container.getComponents()) {
            component.setEnabled(false);
            if (component instanceof Container) {
                disableComponents((Container) component);
            }
        }
    }
}
