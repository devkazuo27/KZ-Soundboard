package exp.gui;

import exp.soundboard.SoundboardEntry;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

/**
 * Draws one clip as a pad: the file name on top and its hotkey on a chip underneath.
 *
 * It is a renderer rather than a real component because the grid is a JList, which brings
 * selection, keyboard navigation and reflowing for free.
 */
public class SoundPadRenderer extends JComponent implements ListCellRenderer<SoundboardEntry> {

    private static final long serialVersionUID = 1L;

    public static final int PAD_WIDTH = 132;
    public static final int PAD_HEIGHT = 86;

    private String name = "";
    private String hotkey = "";
    private boolean selected;

    public SoundPadRenderer() {
        setPreferredSize(new Dimension(PAD_WIDTH, PAD_HEIGHT));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SoundboardEntry> list,
            SoundboardEntry entry, int index, boolean isSelected, boolean cellHasFocus) {
        this.name = entry == null ? "" : stripExtension(entry.getFileName());
        this.hotkey = entry == null ? "" : entry.getActivationKeysAsReadableString();
        this.selected = isSelected;
        return this;
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf(46);
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth() - 6;
            int h = getHeight() - 6;
            int x = 3;
            int y = 3;
            boolean dark = Ui.isDark();
            Color surface = UIManager.getColor("List.background");
            if (surface == null) {
                surface = UIManager.getColor("Panel.background");
            }
            Color face = dark ? Ui.blend(surface, Color.WHITE, 0.07f) : Ui.blend(surface, Color.BLACK, 0.04f);
            Color edge = dark ? Ui.blend(surface, Color.WHITE, 0.16f) : Ui.blend(surface, Color.BLACK, 0.14f);

            if (this.selected) {
                // Selected pad: violet wash plus the neon edge from the logo.
                face = Ui.blend(face, Ui.ACCENT, dark ? 0.32f : 0.18f);
                edge = Ui.NEON;
            }

            g.setColor(face);
            g.fillRoundRect(x, y, w, h, 12, 12);
            g.setColor(edge);
            g.setStroke(new java.awt.BasicStroke(this.selected ? 1.6f : 1.0f));
            g.drawRoundRect(x, y, w, h, 12, 12);

            Font base = UIManager.getFont("Label.font");
            Color text = UIManager.getColor("Label.foreground");

            // Clip name, trimmed with an ellipsis so a long file name cannot overflow the pad.
            g.setFont(base.deriveFont(Font.BOLD, base.getSize2D()));
            g.setColor(text);
            FontMetrics fm = g.getFontMetrics();
            String label = ellipsize(this.name, fm, w - 16);
            g.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + 30);

            // Hotkey chip.
            String key = this.hotkey.isEmpty() ? "no hotkey" : this.hotkey;
            g.setFont(base.deriveFont(base.getSize2D() - 1f));
            fm = g.getFontMetrics();
            String chip = ellipsize(key, fm, w - 24);
            int chipW = fm.stringWidth(chip) + 16;
            int chipH = fm.getHeight() + 4;
            int chipX = x + (w - chipW) / 2;
            int chipY = y + h - chipH - 12;

            g.setColor(this.hotkey.isEmpty()
                    ? Ui.blend(face, text, 0.12f)
                    : Ui.blend(face, Ui.NEON, dark ? 0.45f : 0.25f));
            g.fillRoundRect(chipX, chipY, chipW, chipH, chipH, chipH);
            g.setColor(this.hotkey.isEmpty() ? Ui.blend(text, face, 0.55f) : text);
            g.drawString(chip, chipX + 8, chipY + fm.getAscent() + 2);
        }
        finally {
            g.dispose();
        }
    }

    private static String ellipsize(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        int end = text.length();
        while (end > 1 && fm.stringWidth(text.substring(0, end) + ellipsis) > maxWidth) {
            end--;
        }
        return text.substring(0, end) + ellipsis;
    }
}
