/*
 * Decompiled with CFR 0.152.
 */
package exp.gui;

import exp.gui.SoundboardFrame;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;

public class UpdateConfirmFrame
extends JFrame {
    private static final long serialVersionUID = -6700862565543741036L;
    private static final String url = "https://sourceforge.net/projects/expsoundboard/";
    private JTextPane textPane;

    public UpdateConfirmFrame(String updateNotes) {
        this.setResizable(false);
        this.setDefaultCloseOperation(2);
        this.setTitle("Update Available!");
        JLabel lblSoundboardUpdateAvailable = new JLabel("EXP Soundboard Update Available");
        JScrollPane scrollPane = new JScrollPane();
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                UpdateConfirmFrame.this.dispose();
            }
        });
        JButton btnGetUpdate = new JButton("Get Update");
        btnGetUpdate.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(UpdateConfirmFrame.url));
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
                catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
                UpdateConfirmFrame.this.dispose();
            }
        });
        final JCheckBox chckbxCheckForUpdates = new JCheckBox("Check for Updates on launch");
        chckbxCheckForUpdates.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.updateCheck = !SoundboardFrame.updateCheck;
                chckbxCheckForUpdates.setSelected(SoundboardFrame.updateCheck);
            }
        });
        chckbxCheckForUpdates.setSelected(SoundboardFrame.updateCheck);
        GroupLayout groupLayout = new GroupLayout(this.getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(scrollPane, -1, 480, Short.MAX_VALUE).addComponent(lblSoundboardUpdateAvailable).addGroup(groupLayout.createSequentialGroup().addComponent(chckbxCheckForUpdates).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 161, Short.MAX_VALUE).addComponent(btnGetUpdate).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(btnClose))).addContainerGap()));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblSoundboardUpdateAvailable).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(scrollPane, -2, 124, -2).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(btnClose).addComponent(chckbxCheckForUpdates).addComponent(btnGetUpdate)).addContainerGap(78, Short.MAX_VALUE)));
        this.textPane = new JTextPane();
        this.textPane.setEditable(false);
        this.textPane.setText(updateNotes);
        scrollPane.setViewportView(this.textPane);
        this.getContentPane().setLayout(groupLayout);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}

