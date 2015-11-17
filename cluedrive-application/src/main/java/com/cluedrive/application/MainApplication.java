package com.cluedrive.application;

import javax.swing.*;

/**
 * Created by Tamas on 2015-11-16.
 */

public class MainApplication {
    private static MainWindow mainWindow;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApplication::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
        mainWindow = MainWindow.getInstance();
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.setVisible(true);
    }
}
