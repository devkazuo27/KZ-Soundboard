package exp.soundboard;

import java.io.ByteArrayInputStream;
import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Smoke test for the critical fixes. Exercises the audio engine with no GUI.
 * Usage: java -Djava.awt.headless=true -cp ... exp.soundboard.SmokeTest
 */
public class SmokeTest {

    private static int failures = 0;

    public static void main(String[] args) throws Exception {
        Utils.setAutoPTThold(false);   // auto-PTT needs the GUI loaded; it only gets in the way here
        File clip = makeToneFile(5);

        testNoDeviceSelectedDoesNotCrash(clip);
        testPlayAndStopAll(clip);
        testStopAllIsNotRevivedByANewClip(clip);

        clip.delete();
        System.out.println(failures == 0 ? "\nALL TESTS PASSED" : "\n" + failures + " TEST(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }

    /** P1: this used to blow up with a NullPointerException when no device was selected. */
    private static void testNoDeviceSelectedDoesNotCrash(File clip) {
        AudioManager audio = new AudioManager();   // no setPrimaryOutputMixer() call
        try {
            audio.playSoundClip(clip, false);
            check("P1  playing with no device selected throws nothing", true);
        } catch (Throwable t) {
            check("P1  playing with no device selected throws nothing (" + t + ")", false);
        }
    }

    /** P2: "Stop All" must actually cut off a clip that is playing. */
    private static void testPlayAndStopAll(File clip) throws Exception {
        AudioManager audio = openDefaultOutput();
        if (audio == null) {
            System.out.println("SKIP P2 (no output device available)");
            return;
        }
        audio.playSoundClip(clip, false);
        check("P2  the clip starts", waitForClipThreads(1, 3000));
        Utils.stopAllClips();
        check("P2  Stop All stops the clip", waitForClipThreads(0, 3000));
    }

    /**
     * P2, the real race: with the old PLAYALL flag, a clip starting just after "Stop All" set
     * the flag back to true and resurrected the previous clip.
     */
    private static void testStopAllIsNotRevivedByANewClip(File clip) throws Exception {
        AudioManager audio = openDefaultOutput();
        if (audio == null) {
            System.out.println("SKIP P2b (no output device available)");
            return;
        }
        File second = makeToneFile(5);
        audio.playSoundClip(clip, false);
        waitForClipThreads(1, 3000);

        Utils.stopAllClips();
        audio.playSoundClip(second, false);          // the "reviver"
        Thread.sleep(700);

        boolean firstStillAlive = countClipThreads(clip) > 0;
        check("P2b a new clip does not revive the ones just stopped", !firstStillAlive);

        Utils.stopAllClips();
        waitForClipThreads(0, 3000);
        second.delete();
    }

    // ------------------------------------------------------------------ helpers

    private static AudioManager openDefaultOutput() {
        AudioManager audio = new AudioManager();
        String[] mixers = Utils.getMixerNames(audio.standardDataLineInfo);
        if (mixers.length == 0) {
            return null;
        }
        audio.setPrimaryOutputMixer(mixers[0]);
        AudioManager.setFirstOutputGain(-70.0f);     // near silence, this is a test
        return audio;
    }

    /** Playback threads are named after the file path. */
    private static int countClipThreads(File clip) {
        String name = clip.toString();
        int n = 0;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (name.equals(t.getName()) && t.isAlive()) {
                n++;
            }
        }
        return n;
    }

    private static boolean waitForClipThreads(int expected, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            int alive = 0;
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if (t.getName().contains("smoketone") && t.isAlive()) {
                    alive++;
                }
            }
            if (alive == expected) {
                return true;
            }
            Thread.sleep(50);
        }
        return false;
    }

    /** An N second WAV holding a quiet tone, in the application's native format. */
    private static File makeToneFile(int seconds) throws Exception {
        int frames = (int)(Utils.format.getSampleRate() * seconds);
        byte[] pcm = new byte[frames * 4];
        for (int i = 0; i < frames; i++) {
            short s = (short)(Math.sin(i * 2 * Math.PI * 440 / Utils.format.getSampleRate()) * 6000);
            pcm[i * 4]     = (byte)(s & 0xFF);
            pcm[i * 4 + 1] = (byte)(s >> 8);
            pcm[i * 4 + 2] = (byte)(s & 0xFF);
            pcm[i * 4 + 3] = (byte)(s >> 8);
        }
        File f = File.createTempFile("smoketone", ".wav");
        try (AudioInputStream in = new AudioInputStream(new ByteArrayInputStream(pcm), Utils.format, frames)) {
            AudioSystem.write(in, AudioFileFormat.Type.WAVE, f);
        }
        return f;
    }

    private static void check(String label, boolean ok) {
        System.out.println((ok ? "  PASS  " : "  FAIL  ") + label);
        if (!ok) {
            failures++;
        }
    }
}
