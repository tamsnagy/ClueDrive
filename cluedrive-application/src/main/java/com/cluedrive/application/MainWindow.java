package com.cluedrive.application;

import com.cluedrive.commons.ClueDrive;
import com.cluedrive.commons.PropertiesUtility;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by Tamas on 2015-11-16.
 */
public class MainWindow extends JFrame {
    private static MainWindow instance = null;
    private ClueApplication model;

    private JPanel drivesPanel;
    private JPanel resourcePanel;
    private ImageIcon add;
    private ImageIcon remove;
    private JSplitPane splitPane;

    private java.util.List<Color> colorList = Arrays.asList(Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN);

    private MainWindow(ClueApplication model) {
        add = new ImageIcon("cluedrive-application/build/resources/main/images/add.png");
        remove = new ImageIcon("cluedrive-application/build/resources/main/images/remove.png");
        this.model = model;
        setTitle("ClueDrive Application");
        setSize(1100, 650);
        setMinimumSize(new Dimension(700, 400));

        setJMenuBar(initializedMenuBar());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, initializeDrivePane(), initializeResourcePane());
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
        iconLabel.setIcon(add);
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
        for(ClueDrive drive : model.getMyDrives()) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel ,BoxLayout.LINE_AXIS));
            panel.add(Box.createRigidArea(new Dimension(20, 40)));
            panel.setBackground(colorList.get(color));
            color++;
            if(color == colorList.size()) {
                color = 0;
            }
            label = new JLabel(drive.getProvider().toString());
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

    private JScrollPane initializeResourcePane() {
        resourcePanel = new JPanel();
        resourcePanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(resourcePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(500, 400));
        return scrollPane;
    }

    private JMenuBar initializedMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Add Drive");
        menuItem.addActionListener(actionEvent -> {
            startAddNewDrive();
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("DriveSettings");
        //TODO: Extend with registered menus
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Set Local Directory");
        menuItem.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setCurrentDirectory(model.getLocalRootPath().toFile());
            if(JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this, "Set as local home directory")) {
                //TODO: maybe copy all files from there to new place
                model.setLocalRootPath(fileChooser.getSelectedFile().toPath());
            }
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
}
