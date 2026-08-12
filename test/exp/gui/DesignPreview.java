package exp.gui;

import java.io.File;
import javax.swing.SwingUtilities;

/**
 * Opens the main window with sample clips so the design can be judged against real data. It is
 * not packaged into the JAR (it lives under test/) and it does not touch the saved preferences.
 *
 * Usage: java -cp ... exp.gui.DesignPreview [light|dark]
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
