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

    private JPanel initializeMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        initializeAddressPanel();
        JScrollPane scrollResourcePane = initializeResourcePane();

        mainPanel.add(addressPanel);
        mainPanel.add(scrollResourcePane);

        return mainPanel;
    }

    private void initializeAddressPanel() {
        addressPanel = new JPanel();
        addressPanel.setAlignmentY(LEFT_ALIGNMENT);
        addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.LINE_AXIS));

        addressPanel.add(Box.createRigidArea(new Dimension(20,20)));

        JLabel label = new JLabel(iconBack);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(ClueApplication.currentPath.equals(ClueApplication.basePath)) {
                    return;
                }
                ClueApplication.stepOutFolder();
                refreshAddressPane();
                refreshResourcePane();
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10,10)));

        label = new JLabel(iconNewFolder);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String folderName = JOptionPane.showInputDialog(instance, "New folders name:", "New folder", JOptionPane.QUESTION_MESSAGE);
                if(folderName != null &&  ! "".equals(folderName)) {
                    model.createFolder(folderName);
                    refreshAddressPane();
                    refreshResourcePane();
                }
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10,10)));

        label = new JLabel(iconAdd);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //TODO: iconAdd file
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10,10)));

        label = new JLabel(iconRemove);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //TODO: iconRemove
            }
        });
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        label = new JLabel(" " + ClueApplication.getLocalRootPath().toString() + "  ");
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
        label.setToolTipText("Offline files root directory on your computer");
        addressPanel.add(label);

        addressPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        label = new JLabel(ClueApplication.currentPath.toString());
        label.setAlignmentY(CENTER_ALIGNMENT);
        addressPanel.add(label);

        addressPanel.add(Box.createHorizontalGlue());

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

    private JScrollPane initializeResourcePane() {
        resourcePanel = new JPanel();
        resourcePanel.setBackground(Color.WHITE);
        resourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        resourcePanel.setPreferredSize(new Dimension(480, 380));
        SwingWorker worker = new SwingWorker<Void, CResourceUI>() {
            @Override
            protected Void doInBackground() throws Exception {
                if(ClueApplication.currentDrive == null) {
                    model.getMyDrives().parallelStream().forEach(drive -> {
                        try {
                            publish(drive.getDrive().list(ClueApplication.currentPath).stream().map(resource -> new CResourceUI(resource, drive))
                                            .toArray(CResourceUI[]::new)
                            );
                        } catch (ClueException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    publish(ClueApplication.currentDrive.getDrive().list(ClueApplication.currentPath).stream().map(resource -> new CResourceUI(resource, ClueApplication.currentDrive))
                            .toArray(CResourceUI[]::new));
                }
                return null;
            }

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
            refreshAddressPane();
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

    private void removeSelections(){
        for (Component component : resourcePanel.getComponents()) {
            ((CResourceUI) component).hideSelected();
        }
        ClueApplication.emptySelected();
        invertShowRemoveSelectionLabel();
    }

    private void startAddNewDrive() {
        DriveChooserFrame frame = new DriveChooserFrame(instance);
    }

    public static MainWindow getInstance(ClueApplication model) {
        if(instance == null) {
            instance = new MainWindow(model);
        }
        return instance;
    }


    public ClueApplication getModel() {
        return model;
    }

    public void refreshDrivePane() {
        splitPane.setLeftComponent(initializeDrivePane());
    }

    public void refreshResourcePane() {
        splitPane.setRightComponent(initializeMainPanel());
    }

    public void refreshAddressPane() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        initializeAddressPanel();

        mainPanel.add(addressPanel);
        JScrollPane scrollPane = new JScrollPane(resourcePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(500, 400));
        mainPanel.add(scrollPane);
    }

    public void invertShowRemoveSelectionLabel() {
        removeSelectionIcon.setVisible(!removeSelectionIcon.isVisible());
        removeSelectionText.setVisible(!removeSelectionText.isVisible());
        addressPanel.repaint();
    }
}
