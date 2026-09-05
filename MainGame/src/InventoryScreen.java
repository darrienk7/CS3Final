import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
/**
 * Builds and manages the inventory screen.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class InventoryScreen {

    private static final int SLOT_SIZE = 64;
    private static final int SLOT_GAP  = 6;

    private static final Map<String, String> ITEM_SPRITE_PATHS = buildSpritePaths();

    /**
     * Builds the sprite paths.
     *
     * @return the resource cost or loot map
     */
    private static Map<String, String> buildSpritePaths() {
        Map<String, String> m = new HashMap<>();
        m.put("Fish Scales",  "Sprites/FishScales.png");
        m.put("Raw Fish",     "Sprites/RawFish.png");
        m.put("Crab Claw",    "Sprites/CrabClaw.png");
        m.put("Royal Chitin", "Sprites/RoyalChitin.png");
        m.put("Tide Crystal", "Sprites/TideCrystal.png");
        m.put("Deep Kelp",    "Sprites/DeepKelp.png");
        m.put("Health Orb", "Sprites/HealthOrb.png");
        return m;
    }

    private final Screen owner;
    private final Player player;

    /**
     * Creates a new InventoryScreen object.
     *
     * @param owner the owner value
     * @param player the player value
     */
    public InventoryScreen(Screen owner, Player player) {
        this.owner  = owner;
        this.player = player;
    }

    /**
     * Builds the value.
     *
     * @return the value component
     */
    public JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelpers.BG_DARK);
        root.setOpaque(true);

        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(buildSlotArea(), BorderLayout.CENTER);
        root.add(buildFooter(),   BorderLayout.SOUTH);

        return root;
    }

    /**
     * Builds the top bar.
     *
     * @return the top bar component
     */
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0, 0, 0, 220));
        top.setBorder(new EmptyBorder(12, 28, 12, 28));

        JLabel title = new JLabel("INVENTORY");
        title.setFont(UIHelpers.FONT_TITLE.deriveFont(30f));
        title.setForeground(UIHelpers.TEXT_LIGHT);
        top.add(title, BorderLayout.WEST);

        JButton back = UIHelpers.makeButton("BACK", 130, 40, new BackListener());
        top.add(back, BorderLayout.EAST);
        return top;
    }

    /**
     * Builds the slot area.
     *
     * @return the slot area component
     */
    private Component buildSlotArea() {
        Map<Resource, Integer> inv = player.getInventory();
        List<Map.Entry<Resource, Integer>> entries =
                new ArrayList<>(inv.entrySet());
        entries.sort(new Comparator<Map.Entry<Resource, Integer>>() {
            @Override
            /**
             * Handles the compare behavior.
             *
             * @param a the a value
             * @param b the b value
             *
             * @return the compare number
             */
            public int compare(Map.Entry<Resource, Integer> a,
                               Map.Entry<Resource, Integer> b) {
                return a.getKey().getName().compareTo(b.getKey().getName());
            }
        });

        JPanel slotGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, SLOT_GAP, SLOT_GAP));
        slotGrid.setOpaque(true);
        slotGrid.setBackground(new Color(14, 12, 22));
        slotGrid.setBorder(new EmptyBorder(14, 14, 14, 14));

        if (entries.isEmpty()) {
            JLabel none = new JLabel("No items yet — defeat enemies to collect resources.");
            none.setFont(UIHelpers.FONT_BODY);
            none.setForeground(UIHelpers.TEXT_DIM);
            slotGrid.add(none);
        } else {
            for (Map.Entry<Resource, Integer> entry : entries) {
                slotGrid.add(new InventorySlot(entry.getKey().getName(),
                        entry.getValue()));
            }
        }

        JScrollPane scroll = new JScrollPane(slotGrid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(true);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(new Color(14, 12, 22));
        scroll.setBackground(new Color(14, 12, 22));
        scroll.setBorder(BorderFactory.createMatteBorder(
                1, 0, 1, 0, new Color(70, 60, 100)));
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        return scroll;
    }

    /**
     * Builds the footer.
     *
     * @return the footer component
     */
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0, 0, 0, 200));
        footer.setBorder(new EmptyBorder(9, 28, 9, 28));

        Map<Resource, Integer> inv = player.getInventory();
        int stacks = inv.size();
        int total  = 0;
        for (int q : inv.values()) total += q;

        String label;
        if (stacks == 1) {
            label = "1 resource type  ·  " + total + " total";
        } else {
            label = stacks + " resource types  ·  " + total + " total";
        }
        JLabel fl = new JLabel(label);
        fl.setFont(UIHelpers.FONT_BODY);
        fl.setForeground(UIHelpers.TEXT_DIM);
        footer.add(fl, BorderLayout.WEST);
        return footer;
    }


    private static class InventorySlot extends JPanel {
        private final String resourceName;
        private final int    quantity;
        private final Image  sprite;
        private boolean      hovered = false;

        /**
         * Creates a new InventorySlot object.
         *
         * @param resourceName the resource name value
         * @param quantity the quantity value
         */
        public InventorySlot(String resourceName, int quantity) {
            this.resourceName = resourceName;
            this.quantity     = quantity;
            String path = ITEM_SPRITE_PATHS.get(resourceName);
            this.sprite = (path != null) ? UIHelpers.loadSprite(path) : null;

            setOpaque(false);
            setToolTipText(resourceName);
            addMouseListener(new HoverHandler());
        }

        @Override
        /**
         * Returns the preferred size.
         *
         * @return the preferred size
         */
        public Dimension getPreferredSize() {
            return new Dimension(SLOT_SIZE, SLOT_SIZE);
        }

        @Override
        /**
         * Handles the paint component behavior.
         *
         * @param g the g value
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            g2.setColor(hovered ? new Color(40, 36, 60) : new Color(24, 20, 38));
            g2.fillRoundRect(0, 0, w, h, 6, 6);
            g2.setColor(hovered ? UIHelpers.ACCENT_GOLD : new Color(60, 52, 85));
            g2.setStroke(new BasicStroke(hovered ? 2f : 1.5f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, 6, 6);

            int pad = 8;
            int size = w - pad * 2;
            if (sprite != null) {
                g2.drawImage(sprite, pad, pad, size, size, this);
            } else {
                drawLetterFallback(g2, pad, size);
            }

            String qtyStr = String.valueOf(quantity);
            Font qf = new Font("Georgia", Font.BOLD, 13);
            g2.setFont(qf);
            FontMetrics qfm = g2.getFontMetrics();
            int qx = w - qfm.stringWidth(qtyStr) - 4;
            int qy = h - 4;
            g2.setColor(new Color(0, 0, 0, 180));
            g2.drawString(qtyStr, qx + 1, qy + 1);
            g2.setColor(UIHelpers.ACCENT_GOLD);
            g2.drawString(qtyStr, qx, qy);
        }

        /**
         * Draws the letter fallback.
         *
         * @param g2 the g2 value
         * @param pad the pad value
         * @param size the size value
         */
        private void drawLetterFallback(Graphics2D g2, int pad, int size) {
            g2.setColor(new Color(36, 30, 50));
            g2.fillRoundRect(pad, pad, size, size, 4, 4);
            g2.setColor(new Color(90, 75, 130));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(pad, pad, size, size, 4, 4);

            String letter = resourceName.substring(0, 1).toUpperCase();
            g2.setFont(new Font("Georgia", Font.BOLD, size / 2));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(160, 140, 200));
            g2.drawString(letter,
                    pad + (size - fm.stringWidth(letter)) / 2,
                    pad + (size + fm.getAscent() - fm.getDescent()) / 2);
        }

        private class HoverHandler extends MouseAdapter {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
        }
    }

    private class BackListener implements ActionListener {
        @Override
        /**
         * Handles the action performed behavior.
         *
         * @param e the e value
         */
        public void actionPerformed(ActionEvent e) {
            owner.showCard(Screen.CARD_CHARACTER);
        }
    }
}