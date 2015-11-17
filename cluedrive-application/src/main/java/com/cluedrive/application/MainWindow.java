package com.cluedrive.application;

import org.springframework.beans.factory.annotation.Value;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Tamas on 2015-11-16.
 */
public class MainWindow extends JFrame {
    private static MainWindow instance = null;

    private MainWindow() {


        setTitle("ClueDrive Application");
        setSize(1100, 650);

        setJMenuBar(initializedMenuBar());
    }

    private JMenuBar initializedMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Add Drive");
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("DriveSettings");
        //TODO: Extend with registered menus
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Set Local Directory");
        //TODO: Add file picker
        fileMenu.add(menuItem);


        fileMenu.addSeparator();
        menuItem = new JMenuItem("Exit");
        fileMenu.add(menuItem);


        JMenu aboutMenu = new JMenu("About");

        menuItem = new JMenuItem("GitHUB");
        menuItem.addActionListener(actionEvent -> {
                    String gitHUB = PropertiesUtility.readProperty("gitHUB");
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

    public static MainWindow getInstance() {
        if(instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }
}
