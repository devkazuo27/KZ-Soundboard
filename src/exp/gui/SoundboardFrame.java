/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.apple.eawt.Application
 *  com.google.gson.Gson
 *  net.miginfocom.swing.MigLayout
 *  org.jnativehook.GlobalScreen
 *  org.jnativehook.keyboard.NativeKeyListener
 */
package exp.gui;

import com.apple.eawt.Application;
import com.google.gson.Gson;
import exp.converter.ConverterFrame;
import exp.gui.AudioLevelsFrame;
import exp.gui.SettingsFrame;
import exp.gui.SoundboardEntryEditor;
import exp.soundboard.AudioManager;
import exp.soundboard.GlobalKeyMacroListener;
import exp.soundboard.Soundboard;
import exp.soundboard.SoundboardEntry;
import exp.soundboard.UpdateChecker;
import exp.soundboard.Utils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Taskbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.BorderFactory;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import net.miginfocom.swing.MigLayout;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyListener;

public class SoundboardFrame
extends JFrame {
    private static final long serialVersionUID = 8934802095461138592L;
    final SoundboardFrame thisFrameInstance;
    public static final float VERSION = 0.5f;
    private static final String TITLE = "KZ Soundboard 1.0";
    private JComboBox<String> secondarySpeakerComboBox;
    private JComboBox<String> primarySpeakerComboBox;
    public AudioManager audioManager;
    public static Soundboard soundboard;
    public File testFile;
    private JList<SoundboardEntry> padGrid;
    public static GlobalKeyMacroListener macroListener;
    static boolean updateCheck;
    public static String micInjectorInputMixerName;
    public static String micInjectorOutputMixerName;
    public static boolean useMicInjector;
    public static final Image icon;
    public static JFileChooser filechooser;
    private static final String PROJECT_URL = "https://github.com/devkazuo27/exp-soundboard-rework";
    private static final String ORIGINAL_URL = "https://sourceforge.net/projects/expsoundboard/";
    private final String useSecondaryKey = "useSecondSpeaker";
    private final String firstSpeakerKey = "firstSpeaker";
    private final String secondSpeakerKey = "secondSpeaker";
    private final String lastSoundboardFileKey = "lastSoundboardUsed";
    private final String stopallKeyKey = "stopAllKey";
    private final String modPlaybackSpeedKey = "modplaybackspeed";
    private final String modPlaybackSpeedKeyKey = "slowSoundKey";
    private final String modSpeedIncKeyKey = "modSpeedIncKey";
    private final String modSpeedDecKeyKey = "modSpeedDecKey";
    private final String updateCheckKey = "updateCheckOnLaunch";
    private final String micInjectorInputKey = "micInjectorInput";
    private final String micInjectorOutputKey = "micInjectorOutput";
    private final String micInjectorEnabledKey = "micInjectorEnabled";
    private final String primaryOutputGainKey = "primaryOutputGain";
    private final String secondaryOutputGainKey = "secondaryOutputGain";
    private final String micInjectorOutputGainKey = "micInjectorOutputGain";
    private final String autoPPTenabledKey = "autoPPTenabled";
    private final String autoPPTKeysKey = "autoPTTkeys";
    private final String overlapClipsKey = "OverlapClipsWhilePlaying";
    private final String OVERLAPSWITCHKEYKEY = "OverlapClipsKey";
    private JCheckBox useSecondaryCheckBox;
    private File currentSoundboardFile = null;
    private JCheckBox useMicInjectorCheckBox;
    private JMenuBar menuBar;
    private JCheckBox autoPptCheckBox;

    static {
        micInjectorInputMixerName = "";
        micInjectorOutputMixerName = "";
        useMicInjector = false;
        icon = Ui.logoImage();
    }

    public static void main(String[] args) {
        // DESIGN: the theme must be installed before any component is created.
        Ui.installTheme();
        Utils.initGlobalKeyLibrary();
        Utils.startMp3Decoder();
        // FIX: Swing components must be built on the event dispatch thread.
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                new SoundboardFrame().setVisible(true);
            }
        });
    }

    public SoundboardFrame() {
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                SoundboardFrame.this.exit();
            }
        });
        // DESIGN: the look and feel is installed by main() via Ui.installTheme(), before any
        // component exists. The original block looked for Nimbus but ended up applying the
        // system one, which is where the Windows XP look came from.
        filechooser = new JFileChooser();
        this.audioManager = new AudioManager();
        soundboard = new Soundboard();
        this.setDefaultCloseOperation(3);
        this.setTitle(TITLE);
        this.setIconImage(icon);
        this.macInit();
        this.secondarySpeakerComboBox = new JComboBox();
        this.secondarySpeakerComboBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    String name = (String)SoundboardFrame.this.secondarySpeakerComboBox.getSelectedItem();
                    SoundboardFrame.this.audioManager.setSecondaryOutputMixer(name);
                }
            }
        });
        this.primarySpeakerComboBox = new JComboBox();
        this.primarySpeakerComboBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    String name = (String)SoundboardFrame.this.primarySpeakerComboBox.getSelectedItem();
                    SoundboardFrame.this.audioManager.setPrimaryOutputMixer(name);
                }
            }
        });
        JButton btnStop = new JButton("Stop All");
        btnStop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.stopAllClips();
            }
        });
        this.useSecondaryCheckBox = new JCheckBox("Enable");
        this.useSecondaryCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.this.audioManager.setUseSecondary(SoundboardFrame.this.useSecondaryCheckBox.isSelected());
            }
        });
        JScrollPane scrollPane = new JScrollPane();
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                new SoundboardEntryEditor(SoundboardFrame.this.thisFrameInstance);
            }
        });
        this.useMicInjectorCheckBox = new JCheckBox("Use Mic Injector");
        this.useMicInjectorCheckBox.setToolTipText("Mixes your microphone into the secondary output. Configure it in Option \u2192 Settings");
        this.useMicInjectorCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                useMicInjector = SoundboardFrame.this.useMicInjectorCheckBox.isSelected();
                SoundboardFrame.this.updateMicInjector();
            }
        });
        JLabel lblstOutputeg = new JLabel("Primary output");
        lblstOutputeg.setToolTipText("Where you hear the clips \u2014 normally your speakers or headphones");
        JLabel lblndOutputeg = new JLabel("Secondary output");
        lblndOutputeg.setToolTipText("Optional \u2014 e.g. a virtual audio cable input, so other people hear the clips");
        this.autoPptCheckBox = new JCheckBox("Auto-hold PTT key(s)");
        this.autoPptCheckBox.setToolTipText("Holds your push-to-talk keys down while a clip plays");
        this.autoPptCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean selected = SoundboardFrame.this.autoPptCheckBox.isSelected();
                Utils.setAutoPTThold(selected);
            }
        });
        // DESIGN: clips are a grid of pads rather than a table row. A JList in horizontal
        // wrap mode gives the reflowing grid, the selection and the keyboard navigation for
        // free; SoundPadRenderer paints each pad.
        this.padGrid = new JList<SoundboardEntry>(new DefaultListModel<SoundboardEntry>());
        this.padGrid.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        this.padGrid.setVisibleRowCount(-1);
        this.padGrid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.padGrid.setFixedCellWidth(SoundPadRenderer.PAD_WIDTH);
        this.padGrid.setFixedCellHeight(SoundPadRenderer.PAD_HEIGHT);
        this.padGrid.setCellRenderer(new SoundPadRenderer());
        this.padGrid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        this.padGrid.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                int index = SoundboardFrame.this.padGrid.locationToIndex(e.getPoint());
                if (index < 0 || !SoundboardFrame.this.padGrid.getCellBounds(index, index).contains(e.getPoint())) {
                    SoundboardFrame.this.padGrid.clearSelection();
                    return;
                }
                SoundboardFrame.this.padGrid.setSelectedIndex(index);
                if (SwingUtilities.isRightMouseButton(e)) {
                    SoundboardFrame.this.padMenu().show(SoundboardFrame.this.padGrid, e.getX(), e.getY());
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    SoundboardFrame.this.playSelected();
                }
            }
        });
        scrollPane.setViewportView(this.padGrid);
        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SoundboardFrame.this.removeSelected();
            }
        });
        JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SoundboardFrame.this.editSelected();
            }
        });
        JButton btnPlay = new JButton("Play");
        btnPlay.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.this.playSelected();
            }
        });
        // ------------------------------------------------------------ DESIGN
        // A single column with separate sections, real margins, and sizes derived from the
        // font instead of fixed pixel columns (which do not scale with the display DPI).
        this.padGrid.setToolTipText("Click a pad to play it, right-click for more");
        Ui.styleTableContainer(scrollPane);

        btnAdd.setToolTipText("Add a sound clip and assign hotkeys to it");
        // DESIGN: Add is the primary action, so it carries the KZ accent.
        btnAdd.putClientProperty("JButton.buttonType", "default");
        btnAdd.setBackground(Ui.ACCENT);
        btnAdd.setForeground(java.awt.Color.WHITE);
        btnRemove.setToolTipText("Remove the selected clip");
        btnEdit.setToolTipText("Change the file or hotkeys of the selected clip");
        btnPlay.setToolTipText("Play the selected clip");
        btnStop.setToolTipText("Stop everything that is playing");

        JPanel toolbar = new JPanel(new MigLayout("insets 0, gap 6", "[][][]push[][]"));
        toolbar.setOpaque(false);
        toolbar.add(btnAdd);
        toolbar.add(btnRemove);
        toolbar.add(btnEdit);
        toolbar.add(btnPlay);
        toolbar.add(btnStop);

        JPanel outputs = new JPanel(new MigLayout("insets 0, gap 6 4, fillx", "[grow,fill][]"));
        outputs.setOpaque(false);
        outputs.add(lblstOutputeg, "span 2, wrap");
        outputs.add(this.primarySpeakerComboBox, "span 2, growx, gapbottom 10, wrap");
        outputs.add(lblndOutputeg, "span 2, wrap");
        outputs.add(this.secondarySpeakerComboBox, "growx");
        outputs.add(this.useSecondaryCheckBox, "gapleft 6");

        JPanel options = new JPanel(new MigLayout("insets 0, gap 6, fillx", "[grow,fill][]"));
        options.setOpaque(false);
        options.add(this.useMicInjectorCheckBox);
        options.add(this.autoPptCheckBox, "align right");

        Container content = this.getContentPane();
        content.setLayout((LayoutManager)new MigLayout("insets 14, gap 10, fillx, wrap 1", "[grow,fill]", "[][grow,fill][][][][][]"));
        content.add(Ui.brandHeader(), "growx");
        content.add(scrollPane, "grow");
        content.add(toolbar, "growx");
        content.add(Ui.section("Output devices"), "growx, gaptop 6");
        content.add(outputs, "growx");
        content.add(Ui.section("Options"), "growx, gaptop 6");
        content.add(options, "growx");
        // Without this the table starts with a phantom empty row.
        this.updateSoundboardTable();

        this.menuBar = new JMenuBar();
        this.setJMenuBar(this.menuBar);
        JMenu mnFile = new JMenu("File");
        this.menuBar.add(mnFile);
        JMenuItem mntmNew = new JMenuItem("New");
        mntmNew.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.this.fileNew();
            }
        });
        mnFile.add(mntmNew);
        JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.this.fileOpen();
            }
        });
        mnFile.add(mntmOpen);
        JSeparator separator = new JSeparator();
        mnFile.add(separator);
        JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.setAccelerator(KeyStroke.getKeyStroke(83, 2));
        mntmSave.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.this.fileSave();
            }
        });
        mnFile.add(mntmSave);
        JMenuItem mntmSaveAs = new JMenuItem("Save As...");
        mntmSaveAs.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SoundboardFrame.this.fileSaveAs();
            }
        });
        mnFile.add(mntmSaveAs);
        JSeparator separator_3 = new JSeparator();
        mnFile.add(separator_3);
        JMenuItem mntmProjectPage = new JMenuItem("GitHub Page");
        mntmProjectPage.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SoundboardFrame.this.browse(PROJECT_URL);
            }
        });
        mnFile.add(mntmProjectPage);
        // The licence of the original work requires keeping the credit reachable.
        JMenuItem mntmOriginal = new JMenuItem("Original project by Expenosa");
        mntmOriginal.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SoundboardFrame.this.browse(ORIGINAL_URL);
            }
        });
        mnFile.add(mntmOriginal);
        JSeparator separator_1 = new JSeparator();
        mnFile.add(separator_1);
        JMenuItem mntmQuit = new JMenuItem("Quit");
        mntmQuit.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SoundboardFrame.this.exit();
            }
        });
        mnFile.add(mntmQuit);
        JMenu mnEdit = new JMenu("Option");
        this.menuBar.add(mnEdit);
        JMenuItem mntmSettings = new JMenuItem("Settings");
        mntmSettings.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SoundboardFrame.this.getSettingsMenu();
            }
        });
        mnEdit.add(mntmSettings);
        JMenu mnAppearance = new JMenu("Appearance");
        ButtonGroup themeGroup = new ButtonGroup();
        String[][] themes = new String[][]{{Ui.SYSTEM, "Match system"}, {Ui.LIGHT, "Light"}, {Ui.DARK, "Dark"}};
        String[][] stringArray = themes;
        int themeCount = themes.length;
        int themeIndex = 0;
        while (themeIndex < themeCount) {
            final String key = stringArray[themeIndex][0];
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(stringArray[themeIndex][1]);
            item.setSelected(key.equals(Ui.currentTheme()));
            item.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    Ui.setTheme(key);
                }
            });
            themeGroup.add(item);
            mnAppearance.add(item);
            ++themeIndex;
        }
        mnEdit.add(mnAppearance);

        JMenuItem mntmAudioLevels = new JMenuItem("Audio Levels");
        mntmAudioLevels.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AudioLevelsFrame.getInstance().setLocationRelativeTo(SoundboardFrame.this.thisFrameInstance);
            }
        });
        mnEdit.add(mntmAudioLevels);
        JSeparator separator_2 = new JSeparator();
        mnEdit.add(separator_2);
        JMenuItem mntmVirtualCable = new JMenuItem("Virtual Audio Cable\u2026");
        mntmVirtualCable.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (VbCable.promptIfMissing(SoundboardFrame.this.thisFrameInstance, true)) {
                    JOptionPane.showMessageDialog(SoundboardFrame.this.thisFrameInstance,
                            "VB-Audio Virtual Cable is installed.\n\n"
                            + "Point the second output at \"CABLE Input\" and have your voice "
                            + "software listen on \"CABLE Output\".",
                            "Virtual audio cable", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        mnEdit.add(mntmVirtualCable);

        JMenuItem mntmAudioConverter = new JMenuItem("Audio Converter");
        mntmAudioConverter.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
                    new ConverterFrame();
                } else {
                    JOptionPane.showMessageDialog(null, "Audio Converter currently not supported on Mac OS X", "Feature not supported", 1);
                }
            }
        });
        mnEdit.add(mntmAudioConverter);
        this.setMinimumSize(new Dimension(460, 520));
        this.updateSpeakerComboBoxes();
        this.pack();
        this.thisFrameInstance = this;
        macroListener = new GlobalKeyMacroListener(this);
        GlobalScreen.getInstance().addNativeKeyListener((NativeKeyListener)macroListener);
        this.setLocationRelativeTo(null);
        this.loadPrefs();
        // Offer the virtual cable once, after the window is up: without it the second output
        // and the Mic Injector have nowhere useful to point.
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                VbCable.promptIfMissing(SoundboardFrame.this.thisFrameInstance, false);
            }
        });
    }

    /** Rebuilds the pad grid from the soundboard, keeping the selection where possible. */
    public void updateSoundboardTable() {
        int previous = this.padGrid.getSelectedIndex();
        DefaultListModel<SoundboardEntry> model = new DefaultListModel<SoundboardEntry>();
        for (SoundboardEntry entry : soundboard.getSoundboardEntries()) {
            model.addElement(entry);
        }
        this.padGrid.setModel(model);
        if (previous >= 0 && previous < model.size()) {
            this.padGrid.setSelectedIndex(previous);
        }
    }

    /** Context menu of a pad. The toolbar buttons do exactly the same on the selection. */
    private JPopupMenu padMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem play = new JMenuItem("Play");
        play.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.this.playSelected();
            }
        });
        JMenuItem edit = new JMenuItem("Edit\u2026");
        edit.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.this.editSelected();
            }
        });
        JMenuItem remove = new JMenuItem("Remove");
        remove.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SoundboardFrame.this.removeSelected();
            }
        });
        menu.add(play);
        menu.add(edit);
        menu.addSeparator();
        menu.add(remove);
        return menu;
    }

    private void playSelected() {
        SoundboardEntry entry = this.selectedEntry();
        if (entry != null) {
            entry.play(this.audioManager, macroListener.isSpeedModKeyHeld());
        }
    }

    private void editSelected() {
        SoundboardEntry entry = this.selectedEntry();
        if (entry != null) {
            new SoundboardEntryEditor(this.thisFrameInstance, entry);
        }
    }

    private void removeSelected() {
        int index = this.padGrid.getSelectedIndex();
        if (index < 0) {
            return;
        }
        soundboard.removeEntry(index);
        this.updateSoundboardTable();
        int count = this.padGrid.getModel().getSize();
        if (count > 0) {
            this.padGrid.setSelectedIndex(Math.min(index, count - 1));
        }
    }

    private SoundboardEntry selectedEntry() {
        int index = this.padGrid.getSelectedIndex();
        return index < 0 ? null : soundboard.getEntry(index);
    }

    private void fileNew() {
        Utils.stopAllClips();
        this.saveReminder();
        this.currentSoundboardFile = null;
        soundboard = new Soundboard();
        this.updateSoundboardTable();
        this.setTitle(TITLE);
    }

    private void fileOpen() {
        Utils.stopAllClips();
        this.saveReminder();
        filechooser.setFileFilter(new JsonFileFilter());
        int session = filechooser.showOpenDialog(null);
        if (session == 0) {
            File jsonfile = filechooser.getSelectedFile();
            this.open(jsonfile);
        }
    }

    private void fileSave() {
        if (this.currentSoundboardFile != null) {
            soundboard.saveAsJsonFile(this.currentSoundboardFile);
        } else {
            this.fileSaveAs();
        }
    }

    private void fileSaveAs() {
        int session;
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new JsonFileFilter());
        if (this.currentSoundboardFile != null) {
            fc.setSelectedFile(this.currentSoundboardFile);
        }
        if ((session = fc.showSaveDialog(null)) == 0) {
            File file = fc.getSelectedFile();
            this.currentSoundboardFile = soundboard.saveAsJsonFile(file);
            this.setTitle(TITLE + "  \u2014  " + this.currentSoundboardFile.getName());
        }
    }

    private void getSettingsMenu() {
        SettingsFrame.getInstanceOf().setLocationRelativeTo(this);
    }

    private void open(File jsonfile) {
        Soundboard sb;
        if (jsonfile.exists() && (sb = Soundboard.loadFromJsonFile(jsonfile)) != null) {
            soundboard = sb;
            this.updateSoundboardTable();
            this.currentSoundboardFile = jsonfile;
            this.setTitle(TITLE + "  \u2014  " + this.currentSoundboardFile.getName());
        }
    }

    public void updateSpeakerComboBoxes() {
        String[] outputmixerStringArray;
        String[] stringArray = outputmixerStringArray = Utils.getMixerNames(this.audioManager.standardDataLineInfo);
        int n = outputmixerStringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String speaker = stringArray[n2];
            this.primarySpeakerComboBox.addItem(speaker);
            this.secondarySpeakerComboBox.addItem(speaker);
            ++n2;
        }
    }

    private int getSelectedEntryIndex() {
        return this.padGrid.getSelectedIndex();
    }

    public void updateMicInjector() {
        this.useMicInjectorCheckBox.setSelected(useMicInjector);
        if (useMicInjector) {
            Utils.startMicInjector(micInjectorInputMixerName, micInjectorOutputMixerName);
        } else {
            Utils.stopMicInjector();
        }
    }

    private void browse(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        }
        catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void savePrefs() {
        Preferences prefs = Utils.prefs;
        prefs.putBoolean("useSecondSpeaker", this.useSecondaryCheckBox.isSelected());
        prefs.put("firstSpeaker", (String)this.primarySpeakerComboBox.getSelectedItem());
        prefs.put("secondSpeaker", (String)this.secondarySpeakerComboBox.getSelectedItem());
        if (this.currentSoundboardFile != null) {
            prefs.put("lastSoundboardUsed", this.currentSoundboardFile.getAbsolutePath());
        }
        prefs.putBoolean("OverlapClipsWhilePlaying", Utils.isOverlapSameClipWhilePlaying());
        prefs.putInt("OverlapClipsKey", Utils.getOverlapSwitchKey());
        prefs.putInt("stopAllKey", Utils.getStopKey());
        prefs.putFloat("modplaybackspeed", Utils.getModifiedPlaybackSpeed());
        prefs.putInt("slowSoundKey", Utils.getModifiedSpeedKey());
        prefs.putInt("modSpeedIncKey", Utils.getModspeedupKey());
        prefs.putInt("modSpeedDecKey", Utils.getModspeeddownKey());
        prefs.putBoolean("updateCheckOnLaunch", updateCheck);
        prefs.put("micInjectorInput", micInjectorInputMixerName);
        prefs.put("micInjectorOutput", micInjectorOutputMixerName);
        prefs.putBoolean("micInjectorEnabled", useMicInjector);
        prefs.putBoolean("autoPPTenabled", Utils.autoPTThold);
        prefs.put("autoPTTkeys", Utils.getPTTkeys().toString());
        prefs.putFloat("primaryOutputGain", AudioManager.getFirstOutputGain());
        prefs.putFloat("secondaryOutputGain", AudioManager.getSecondOutputGain());
        prefs.putFloat("micInjectorOutputGain", Utils.getMicInjectorGain());
        // DESIGN: remember the window size across sessions.
        if ((this.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
            prefs.putInt("windowWidth", this.getWidth());
            prefs.putInt("windowHeight", this.getHeight());
        }
    }

    private void loadPrefs() {
        String lastfile;
        Preferences prefs = Utils.prefs;
        boolean useSecond = prefs.getBoolean("useSecondSpeaker", false);
        this.useSecondaryCheckBox.setSelected(useSecond);
        this.audioManager.setUseSecondary(useSecond);
        String firstspeaker = prefs.get("firstSpeaker", null);
        String secondspeaker = prefs.get("secondSpeaker", null);
        if (secondspeaker == null) {
            // First run with the cable already installed: point the second output at it.
            secondspeaker = VbCable.findPlaybackDevice(Utils.getMixerNames(this.audioManager.standardDataLineInfo));
        }
        if (firstspeaker != null) {
            this.primarySpeakerComboBox.setSelectedItem(firstspeaker);
            this.audioManager.setPrimaryOutputMixer(firstspeaker);
        }
        if (secondspeaker != null) {
            this.secondarySpeakerComboBox.setSelectedItem(secondspeaker);
            this.audioManager.setSecondaryOutputMixer(secondspeaker);
        }
        if ((lastfile = prefs.get("lastSoundboardUsed", null)) != null) {
            this.open(new File(lastfile));
        }
        float modSpeed = prefs.getFloat("modplaybackspeed", 0.5f);
        Utils.setModifiedPlaybackSpeed(modSpeed);
        int slowkey = prefs.getInt("slowSoundKey", 35);
        Utils.setModifiedSpeedKey(slowkey);
        int stopkey = prefs.getInt("stopAllKey", 19);
        Utils.setStopKey(stopkey);
        int incKey = prefs.getInt("modSpeedIncKey", 39);
        Utils.setModspeedupKey(incKey);
        int decKey = prefs.getInt("modSpeedDecKey", 37);
        Utils.setModspeeddownKey(decKey);
        updateCheck = prefs.getBoolean("updateCheckOnLaunch", true);
        if (updateCheck) {
            new Thread(new UpdateChecker()).start();
        }
        float firstOutputGain = prefs.getFloat("primaryOutputGain", 0.0f);
        float secondOutputGain = prefs.getFloat("secondaryOutputGain", 0.0f);
        float micinjectorOutputGain = prefs.getFloat("micInjectorOutputGain", 0.0f);
        AudioManager.setFirstOutputGain(firstOutputGain);
        AudioManager.setSecondOutputGain(secondOutputGain);
        Utils.setMicInjectorGain(micinjectorOutputGain);
        micInjectorInputMixerName = prefs.get("micInjectorInput", "");
        micInjectorOutputMixerName = prefs.get("micInjectorOutput", "");
        useMicInjector = prefs.getBoolean("micInjectorEnabled", false);
        this.updateMicInjector();
        boolean useautoptt = prefs.getBoolean("autoPPTenabled", false);
        this.autoPptCheckBox.setSelected(useautoptt);
        Utils.setAutoPTThold(useautoptt);
        String autopttkeys = prefs.get("autoPTTkeys", null);
        if (autopttkeys != null) {
            ArrayList<Integer> keys = Utils.stringToIntArrayList(autopttkeys);
            Utils.setPTTkeys(keys);
        }
        Utils.setOverlapSameClipWhilePlaying(prefs.getBoolean("OverlapClipsWhilePlaying", true));
        int overlapKey = prefs.getInt("OverlapClipsKey", 36);
        Utils.setOverlapSwitchKey(overlapKey);
        int savedWidth = prefs.getInt("windowWidth", 0);
        int savedHeight = prefs.getInt("windowHeight", 0);
        if (savedWidth >= 460 && savedHeight >= 520) {
            this.setSize(savedWidth, savedHeight);
            this.setLocationRelativeTo(null);
        }
    }

    private void exit() {
        Utils.stopAllClips();
        this.saveReminder();
        this.savePrefs();
        this.dispose();
        Utils.deregisterGlobalKeyLibrary();
        System.exit(0);
    }

    private void saveReminder() {
        int option;
        if (this.currentSoundboardFile != null) {
            int option2;
            String currentjson;
            Soundboard savedFile;
            Gson gson;
            String savedjson;
            if (this.currentSoundboardFile.exists() && !(savedjson = (gson = new Gson()).toJson((Object)(savedFile = Soundboard.loadFromJsonFile(this.currentSoundboardFile)))).equals(currentjson = gson.toJson((Object)soundboard)) && (option2 = JOptionPane.showConfirmDialog(null, "Soundboard has changed. Do you want to save?", "Save Reminder", 0)) == 0) {
                soundboard.saveAsJsonFile(this.currentSoundboardFile);
            }
        } else if (soundboard.getSoundboardEntries().size() > 0 && (option = JOptionPane.showConfirmDialog(null, "Soundboard has not been saved. Do you want to save?", "Save Reminder", 0)) == 0) {
            this.fileSave();
        }
    }

    private void macInit() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "KZ Soundboard");
            // FIX: com.apple.eawt was removed from the JDK in Java 9, so on a macOS running a
            // modern Java the application would not even open. The dock icon is optional.
            try {
                Application application = Application.getApplication();
                application.setDockIconImage(icon);
            }
            catch (Throwable t) {
                try {
                    Taskbar.getTaskbar().setIconImage(icon);
                }
                catch (Throwable ignored) {
                    // no dock icon, never mind
                }
            }
        }
    }

    private class JsonFileFilter
    extends FileFilter {
        private JsonFileFilter() {
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return f.getName().toLowerCase().endsWith(".json");
        }

        @Override
        public String getDescription() {
            return ".json Soundboard save file";
        }
    }
}

