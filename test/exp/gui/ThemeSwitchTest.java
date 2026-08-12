package exp.gui;

import exp.soundboard.Utils;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Comprueba que cambiar de tema en caliente (Option -> Appearance) reconstruye la interfaz
 * sin excepciones. No muestra ninguna ventana ni deja tocada la preferencia del usuario.
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
                    check("cambio a oscuro: " + UIManager.getLookAndFeel().getName(),
                            UIManager.getLookAndFeel().getName().toLowerCase().contains("dark"));
                    Ui.setTheme(Ui.LIGHT);
                    check("cambio a claro: " + UIManager.getLookAndFeel().getName(),
                            !UIManager.getLookAndFeel().getName().toLowerCase().contains("dark"));
                    Ui.setTheme(Ui.SYSTEM);
                    check("cambio a 'seguir al sistema' sin excepciones", true);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    check("cambio de tema sin excepciones (" + t + ")", false);
                }
                frame.dispose();
            }
        });

        Utils.prefs.put("uiTheme", original);   // dejar la preferencia como estaba
        System.out.println(failures == 0 ? "\nTEMAS OK" : "\n" + failures + " FALLO(S)");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void check(String label, boolean ok) {
        System.out.println((ok ? "  OK   " : "  FALLA") + "  " + label);
        if (!ok) {
            failures++;
        }
    }
}
