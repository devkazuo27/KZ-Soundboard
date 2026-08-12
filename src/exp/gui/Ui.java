package exp.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import exp.soundboard.Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
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
 * Everything about how the application looks: the KZ palette, the theme (dark, light, or
 * whatever the system uses) and a handful of reusable components.
 *
 * The original applied the system look and feel and, on top of that, hard-coded colours
 * (Color.WHITE, Color.CYAN, Color.RED, Color.DARK_GRAY) and fixed-size "Tahoma" fonts. That
 * makes any dark theme impossible and looks wrong once the display DPI scales things. It all
 * goes through here now.
 */
public final class Ui {

    public static final String SYSTEM = "system";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    /** Neon violet sampled straight from the KZ logo: used for glows and highlights. */
    public static final Color NEON = new Color(0xA301F4);
    /** Slightly tempered violet for filled controls, so white text on it stays readable. */
    public static final Color ACCENT = new Color(0x8B3DF0);

    private static final String PREF_KEY = "uiTheme";
    private static String current = DARK;
    private static ImageIcon logo;

    private Ui() {
    }

    /** Installs the saved theme. Must be called before any window is created. */
    public static void installTheme() {
        apply(Utils.prefs.get(PREF_KEY, DARK));
    }

    public static String currentTheme() {
        return current;
    }

    /** Switches theme on the fly and remembers the choice. */
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

    static void apply(String theme) {   // visible to DesignPreview (does not persist the theme)
        current = theme;
        boolean dark = DARK.equals(theme) || (SYSTEM.equals(theme) && systemPrefersDark());
        // The accent recolours selection, focus rings and default buttons across every window.
        Map<String, String> brand = new HashMap<String, String>();
        brand.put("@accentColor", "#8B3DF0");
        FlatLaf.setGlobalExtraDefaults(brand);
        FlatLaf.setup(dark ? new FlatMacDarkLaf() : new FlatMacLightLaf());
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumbArc", 8);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("TitlePane.unifiedBackground", Boolean.TRUE);
    }

    /** The application icon and brand mark, scaled to the requested size. */
    public static ImageIcon logo(int size) {
        if (logo == null) {
            logo = new ImageIcon(Ui.class.getResource("kz-logo.png"));
        }
        return new ImageIcon(logo.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    public static Image logoImage() {
        return logo(256).getImage();
    }

    /** The brand strip at the top of the main window: logo plus wordmark. */
    public static JComponent brandHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 0 0 4 0, gap 10, fillx", "[][grow,fill]"));
        panel.setOpaque(false);
        panel.add(new JLabel(logo(34)));
        panel.add(new Wordmark());
        return panel;
    }

    /** Section header: a quiet title followed by a rule running to the edge. */
    public static JComponent section(String title) {
        JPanel panel = new JPanel(new MigLayout("insets 0, gap 8, fillx", "[][grow,fill]"));
        panel.setOpaque(false);
        panel.add(new SectionLabel(title));
        panel.add(new JSeparator());
        return panel;
    }

    /** Secondary text, dimmer than the regular one. */
    public static JLabel hint(String text) {
        return new HintLabel(text);
    }

    /**
     * Frames a scrolling area: rounded corners and no blue focus ring, which with the content
     * focused used to draw a bright box around half the window.
     */
    public static void styleTableContainer(JScrollPane scrollPane) {
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "focusWidth: 0; arc: 10");
        scrollPane.setViewportBorder(null);
    }

    /**
     * Marks a field that is currently capturing keys. This used to paint the background a
     * fixed cyan, which over a dark theme leaves the text unreadable; the FlatLaf outline
     * follows the theme instead.
     */
    public static void markCapturing(JTextField field) {
        field.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
    }

    /** Returns the field to its normal look (previously: a fixed white background). */
    public static void markIdle(JTextField field) {
        field.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    /** Blends two colours; ratio 0 returns the first one, 1 the second. */
    public static Color blend(Color from, Color to, float ratio) {
        float keep = 1f - ratio;
        return new Color(
                Math.round(from.getRed() * keep + to.getRed() * ratio),
                Math.round(from.getGreen() * keep + to.getGreen() * ratio),
                Math.round(from.getBlue() * keep + to.getBlue() * ratio));
    }

    public static boolean isDark() {
        return FlatLaf.isLafDark();
    }

    /**
     * Windows keeps the app theme preference in the registry. Java does not expose it, so it
     * is read with "reg query". If anything goes wrong, assume the light theme.
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
            return at >= 0 && text.charAt(at + 2) == '0';   // 0x0 means apps use the dark theme
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

    // ------------------------------------------------------------------ components

    /**
     * These labels restyle themselves in updateUI(), which Swing calls on a theme change. Were
     * the colour set just once, switching theme would leave the previous one behind.
     */
    private static class Wordmark extends JLabel {
        private static final long serialVersionUID = 1L;

        Wordmark() {
            super("KZ SOUNDBOARD");
        }

        @Override
        public void updateUI() {
            super.updateUI();
            Font base = UIManager.getFont("Label.font");
            if (base != null) {
                setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 3f));
            }
            putClientProperty("html.disable", Boolean.TRUE);
            setForeground(isDark() ? blend(NEON, Color.WHITE, 0.35f) : ACCENT);
        }
    }

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
