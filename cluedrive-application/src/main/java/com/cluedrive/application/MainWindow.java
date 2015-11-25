package com.cluedrive.application;

import com.cluedrive.commons.PropertiesUtility;
import com.cluedrive.exception.ClueException;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

/**
 * Created by Tamas on 2015-11-16.
 */
public class MainWindow extends JFrame {
    private static MainWindow instance = null;
    private ClueApplication model;

    private JPanel drivesPanel;
    private JPanel mainPanel;
    private JPanel resourcePanel;
    private JPanel addressPanel;
    private ImageIcon iconAdd;
    private ImageIcon iconRemove;
    private ImageIcon iconLoad;
    private ImageIcon iconBack;
    private ImageIcon iconNewFolder;
    private ImageIcon iconRemoveSelection;
    private JSplitPane splitPane;

    public JLabel removeSelectionIcon;
    public JLabel removeSelectionText;

    private java.util.List<Color> colorList = Arrays.asList(Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN);

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

        CResourceUI.iconFile = iconFile;
        CResourceUI.iconFolder = iconFolder;
        CResourceUI.iconTick = iconTick;

        this.model = model;
        setTitle("ClueDrive Application");
        setSize(1100, 650);
        setMinimumSize(new Dimension(700, 400));

        setJMenuBar(initializedMenuBar());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, initializeDrivePane(), initializeMainPanel());
        this.add(splitPane);
    }

    /**
     * Initializes drives panel. Which shows what kind of drives are registered.
     * @return The scrollable panel of drives.
     */
    private JScrollPane initializeDrivePane() {
        drivesPanel = new JPanel();
        drivesPanel.setBackground(Color.BLUE);
        drivesPanel.setLayout(new BoxLayout(drivesPanel, BoxLayout.PAGE_AXIS));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel ,BoxLayout.LINE_AXIS));
        panel.add(Box.createRigidArea(new Dimension(5, 40)));
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(iconAdd);
        panel.add(iconLabel);
        JLabel label = new JLabel("Add new Drive for more space");
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

        int color = 0;
        for(AppDrive drive : model.getMyDrives()) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel ,BoxLayout.LINE_AXIS));
            panel.add(Box.createRigidArea(new Dimension(20, 40)));
            panel.setBackground(colorList.get(color));
            color++;
            if(color == colorList.size()) {
                color = 0;
            }
            label = new JLabel(drive.getDrive().getProvider().toString());
            label.setHorizontalTextPosition(SwingConstants.LEADING);
            panel.add(label);
            panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
            panel.setAlignmentX(LEFT_ALIGNMENT);
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

        return mainPanel;
    }

    /**
     * Initializes address panel. Contains the mouse listeners of action icons.
     */
    private void initializeAddressPanel() {
        addressPanel = new JPanel();
        addressPanel.setAlignmentY(LEFT_ALIGNMENT);
        addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.LINE_AXIS));

        addressPanel.add(Box.createRigidArea(new Dimension(20,20)));

        // Go a level up in directory tree

        JLabel label = new JLabel(iconBack);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("Go Up");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(ClueApplication.currentPath.equals(ClueApplication.basePath)) {
                    return;
                }
                ClueApplication.stepOutFolder();
                refreshResourcePane();
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10,10)));

        // Create new folder

        label = new JLabel(iconNewFolder);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("New Folder");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String folderName = JOptionPane.showInputDialog(instance, "New folders name:", "New folder", JOptionPane.QUESTION_MESSAGE);
                if(folderName != null &&  ! "".equals(folderName)) {
                    model.createFolder(folderName);
                    refreshResourcePane();
                }
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10,10)));

        // Add new item to current folder.

        label = new JLabel(iconAdd);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("Add item");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //TODO: iconAdd file
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10,10)));

        //Delete items if there is any selected one.

        label = new JLabel(iconRemove);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setToolTipText("Delete selected items");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(ClueApplication.deleteSelectedResources()) {
                    removeSelectionText.setVisible(false);
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

        removeSelectionIcon = new JLabel(iconRemoveSelection);
        removeSelectionIcon.setVisible(false);
        removeSelectionIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeSelections();
            }
        });

        addressPanel.add(removeSelectionIcon);

        addressPanel.add(Box.createRigidArea(new Dimension(5, 5)));

        removeSelectionText = new JLabel("Erase selection");
        removeSelectionText.setVisible(false);
        removeSelectionText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeSelections();
            }
        });
        addressPanel.add(removeSelectionText);

        addressPanel.add(Box.createRigidArea(new Dimension(5, 5)));
    }

    /**
     * Initializes resource pane. Lists resources from current path.
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
                if(ClueApplication.currentDrive == null) {
                    //current path is the root, every drive must be asked for its root resources.
                    model.getMyDrives().parallelStream().forEach(drive -> {
                        try {
                            publish(drive.getDrive().list(ClueApplication.currentPath).parallelStream().map(resource -> new CResourceUI(resource, drive))
                                            .toArray(CResourceUI[]::new)
                            );
                        } catch (ClueException e) {
                            e.printStackTrace();
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
             * @param chunks
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
     * @return
     */
    private JMenuBar initializedMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Add Drive");
        menuItem.addActionListener(actionEvent ->
            startAddNewDrive()
        );
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("DriveSettings");
        //TODO: Extend with registered menus
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Set Local Directory");
        menuItem.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setCurrentDirectory(ClueApplication.getLocalRootPath().toFile());
            if(JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this, "Set as local home directory")) {
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
        menuItem.addActionListener(actionEvent -> {
            this.dispose();
        });
        fileMenu.add(menuItem);


        JMenu aboutMenu = new JMenu("About");

        menuItem = new JMenuItem("GitHUB");
        menuItem.addActionListener(actionEvent -> {
                    String gitHUB = PropertiesUtility.applicationProperty("gitHUB");
            try {
                java.awt.Desktop.getDesktop().browse(
                        new URI(gitHUB));
            } catch (IOException | URISyntaxException e) {
                JOptionPane.showMessageDialog(instance, "<html><a href=\""+gitHUB+"\">"+gitHUB+"</a></html>");
            }
        }
        );
        aboutMenu.add(menuItem);

        menuItem = new JMenuItem("Help");
        //TODO: Open help window
        aboutMenu.add(menuItem);

        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        return menuBar;
    }

    /**
     * Erases the selections on UI.
     */
    private void removeSelections(){
        for (Component component : resourcePanel.getComponents()) {
            ((CResourceUI) component).hideSelected();
        }
        ClueApplication.emptySelected();
        invertShowRemoveSelectionLabel();
    }

    /**
     * Begins the adding new drive flow.
     */
    private void startAddNewDrive() {
        DriveChooserFrame frame = new DriveChooserFrame(instance);
    }

    /**
     * Returns singleton instance of the MainWindow.
     * @param model The application model on which View and controllers are based.
     * @return singleton instance of the MainWindow.
     */
    public static MainWindow getInstance(ClueApplication model) {
        if(instance == null) {
            instance = new MainWindow(model);
        }
        return instance;
    }

    /**
     * Model getter
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
        splitPane.setRightComponent(mainPanel);
    }

    /**
     * Inverts the remove selection icon and text labels.
     */
    public void invertShowRemoveSelectionLabel() {
        removeSelectionIcon.setVisible(!removeSelectionIcon.isVisible());
        removeSelectionText.setVisible(!removeSelectionText.isVisible());
        addressPanel.repaint();
    }


}
