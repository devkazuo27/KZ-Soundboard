package exp.gui;

import exp.soundboard.Utils;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Checks that switching theme on the fly (Option -> Appearance) rebuilds the interface without
 * throwing. Shows no window and leaves the user's saved preference untouched.
 */
public class ThemeSwitchTest {

    private static int failures = 0;

    public static void main(String[] args) throws Exception {
        final String original = Utils.prefs.get("uiTheme", Ui.SYSTEM);
        Ui.apply(Ui.LIGHT);

        SwingUtilities.invokeAndWait(new Runnable(){

            @Override
            public void run() {
                JFrame frame = new SoundboardFrame();
                frame.pack();
                try {
                    Ui.setTheme(Ui.DARK);
                    check("switch to dark: " + UIManager.getLookAndFeel().getName(),
                            UIManager.getLookAndFeel().getName().toLowerCase().contains("dark"));
                    Ui.setTheme(Ui.LIGHT);
                    check("switch to light: " + UIManager.getLookAndFeel().getName(),
                            !UIManager.getLookAndFeel().getName().toLowerCase().contains("dark"));
                    Ui.setTheme(Ui.SYSTEM);
                    check("switch to 'match system' without throwing", true);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    check("theme switch without throwing (" + t + ")", false);
                }
                frame.dispose();
            }
        });

        Utils.prefs.put("uiTheme", original);   // leave the preference as it was
        System.out.println(failures == 0 ? "\nTHEMES OK" : "\n" + failures + " FAILURE(S)");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void check(String label, boolean ok) {
        System.out.println((ok ? "  PASS  " : "  FAIL  ") + label);
        if (!ok) {
            failures++;
        }
    }
}
