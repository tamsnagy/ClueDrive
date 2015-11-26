package com.cluedrive.application;

import com.cluedrive.commons.CFile;
import com.cluedrive.commons.CFolder;
import com.cluedrive.commons.CResource;
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
 * Panel which represents a resource from cloud.
 */
public class CResourceUI extends JPanel {
    /**
     * Icon representing a folder.
     */
    public static ImageIcon iconFolder;
    /**
     * Icon representing a file.
     */
    public static ImageIcon iconFile;
    /**
     * Icon representing if resource is selected.
     */
    public static ImageIcon iconTick;
    /**
     * The model of the resource.
     */
    private CResource resource;
    /**
     * The drive which contains the resource.
     */
    private AppDrive holder;
    /**
     * Label which contains the tick icon.
     */
    private JLabel tickLabel;


    /**
     * Initializes a ResourceUI.
     * @param resource The model of the resource.
     * @param holder The model of the drive which holds the resource.
     */
    public CResourceUI(CResource resource, AppDrive holder) {
        this.holder = holder;
        this.resource = resource;
        this.setOpaque(false);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setMinimumSize(new Dimension(60, 60));

        JLabel label = new JLabel();
        label.setOpaque(false);
        label.setAlignmentX(CENTER_ALIGNMENT);
        if (resource.isFolder()) {
            label.setIcon(iconFolder);
        } else {
            label.setIcon(iconFile);
        }
        this.add(label);

        tickLabel = new JLabel(iconTick);
        tickLabel.setAlignmentX(CENTER_ALIGNMENT);
        tickLabel.setOpaque(false);
        tickLabel.setVisible(false);

        this.add(tickLabel);

        label = new JLabel(resource.getName());
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setOpaque(false);
        this.add(label);

        this.add(Box.createVerticalGlue());

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    if (tickLabel.isVisible()) {
                        tickLabel.setVisible(false);
                        ClueApplication.removeSelected((CResourceUI) e.getComponent());
                    } else {
                        tickLabel.setVisible(true);
                        ClueApplication.addSelected((CResourceUI) e.getComponent());
                    }
                }
                if (e.getClickCount() == 2) {
                    if (resource.isFolder()) {
                        ClueApplication.emptySelected();
                        ClueApplication.currentDrive = holder;
                        ClueApplication.stepInFolder((CFolder) resource);
                        ClueApplication.refreshMainPanel();
                    } else {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(e.getComponent().getParent(),
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
                                        holder.getDrive().downloadFile((CFile) resource, localPath);
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

    /**
     * Returns the resource model of the resourceUI.
     * @return resource model.
     */
    public CResource getResource() {
        return resource;
    }

    /**
     * Returns the drive model of the resourceUI.
     * @return drive model.
     */
    public AppDrive getHolder() {
        return holder;
    }

    /**
     * Hides tick label.
     */
    public void hideSelected() {
        tickLabel.setVisible(false);
        this.repaint();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CResourceUI that = (CResourceUI) o;

        if (!resource.equals(that.resource)) return false;
        return holder.equals(that.holder);

    }

    @Override
    public int hashCode() {
        int result = resource.hashCode();
        result = 31 * result + holder.hashCode();
        return result;
    }
}
