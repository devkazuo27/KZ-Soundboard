package exp.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import exp.soundboard.Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

/**
 * Aspecto visual de la aplicacion: tema (claro / oscuro / el del sistema) y unos pocos
 * componentes reutilizables.
 *
 * El original usaba el Look and Feel del sistema y ademas fijaba colores a mano
 * (Color.WHITE, Color.CYAN, Color.RED, Color.DARK_GRAY, fuentes "Tahoma"), lo que impide
 * cualquier tema oscuro y se ve mal al escalar por DPI. Todo eso pasa por aqui.
 */
public final class Ui {

    public static final String SYSTEM = "system";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    private static final String PREF_KEY = "uiTheme";
    private static String current = SYSTEM;

    private Ui() {
    }

    /** Instala el tema guardado. Debe llamarse antes de crear ninguna ventana. */
    public static void installTheme() {
        apply(Utils.prefs.get(PREF_KEY, SYSTEM));
    }

    public static String currentTheme() {
        return current;
    }

    /** Cambia el tema en caliente y lo recuerda. */
    public static void setTheme(String theme) {
        if (theme.equals(current)) {
            return;
        }
        apply(theme);
        Utils.prefs.put(PREF_KEY, theme);
        FlatLaf.updateUI();
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    static void apply(String theme) {   // visible para DesignPreview (no persiste el tema)
        current = theme;
        boolean dark = DARK.equals(theme) || (SYSTEM.equals(theme) && systemPrefersDark());
        FlatLaf.setup(dark ? new FlatMacDarkLaf() : new FlatMacLightLaf());
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumbArc", 8);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("Table.showHorizontalLines", Boolean.TRUE);
        UIManager.put("TitlePane.unifiedBackground", Boolean.TRUE);
        UIManager.put("TableHeader.height", 30);
        // Filas alternas discretas: una lista de clips se lee mucho mejor asi.
        Color rowBackground = UIManager.getColor("Table.background");
        if (rowBackground != null) {
            UIManager.put("Table.alternateRowColor", shift(rowBackground, dark ? 10 : -8));
        }
    }

    /** Aclara (delta positivo) u oscurece un color, sin salirse de rango. */
    private static Color shift(Color color, int delta) {
        return new Color(
                Math.max(0, Math.min(255, color.getRed() + delta)),
                Math.max(0, Math.min(255, color.getGreen() + delta)),
                Math.max(0, Math.min(255, color.getBlue() + delta)));
    }

    /**
     * Marco de la tabla: esquinas redondeadas y sin el aro de foco azul, que al tener la
     * tabla el foco pintaba un recuadro brillante alrededor de media ventana.
     */
    public static void styleTableContainer(JScrollPane scrollPane) {
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "focusWidth: 0; arc: 10");
        scrollPane.setViewportBorder(null);
    }

    /**
     * Windows guarda la preferencia de tema de las aplicaciones en el registro. Java no la
     * expone, asi que se consulta con "reg query". Si algo falla, se asume tema claro.
     */
    private static boolean systemPrefersDark() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return false;
        }
        Process process = null;
        try {
            process = new ProcessBuilder("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme").redirectErrorStream(true).start();
            StringBuilder out = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
            }
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                return false;
            }
            String text = out.toString();
            int at = text.indexOf("0x");
            return at >= 0 && text.charAt(at + 2) == '0';   // 0x0 = aplicaciones en oscuro
        }
        catch (Exception e) {
            return false;
        }
        finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    // ------------------------------------------------------------------ componentes

    /** Cabecera de seccion: titulo discreto seguido de una linea hasta el borde. */
    public static JComponent section(String title) {
        JPanel panel = new JPanel(new MigLayout("insets 0, gap 8, fillx", "[][grow,fill]"));
        panel.setOpaque(false);
        panel.add(new SectionLabel(title));
        panel.add(new JSeparator());
        return panel;
    }

    /** Texto auxiliar, mas tenue que el normal. */
    public static JLabel hint(String text) {
        return new HintLabel(text);
    }

    /**
     * Marca un campo que esta capturando teclas. Antes se pintaba el fondo de cian fijo, que
     * sobre un tema oscuro deja el texto ilegible; el contorno de FlatLaf se adapta al tema.
     */
    public static void markCapturing(JTextField field) {
        field.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
    }

    /** Devuelve el campo a su aspecto normal (antes: fondo blanco fijo). */
    public static void markIdle(JTextField field) {
        field.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    /**
     * Las etiquetas se re-estilan en updateUI(), que Swing invoca al cambiar de tema; si se
     * fijara el color una sola vez, al pasar a oscuro se quedaria el color del tema anterior.
     */
    private static class SectionLabel extends JLabel {
        private static final long serialVersionUID = 1L;

        SectionLabel(String text) {
            super(text);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            Font base = UIManager.getFont("Label.font");
            if (base != null) {
                setFont(base.deriveFont(Font.BOLD, base.getSize2D() - 1f));
            }
            setForeground(muted());
        }
    }

    private static class HintLabel extends JLabel {
        private static final long serialVersionUID = 1L;

        HintLabel(String text) {
            super(text);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            Font base = UIManager.getFont("Label.font");
            if (base != null) {
                setFont(base.deriveFont(base.getSize2D() - 1f));
            }
            setForeground(muted());
        }
    }

    private static Color muted() {
        Color color = UIManager.getColor("Label.disabledForeground");
        return color != null ? color : UIManager.getColor("Label.foreground");
    }
}
