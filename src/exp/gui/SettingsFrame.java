/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.jnativehook.GlobalScreen
 *  org.jnativehook.keyboard.NativeKeyEvent
 *  org.jnativehook.keyboard.NativeKeyListener
 */
package exp.gui;

import exp.gui.SoundboardFrame;
import exp.soundboard.MicInjector;
import exp.soundboard.UpdateChecker;
import exp.soundboard.Utils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import net.miginfocom.swing.MigLayout;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class SettingsFrame
extends JFrame {
    public static SettingsFrame instance = null;
    private static final long serialVersionUID = -4758092886690912749L;
    private JTextField stopAllTextField;
    private StopKeyNativeKeyInputGetter stopKeyInputGetter;
    private ModSpeedKeyNativeKeyInputGetter slowKeyInputGetter;
    private IncKeyNativeKeyInputGetter incKeyInputGetter;
    private DecKeyNativeKeyInputGetter decKeyInputGetter;
    private PttKeysNativeKeyInputGetter pttKeysInputGetter;
    private OverlapSwitchNativeKeyInputGetter fOverlapKeyInputGetter;
    private JComboBox<String> micComboBox;
    private JComboBox<String> vacComboBox;
    private JTextField slowKeyTextField;
    private JSpinner modSpeedSpinner;
    private JTextField incModSpeedHotKeyTextField;
    private JTextField decModSpeedHotKeyTextField;
    private JTextField pttKeysTextField;
    private JCheckBox fOverlapClipsCheckbox;
    private JTextField fOverlapHotkeyTextField;

    private SettingsFrame() {
        this.getContentPane().addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent arg0) {
                SettingsFrame.this.focusLostOnItems();
            }
        });
        this.addWindowFocusListener(new WindowFocusListener(){

            @Override
            public void windowGainedFocus(WindowEvent arg0) {
            }

            @Override
            public void windowLostFocus(WindowEvent arg0) {
                SettingsFrame.this.focusLostOnItems();
            }
        });
        this.setResizable(false);
        this.stopKeyInputGetter = new StopKeyNativeKeyInputGetter();
        this.slowKeyInputGetter = new ModSpeedKeyNativeKeyInputGetter();
        this.incKeyInputGetter = new IncKeyNativeKeyInputGetter();
        this.decKeyInputGetter = new DecKeyNativeKeyInputGetter();
        this.pttKeysInputGetter = new PttKeysNativeKeyInputGetter();
        this.fOverlapKeyInputGetter = new OverlapSwitchNativeKeyInputGetter();
        this.setDefaultCloseOperation(2);
        this.setTitle("Settings");
        JLabel lblstopAllSounds = new JLabel("'Stop All Sounds' hotkey:");
        lblstopAllSounds.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
        this.stopAllTextField = new JTextField();
        this.stopAllTextField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent arg0) {
                GlobalScreen.getInstance().removeNativeKeyListener((NativeKeyListener)SettingsFrame.this.stopKeyInputGetter);
                Ui.markIdle(SettingsFrame.this.stopAllTextField);
            }
        });
        this.stopAllTextField.setEditable(false);
        this.stopAllTextField.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent arg0) {
                Ui.markCapturing(SettingsFrame.this.stopAllTextField);
                GlobalScreen.getInstance().addNativeKeyListener((NativeKeyListener)SettingsFrame.this.stopKeyInputGetter);
            }
        });
        this.stopAllTextField.setColumns(10);
        final JCheckBox chckbxCheckForUpdate = new JCheckBox("Check for update on launch.");
        chckbxCheckForUpdate.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.updateCheck = !SoundboardFrame.updateCheck;
                chckbxCheckForUpdate.setSelected(SoundboardFrame.updateCheck);
            }
        });
        chckbxCheckForUpdate.setSelected(SoundboardFrame.updateCheck);
        final JButton btnCheckForUpdate = new JButton("Check for Update");
        btnCheckForUpdate.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (UpdateChecker.isUpdateAvailable()) {
                    SwingUtilities.invokeLater(new UpdateChecker());
                } else {
                    btnCheckForUpdate.setText("No Updates");
                }
            }
        });
        JLabel lblExpenosa = new JLabel("Original \u00a9 Expenosa, 2014 \u00b7 CC BY-SA 3.0");
        lblExpenosa.setToolTipText("EXP Soundboard was created by Expenosa and released under Creative Commons Attribution-ShareAlike 3.0");
        JButton btnProjectWebsite = new JButton("Project Website");
        btnProjectWebsite.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://sourceforge.net/projects/expsoundboard/"));
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
                catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        JSeparator separator = new JSeparator();
        JSeparator separator_1 = new JSeparator();
        JLabel lblMicInjectorSettings = new JLabel("Mic Injector settings:");
        
        JLabel lblMicrophone = new JLabel("Microphone:");
        this.micComboBox = new JComboBox();
        JLabel lblVirtualAudioCable = new JLabel("Virtual Audio Cable:");
        this.vacComboBox = new JComboBox();
        JLabel lblUseMicInjector = new JLabel("*Use Mic Injector when your using a virtual audio cable as your input on other software.");
        lblUseMicInjector.setFont(UIManager.getFont("Label.font").deriveFont(Font.ITALIC));
        JLabel lblVersion = new JLabel("Version: 0.5.1 \u2014 rework 2026");
        JLabel lblhalfSpeedPlayback = new JLabel("'Modified playback speed' combo key:");
        this.slowKeyTextField = new JTextField();
        this.slowKeyTextField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent e) {
                GlobalScreen.getInstance().removeNativeKeyListener((NativeKeyListener)SettingsFrame.this.slowKeyInputGetter);
                Ui.markIdle(SettingsFrame.this.slowKeyTextField);
            }
        });
        this.slowKeyTextField.setEditable(false);
        this.slowKeyTextField.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent arg0) {
                Ui.markCapturing(SettingsFrame.this.slowKeyTextField);
                GlobalScreen.getInstance().addNativeKeyListener((NativeKeyListener)SettingsFrame.this.slowKeyInputGetter);
            }
        });
        this.slowKeyTextField.setColumns(10);
        JLabel lblModifiedPlaybackSpeed = new JLabel("Modified playback speed multiplier:");
        this.modSpeedSpinner = new JSpinner();
        this.modSpeedSpinner.setModel(new SpinnerNumberModel(new Float(Utils.getModifiedPlaybackSpeed()), new Float(0.1f), new Float(2.0f), new Float(0.05f)));
        JComponent comp = this.modSpeedSpinner.getEditor();
        JFormattedTextField field = (JFormattedTextField)comp.getComponent(0);
        field.setEditable(false);
        DefaultFormatter formatter = (DefaultFormatter)field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        this.modSpeedSpinner.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent arg0) {
                float speed = ((Float)SettingsFrame.this.modSpeedSpinner.getValue()).floatValue();
                if (speed >= 0.1f && speed <= 2.0f) {
                    Utils.setModifiedPlaybackSpeed(speed);
                }
            }
        });
        this.incModSpeedHotKeyTextField = new JTextField();
        this.incModSpeedHotKeyTextField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent arg0) {
                GlobalScreen.getInstance().removeNativeKeyListener((NativeKeyListener)SettingsFrame.this.incKeyInputGetter);
                Ui.markIdle(SettingsFrame.this.incModSpeedHotKeyTextField);
            }
        });
        this.incModSpeedHotKeyTextField.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent arg0) {
                GlobalScreen.getInstance().addNativeKeyListener((NativeKeyListener)SettingsFrame.this.incKeyInputGetter);
                Ui.markCapturing(SettingsFrame.this.incModSpeedHotKeyTextField);
            }
        });
        this.incModSpeedHotKeyTextField.setEditable(false);
        this.incModSpeedHotKeyTextField.setColumns(10);
        this.decModSpeedHotKeyTextField = new JTextField();
        this.decModSpeedHotKeyTextField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent e) {
                Ui.markIdle(SettingsFrame.this.decModSpeedHotKeyTextField);
                GlobalScreen.getInstance().removeNativeKeyListener((NativeKeyListener)SettingsFrame.this.decKeyInputGetter);
            }
        });
        this.decModSpeedHotKeyTextField.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                GlobalScreen.getInstance().addNativeKeyListener((NativeKeyListener)SettingsFrame.this.decKeyInputGetter);
                Ui.markCapturing(SettingsFrame.this.decModSpeedHotKeyTextField);
            }
        });
        this.decModSpeedHotKeyTextField.setEditable(false);
        this.decModSpeedHotKeyTextField.setColumns(10);
        JLabel lblModifierSpeedIncrement = new JLabel("Modifier speed Increment hotkey:");
        JLabel lblNewLabel = new JLabel("Modifier speed Decrement hotkey:");
        JLabel lblpushToTalk = new JLabel("VoIP 'Push To Talk' Key(s): ");
        lblpushToTalk.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
        this.pttKeysTextField = new JTextField();
        this.pttKeysTextField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent arg0) {
                Ui.markIdle(SettingsFrame.this.pttKeysTextField);
                SettingsFrame.this.pttKeysInputGetter.clearPressedKeys();
                SettingsFrame.this.pttKeysTextField.removeKeyListener(SettingsFrame.this.pttKeysInputGetter);
            }
        });
        this.pttKeysTextField.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent arg0) {
                SettingsFrame.this.pttKeysTextField.removeKeyListener(SettingsFrame.this.pttKeysInputGetter);
                SettingsFrame.this.pttKeysTextField.addKeyListener(SettingsFrame.this.pttKeysInputGetter);
                Ui.markCapturing(SettingsFrame.this.pttKeysTextField);
            }
        });
        this.pttKeysTextField.setEditable(false);
        this.pttKeysTextField.setColumns(10);
        this.pttKeysTextField.setFocusTraversalKeysEnabled(false);
        JLabel lblOverlapSameSound = new JLabel("Overlap same sound file:");
        lblOverlapSameSound.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
        this.fOverlapClipsCheckbox = new JCheckBox("");
        this.fOverlapClipsCheckbox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean selected = SettingsFrame.this.fOverlapClipsCheckbox.isSelected();
                Utils.setOverlapSameClipWhilePlaying(selected);
            }
        });
        this.setIconImage(SoundboardFrame.icon);
        String[] inputMixers = MicInjector.getMixerNames(MicInjector.targetDataLineInfo);
        String[] outputMixers = MicInjector.getMixerNames(MicInjector.sourceDataLineInfo);
        String[] stringArray = inputMixers;
        int n = inputMixers.length;
        int n2 = 0;
        while (n2 < n) {
            String input = stringArray[n2];
            this.micComboBox.addItem(input);
            ++n2;
        }
        stringArray = outputMixers;
        n = outputMixers.length;
        n2 = 0;
        while (n2 < n) {
            String output = stringArray[n2];
            this.vacComboBox.addItem(output);
            ++n2;
        }
        this.micComboBox.setSelectedItem(SoundboardFrame.micInjectorInputMixerName);
        this.vacComboBox.setSelectedItem(SoundboardFrame.micInjectorOutputMixerName);
        this.micComboBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    SettingsFrame.this.updateMicInjectorSettings();
                }
            }
        });
        this.vacComboBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    SettingsFrame.this.updateMicInjectorSettings();
                }
            }
        });
        this.stopAllTextField.setText(NativeKeyEvent.getKeyText((int)Utils.getStopKey()));
        this.slowKeyTextField.setText(NativeKeyEvent.getKeyText((int)Utils.getModifiedSpeedKey()));
        this.incModSpeedHotKeyTextField.setText(NativeKeyEvent.getKeyText((int)Utils.getModspeedupKey()));
        this.decModSpeedHotKeyTextField.setText(NativeKeyEvent.getKeyText((int)Utils.getModspeeddownKey()));
        this.pttKeysInputGetter.updateTextField();
        this.fOverlapClipsCheckbox.setSelected(Utils.isOverlapSameClipWhilePlaying());
        this.getContentPane().setLayout((LayoutManager)new MigLayout("insets 14, gapx 8, gapy 6", "[101px][20px][45px][13px][71px][4px][34px,grow][10px][135px]", "[20px][20px][20px][20px][20px][20px][21px][][2px][14px][20px][20px][13px][2px][14px][23px]"));
        this.getContentPane().add((Component)lblstopAllSounds, "cell 0 0 3 1,alignx left,aligny center");
        this.getContentPane().add((Component)lblhalfSpeedPlayback, "cell 0 1 5 1,growx,aligny center");
        this.getContentPane().add((Component)lblModifiedPlaybackSpeed, "cell 0 2 3 1,alignx left,aligny center");
        this.getContentPane().add((Component)this.stopAllTextField, "cell 6 0 3 1,growx,aligny top");
        this.getContentPane().add((Component)this.slowKeyTextField, "cell 6 1 3 1,growx,aligny top");
        this.getContentPane().add((Component)this.modSpeedSpinner, "cell 6 2 3 1,growx,aligny top");
        JLabel lblOverlapSwitchHotkey = new JLabel("Overlap switch hotkey:");
        this.getContentPane().add((Component)lblOverlapSwitchHotkey, "cell 0 7 3 1");
        this.fOverlapHotkeyTextField = new JTextField();
        this.fOverlapHotkeyTextField.setEditable(false);
        this.getContentPane().add((Component)this.fOverlapHotkeyTextField, "cell 6 7 3 1,growx");
        this.fOverlapHotkeyTextField.setColumns(10);
        this.fOverlapHotkeyTextField.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                GlobalScreen.getInstance().addNativeKeyListener((NativeKeyListener)SettingsFrame.this.fOverlapKeyInputGetter);
                Ui.markCapturing(SettingsFrame.this.fOverlapHotkeyTextField);
            }
        });
        this.fOverlapHotkeyTextField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent e) {
                Ui.markIdle(SettingsFrame.this.fOverlapHotkeyTextField);
                GlobalScreen.getInstance().removeNativeKeyListener((NativeKeyListener)SettingsFrame.this.fOverlapKeyInputGetter);
            }
        });
        this.fOverlapHotkeyTextField.setText(NativeKeyEvent.getKeyText((int)Utils.getOverlapSwitchKey()));
        this.getContentPane().add((Component)separator, "cell 0 13 9 1,growx,aligny top");
        this.getContentPane().add((Component)chckbxCheckForUpdate, "cell 0 15 3 1,alignx left,aligny top");
        this.getContentPane().add((Component)btnProjectWebsite, "cell 4 15 3 1,alignx right,aligny top");
        this.getContentPane().add((Component)btnCheckForUpdate, "cell 8 15,alignx right,aligny top");
        this.getContentPane().add((Component)lblVersion, "cell 0 14,alignx left,aligny top");
        this.getContentPane().add((Component)lblExpenosa, "cell 8 14,alignx right,aligny top");
        this.getContentPane().add((Component)lblMicInjectorSettings, "cell 0 9,alignx left,aligny top");
        this.getContentPane().add((Component)lblMicrophone, "cell 0 10,alignx left,aligny center");
        this.getContentPane().add((Component)lblVirtualAudioCable, "cell 0 11,alignx left,aligny center");
        this.getContentPane().add(this.vacComboBox, "cell 2 11 7 1,growx,aligny top");
        this.getContentPane().add(this.micComboBox, "cell 2 10 7 1,growx,aligny top");
        this.getContentPane().add((Component)lblUseMicInjector, "cell 0 12 9 1,alignx left,aligny top");
        this.getContentPane().add((Component)separator_1, "cell 0 8 9 1,growx,aligny top");
        this.getContentPane().add((Component)lblNewLabel, "cell 0 4 5 1,growx,aligny center");
        this.getContentPane().add((Component)lblModifierSpeedIncrement, "cell 0 3 5 1,growx,aligny center");
        this.getContentPane().add((Component)lblpushToTalk, "cell 0 5 3 1,alignx left,aligny center");
        this.getContentPane().add((Component)lblOverlapSameSound, "cell 0 6 3 1,alignx left,growy");
        this.getContentPane().add((Component)this.fOverlapClipsCheckbox, "cell 6 6,alignx left,aligny top");
        this.getContentPane().add((Component)this.pttKeysTextField, "cell 6 5 3 1,growx,aligny top");
        this.getContentPane().add((Component)this.decModSpeedHotKeyTextField, "cell 6 4 3 1,growx,aligny top");
        this.getContentPane().add((Component)this.incModSpeedHotKeyTextField, "cell 6 3 3 1,growx,aligny top");
        this.pack();
        this.setVisible(true);
    }

    private void updateMicInjectorSettings() {
        SoundboardFrame.micInjectorInputMixerName = (String)this.micComboBox.getSelectedItem();
        SoundboardFrame.micInjectorOutputMixerName = (String)this.vacComboBox.getSelectedItem();
        if (SoundboardFrame.useMicInjector) {
            Utils.startMicInjector(SoundboardFrame.micInjectorInputMixerName, SoundboardFrame.micInjectorOutputMixerName);
        }
    }

    public void updateDisplayedModSpeed() {
        this.modSpeedSpinner.setValue(Float.valueOf(Utils.getModifiedPlaybackSpeed()));
    }

    public void updateOverlapSwitchCheckBox() {
        this.fOverlapClipsCheckbox.setSelected(Utils.isOverlapSameClipWhilePlaying());
    }

    @Override
    public void dispose() {
        super.dispose();
        GlobalScreen gs = GlobalScreen.getInstance();
        gs.removeNativeKeyListener((NativeKeyListener)this.slowKeyInputGetter);
        gs.removeNativeKeyListener((NativeKeyListener)this.stopKeyInputGetter);
        gs.removeNativeKeyListener((NativeKeyListener)this.incKeyInputGetter);
        gs.removeNativeKeyListener((NativeKeyListener)this.decKeyInputGetter);
        this.pttKeysTextField.removeKeyListener(this.pttKeysInputGetter);
        instance = null;
    }

    public static SettingsFrame getInstanceOf() {
        if (instance == null) {
            instance = new SettingsFrame();
        } else {
            instance.setVisible(true);
            instance.requestFocus();
        }
        return instance;
    }

    private void focusLostOnItems() {
        Ui.markIdle(this.stopAllTextField);
        Ui.markIdle(this.slowKeyTextField);
        Ui.markIdle(this.decModSpeedHotKeyTextField);
        Ui.markIdle(this.incModSpeedHotKeyTextField);
        Ui.markIdle(this.fOverlapHotkeyTextField);
        Ui.markIdle(this.pttKeysTextField);
        this.pttKeysInputGetter.clearPressedKeys();
        GlobalScreen gs = GlobalScreen.getInstance();
        gs.removeNativeKeyListener((NativeKeyListener)this.stopKeyInputGetter);
        gs.removeNativeKeyListener((NativeKeyListener)this.slowKeyInputGetter);
        gs.removeNativeKeyListener((NativeKeyListener)this.incKeyInputGetter);
        gs.removeNativeKeyListener((NativeKeyListener)this.decKeyInputGetter);
        gs.removeNativeKeyListener((NativeKeyListener)this.fOverlapKeyInputGetter);
        this.pttKeysTextField.removeKeyListener(this.pttKeysInputGetter);
    }

    private class DecKeyNativeKeyInputGetter
    implements NativeKeyListener {
        int key = Utils.getModspeeddownKey();

        private DecKeyNativeKeyInputGetter() {
        }

        public void nativeKeyPressed(NativeKeyEvent e) {
            this.key = e.getKeyCode();
            Utils.setModspeeddownKey(this.key);
            this.updateTextField();
        }

        public void nativeKeyReleased(NativeKeyEvent e) {
        }

        public void nativeKeyTyped(NativeKeyEvent arg0) {
        }

        private synchronized void updateTextField() {
            String keyname = NativeKeyEvent.getKeyText((int)this.key);
            SettingsFrame.this.decModSpeedHotKeyTextField.setText(keyname);
        }
    }

    private class IncKeyNativeKeyInputGetter
    implements NativeKeyListener {
        int key = Utils.getModspeedupKey();

        private IncKeyNativeKeyInputGetter() {
        }

        public void nativeKeyPressed(NativeKeyEvent e) {
            this.key = e.getKeyCode();
            Utils.setModspeedupKey(this.key);
            this.updateTextField();
        }

        public void nativeKeyReleased(NativeKeyEvent e) {
        }

        public void nativeKeyTyped(NativeKeyEvent arg0) {
        }

        private synchronized void updateTextField() {
            String keyname = NativeKeyEvent.getKeyText((int)this.key);
            SettingsFrame.this.incModSpeedHotKeyTextField.setText(keyname);
        }
    }

    private class ModSpeedKeyNativeKeyInputGetter
    implements NativeKeyListener {
        int key = Utils.slowKey;

        private ModSpeedKeyNativeKeyInputGetter() {
        }

        public void nativeKeyPressed(NativeKeyEvent e) {
            this.key = e.getKeyCode();
            Utils.setModifiedSpeedKey(this.key);
            this.updateTextField();
        }

        public void nativeKeyReleased(NativeKeyEvent e) {
        }

        public void nativeKeyTyped(NativeKeyEvent arg0) {
        }

        private synchronized void updateTextField() {
            String keyname = NativeKeyEvent.getKeyText((int)this.key);
            SettingsFrame.this.slowKeyTextField.setText(keyname);
        }
    }

    private class OverlapSwitchNativeKeyInputGetter
    implements NativeKeyListener {
        int key = Utils.stopKey;

        private OverlapSwitchNativeKeyInputGetter() {
        }

        private void updateTextField() {
            String keyname = NativeKeyEvent.getKeyText((int)this.key);
            SettingsFrame.this.fOverlapHotkeyTextField.setText(keyname);
        }

        public void nativeKeyPressed(NativeKeyEvent e) {
            this.key = e.getKeyCode();
            Utils.setOverlapSwitchKey(this.key);
            this.updateTextField();
        }

        public void nativeKeyReleased(NativeKeyEvent arg0) {
        }

        public void nativeKeyTyped(NativeKeyEvent arg0) {
        }
    }

    private class PttKeysNativeKeyInputGetter
    implements KeyListener {
        HashSet<Integer> pressedkeys = new HashSet();

        private PttKeysNativeKeyInputGetter() {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            this.pressedkeys.add(key);
            Utils.setPTTkeys(this.pressedkeys);
            this.updateTextField();
            System.out.println("PPT listener key pressed: " + KeyEvent.getKeyText(key));
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Integer key = e.getKeyCode();
            this.pressedkeys.remove(key);
            System.out.println("PPT listener key released: " + KeyEvent.getKeyText(key));
        }

        @Override
        public void keyTyped(KeyEvent arg0) {
        }

        private synchronized void updateTextField() {
            StringBuilder keyString = new StringBuilder();
            ArrayList<Integer> keys = Utils.getPTTkeys();
            int i = 0;
            while (i < keys.size()) {
                if (i == 0) {
                    keyString.append(KeyEvent.getKeyText(keys.get(i)));
                } else {
                    keyString.append(" + " + KeyEvent.getKeyText(keys.get(i)));
                }
                ++i;
            }
            SettingsFrame.this.pttKeysTextField.setText(keyString.toString());
            System.out.println("PTT listener text field updated");
        }

        private synchronized void clearPressedKeys() {
            this.pressedkeys.clear();
            System.out.println("PTT listener keys cleared");
        }
    }

    private class StopKeyNativeKeyInputGetter
    implements NativeKeyListener {
        int key = Utils.stopKey;

        private StopKeyNativeKeyInputGetter() {
        }

        public void nativeKeyPressed(NativeKeyEvent e) {
            this.key = e.getKeyCode();
            Utils.setStopKey(this.key);
            this.updateTextField();
        }

        public void nativeKeyReleased(NativeKeyEvent e) {
        }

        public void nativeKeyTyped(NativeKeyEvent arg0) {
        }

        private synchronized void updateTextField() {
            String keyname = NativeKeyEvent.getKeyText((int)this.key);
            SettingsFrame.this.stopAllTextField.setText(keyname);
        }
    }
}

