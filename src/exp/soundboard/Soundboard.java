/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  org.jnativehook.keyboard.NativeKeyEvent
 */
package exp.soundboard;

import com.google.gson.Gson;
import exp.soundboard.KeyEventIntConverter;
import exp.soundboard.SoundboardEntry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import org.jnativehook.keyboard.NativeKeyEvent;

public class Soundboard {
    private ArrayList<SoundboardEntry> soundboardEntries = new ArrayList();
    private static ArrayList<SoundboardEntry> soundboardEntriesClone = new ArrayList();
    private static boolean containsPPTKey = false;
    private static ArrayList<Integer> pttKeysClone = new ArrayList();

    public Object[][] getEntriesAsObjectArrayForTable() {
        Object[][] array = new Object[this.soundboardEntries.size()][4];
        int i = 0;
        while (i < array.length) {
            SoundboardEntry entry = this.soundboardEntries.get(i);
            array[i][0] = entry.getFileName();
            array[i][1] = entry.getActivationKeysAsReadableString();
            array[i][2] = entry.getFileString();
            array[i][3] = i;
            ++i;
        }
        return array;
    }

    public void addEntry(File file, int[] keyNumbers) {
        this.soundboardEntries.add(new SoundboardEntry(file, keyNumbers));
    }

    public SoundboardEntry getEntry(String filename) {
        for (SoundboardEntry entry : this.soundboardEntries) {
            if (!entry.getFileName().equals(filename)) continue;
            return entry;
        }
        return null;
    }

    public void removeEntry(int index) {
        this.soundboardEntries.remove(index);
    }

    public void removeEntry(String filename) {
        for (SoundboardEntry entry : this.soundboardEntries) {
            if (!entry.getFileName().equals(filename)) continue;
            this.soundboardEntries.remove(entry);
            break;
        }
    }

    public ArrayList<SoundboardEntry> getSoundboardEntries() {
        return this.soundboardEntries;
    }

    public File saveAsJsonFile(File file) {
        String filestring = file.getAbsolutePath();
        System.out.println(filestring);
        if (filestring.contains(".")) {
            filestring = filestring.substring(0, filestring.lastIndexOf(46));
        }
        filestring = String.valueOf(filestring) + ".json";
        System.out.println("amended: " + filestring);
        Gson gson = new Gson();
        String json = gson.toJson((Object)this);
        File realfile = new File(filestring);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(realfile));
            writer.write(json);
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return realfile;
    }

    public static Soundboard loadFromJsonFile(File file) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Gson json = new Gson();
        Soundboard sb = (Soundboard)json.fromJson((Reader)br, Soundboard.class);
        try {
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    public SoundboardEntry getEntry(int index) {
        try {
            return this.soundboardEntries.get(index);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean entriesContainPTTKeys(ArrayList<Integer> pttkeys) {
        if (!pttkeys.equals(pttKeysClone) || this.hasSoundboardChanged()) {
            soundboardEntriesClone = (ArrayList)this.soundboardEntries.clone();
            pttKeysClone = (ArrayList)pttkeys.clone();
            String key = null;
            for (SoundboardEntry entry : this.soundboardEntries) {
                int[] nArray = entry.getActivationKeys();
                int n = nArray.length;
                int n2 = 0;
                while (n2 < n) {
                    int actKey = nArray[n2];
                    key = NativeKeyEvent.getKeyText((int)actKey).toLowerCase();
                    for (int i : pttkeys) {
                        if (!key.equals(KeyEventIntConverter.getKeyEventText(i).toLowerCase())) continue;
                        containsPPTKey = true;
                        return true;
                    }
                    ++n2;
                }
            }
            containsPPTKey = false;
            return false;
        }
        return containsPPTKey;
    }

    public boolean hasSoundboardChanged() {
        if (!this.soundboardEntries.equals(soundboardEntriesClone)) {
            System.out.println("Soundboard changed");
            return true;
        }
        return false;
    }
}

