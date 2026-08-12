/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jnativehook.keyboard.NativeKeyEvent
 */
package exp.soundboard;

import exp.soundboard.AudioManager;
import exp.soundboard.Utils;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.jnativehook.keyboard.NativeKeyEvent;

public class SoundboardEntry {
    private String file;
    public int[] activationKeysNumbers;

    public SoundboardEntry(File file, int[] keys) {
        Path p = Paths.get(new String(file.getAbsolutePath()), new String[0]);
        this.file = p.toAbsolutePath().toString();
        this.activationKeysNumbers = keys;
        if (this.activationKeysNumbers == null) {
            this.activationKeysNumbers = new int[0];
        }
    }

    public boolean matchesPressed(ArrayList<Integer> pressedKeys) {
        int keysRemaining = this.activationKeysNumbers.length;
        if (keysRemaining == 0) {
            return false;
        }
        int[] nArray = this.activationKeysNumbers;
        int n = this.activationKeysNumbers.length;
        int n2 = 0;
        while (n2 < n) {
            int actkey = nArray[n2];
            for (int presskey : pressedKeys) {
                if (actkey != presskey) continue;
                --keysRemaining;
            }
            ++n2;
        }
        return keysRemaining <= 0;
    }

    public int matchesHowManyPressed(ArrayList<Integer> pressedKeys) {
        int matches = 0;
        for (int key : pressedKeys) {
            int[] nArray = this.activationKeysNumbers;
            int n = this.activationKeysNumbers.length;
            int n2 = 0;
            while (n2 < n) {
                int hotkey = nArray[n2];
                if (key == hotkey) {
                    ++matches;
                }
                ++n2;
            }
        }
        return matches;
    }

    public void play(AudioManager audio, boolean moddedspeed) {
        File file = this.toFile();
        audio.playSoundClip(file, moddedspeed);
    }

    public File toFile() {
        File f = new File(this.file);
        if (!f.exists()) {
            Path p = Paths.get(this.file, new String[0]);
            return p.toFile();
        }
        return f;
    }

    public void setFile(File file) {
        try {
            this.file = new String(file.getAbsolutePath().getBytes(Utils.fileEncoding));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getFileString() {
        return this.file;
    }

    public String getFileName() {
        char seperator = File.separatorChar;
        return this.file.substring(this.file.lastIndexOf(seperator) + 1);
    }

    public int[] getActivationKeys() {
        return this.activationKeysNumbers;
    }

    public String getActivationKeysAsReadableString() {
        String s = "";
        if (this.activationKeysNumbers.length == 0) {
            return s;
        }
        int[] nArray = this.getActivationKeys();
        int n = nArray.length;
        int n2 = 0;
        while (n2 < n) {
            int i = nArray[n2];
            s = s.concat(String.valueOf(NativeKeyEvent.getKeyText((int)i)) + "+");
            ++n2;
        }
        s = s.substring(0, s.length() - 1);
        return s;
    }

    public void setActivationKeys(int[] activationKeys) {
        this.activationKeysNumbers = activationKeys;
    }
}

