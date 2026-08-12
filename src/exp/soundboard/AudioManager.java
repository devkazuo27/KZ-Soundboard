/*
 * Decompiled with CFR 0.152.
 */
package exp.soundboard;

import exp.soundboard.Utils;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class AudioManager {
    private static final int INTERNAL_BUFFER_SIZE = 8192;
    public final DataLine.Info standardDataLineInfo = new DataLine.Info(SourceDataLine.class, Utils.format, 2048);
    Mixer primaryOutput;
    Mixer secondaryOutput;
    private boolean useSecondary = false;
    private static float firstOutputGain;
    private static float secondOutputGain;
    private static volatile String lastWarning = null;

    void playSoundClip(File file, boolean halfspeed) {
        AudioFormat format = halfspeed ? Utils.modifiedPlaybackFormat : Utils.format;
        if (file.exists() && file.canRead()) {
            SourceDataLine primarySpeaker = null;
            SourceDataLine secondarySpeaker = null;
            if (this.primaryOutput == null) {
                // FIX: sin mixer primario seleccionado se producia un NullPointerException.
                warnOnce("No primary output device is selected. Choose one in the main window.", "No Output Device");
                return;
            }
            try {
                primarySpeaker = (SourceDataLine)this.primaryOutput.getLine(this.standardDataLineInfo);
                primarySpeaker.open(format, 8192);
                applyGain(primarySpeaker, firstOutputGain);
                primarySpeaker.start();
            }
            catch (LineUnavailableException | IllegalArgumentException ex) {
                closeQuietly(primarySpeaker);
                primarySpeaker = null;
                warnOnce("Selected Output Line: Primary Speaker is currently unavailable.", "Line Unavailable Exception");
            }
            // FIX: sin linea primaria abierta no se puede reproducir; antes se seguia adelante
            // y ClipPlayer petaba con NullPointerException al escribir y al cerrar.
            if (primarySpeaker == null) {
                return;
            }
            if (this.secondaryOutput != null && this.useSecondary) {
                try {
                    secondarySpeaker = (SourceDataLine)this.secondaryOutput.getLine(this.standardDataLineInfo);
                    secondarySpeaker.open(format, 8192);
                    applyGain(secondarySpeaker, secondOutputGain);
                    secondarySpeaker.start();
                }
                catch (LineUnavailableException | IllegalArgumentException ex) {
                    closeQuietly(secondarySpeaker);
                    secondarySpeaker = null;
                    warnOnce("Selected Output Line: Secondary Speaker is currently unavailable.", "Line Unavailable Exception");
                }
            }
            Utils.playNewSoundClipThreaded(file, primarySpeaker, secondarySpeaker);
        }
    }

    /** FIX: MASTER_GAIN no existe en todas las lineas; antes lanzaba IllegalArgumentException. */
    private static void applyGain(SourceDataLine line, float value) {
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
            float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), value));
            gain.setValue(clamped);
        }
    }

    private static void closeQuietly(SourceDataLine line) {
        if (line != null) {
            try {
                line.close();
            }
            catch (RuntimeException ignored) {
                // nada que hacer al cerrar
            }
        }
    }

    /** FIX: evita una cascada de dialogos modales al mantener pulsada una hotkey. */
    private static void warnOnce(String message, String title) {
        if (message.equals(lastWarning)) {
            return;
        }
        lastWarning = message;
        final String msg = message;
        final String ttl = title;
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, msg, ttl, JOptionPane.ERROR_MESSAGE);
                lastWarning = null;
            }
        });
    }

    public synchronized void setPrimaryOutputMixer(String mixerName) {
        String[] mixers;
        String[] stringArray = mixers = Utils.getMixerNames(this.standardDataLineInfo);
        int n = mixers.length;
        int n2 = 0;
        while (n2 < n) {
            String x = stringArray[n2];
            Mixer.Info[] infoArray = AudioSystem.getMixerInfo();
            int n3 = infoArray.length;
            int n4 = 0;
            while (n4 < n3) {
                Mixer.Info mixerInfo = infoArray[n4];
                if (mixerName.equals(mixerInfo.getName())) {
                    this.primaryOutput = AudioSystem.getMixer(mixerInfo);
                    return;
                }
                ++n4;
            }
            ++n2;
        }
    }

    public void setUseSecondary(boolean use) {
        this.useSecondary = use;
    }

    public boolean useSecondary() {
        return this.useSecondary;
    }

    public synchronized void setSecondaryOutputMixer(String mixerName) {
        String[] mixers;
        String[] stringArray = mixers = Utils.getMixerNames(this.standardDataLineInfo);
        int n = mixers.length;
        int n2 = 0;
        while (n2 < n) {
            String x = stringArray[n2];
            Mixer.Info[] infoArray = AudioSystem.getMixerInfo();
            int n3 = infoArray.length;
            int n4 = 0;
            while (n4 < n3) {
                Mixer.Info mixerInfo = infoArray[n4];
                if (mixerName.equals(mixerInfo.getName())) {
                    this.secondaryOutput = AudioSystem.getMixer(mixerInfo);
                    return;
                }
                ++n4;
            }
            ++n2;
        }
    }

    public static float getFirstOutputGain() {
        return firstOutputGain;
    }

    public static void setFirstOutputGain(float firstOutputGain) {
        AudioManager.firstOutputGain = firstOutputGain;
    }

    public static float getSecondOutputGain() {
        return secondOutputGain;
    }

    public static void setSecondOutputGain(float secondOutputGain) {
        AudioManager.secondOutputGain = secondOutputGain;
    }
}

