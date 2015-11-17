package com.cluedrive.application;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Tamas on 2015-11-16.
 */
public class MainApplication {
    private static MainWindow mainWindow;


    public static void main(String[] args) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    createAndShowGUI();
                }
            });
    }

    private static void createAndShowGUI() {
        mainWindow = MainWindow.getInstance();
        mainWindow.setVisible(true);
    }
}
