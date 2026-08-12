/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jnativehook.keyboard.NativeKeyEvent
 *  org.jnativehook.keyboard.NativeKeyListener
 */
package exp.soundboard;

import exp.gui.SoundboardFrame;
import exp.soundboard.SoundboardEntry;
import exp.soundboard.Utils;
import java.util.ArrayList;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyMacroListener
implements NativeKeyListener {
    SoundboardFrame soundboardFrame;
    ArrayList<Integer> pressedKeys;

    public GlobalKeyMacroListener(SoundboardFrame frame) {
        this.soundboardFrame = frame;
        this.pressedKeys = new ArrayList();
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        int pressed = e.getKeyCode();
        Utils.submitNativeKeyPressTime(NativeKeyEvent.getKeyText((int)pressed), e.getWhen());
        boolean alreadyPressed = false;
        for (int i : this.pressedKeys) {
            if (pressed != i) continue;
            alreadyPressed = true;
            break;
        }
        if (!alreadyPressed) {
            this.pressedKeys.add(pressed);
        }
        if (pressed == Utils.stopKey) {
            Utils.stopAllClips();
        } else if (pressed == Utils.modspeedupKey) {
            Utils.incrementModSpeedUp();
        } else if (pressed == Utils.modspeeddownKey) {
            Utils.decrementModSpeedDown();
        } else if (pressed == Utils.getOverlapSwitchKey()) {
            boolean overlap = Utils.isOverlapSameClipWhilePlaying();
            Utils.setOverlapSameClipWhilePlaying(!overlap);
        }
        this.checkMacros();
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        int released = e.getKeyCode();
        int i = 0;
        while (i < this.pressedKeys.size()) {
            if (released == this.pressedKeys.get(i)) {
                this.pressedKeys.remove(i);
            }
            ++i;
        }
    }

    public void nativeKeyTyped(NativeKeyEvent arg0) {
    }

    public boolean isSpeedModKeyHeld() {
        for (int key : this.pressedKeys) {
            if (key != Utils.slowKey) continue;
            return true;
        }
        return false;
    }

    public ArrayList<Integer> getPressedNativeKeys() {
        ArrayList<Integer> array = new ArrayList<Integer>();
        for (Integer i : this.pressedKeys) {
            array.add(new Integer(i));
        }
        return array;
    }

    private void checkMacros() {
        boolean modspeed = false;
        if (this.isSpeedModKeyHeld()) {
            modspeed = true;
        }
        ArrayList<SoundboardEntry> potential = new ArrayList<SoundboardEntry>();
        for (SoundboardEntry entry : SoundboardFrame.soundboard.getSoundboardEntries()) {
            int[] actKeys = entry.getActivationKeys();
            if (actKeys.length <= 0 || !entry.matchesPressed(this.pressedKeys)) continue;
            potential.add(entry);
        }
        if (potential.size() == 1) {
            ((SoundboardEntry)potential.get(0)).play(this.soundboardFrame.audioManager, modspeed);
        } else {
            int matches;
            int highest = 0;
            ArrayList<SoundboardEntry> potentialCopy = new ArrayList<SoundboardEntry>(potential);
            for (SoundboardEntry p : potentialCopy) {
                matches = p.matchesHowManyPressed(this.pressedKeys);
                if (matches > highest) {
                    highest = matches;
                    continue;
                }
                if (matches >= highest) continue;
                potential.remove(p);
            }
            potentialCopy = new ArrayList<SoundboardEntry>(potential);
            for (SoundboardEntry p : potentialCopy) {
                matches = p.matchesHowManyPressed(this.pressedKeys);
                if (matches >= highest) continue;
                potential.remove(p);
            }
            for (SoundboardEntry p : potential) {
                p.play(this.soundboardFrame.audioManager, modspeed);
            }
        }
    }
}

