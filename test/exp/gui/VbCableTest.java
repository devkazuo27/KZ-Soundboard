package exp.gui;

import exp.soundboard.MicInjector;
import exp.soundboard.Utils;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

/**
 * Reports what the application sees of VB-Audio Virtual Cable on this machine. Useful to check
 * the detection against a real install, since Windows truncates mixer names at 31 characters.
 */
public class VbCableTest {

    /** Renders the prompt to a PNG without ever showing it on screen. */
    private static void shootDialog(String path) throws Exception {
        Ui.apply(Ui.DARK);
        javax.swing.SwingUtilities.invokeAndWait(new Runnable(){

            @Override
            public void run() {
                javax.swing.JOptionPane pane = new javax.swing.JOptionPane(
                        VbCable.missingMessage(), javax.swing.JOptionPane.INFORMATION_MESSAGE,
                        javax.swing.JOptionPane.DEFAULT_OPTION, null,
                        new String[]{"Open download page", "Not now", "Don't ask again"});
                javax.swing.JDialog dialog = pane.createDialog(null, "Virtual audio cable");
                dialog.setModal(false);
                dialog.setLocation(-5000, -5000);
                dialog.pack();
                dialog.setVisible(true);
                java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                        dialog.getWidth(), dialog.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g = img.createGraphics();
                dialog.printAll(g);
                g.dispose();
                try {
                    javax.imageio.ImageIO.write(img, "png", new java.io.File(path));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dispose();
            }
        });
        System.out.println("dialog written to " + path);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Audio devices visible to Java:");
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            System.out.println("   " + info.getName());
        }

        boolean driver = VbCable.isDriverInstalled();
        System.out.println("  driver present in registry: " + driver);
        if (args.length > 1 && args[0].equals("shot")) {
            shootDialog(args[1]);
            System.exit(0);
        }
        if (args.length > 0 && args[0].equals("prompt")) {
            System.out.println("  showing the prompt...");
            System.out.println("  prompt returned: " + VbCable.promptIfMissing(null, true));
            System.exit(0);
        }
        boolean installed = VbCable.isInstalled();
        System.out.println("\n  " + (installed ? "PASS  " : "FAIL  ") + "VB-Cable detected: " + installed);

        String playback = VbCable.findPlaybackDevice(
                Utils.getMixerNames(new javax.sound.sampled.DataLine.Info(
                        javax.sound.sampled.SourceDataLine.class, Utils.format, 2048)));
        String recording = VbCable.findRecordingDevice(
                MicInjector.getMixerNames(MicInjector.targetDataLineInfo));

        System.out.println("  play clips into : " + playback);
        System.out.println("  voice app hears : " + recording);
        System.exit(installed && playback != null ? 0 : 1);
    }
}
