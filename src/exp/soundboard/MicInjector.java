/*
 * Decompiled with CFR 0.152.
 */
package exp.soundboard;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

public class MicInjector
extends Thread {
    private static final int INTERNAL_BUFFER_SIZE = 8192;
    private static final int bufferSize = 512;
    private static float fFrameRate = 44100.0f;
    Mixer inputMixer;
    Mixer outputMixer;
    private String inputLineName = "none selected";
    private String outputLineName = "none selected";
    private SourceDataLine sourceDataLine;
    private TargetDataLine targetDataLine;
    private final byte[] inputBuffer = new byte[512];
    private int bytesRead;
    private static final AudioFormat signedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fFrameRate, 16, 2, 4, fFrameRate, false);
    public static final DataLine.Info targetDataLineInfo = new DataLine.Info(TargetDataLine.class, signedFormat, 8192);
    public static final DataLine.Info sourceDataLineInfo = new DataLine.Info(SourceDataLine.class, signedFormat, 8192);
    private static float gainLevel;
    private boolean bypass;
    FloatControl gainControl;
    private boolean fadeOut;
    int userVolume;
    private boolean muted = false;
    private volatile boolean run = false; // FIX: lo escribe stopRunning() desde el EDT
    private long nextDrift;
    private final long driftinterval = 1800000L;

    MicInjector() {
    }

    public synchronized void setInputMixer(String mixerName) {
        String[] mixers;
        String[] stringArray = mixers = MicInjector.getMixerNames(targetDataLineInfo);
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
                    this.inputMixer = AudioSystem.getMixer(mixerInfo);
                    return;
                }
                ++n4;
            }
            ++n2;
        }
    }

    public synchronized void setOutputMixer(String mixerName) {
        String[] mixers;
        String[] stringArray = mixers = MicInjector.getMixerNames(sourceDataLineInfo);
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
                    this.outputMixer = AudioSystem.getMixer(mixerInfo);
                    return;
                }
                ++n4;
            }
            ++n2;
        }
    }

    public synchronized void setupGate() {
        if (this.targetDataLine != null) {
            this.clearLines();
        }
        // FIX: sin mixer seleccionado se producia un NullPointerException.
        if (this.inputMixer == null || this.outputMixer == null) {
            JOptionPane.showMessageDialog(null, "Mic Injector: input or output device not selected.", "Mic Injector", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            this.targetDataLine = (TargetDataLine)this.inputMixer.getLine(targetDataLineInfo);
            this.inputLineName = this.inputMixer.getMixerInfo().getName();
            this.targetDataLine.open(signedFormat, 8192);
            this.targetDataLine.start();
        }
        catch (LineUnavailableException ex) {
            JOptionPane.showMessageDialog(null, "Selected Input Line " + this.inputLineName + " is currently unavailable.", "Line Unavailable Exception", 0);
        }
        try {
            this.sourceDataLine = (SourceDataLine)this.outputMixer.getLine(sourceDataLineInfo);
            this.outputLineName = this.outputMixer.getMixerInfo().getName();
            this.sourceDataLine.open(signedFormat, 8192);
            this.sourceDataLine.start();
        }
        catch (LineUnavailableException ex) {
            JOptionPane.showMessageDialog(null, "Selected Output Line " + this.outputLineName + " is currently unavailable.", "Line Unavailable Exception", 0);
        }
        // FIX: si alguna de las dos lineas no llego a abrirse, aqui saltaba un NullPointerException.
        if (this.sourceDataLine == null || this.targetDataLine == null) {
            return;
        }
        if (this.sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            this.gainControl = (FloatControl)this.sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            this.gainControl.setValue(gainLevel);
        }
        System.out.println(this.targetDataLine.getLineInfo().toString());
        System.out.println("Buffer size is " + this.targetDataLine.getBufferSize());
        this.fadeOut = true;
    }

    public synchronized void setGain(float level) {
        gainLevel = level;
        if (this.gainControl != null) {
            this.gainControl.setValue(level);
        }
    }

    public static synchronized float getGain() {
        return gainLevel;
    }

    private synchronized void clearLines() {
        // FIX: null-safe; ademas ahora se llama tambien al parar el hilo, para no dejar el
        // microfono y el cable virtual ocupados indefinidamente.
        if (this.targetDataLine != null) {
            this.targetDataLine.close();
            this.targetDataLine = null;
        }
        if (this.sourceDataLine != null) {
            this.sourceDataLine.close();
            this.sourceDataLine = null;
        }
        this.gainControl = null;
    }

    protected void read() {
        TargetDataLine line = this.targetDataLine;
        this.bytesRead = line == null ? 0 : line.read(this.inputBuffer, 0, 512);
    }

    protected void write() {
        SourceDataLine line = this.sourceDataLine;
        if (line != null && this.bytesRead > 0) {
            line.write(this.inputBuffer, 0, this.bytesRead);
        }
    }

    private void writeFadeIn() {
        this.sourceDataLine.write(this.inputBuffer, 0, this.bytesRead);
        if (this.gainControl.getValue() < (float)this.userVolume) {
            if (this.gainControl.getValue() < -20.0f) {
                this.gainControl.setValue(-20.0f);
            }
            this.gainControl.shift(this.gainControl.getValue(), this.gainControl.getValue() + 1.0f, 10000000);
        }
        if (this.gainControl.getValue() >= (float)this.userVolume) {
            this.resetGain();
        }
    }

    private void writeFadeOut() {
        this.sourceDataLine.write(this.inputBuffer, 0, this.bytesRead);
        if (this.fadeOut) {
            if (this.gainControl.getValue() > -70.0f) {
                this.gainControl.setValue(this.gainControl.getValue() - 0.1f);
            }
            if (this.gainControl.getValue() <= -69.9f) {
                this.gainControl.setValue(-80.0f);
                this.fadeOut = false;
                System.out.println("Fade OUT off!");
            }
        }
    }

    @Override
    public void run() {
        this.setupGate();
        // FIX: si las lineas no abrieron, el bucle giraba lanzando NullPointerException sin fin.
        if (this.targetDataLine == null || this.sourceDataLine == null) {
            this.run = false;
            return;
        }
        this.run = true;
        this.nextDrift = System.currentTimeMillis() + 1800000L;
        try {
            while (this.run) {
                this.read();
                if (this.bytesRead > 0) {
                    this.write();
                } else {
                    // FIX: cuando la linea se para o se desconecta el dispositivo, read()
                    // devuelve 0 sin bloquear y el bucle consumia un nucleo entero al 100%.
                    try {
                        Thread.sleep(5L);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                if (System.currentTimeMillis() <= this.nextDrift) continue;
                this.driftReset();
            }
        }
        finally {
            this.run = false;
            this.clearLines();
        }
    }

    public boolean isRunning() {
        return this.run;
    }

    synchronized void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    synchronized void setFadeOut(boolean fadeOut) {
        this.fadeOut = fadeOut;
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

    private static float findLevel(byte[] buffer) {
        double dB = 0.0;
        int i = 0;
        while (i < buffer.length) {
            dB = 20.0 * Math.log10(Math.abs((double)buffer[i] / 32767.0));
            if (dB == Double.NEGATIVE_INFINITY || dB == Double.NaN) {
                dB = -90.0;
            }
            ++i;
        }
        float level = (float)dB + 91.0f;
        return level;
    }

    public static float getdB(byte[] buffer) {
        double dB = 0.0;
        short[] shortArray = new short[buffer.length / 2];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray);
        int i = 0;
        while (i < shortArray.length) {
            dB = 20.0 * Math.log10(Math.abs((double)shortArray[i] / 32767.0));
            if (dB == Double.NEGATIVE_INFINITY || dB == Double.NaN) {
                dB = -90.0;
            }
            ++i;
        }
        float level = (float)dB + 91.0f;
        return level;
    }

    @Deprecated
    public static short[] byteToShortArray(byte[] byteArray) {
        short[] shortArray = new short[byteArray.length / 2];
        int i = 0;
        while (i < shortArray.length) {
            int ub1 = byteArray[i * 2 + 0] & 0xFF;
            int ub2 = byteArray[i * 2 + 1] & 0xFF;
            shortArray[i] = (short)((ub2 << 8) + ub1);
            ++i;
        }
        return shortArray;
    }

    @Deprecated
    public static byte[] shortArrayToByteArray(short[] shortArray) {
        byte[] byteArray = new byte[shortArray.length * 2];
        ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArray);
        return byteArray;
    }

    synchronized void setMute(boolean mute) {
        this.muted = mute;
        if (this.muted) {
            this.bypass = false;
            this.fadeOut = true;
        }
    }

    boolean isMuted() {
        return this.muted;
    }

    public void resetGain() {
        this.gainControl.setValue(this.userVolume);
    }

    public boolean isBypassing() {
        return this.bypass;
    }

    public String getSelectedInputLineName() {
        return this.inputLineName;
    }

    public String getSelectedOutputLineName() {
        return this.outputLineName;
    }

    public void stopRunning() {
        this.run = false;
    }

    private synchronized void driftReset() {
        if (System.currentTimeMillis() > this.nextDrift) {
            this.nextDrift = System.currentTimeMillis() + 1800000L;
            if (this.targetDataLine == null || this.sourceDataLine == null) {
                return;
            }
            try {
                this.targetDataLine.open(signedFormat, 8192);
                this.targetDataLine.start();
            }
            catch (LineUnavailableException ex) {
                JOptionPane.showMessageDialog(null, "Selected Input Line is currently unavailable", "Line Unavailable Exception", 0);
            }
            try {
                this.sourceDataLine.open(signedFormat, 8192);
                this.sourceDataLine.start();
            }
            catch (LineUnavailableException ex) {
                JOptionPane.showMessageDialog(null, "Selected Output Line is currently unavailable.", "Line Unavailable Exception", 0);
            }
            System.out.println("DriftReset");
        }
    }
}

