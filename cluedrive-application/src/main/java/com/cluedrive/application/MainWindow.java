package com.cluedrive.application;

import javax.swing.*;

/**
 * Created by Tamas on 2015-11-16.
 */
public class MainWindow extends JFrame {
    private static MainWindow instance = null;

    private MainWindow() {
        setTitle("ClueDrive Application");
        setSize(900, 700);
    }

    public static MainWindow getInstance() {
        if(instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }
}
