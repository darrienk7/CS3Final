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
 * Builds and manages the skill tree screen.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class SkillTreeScreen {
    private static final int NODE_RADIUS     = 42;
    private static final int HUB_RADIUS      = 56;
    private static final int SPOKE_DISTANCE  = 220;

    private final Screen    owner;
    private final Player    player;
    private final SkillTree tree;

    private SkillNode  selectedNode = null;

    private final Map<String, Point> nodePositions = new HashMap<>();

    private SkillCanvas    canvas;
    private JPanel         detailContainer;
    private JLabel         lvlLabel;

    /**
     * Creates a new SkillTreeScreen object.
     *
     * @param owner the owner value
     * @param player the player value
     * @param tree the tree value
     */
    public SkillTreeScreen(Screen owner, Player player, SkillTree tree) {
        this.owner  = owner;
        this.player = player;
        this.tree   = tree;
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

        root.add(buildTopBar(), BorderLayout.NORTH);

        // Canvas + detail side-by-side using a split
        canvas = new SkillCanvas();
        canvas.addMouseListener(new NodeClickHandler());

        detailContainer = new JPanel(new BorderLayout());
        detailContainer.setBackground(new Color(16, 14, 26));
        detailContainer.setPreferredSize(new Dimension(300, 0));
        detailContainer.setBorder(BorderFactory.createMatteBorder(
                0, 1, 0, 0, new Color(60, 50, 90)));
        rebuildDetailPanel();   // start with empty hint

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                canvas, detailContainer);
        split.setDividerSize(1);
        split.setResizeWeight(0.78);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);
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

        JLabel title = new JLabel("SKILL TREE");
        title.setFont(UIHelpers.FONT_TITLE.deriveFont(30f));
        title.setForeground(UIHelpers.ACCENT_GOLD);
        top.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        lvlLabel = new JLabel("Level " + player.getLevel());
        lvlLabel.setFont(UIHelpers.FONT_SUBHEAD);
        lvlLabel.setForeground(UIHelpers.TEXT_LIGHT);
        right.add(lvlLabel);

        JButton back = UIHelpers.makeButton("BACK", 130, 40, new BackListener());
        right.add(back);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private class SkillCanvas extends JPanel {

        /**
         * Creates a new SkillCanvas object.
         */
        public SkillCanvas() {
            setBackground(new Color(8, 6, 16));
            setOpaque(true);
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

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;


            RadialGradientPaint glow = new RadialGradientPaint(
                    cx, cy, SPOKE_DISTANCE + 100,
                    new float[]{0f, 1f},
                    new Color[]{new Color(40, 30, 70, 90), new Color(0, 0, 0, 0)});
            g2.setPaint(glow);
            g2.fillRect(0, 0, getWidth(), getHeight());

            recomputeNodePositions(cx, cy);

            drawSpokes(g2, cx, cy);


            List<SkillNode> all = tree.getAllNodes();
            for (SkillNode node : all) {
                if (node.getType() == SkillNode.NodeType.WEAPON) continue;
                Point p = nodePositions.get(node.getId());
                drawOuterNode(g2, p.x, p.y, node);
            }

            SkillNode weapon = tree.getNode("WEAPON");
            drawWeaponHub(g2, cx, cy, weapon);
        }

        /**
         * Draws the spokes.
         *
         * @param g2 the g2 value
         * @param cx the cx value
         * @param cy the cy value
         */
        private void drawSpokes(Graphics2D g2, int cx, int cy) {
            g2.setColor(new Color(80, 70, 110));
            g2.setStroke(new BasicStroke(2f));
            for (Map.Entry<String, Point> entry : nodePositions.entrySet()) {
                if ("WEAPON".equals(entry.getKey())) continue;
                Point p = entry.getValue();
                g2.drawLine(cx, cy, p.x, p.y);
            }
        }

        /**
         * Draws the outer node.
         *
         * @param g2 the g2 value
         * @param x the x value
         * @param y the y value
         * @param node the node value
         */
        private void drawOuterNode(Graphics2D g2, int x, int y, SkillNode node) {
            boolean isSelected    = (selectedNode != null
                    && selectedNode.getId().equals(node.getId()));
            boolean upgradeReady  = canAffordUpgrade(node);

            if (upgradeReady) {
                g2.setColor(new Color(255, 255, 180, 90));
                g2.fillOval(x - NODE_RADIUS - 12, y - NODE_RADIUS - 12,
                        (NODE_RADIUS + 12) * 2, (NODE_RADIUS + 12) * 2);
            }

            if (isSelected) {
                g2.setColor(new Color(212, 175, 55, 70));
                g2.fillOval(x - NODE_RADIUS - 8, y - NODE_RADIUS - 8,
                        (NODE_RADIUS + 8) * 2, (NODE_RADIUS + 8) * 2);
            }

            g2.setColor(nodeFillColor(node));
            g2.fillOval(x - NODE_RADIUS, y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);

            if (upgradeReady) {
                g2.setColor(new Color(255, 255, 200));
                g2.setStroke(new BasicStroke(3.5f));
            } else {
                g2.setColor(node.isUnlocked() ? nodeBorderColor(node)
                        : new Color(80, 70, 110));
                g2.setStroke(new BasicStroke(node.isUnlocked() ? 3f : 1.5f));
            }
            g2.drawOval(x - NODE_RADIUS, y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);


            String tierStr = String.valueOf(node.getCurrentTier());
            g2.setFont(UIHelpers.FONT_HEAD.deriveFont(20f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(upgradeReady ? new Color(255, 255, 180) : UIHelpers.TEXT_LIGHT);
            g2.drawString(tierStr,
                    x - fm.stringWidth(tierStr) / 2,
                    y + fm.getAscent() / 2 - 4);


            g2.setFont(UIHelpers.FONT_SUBHEAD);
            FontMetrics fm2 = g2.getFontMetrics();
            g2.setColor(upgradeReady ? new Color(255, 255, 180) : UIHelpers.TEXT_LIGHT);
            String name = node.getDisplayName();
            g2.drawString(name,
                    x - fm2.stringWidth(name) / 2,
                    y + NODE_RADIUS + 22);


            if (upgradeReady) {
                int bx = x + NODE_RADIUS - 8;
                int by = y - NODE_RADIUS + 8;
                g2.setColor(new Color(255, 220, 40));
                g2.fillOval(bx - 10, by - 10, 20, 20);
                g2.setColor(new Color(20, 16, 8));
                g2.setFont(new Font("Georgia", Font.BOLD, 13));
                FontMetrics fm3 = g2.getFontMetrics();
                g2.drawString("!", bx - fm3.stringWidth("!") / 2,
                        by + fm3.getAscent() / 2 - 2);
            }
        }

        /**
         * Draws the weapon hub.
         *
         * @param g2 the g2 value
         * @param cx the cx value
         * @param cy the cy value
         * @param weapon the weapon value
         */
        private void drawWeaponHub(Graphics2D g2, int cx, int cy, SkillNode weapon) {
            boolean isSelected   = (selectedNode != null
                    && "WEAPON".equals(selectedNode.getId()));
            boolean upgradeReady = canAffordUpgrade(weapon);

            if (upgradeReady) {
                g2.setColor(new Color(255, 255, 180, 90));
                g2.fillOval(cx - HUB_RADIUS - 14, cy - HUB_RADIUS - 14,
                        (HUB_RADIUS + 14) * 2, (HUB_RADIUS + 14) * 2);
            }

            if (isSelected) {
                g2.setColor(new Color(212, 175, 55, 90));
                g2.fillOval(cx - HUB_RADIUS - 10, cy - HUB_RADIUS - 10,
                        (HUB_RADIUS + 10) * 2, (HUB_RADIUS + 10) * 2);
            }

            g2.setColor(new Color(40, 32, 14));
            g2.fillOval(cx - HUB_RADIUS, cy - HUB_RADIUS,
                    HUB_RADIUS * 2, HUB_RADIUS * 2);

            g2.setColor(upgradeReady ? new Color(255, 255, 200) : UIHelpers.ACCENT_GOLD);
            g2.setStroke(new BasicStroke(upgradeReady ? 4f : 3f));
            g2.drawOval(cx - HUB_RADIUS, cy - HUB_RADIUS,
                    HUB_RADIUS * 2, HUB_RADIUS * 2);


            String label = weapon.isMaxed() ? "MAX"
                    : "T" + (weapon.getCurrentTier() + 1);
            g2.setFont(UIHelpers.FONT_HEAD.deriveFont(22f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(upgradeReady ? new Color(255, 255, 180) : UIHelpers.ACCENT_GOLD);
            g2.drawString(label,
                    cx - fm.stringWidth(label) / 2,
                    cy + fm.getAscent() / 2 - 8);

            g2.setFont(UIHelpers.FONT_SMALL);
            FontMetrics fm2 = g2.getFontMetrics();
            String wname = player.getWeapon().getName();
            g2.setColor(UIHelpers.TEXT_LIGHT);
            g2.drawString(wname,
                    cx - fm2.stringWidth(wname) / 2,
                    cy + 14);

            g2.setFont(UIHelpers.FONT_SUBHEAD);
            FontMetrics fm3 = g2.getFontMetrics();
            String hdr = "WEAPON";
            g2.setColor(upgradeReady ? new Color(255, 255, 180) : UIHelpers.ACCENT_GOLD);
            g2.drawString(hdr,
                    cx - fm3.stringWidth(hdr) / 2,
                    cy + HUB_RADIUS + 22);


            if (upgradeReady) {
                int bx = cx + HUB_RADIUS - 10;
                int by = cy - HUB_RADIUS + 10;
                g2.setColor(new Color(255, 220, 40));
                g2.fillOval(bx - 11, by - 11, 22, 22);
                g2.setColor(new Color(20, 16, 8));
                g2.setFont(new Font("Georgia", Font.BOLD, 14));
                FontMetrics fm4 = g2.getFontMetrics();
                g2.drawString("!", bx - fm4.stringWidth("!") / 2,
                        by + fm4.getAscent() / 2 - 2);
            }
        }
    }

    /**
     * Handles the recompute node positions behavior.
     *
     * @param cx the cx value
     * @param cy the cy value
     */
    private void recomputeNodePositions(int cx, int cy) {
        nodePositions.clear();
        nodePositions.put("WEAPON", new Point(cx, cy));

        String[] outerOrder = { "ATTACK", "SPEED", "RESOURCE", "DEFENSE", "HEALTH" };
        int count = outerOrder.length;

        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2 + (2 * Math.PI * i / count);
            int x = cx + (int)(Math.cos(angle) * SPOKE_DISTANCE);
            int y = cy + (int)(Math.sin(angle) * SPOKE_DISTANCE);
            nodePositions.put(outerOrder[i], new Point(x, y));
        }
    }

    /**
     * Handles the can afford upgrade behavior.
     *
     * @param node the node value
     *
     * @return true if afford upgrade, false otherwise
     */
    private boolean canAffordUpgrade(SkillNode node) {
        if (node.isMaxed()) return false;
        if (player.getLevel() < node.getNextLvlReq()) return false;
        Map<Resource, Integer> cost = node.getNextCost();
        if (cost == null) return false;
        for (Map.Entry<Resource, Integer> entry : cost.entrySet()) {
            int have = player.getInventory().getOrDefault(entry.getKey(), 0);
            if (have < entry.getValue()) return false;
        }
        return true;
    }

    /**
     * Handles the node fill color behavior.
     *
     * @param node the node value
     *
     * @return the node fill color value
     */
    private Color nodeFillColor(SkillNode node) {
        switch (node.getType()) {
            case ATTACK:   return new Color(80, 30, 30);
            case DEFENSE:  return new Color(30, 50, 80);
            case HEALTH:   return new Color(30, 70, 30);
            case SPEED:    return new Color(80, 70, 25);
            case RESOURCE: return new Color(60, 35, 80);
            default:       return new Color(60, 50, 30);
        }
    }

    /**
     * Handles the node border color behavior.
     *
     * @param node the node value
     *
     * @return the node border color value
     */
    private Color nodeBorderColor(SkillNode node) {
        switch (node.getType()) {
            case ATTACK:   return new Color(220, 100,  80);
            case DEFENSE:  return new Color( 80, 140, 220);
            case HEALTH:   return new Color( 80, 200,  80);
            case SPEED:    return new Color(220, 200,  60);
            case RESOURCE: return new Color(160, 100, 220);
            default:       return UIHelpers.ACCENT_GOLD;
        }
    }



    /**
     * Handles the rebuild detail panel behavior.
     */
    private void rebuildDetailPanel() {
        detailContainer.removeAll();
        detailContainer.add(buildDetailContent(), BorderLayout.NORTH);
        detailContainer.revalidate();
        detailContainer.repaint();
    }

    /**
     * Builds the detail content.
     *
     * @return the detail content component
     */
    private JPanel buildDetailContent() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 16, 20, 16));

        if (selectedNode == null) {
            JLabel hint = new JLabel("<html><center>Click a node<br>to see details</center></html>");
            hint.setFont(UIHelpers.FONT_BODY);
            hint.setForeground(UIHelpers.TEXT_DIM);
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(hint);
            return p;
        }

        SkillNode node = selectedNode;

        JLabel nameLbl = new JLabel(node.getDisplayName());
        nameLbl.setFont(UIHelpers.FONT_HEAD);
        nameLbl.setForeground(nodeBorderColor(node));
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(nameLbl);

        p.add(Box.createVerticalStrut(6));

        String statusText;
        if (node.isMaxed()) {
            statusText = "MAX TIER — " + node.getCurrentTierName();
        } else if (node.isUnlocked()) {
            statusText = "Current: Tier " + node.getCurrentTier() + " ("
                    + node.getCurrentTierName() + ")";
        } else {
            statusText = "Not yet unlocked";
        }
        JLabel statusLbl = new JLabel(statusText);
        statusLbl.setFont(UIHelpers.FONT_SMALL);
        statusLbl.setForeground(node.isUnlocked() ? new Color(100, 200, 100)
                : UIHelpers.TEXT_DIM);
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(statusLbl);

        if (node.isMaxed()) {
            return p;
        }

        p.add(Box.createVerticalStrut(14));

        JLabel nextHdr = new JLabel("Next: " + node.getNextTierName());
        nextHdr.setFont(UIHelpers.FONT_SUBHEAD);
        nextHdr.setForeground(UIHelpers.ACCENT_GOLD);
        nextHdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(nextHdr);

        p.add(Box.createVerticalStrut(6));

        int lvlReq = node.getNextLvlReq();
        boolean meetsLvl = player.getLevel() >= lvlReq;
        String lvlText;
        if (meetsLvl) {
            lvlText = "Requires Level " + lvlReq + " ✓";
        } else {
            lvlText = "Requires Level " + lvlReq
                    + " (you: " + player.getLevel() + ")";
        }
        JLabel lvlReqLbl = new JLabel(lvlText);
        lvlReqLbl.setFont(UIHelpers.FONT_SMALL);
        lvlReqLbl.setForeground(meetsLvl ? new Color(100, 200, 100)
                : UIHelpers.ACCENT_RED);
        lvlReqLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lvlReqLbl);

        p.add(Box.createVerticalStrut(10));

        JLabel costHdr = new JLabel("Cost:");
        costHdr.setFont(UIHelpers.FONT_SMALL.deriveFont(Font.BOLD));
        costHdr.setForeground(UIHelpers.TEXT_DIM);
        costHdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(costHdr);

        Map<Resource, Integer> cost = node.getNextCost();
        if (cost != null) {
            for (Map.Entry<Resource, Integer> entry : cost.entrySet()) {
                int have = player.getInventory().getOrDefault(entry.getKey(), 0);
                boolean enough = have >= entry.getValue();
                String line = "  " + entry.getKey().getName()
                        + " x" + entry.getValue()
                        + "  (have: " + have + ")";
                JLabel costLbl = new JLabel(line);
                costLbl.setFont(UIHelpers.FONT_SMALL);
                costLbl.setForeground(enough ? UIHelpers.TEXT_LIGHT
                        : UIHelpers.ACCENT_RED);
                costLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                p.add(costLbl);
            }
        }

        p.add(Box.createVerticalStrut(16));

        JButton upgrade = UIHelpers.makeButton("UPGRADE", 220, 46,
                new UpgradeListener(node));
        upgrade.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(upgrade);
        return p;
    }

    private class NodeClickHandler extends MouseAdapter {
        @Override
        /**
         * Handles the mouse clicked behavior.
         *
         * @param e the e value
         */
        public void mouseClicked(MouseEvent e) {
            int mx = e.getX();
            int my = e.getY();
            Point hub = nodePositions.get("WEAPON");
            if (hub != null) {
                int dx = mx - hub.x, dy = my - hub.y;
                if (dx * dx + dy * dy <= HUB_RADIUS * HUB_RADIUS) {
                    selectedNode = tree.getNode("WEAPON");
                    rebuildDetailPanel();
                    canvas.repaint();
                    return;
                }
            }
            for (Map.Entry<String, Point> entry : nodePositions.entrySet()) {
                if ("WEAPON".equals(entry.getKey())) continue;
                Point p = entry.getValue();
                int dx = mx - p.x, dy = my - p.y;
                if (dx * dx + dy * dy <= NODE_RADIUS * NODE_RADIUS) {
                    selectedNode = tree.getNode(entry.getKey());
                    rebuildDetailPanel();
                    canvas.repaint();
                    return;
                }
            }
        }
    }

    private class UpgradeListener implements ActionListener {
        private final SkillNode node;
        public UpgradeListener(SkillNode node) { this.node = node; }

        @Override
        /**
         * Handles the action performed behavior.
         *
         * @param e the e value
         */
        public void actionPerformed(ActionEvent e) {
            SkillNode.UpgradeResult result = node.tryUpgrade(player);
            String msg;
            switch (result) {
                case SUCCESS:
                    SoundPlayer.play("Sounds/LevelUp.wav");
                    owner.refreshCharacterScreen();
                    canvas.repaint();
                    rebuildDetailPanel();
                    return;
                case LEVEL_TOO_LOW:
                    msg = "Requires Level " + node.getNextLvlReq() + ".";
                    break;
                case NOT_ENOUGH_RESOURCES:
                    msg = "Not enough resources.";
                    break;
                case ALREADY_MAXED:
                    msg = "Already at maximum tier.";
                    break;
                default:
                    msg = "Cannot upgrade.";
            }
            JOptionPane.showMessageDialog(owner.getFrame(), msg,
                    "Cannot Upgrade", JOptionPane.WARNING_MESSAGE);
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
            owner.refreshCharacterScreen();
            owner.showCard(Screen.CARD_CHARACTER);
        }
    }
}