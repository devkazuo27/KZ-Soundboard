package exp.gui;

import exp.converter.ConverterFrame;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Renders every window to a PNG so the design can be reviewed without disturbing the desktop:
 * each window is moved off-screen within the same event dispatch thread cycle, so it never
 * actually gets painted on the monitor.
 *
 * Usage: java -cp ... exp.gui.DesignShots &lt;output-dir&gt; [light|dark]
 */
public class DesignShots {

    private static File outputDir;

    public static void main(String[] args) throws Exception {
        outputDir = new File(args[0]);
        outputDir.mkdirs();
        final String theme = args.length > 1 ? args[1] : Ui.SYSTEM;
        Ui.apply(theme);

        SwingUtilities.invokeAndWait(new Runnable(){

            @Override
            public void run() {
                try {
                    SoundboardFrame main = new SoundboardFrame();   // does not show itself
                    shoot(new SoundboardEntryEditor(main), "entry-editor-" + theme);
                    shoot(SettingsFrame.getInstanceOf(), "settings-" + theme);
                    shoot(AudioLevelsFrame.getInstance(), "audio-levels-" + theme);
                    shoot(new ConverterFrame(), "converter-" + theme);
                    main.dispose();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("screenshots written to " + outputDir.getAbsolutePath());
        System.exit(0);
    }

    private static void shoot(Window window, String name) {
        window.setLocation(-5000, -5000);     // before the system gets to paint it
        int width = Math.max(1, window.getWidth());
        int height = Math.max(1, window.getHeight());
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            window.printAll(g);
        }
        finally {
            g.dispose();
        }
        try {
            ImageIO.write(image, "png", new File(outputDir, name + ".png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        window.dispose();
    }
}
