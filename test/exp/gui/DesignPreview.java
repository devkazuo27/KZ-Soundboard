package exp.gui;

import java.io.File;
import javax.swing.SwingUtilities;

/**
 * Abre la ventana principal con clips de ejemplo para poder juzgar el diseno con datos
 * reales. No se empaqueta en el JAR (vive en test/) y no toca las preferencias guardadas.
 *
 * Uso: java -cp ... exp.gui.DesignPreview [light|dark]
 */
public class DesignPreview {

    public static void main(String[] args) {
        Ui.apply(args.length > 0 ? args[0] : Ui.SYSTEM);
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                SoundboardFrame frame = new SoundboardFrame();
                SoundboardFrame.soundboard.addEntry(new File("C:\\Sounds\\airhorn.mp3"), new int[]{112});
                SoundboardFrame.soundboard.addEntry(new File("C:\\Sounds\\bruh.wav"), new int[]{113});
                SoundboardFrame.soundboard.addEntry(new File("C:\\Sounds\\wilhelm scream.mp3"), new int[]{17, 65});
                SoundboardFrame.soundboard.addEntry(new File("C:\\Sounds\\sad violin.mp3"), new int[]{114});
                SoundboardFrame.soundboard.addEntry(new File("C:\\Sounds\\vine boom.wav"), new int[]{96});
                frame.updateSoundboardTable();
                frame.setVisible(true);
            }
        });
    }
}
