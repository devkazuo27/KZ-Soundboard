package exp.gui;

import exp.converter.ConverterFrame;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Renderiza cada ventana a un PNG para poder revisar el diseno sin tocar el escritorio:
 * las mueve fuera de pantalla dentro del mismo ciclo del hilo de eventos, asi que nunca
 * llegan a pintarse en el monitor.
 *
 * Uso: java -cp ... exp.gui.DesignShots <carpeta-destino> [light|dark]
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
                    SoundboardFrame main = new SoundboardFrame();   // no se muestra solo
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
        System.out.println("capturas en " + outputDir.getAbsolutePath());
        System.exit(0);
    }

    private static void shoot(Window window, String name) {
        window.setLocation(-5000, -5000);     // antes de que el sistema la pinte
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
