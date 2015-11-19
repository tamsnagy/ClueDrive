package com.cluedrive.application;

import com.cluedrive.commons.CResource;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Tamas on 2015-11-19.
 */
public class CResourceUI extends JPanel {
    private CResource resource;
    public static ImageIcon iconFolder;
    public static ImageIcon iconFile;

    public CResourceUI(CResource resource) {
        this.resource = resource;
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setMinimumSize(new Dimension(60, 60));

        JLabel label = new JLabel();
        label.setAlignmentX(CENTER_ALIGNMENT);
        if(resource.isFolder()) {
            label.setIcon(iconFolder);
        } else {
            label.setIcon(iconFile);
        }
        this.add(label);

        this.add(Box.createRigidArea(new Dimension(5, 5)));

        label = new JLabel(resource.getName());
        label.setAlignmentX(CENTER_ALIGNMENT);
        this.add(label);

        this.add(Box.createVerticalGlue());
    }
}
