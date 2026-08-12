package exp.soundboard;

import java.io.ByteArrayInputStream;
import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Test de humo para los arreglos criticos. Ejercita el motor de audio sin GUI.
 * Uso: java -Djava.awt.headless=true -cp ... exp.soundboard.SmokeTest
 */
public class SmokeTest {

    private static int failures = 0;

    public static void main(String[] args) throws Exception {
        Utils.setAutoPTThold(false);   // el auto-PTT necesita la GUI cargada; aqui estorba
        File clip = makeToneFile(5);

        testNoDeviceSelectedDoesNotCrash(clip);
        testPlayAndStopAll(clip);
        testStopAllIsNotRevivedByANewClip(clip);

        clip.delete();
        System.out.println(failures == 0 ? "\nTODOS LOS TESTS OK" : "\n" + failures + " TEST(S) FALLIDOS");
        System.exit(failures == 0 ? 0 : 1);
    }

    /** P1: antes petaba con NullPointerException al no haber mixer seleccionado. */
    private static void testNoDeviceSelectedDoesNotCrash(File clip) {
        AudioManager audio = new AudioManager();   // sin setPrimaryOutputMixer()
        try {
            audio.playSoundClip(clip, false);
            check("P1  reproducir sin dispositivo seleccionado no lanza excepcion", true);
        } catch (Throwable t) {
            check("P1  reproducir sin dispositivo seleccionado no lanza excepcion (" + t + ")", false);
        }
    }

    /** P2: "Stop All" debe cortar de verdad un clip en curso. */
    private static void testPlayAndStopAll(File clip) throws Exception {
        AudioManager audio = openDefaultOutput();
        if (audio == null) {
            System.out.println("SKIP P2 (no hay dispositivo de salida disponible)");
            return;
        }
        audio.playSoundClip(clip, false);
        check("P2  el clip arranca", waitForClipThreads(1, 3000));
        Utils.stopAllClips();
        check("P2  Stop All detiene el clip", waitForClipThreads(0, 3000));
    }

    /**
     * P2 (la race de verdad): con el flag PLAYALL antiguo, un clip que arrancaba justo
     * despues de "Stop All" ponia el flag a true otra vez y revivia al clip anterior.
     */
    private static void testStopAllIsNotRevivedByANewClip(File clip) throws Exception {
        AudioManager audio = openDefaultOutput();
        if (audio == null) {
            System.out.println("SKIP P2b (no hay dispositivo de salida disponible)");
            return;
        }
        File second = makeToneFile(5);
        audio.playSoundClip(clip, false);
        waitForClipThreads(1, 3000);

        Utils.stopAllClips();
        audio.playSoundClip(second, false);          // el "revividor"
        Thread.sleep(700);

        boolean firstStillAlive = countClipThreads(clip) > 0;
        check("P2b un clip nuevo no revive a los que se acaban de parar", !firstStillAlive);

        Utils.stopAllClips();
        waitForClipThreads(0, 3000);
        second.delete();
    }

    // ------------------------------------------------------------------ utilidades

    private static AudioManager openDefaultOutput() {
        AudioManager audio = new AudioManager();
        String[] mixers = Utils.getMixerNames(audio.standardDataLineInfo);
        if (mixers.length == 0) {
            return null;
        }
        audio.setPrimaryOutputMixer(mixers[0]);
        AudioManager.setFirstOutputGain(-70.0f);     // casi silencio, esto es un test
        return audio;
    }

    /** Los hilos de reproduccion se llaman como la ruta del fichero. */
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

    /** WAV de N segundos con un tono suave, en el formato nativo de la aplicacion. */
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
        System.out.println((ok ? "  OK   " : "  FALLA") + "  " + label);
        if (!ok) {
            failures++;
        }
    }
}
