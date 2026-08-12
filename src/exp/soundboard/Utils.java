/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jnativehook.GlobalScreen
 *  org.jnativehook.NativeHookException
 *  org.jnativehook.keyboard.NativeKeyEvent
 */
package exp.soundboard;

import exp.gui.SettingsFrame;
import exp.gui.SoundboardFrame;
import exp.soundboard.KeyEventIntConverter;
import exp.soundboard.MicInjector;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;

public class Utils {
    private static ThreadGroup clipPlayerThreadGroup = new ThreadGroup("Clip Player Group");
    private static final String prefsName = "KZ Soundboard";
    private static final String legacyPrefsName = "Expenosa's Soundboard";
    public static final Preferences prefs = openPreferences();

    /**
     * The settings node was renamed along with the application. Anything saved under the old
     * EXP Soundboard name is copied across the first time, so nobody loses their hotkeys,
     * devices or last soundboard on upgrading.
     */
    private static Preferences openPreferences() {
        Preferences node = Preferences.userRoot().node(prefsName);
        try {
            if (node.keys().length == 0 && Preferences.userRoot().nodeExists(legacyPrefsName)) {
                Preferences legacy = Preferences.userRoot().node(legacyPrefsName);
                for (String key : legacy.keys()) {
                    node.put(key, legacy.get(key, ""));
                }
                node.flush();
                System.out.println("Settings migrated from the previous EXP Soundboard install");
            }
        }
        catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return node;
    }
    /**
     * FIX: this used to be a non-volatile static boolean (PLAYALL). "Stop All" set it to
     * false, but any clip starting right afterwards set it back to true and resurrected the
     * clips that were being stopped. Now each clip captures the current generation and
     * "Stop All" just bumps the counter: clips already running die, new ones are unaffected.
     */
    private static final AtomicInteger playGeneration = new AtomicInteger();
    public static final int BUFFERSIZE = 2048;
    public static final float STANDARDSAMPLERATE = 44100.0f;
    private static float modifiedPlaybackSpeed;
    public static final float modifiedSpeedIncrements = 0.05f;
    public static final float modifiedSpeedMin = 0.1f;
    public static final float modifiedSpeedMax = 2.0f;
    public static final AudioFormat format;
    public static AudioFormat modifiedPlaybackFormat;
    public static int stopKey;
    public static int slowKey;
    public static int modspeedupKey;
    public static int modspeeddownKey;
    private static int overlapSwitchKey;
    public static MicInjector micInjector;
    private static Robot robot;
    public static boolean autoPTThold;
    private static ArrayList<Integer> pttkeys;
    private static int currentlyPlayingClipCount;
    private static ConcurrentHashMap<String, Long> lastNativeKeyPressMap;
    private static ConcurrentHashMap<String, Long> lastRobotKeyPressMap;
    public static String fileEncoding;
    public static boolean overlapSameClipWhilePlaying;

    static {
        format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 2, 4, 44100.0f, false);
        stopKey = 19;
        slowKey = 35;
        modspeedupKey = 39;
        modspeeddownKey = 37;
        overlapSwitchKey = 36;
        micInjector = new MicInjector();
        autoPTThold = true;
        pttkeys = new ArrayList();
        currentlyPlayingClipCount = 0;
        lastNativeKeyPressMap = new ConcurrentHashMap();
        lastRobotKeyPressMap = new ConcurrentHashMap();
        fileEncoding = System.getProperty("file.encoding");
        overlapSameClipWhilePlaying = true;
    }

    public static void playNewSoundClipThreaded(final File file, final SourceDataLine primarySpeaker, final SourceDataLine secondarySpeaker) {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                ClipPlayer clip = new ClipPlayer(file, primarySpeaker, secondarySpeaker);
                if (!overlapSameClipWhilePlaying) {
                    Utils.stopFilePlaying(file);
                }
                clip.start();
            }
        });
    }

    public static String[] getMixerNames(DataLine.Info lineInfo) {
        Mixer.Info[] info;
        ArrayList<String> mixerNames = new ArrayList<String>();
        Mixer.Info[] infoArray = info = AudioSystem.getMixerInfo();
        int n = info.length;
        int n2 = 0;
        while (n2 < n) {
            Mixer.Info elem = infoArray[n2];
            Mixer mixer = AudioSystem.getMixer(elem);
            try {
                if (mixer.isLineSupported(lineInfo)) {
                    mixerNames.add(elem.getName());
                }
            }
            catch (NullPointerException e) {
                System.err.println(e);
            }
            ++n2;
        }
        String[] returnarray = new String[mixerNames.size()];
        return mixerNames.toArray(returnarray);
    }

    public static void stopAllClips() {
        playGeneration.incrementAndGet();
        Utils.zeroCurrentClipCount();
    }

    public static int getStopKey() {
        return stopKey;
    }

    public static void setStopKey(int stopKey) {
        Utils.stopKey = stopKey;
    }

    public static int getModifiedSpeedKey() {
        return slowKey;
    }

    public static void setModifiedSpeedKey(int slowKey) {
        Utils.slowKey = slowKey;
    }

    public static void startMicInjector(String inputMixerName, String outputMixerName) {
        String mixer;
        boolean inputexists = false;
        boolean outputexists = false;
        if (Utils.isMicInjectorRunning()) {
            Utils.stopMicInjector();
        }
        String[] stringArray = MicInjector.getMixerNames(MicInjector.targetDataLineInfo);
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            mixer = stringArray[n2];
            if (mixer.equals(inputMixerName)) {
                inputexists = true;
            }
            ++n2;
        }
        stringArray = MicInjector.getMixerNames(MicInjector.sourceDataLineInfo);
        n = stringArray.length;
        n2 = 0;
        while (n2 < n) {
            mixer = stringArray[n2];
            if (mixer.equals(outputMixerName)) {
                outputexists = true;
            }
            ++n2;
        }
        if (inputexists && outputexists) {
            // FIX: a Thread cannot be started twice. Turning the Mic Injector off and back
            // on threw IllegalThreadStateException and left it dead until the application was
            // restarted. A fresh instance is now created on every start.
            MicInjector previous = micInjector;
            if (previous.isAlive()) {
                try {
                    previous.join(2000L);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            MicInjector fresh = new MicInjector();
            fresh.setGain(MicInjector.getGain());
            fresh.setInputMixer(inputMixerName);
            fresh.setOutputMixer(outputMixerName);
            micInjector = fresh;
            fresh.start();
        }
    }

    public static void stopMicInjector() {
        micInjector.stopRunning();
    }

    public static void setMicInjectorGain(float level) {
        micInjector.setGain(level);
    }

    public static float getMicInjectorGain() {
        return MicInjector.getGain();
    }

    public static boolean isMicInjectorRunning() {
        return micInjector.isRunning();
    }

    public static void startMp3Decoder() {
        // FIX: AudioSystem needs a stream supporting mark/reset. Reading the resource straight
        // out of the JAR yields an InflaterInputStream, which does not, so warming up the MP3
        // decoder always failed with "mark/reset not supported" and the first MP3 clip paid the
        // cost of loading the decoder.
        InputStream loaderfile = new BufferedInputStream(ClipPlayer.class.getResourceAsStream("loader.mp3"));
        try {
            AudioSystem.getAudioFileFormat(loaderfile);
            AudioInputStream stream = AudioSystem.getAudioInputStream(loaderfile);
            stream.close();
        }
        catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    public static boolean initGlobalKeyLibrary() {
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error occured whilst initiating global hotkeys", 0);
        }
        return true;
    }

    public static boolean deregisterGlobalKeyLibrary() {
        if (GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.unregisterNativeHook();
            return true;
        }
        return false;
    }

    public static boolean isFileSupported(File file) {
        try {
            AudioSystem.getAudioFileFormat(file);
            return true;
        }
        catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static synchronized void setModifiedPlaybackSpeed(float speed) {
        modifiedPlaybackSpeed = speed;
        float newSampleRate = 44100.0f * speed;
        modifiedPlaybackFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, newSampleRate, 16, 2, 4, newSampleRate, false);
    }

    public static synchronized float getModifiedPlaybackSpeed() {
        return modifiedPlaybackSpeed;
    }

    public static int getModspeedupKey() {
        return modspeedupKey;
    }

    public static void setModspeedupKey(int modspeedupKey) {
        Utils.modspeedupKey = modspeedupKey;
    }

    public static int getModspeeddownKey() {
        return modspeeddownKey;
    }

    public static void setModspeeddownKey(int modspeeddownKey) {
        Utils.modspeeddownKey = modspeeddownKey;
    }

    public static int getOverlapSwitchKey() {
        return overlapSwitchKey;
    }

    public static void setOverlapSwitchKey(int overlapSwitchKey) {
        Utils.overlapSwitchKey = overlapSwitchKey;
    }

    public static void incrementModSpeedUp() {
        float speed = modifiedPlaybackSpeed + 0.05f;
        if (speed > 2.0f) {
            speed = 2.0f;
        }
        Utils.setModifiedPlaybackSpeed(speed);
        if (SettingsFrame.instance != null) {
            SettingsFrame.instance.updateDisplayedModSpeed();
        }
    }

    public static void decrementModSpeedDown() {
        float speed = modifiedPlaybackSpeed - 0.05f;
        if (speed < 0.1f) {
            speed = 0.1f;
        }
        Utils.setModifiedPlaybackSpeed(speed);
        if (SettingsFrame.instance != null) {
            SettingsFrame.instance.updateDisplayedModSpeed();
        }
    }

    public static JFileChooser getFileChooser() {
        return SoundboardFrame.filechooser;
    }

    public MicInjector getMicInjector() {
        return micInjector;
    }

    public static Robot getRobotInstance() {
        if (robot != null) {
            return robot;
        }
        try {
            robot = new Robot();
        }
        catch (AWTException e) {
            e.printStackTrace();
        }
        if (robot != null) {
            return robot;
        }
        return null;
    }

    public static boolean checkAndUseAutoPPThold() {
        if (!autoPTThold || pttkeys.size() == 0) {
            return false;
        }
        if (SoundboardFrame.soundboard.entriesContainPTTKeys(pttkeys)) {
            return false;
        }
        ArrayList<Integer> pressed = SoundboardFrame.macroListener.getPressedNativeKeys();
        Robot robot = Utils.getRobotInstance();
        int noofkeys = pttkeys.size();
        int i = 0;
        while (i < noofkeys) {
            int key = pttkeys.get(i);
            boolean pressedAlready = false;
            for (Integer nativekey : pressed) {
                if (!KeyEventIntConverter.getKeyEventText(key).toLowerCase().equals(NativeKeyEvent.getKeyText((int)nativekey).toLowerCase())) continue;
                pressedAlready = true;
                break;
            }
            if (!pressedAlready) {
                robot.keyPress(key);
                Utils.submitRobotKeyPressTime(KeyEventIntConverter.getKeyEventText(key));
                System.out.println("Robot pressed: " + KeyEvent.getKeyText(key));
            }
            ++i;
        }
        return true;
    }

    public static boolean checkAndReleaseHeldPPTKeys() {
        if (!autoPTThold) {
            return false;
        }
        if (SoundboardFrame.soundboard.entriesContainPTTKeys(pttkeys)) {
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "A soundboard entry is using a key that conflicts with a 'Push to Talk' key. \n Disable 'Auto-hold PTT keys', or edit the entry or PTT keys.", "Alert!", 0);
                }
            });
            return false;
        }
        if (currentlyPlayingClipCount == 0) {
            Robot robot = Utils.getRobotInstance();
            for (Integer i : pttkeys) {
                if (!Utils.wasKeyLastPressedByRobot(KeyEventIntConverter.getKeyEventText(i))) continue;
                robot.keyRelease(i);
                System.out.println("Robot released: " + KeyEvent.getKeyText(i));
            }
        }
        return true;
    }

    public static ArrayList<Integer> getPTTkeys() {
        return pttkeys;
    }

    public static void setPTTkeys(Collection<Integer> pTTkeys) {
        pttkeys = new ArrayList<Integer>(pTTkeys);
    }

    public static boolean isAutoPTThold() {
        return autoPTThold;
    }

    public static void setAutoPTThold(boolean autoPTThold) {
        Utils.autoPTThold = autoPTThold;
    }

    public static synchronized void incrementCurrentClipCount() {
        ++currentlyPlayingClipCount;
    }

    public static synchronized void decrementCurrentClipCount() {
        if (currentlyPlayingClipCount >= 1) {
            --currentlyPlayingClipCount;
        }
    }

    public static synchronized void zeroCurrentClipCount() {
        currentlyPlayingClipCount = 0;
    }

    public static ArrayList<Integer> stringToIntArrayList(String string) {
        String[] numberstring;
        String arrayString = string.replace('[', ' ').replace(']', ' ').trim();
        ArrayList<Integer> array = new ArrayList<Integer>();
        String[] stringArray = numberstring = arrayString.split(",");
        int n = numberstring.length;
        int n2 = 0;
        while (n2 < n) {
            String s = stringArray[n2];
            if (!s.equals("")) {
                int i = Integer.parseInt(s.trim());
                array.add(i);
            }
            ++n2;
        }
        return array;
    }

    public static void submitNativeKeyPressTime(String key, long time) {
        lastNativeKeyPressMap.put(key.toLowerCase(), time);
    }

    public static void submitRobotKeyPressTime(String key) {
        long time = System.currentTimeMillis();
        lastNativeKeyPressMap.put(key.toLowerCase(), time);
        lastRobotKeyPressMap.put(key.toLowerCase(), time);
    }

    public static long getLastNativeKeyPressTimeForKey(String keyname) {
        Long time = lastNativeKeyPressMap.get(keyname.toLowerCase());
        if (time == null) {
            return 0L;
        }
        return time;
    }

    public static long getLastRobotKeyPressTimeForKey(String keyname) {
        Long time = lastRobotKeyPressMap.get(keyname.toLowerCase());
        if (time == null) {
            return 0L;
        }
        return time;
    }

    public static boolean wasKeyLastPressedByRobot(String keyname) {
        long human = Utils.getLastNativeKeyPressTimeForKey(keyname);
        long robot = Utils.getLastRobotKeyPressTimeForKey(keyname);
        return robot == human;
    }

    public static boolean isOverlapSameClipWhilePlaying() {
        return overlapSameClipWhilePlaying;
    }

    public static void setOverlapSameClipWhilePlaying(boolean overlap) {
        overlapSameClipWhilePlaying = overlap;
        if (SettingsFrame.instance != null) {
            SettingsFrame.instance.updateOverlapSwitchCheckBox();
        }
    }

    public static boolean stopFilePlaying(File file) {
        boolean stopped = false;
        String filepath = file.toString();
        Thread[] threads = new Thread[clipPlayerThreadGroup.activeCount()];
        clipPlayerThreadGroup.enumerate(threads);
        System.out.println("Thread count: " + threads.length);
        System.out.println("Thread groups: " + clipPlayerThreadGroup.activeGroupCount());
        System.out.println("Requesting: " + filepath + " to stop");
        Thread[] threadArray = threads;
        int n = threads.length;
        int n2 = 0;
        while (n2 < n) {
            Thread thread = threadArray[n2];
            // FIX: enumerate() can leave null holes (NPE) and the unchecked cast could throw
            // ClassCastException if the group ever held a thread of another type.
            if (thread instanceof ClipPlayer && filepath.equals(thread.getName())) {
                ((ClipPlayer)thread).stopPlaying();
                stopped = true;
            }
            ++n2;
        }
        return stopped;
    }

    private static class ClipPlayer
    extends Thread {
        File file;
        SourceDataLine primarySpeaker = null;
        SourceDataLine secondarySpeaker = null;
        volatile boolean playing = true; // FIX: written by another thread (stopFilePlaying)

        public ClipPlayer(File file, SourceDataLine primarySpeaker, SourceDataLine secondarySpeaker) {
            super(clipPlayerThreadGroup, file.toString());
            this.file = file;
            this.primarySpeaker = primarySpeaker;
            this.secondarySpeaker = secondarySpeaker;
        }

        @Override
        public void run() {
            this.playSoundClip(this.file, this.primarySpeaker, this.secondarySpeaker);
        }

        public void stopPlaying() {
            System.out.println("Stopping clip: " + this.file.getName());
            this.playing = false;
        }

        private void playSoundClip(File file, SourceDataLine primarySpeaker, SourceDataLine secondarySpeaker) {
            final int myGeneration = playGeneration.get();
            AudioInputStream clip = null;
            AudioFormat clipformat = null;
            try {
                clip = AudioSystem.getAudioInputStream(file);
                clipformat = clip.getFormat();
                if (!clipformat.equals(format)) {
                    clip = AudioSystem.getAudioInputStream(format, clip);
                }
            }
            catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, String.valueOf(file.getName()) + " uses an unsupported format.", "Unsupported Format", 0);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            if (clip != null) {
                Utils.incrementCurrentClipCount();
                byte[] buffer = new byte[2048];
                int bytesRead = 0;
                while (this.playing && myGeneration == playGeneration.get()) {
                    try {
                        bytesRead = clip.read(buffer, 0, 2048);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    Utils.checkAndUseAutoPPThold();
                    if (bytesRead > 0) {
                        primarySpeaker.write(buffer, 0, bytesRead);
                        if (secondarySpeaker != null) {
                            secondarySpeaker.write(buffer, 0, bytesRead);
                        }
                    }
                    if (bytesRead >= 2048) continue;
                    this.playing = false;
                }
                Utils.decrementCurrentClipCount();
                Utils.checkAndReleaseHeldPPTKeys();
            }
            if (clip != null) {
                try {
                    clip.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            primarySpeaker.close();
            if (secondarySpeaker != null) {
                secondarySpeaker.close();
            }
        }
    }
}

