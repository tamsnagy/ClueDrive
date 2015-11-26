package com.cluedrive.application;

import com.cluedrive.commons.ClueDriveProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Created by Tamas on 2015-11-18.
 */
public class DriveChooserFrame extends JDialog {
    private MainWindow mainFrame;
    private ClueDriveProvider selectedProvider = null;
    private java.util.List<JLabel> providerLabels = new ArrayList<>();
    private JLabel tokenLabel;
    private ImageIcon iconLoad;
    private static final String SELECTOR_PANEL = "SelectorPanel";
    private static final String TOKEN_PANEL = "TokenPanel";
    private static final String LOAD_PANEL = "LaodPanel";
    private static final String DROPBOX_TOKEN_TEXT = "<html><center>Insert here the token you got<br/> from DropBox:</center></html>";
    private static final String ONEDRIVE_TOKEN_TEXT = "<html><center>Insert here the response url <br/> from your browsers addressBar:</center></html>";
    private static final Color selectedColor = new Color(38, 255, 107);
    public static ImageIcon iconDrive, iconDropbox, iconOneDrive;

    public DriveChooserFrame(MainWindow mainFrame, ImageIcon iconLoad) {
        super(mainFrame, true);
        this.mainFrame = mainFrame;
        this.iconLoad = iconLoad;
        setTitle("Add new Drive");
        setSize(350,300);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.getContentPane().setLayout(new CardLayout());
        this.add(createSelectorPanel(), SELECTOR_PANEL);
        this.add(createTokenPanel(), TOKEN_PANEL);
        this.add(createLoadPanel(), LOAD_PANEL);
        ((CardLayout)this.getContentPane().getLayout()).show(this.getContentPane(), SELECTOR_PANEL);
        this.setVisible(true);
    }

    private JPanel createLoadPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        JLabel label = new JLabel(iconLoad);
        card.add(label, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTokenPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.PAGE_AXIS));
        card.add(Box.createRigidArea(new Dimension(30, 70)));

        tokenLabel = new JLabel();
        tokenLabel.setAlignmentX(CENTER_ALIGNMENT);
        tokenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(tokenLabel);

        card.add(Box.createVerticalGlue());

        JTextField tokenTextField = new JTextField();
        tokenTextField.setAlignmentX(CENTER_ALIGNMENT);
        tokenTextField.setMaximumSize(new Dimension(220, 20));
        card.add(tokenTextField);

        card.add(Box.createVerticalGlue());

        JButton button = new JButton("Finish");
        button.addActionListener(actionEvent -> {
            String token = tokenTextField.getText();
            if("".equals(token)) {
                JOptionPane.showMessageDialog(this, "Paste the token from your browser.",
                        "Missing token", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if(ClueDriveProvider.ONEDRIVE.equals(selectedProvider)) {
                int tokenBegin = token.indexOf("access_token=") + "access_token=".length();
                int tokenEnd = token.indexOf("&token_type");
                token = token.substring(tokenBegin, tokenEnd).replace("%2b", " ");
            }
            ((CardLayout)this.getContentPane().getLayout()).show(this.getContentPane(), LOAD_PANEL);
            final String finalToken = token;
            new SwingWorker<Void, Integer>(){

                @Override
                protected Void doInBackground() throws Exception {
                    mainFrame.getModel().addAccessTokenToTmpDrive(finalToken);
                    publish(0);
                    return null;
                }

                @Override
                protected void process(final java.util.List<Integer> chunks) {
                    closeWindow();
                }
            }.execute();
        });
        button.setAlignmentX(CENTER_ALIGNMENT);
        card.add(button);

        card.add(Box.createVerticalGlue());

        card.add(Box.createRigidArea(new Dimension(20, 70)));

        return card;
    }

    private JPanel createSelectorPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.PAGE_AXIS));
        card.add(Box.createRigidArea(new Dimension(30, 40)));

        JLabel label = new JLabel("Choose Cloud provider:");
        label.setAlignmentX(CENTER_ALIGNMENT);

        card.add(label);

        card.add(Box.createVerticalGlue());

        JPanel cloudPanel = createCloudPanel();
        cloudPanel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(cloudPanel);

        card.add(Box.createVerticalGlue());

        label = new JLabel("<html><center>By authorizing <b>ClueDrive</b> to access your cloud,<br/>it will create a new folder on your cloud<br/>and will not touch your other data.<center></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(CENTER_ALIGNMENT);

        card.add(label);

        card.add(Box.createVerticalGlue());

        JButton button = new JButton("Authorize");
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.addActionListener(actionEvent->{
            if(selectedProvider == null) {
                JOptionPane.showMessageDialog(this, "Please select a provider by clicking on their icon.",
                        "Select Cloud provider", JOptionPane.WARNING_MESSAGE);
            } else {
                mainFrame.getModel().addDriveCandidate(selectedProvider);
                switch(selectedProvider){
                    case DROPBOX:
                        tokenLabel.setText(DROPBOX_TOKEN_TEXT);
                        break;
                    case ONEDRIVE:
                        tokenLabel.setText(ONEDRIVE_TOKEN_TEXT);
                        break;
                    case GOOGLE:
                        mainFrame.getModel().addAccessTokenToTmpDrive(null);
                        dispose();
                        return;
                }
                ((CardLayout)this.getContentPane().getLayout()).show(this.getContentPane(), TOKEN_PANEL);
            }
        });
        card.add(button);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel createCloudPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        panel.add(Box.createHorizontalGlue());

        JLabel label = new JLabel("Drive", iconDrive, SwingConstants.LEFT);
        label.setOpaque(true);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(JLabel providerLabel: providerLabels) {
                    providerLabel.setBackground(null);
                }
                if(setSelectedProvider(ClueDriveProvider.GOOGLE)) {
                    e.getComponent().setBackground(selectedColor);
                }
            }
        });
        providerLabels.add(label);
        panel.add(label);

        panel.add(Box.createHorizontalGlue());

        label = new JLabel("OneDrive", iconOneDrive, SwingConstants.LEFT);
        label.setOpaque(true);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(JLabel providerLabel: providerLabels) {
                    providerLabel.setBackground(null);
                }
                if(setSelectedProvider(ClueDriveProvider.ONEDRIVE)) {
                    e.getComponent().setBackground(selectedColor);
                }
            }
        });
        providerLabels.add(label);
        panel.add(label);

        panel.add(Box.createHorizontalGlue());

        label = new JLabel("DropBox", iconDropbox, SwingConstants.LEFT);
        label.setOpaque(true);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(JLabel providerLabel: providerLabels) {
                    providerLabel.setBackground(null);
                }
                if(setSelectedProvider(ClueDriveProvider.DROPBOX)) {
                    e.getComponent().setBackground(selectedColor);
                }
            }
        });
        providerLabels.add(label);
        panel.add(label);

        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    private boolean setSelectedProvider(ClueDriveProvider provider) {
        if(selectedProvider == provider) {
            selectedProvider = null;
            return false;
        }
        selectedProvider = provider;
        return true;
    }

    public void closeWindow(){
        dispose();
    }
}
