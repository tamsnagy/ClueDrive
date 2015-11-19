package com.cluedrive.application;

import com.cluedrive.commons.CFile;
import com.cluedrive.commons.CResource;
import com.cluedrive.commons.ClueDrive;
import com.cluedrive.exception.ClueException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Tamas on 2015-11-19.
 */
public class CResourceUI extends JPanel {
    private CResource resource;
    private ClueDrive holder;
    public static ImageIcon iconFolder;
    public static ImageIcon iconFile;

    public CResourceUI(CResource resource, ClueDrive holder) {
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

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    if(resource.isFolder()) {
                        ClueApplication.currentHolder = holder;
                        ClueApplication.currentPath = resource.getRemotePath();
                        ClueApplication.refreshMainPanel();
                    } else {
                        if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(e.getComponent().getParent(),
                                "Do you want to save file for offline access and open?",
                                "Save file",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE)) {
                            new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    try {
                                        Path localPath = Paths.get(ClueApplication.getLocalRootPath().toString() + File.separator + resource.getRemotePath().toString().substring(1));
                                        Files.createDirectories(localPath.getParent());
                                        holder.downloadFile((CFile) resource, localPath);
                                        Desktop.getDesktop().open(localPath.toFile());
                                    } catch (ClueException e1) {
                                        e1.printStackTrace();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                    return null;
                                }
                            }.execute();
                        }
                    }
                }
            }
        });
    }

    public CResource getResource() {
        return resource;
    }

    public ClueDrive getHolder() {
        return holder;
    }
}
