import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
/**
 * Provides shared user interface helpers.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class UIHelpers {

    public static final Color BG_DARK     = new Color( 10,  10,  18);
    public static final Color ACCENT_GOLD = new Color(212, 175,  55);
    public static final Color ACCENT_RED  = new Color(180,  40,  40);
    public static final Color TEXT_LIGHT  = new Color(230, 225, 210);
    public static final Color TEXT_DIM    = new Color(140, 130, 110);
    public static final Color BTN_BASE    = new Color( 28,  24,  44);
    public static final Color BTN_HOVER   = new Color( 55,  48,  82);
    public static final Color HP_GREEN    = new Color( 40, 180,  60);
    public static final Color HP_YELLOW   = new Color(210, 180,  40);
    public static final Color HP_RED      = new Color(200,  40,  40);
    public static final Color FLASH_RED   = new Color(255,  30,  30, 165);

    public static final Font FONT_TITLE    = new Font("Georgia",    Font.BOLD,  52);
    public static final Font FONT_HEAD     = new Font("Georgia",    Font.BOLD,  22);
    public static final Font FONT_SUBHEAD  = new Font("Georgia",    Font.BOLD,  16);
    public static final Font FONT_BODY     = new Font("Monospaced", Font.PLAIN, 13);
    public static final Font FONT_BTN      = new Font("Georgia",    Font.BOLD,  16);
    public static final Font FONT_SMALL    = new Font("Monospaced", Font.PLAIN, 11);


    /**
     * Handles the load sprite behavior.
     *
     * @param path the path value
     *
     * @return the loaded image, or null if unavailable
     */
    public static Image loadSprite(String path) {
        try {
            java.net.URL url = UIHelpers.class.getClassLoader().getResource(path);
            if (url == null) return null;
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            return null;
        }
    }

    private static class HoverMouse extends MouseAdapter {
        private final JButton btn;
        public boolean hovered = false;
        public HoverMouse(JButton btn) { this.btn = btn; }
        @Override public void mouseEntered(MouseEvent e) { hovered = true;  btn.repaint(); }
        @Override public void mouseExited (MouseEvent e) { hovered = false; btn.repaint(); }
    }

    /**
     * Creates the button.
     *
     * @param text the text value
     * @param width the width value
     * @param height the height value
     * @param action the action value
     *
     * @return the configured button
     */
    public static JButton makeButton(String text, int width, int height,
                                     ActionListener action) {
        final JButton[] holder = new JButton[1];
        final HoverMouse[] hoverRef = new HoverMouse[1];

        JButton btn = new JButton(text) {
            @Override
            /**
             * Handles the paint component behavior.
             *
             * @param g the g value
             */
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hov = (hoverRef[0] != null && hoverRef[0].hovered);
                g2.setColor(hov ? BTN_HOVER : BTN_BASE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(hov ? ACCENT_GOLD : new Color(100, 90, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        holder[0]   = btn;
        hoverRef[0] = new HoverMouse(btn);
        btn.addMouseListener(hoverRef[0]);

        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_LIGHT);
        btn.setPreferredSize(new Dimension(width, height));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(action);
        return btn;
    }

    /**
     * Draws the hp bar.
     *
     * @param g2 the g2 value
     * @param x the x value
     * @param y the y value
     * @param w the w value
     * @param hp the hp value
     * @param maxHp the max hp value
     * @param label the label value
     */
    public static void drawHpBar(Graphics2D g2, int x, int y, int w,
                                 float hp, float maxHp, String label) {
        float pct = Math.max(0f, Math.min(1f, hp / maxHp));
        int barH = 8;

        g2.setColor(new Color(22, 18, 32, 210));
        g2.fillRoundRect(x, y, w, barH, 5, 5);

        Color fill;
        if (pct > 0.5f)      fill = HP_GREEN;
        else if (pct > 0.25f) fill = HP_YELLOW;
        else                  fill = HP_RED;
        g2.setColor(fill);
        g2.fillRoundRect(x, y, Math.max(1, (int)(w * pct)), barH, 5, 5);

        g2.setColor(TEXT_LIGHT);
        g2.setFont(FONT_SMALL);
        g2.drawString(label, x, y + barH + 12);
        g2.setColor(TEXT_DIM);
        g2.drawString(formatBigNumber(hp) + " / " + formatBigNumber(maxHp),
                x, y + barH + 24);
    }

    /**
     * Handles the format big number behavior.
     *
     * @param n the n value
     *
     * @return the format big number text
     */
    public static String formatBigNumber(double n) {
        if (n < 1_000)          return String.format("%.0f", n);
        if (n < 1_000_000)      return String.format("%.1fK", n / 1_000.0);
        if (n < 1_000_000_000)  return String.format("%.1fM", n / 1_000_000.0);
        return String.format("%.1fB", n / 1_000_000_000.0);
    }
}