import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.IntConsumer;
/**
 * Builds and manages the battle screen.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class BattleScreen {

    private static final int PLAYER_SPRITE_W = 160;
    private static final int PLAYER_SPRITE_H = 220;
    private static final int PLAYER_X_PAD    = 60;

    private static final int ENEMY_SPRITE_W = 120;
    private static final int ENEMY_SPRITE_H = 160;
    private static final int ENEMY_X_PAD    = 80;
    private static final int MULTI_ENEMY_SHIFT_X = -70;
    private static final int MULTI_ENEMY_SHIFT_Y = -45;

    private static final int LUNGE_DISTANCE = 50;
    private static final int ANIM_TICK_MS   = 28;
    private static final int LUNGE_TICKS    = 7;
    private static final int HOLD_TICKS     = 5;
    private static final int RETURN_TICKS   = 7;

    private static final int LOG_HEIGHT     = 110;
    private static final int LOG_MAX_LINES  = 120;

    private static final Color LOG_BG = new Color(6, 4, 14);


    private final Player player;

    private Arena      arena;
    private JTextArea  battleLog;
    private JPanel     actionButtonPanel;
    private JLabel     xpLabel;
    private JLabel     waveLabel;

    private List<Enemy> currentEnemies = new ArrayList<>();

    private javax.swing.Timer animTimer;
    private int      playerOffsetX = 0;
    private int[]    enemyOffsetX  = new int[0];
    private int      flashIdx      = Integer.MIN_VALUE;
    private boolean  flashOn       = false;
    private int      animTick      = 0;
    private int      animPhase     = 0;   // 0=lunge,1=hold,2=return
    private boolean  animIsPlayer  = true;

    /**
     * Creates a new BattleScreen object.
     *
     * @param owner the owner value
     * @param player the player value
     */
    public BattleScreen(Screen owner, Player player) {
        this.player = player;
    }

    /**
     * Builds the value.
     *
     * @return the value component
     */
    public JPanel build() {
        ArenaRoot root = new ArenaRoot();
        root.setLayout(new BorderLayout());

        root.add(buildHud(),     BorderLayout.NORTH);

        arena = new Arena();
        root.add(arena, BorderLayout.CENTER);

        root.add(buildBottom(), BorderLayout.SOUTH);
        return root;
    }

    /**
     * Builds the hud.
     *
     * @return the hud component
     */
    private JPanel buildHud() {
        JPanel hud = new JPanel(new BorderLayout());
        hud.setBackground(new Color(0, 0, 0, 220));
        hud.setBorder(new EmptyBorder(8, 22, 8, 22));

        waveLabel = new JLabel("WAVE 1");
        waveLabel.setFont(UIHelpers.FONT_HEAD.deriveFont(Font.BOLD, 17f));
        waveLabel.setForeground(UIHelpers.ACCENT_GOLD);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(waveLabel);

        xpLabel = new JLabel();
        xpLabel.setFont(UIHelpers.FONT_BODY);
        xpLabel.setForeground(UIHelpers.TEXT_LIGHT);
        refreshXp();

        hud.add(left,          BorderLayout.WEST);
        hud.add(xpLabel,       BorderLayout.EAST);
        return hud;
    }

    /**
     * Builds the bottom.
     *
     * @return the bottom component
     */
    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(true);
        bottom.setBackground(LOG_BG);
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setOpaque(true);
        battleLog.setBackground(LOG_BG);
        battleLog.setForeground(new Color(200, 210, 220));
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBorder(new EmptyBorder(8, 16, 8, 16));

        JScrollPane scroll = new JScrollPane(battleLog,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, LOG_HEIGHT));
        scroll.setOpaque(true);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(LOG_BG);
        scroll.setBackground(LOG_BG);
        scroll.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0,
                new Color(80, 65, 30)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        actionButtonPanel.setOpaque(true);
        actionButtonPanel.setBackground(new Color(10, 8, 20));
        actionButtonPanel.setPreferredSize(new Dimension(0, 58));
        actionButtonPanel.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, new Color(55, 45, 80)));
        showWaiting();
        bottom.add(scroll,            BorderLayout.CENTER);
        bottom.add(actionButtonPanel, BorderLayout.SOUTH);
        return bottom;
    }


    /**
     * Resets the for wave.
     *
     * @param waveLabelText the wave label text value
     * @param enemies the enemies value
     */
    public void resetForWave(String waveLabelText, List<Enemy> enemies) {
        currentEnemies = enemies;
        enemyOffsetX   = new int[enemies.size()];
        playerOffsetX  = 0;
        flashOn        = false;
        waveLabel.setText(waveLabelText);
        battleLog.setText("");
        refreshXp();
        showWaiting();
        arena.repaint();
    }


    /**
     * Handles the append log behavior.
     *
     * @param msg the msg value
     */
    public void appendLog(String msg) {
        String text = battleLog.getText();
        String[] lines = text.split("\n", -1);
        if (lines.length > LOG_MAX_LINES) {
            StringBuilder sb = new StringBuilder();
            for (int i = lines.length - LOG_MAX_LINES; i < lines.length; i++) {
                sb.append(lines[i]).append("\n");
            }
            battleLog.setText(sb.toString());
        }
        battleLog.append(msg + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }


    /**
     * Handles the repaint arena behavior.
     *
     * @param refreshXp the refresh xp value
     */
    public void repaintArena() { arena.repaint(); }

    public void refreshXp() {
        if (xpLabel == null) return;
        xpLabel.setText(String.format("XP: %.0f / %d   |   Level %d",
                player.getExp(), player.getXpToNextLevel(), player.getLevel()));
    }

    /**
     * Shows the action buttons.
     *
     * @param targets the targets value
     * @param submit the submit value
     */
    public void showActionButtons(List<Enemy> targets, IntConsumer submit) {
        actionButtonPanel.removeAll();
        if (targets == null || targets.isEmpty()) {
            showWaiting();
            return;
        }
        JLabel prompt = new JLabel("YOUR TURN — Attack:");
        prompt.setFont(UIHelpers.FONT_BODY);
        prompt.setForeground(UIHelpers.ACCENT_GOLD);
        actionButtonPanel.add(prompt);

        FontMetrics fm = actionButtonPanel.getFontMetrics(UIHelpers.FONT_BTN);
        for (int i = 0; i < targets.size(); i++) {
            Enemy e = targets.get(i);
            String label = "Attack " + e.getName()
                    + "  (HP " + UIHelpers.formatBigNumber(e.getHp())
                    + " / " + UIHelpers.formatBigNumber(e.getMaxHp()) + ")";
            int btnW = Math.max(180, fm.stringWidth(label) + 48);
            JButton btn = UIHelpers.makeButton(label, btnW, 42,
                    new TargetClickListener(i, submit));
            actionButtonPanel.add(btn);
        }
        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }

    /**
     * Shows the waiting.
     */
    private void showWaiting() {
        actionButtonPanel.removeAll();
        JLabel w = new JLabel("Enemy's turn…");
        w.setFont(UIHelpers.FONT_BODY);
        w.setForeground(UIHelpers.TEXT_DIM);
        actionButtonPanel.add(w);
        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }


    /**
     * Handles the play animation behavior.
     *
     * @param type the type value
     * @param attacker the attacker value
     * @param target the target value
     * @param battle the battle value
     */
    public void playAnimation(BattleSystem.AnimationType type,
                              Entity attacker, Entity target,
                              BattleSystem battle) {
        int slot = -1;
        if (attacker instanceof Enemy) slot = currentEnemies.indexOf(attacker);
        else if (target instanceof Enemy) slot = currentEnemies.indexOf(target);

        animIsPlayer  = (type == BattleSystem.AnimationType.PLAYER_ATTACK);
        animTick      = 0;
        animPhase     = 0;
        playerOffsetX = 0;
        if (enemyOffsetX.length != currentEnemies.size()) {
            enemyOffsetX = new int[currentEnemies.size()];
        } else {
            Arrays.fill(enemyOffsetX, 0);
        }
        flashIdx = Integer.MIN_VALUE;
        flashOn  = false;

        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        animTimer = new javax.swing.Timer(ANIM_TICK_MS,
                new AnimationTick(slot, battle));
        animTimer.start();
    }

    /**
     * Handles the stop animation behavior.
     */
    public void stopAnimation() {
        if (animTimer != null) animTimer.stop();
        playerOffsetX = 0;
        Arrays.fill(enemyOffsetX, 0);
        flashOn = false;
    }


    private class AnimationTick implements ActionListener {
        private final int slot;
        private final BattleSystem battle;

        /**
         * Creates a new AnimationTick object.
         *
         * @param slot the slot value
         * @param battle the battle value
         */
        public AnimationTick(int slot, BattleSystem battle) {
            this.slot   = slot;
            this.battle = battle;
        }

        @Override
        /**
         * Handles the action performed behavior.
         *
         * @param ev the ev value
         */
        public void actionPerformed(ActionEvent ev) {
            animTick++;
            if (animPhase == 0) {       // LUNGE
                int offset = (int)((float) animTick / LUNGE_TICKS * LUNGE_DISTANCE);
                offset = Math.min(offset, LUNGE_DISTANCE);
                applyOffset(offset);
                if (animTick >= LUNGE_TICKS) { animPhase = 1; animTick = 0; }
            } else if (animPhase == 1) {  // HOLD + FLASH
                flashIdx = animIsPlayer ? slot : -1;
                flashOn  = (animTick % 2 == 0);
                if (animTick >= HOLD_TICKS) {
                    flashOn   = false;
                    animPhase = 2;
                    animTick  = 0;
                }
            } else {
                int offset = (int)((1f - (float) animTick / RETURN_TICKS)
                        * LUNGE_DISTANCE);
                offset = Math.max(0, offset);
                applyOffset(offset);
                if (animTick >= RETURN_TICKS) {
                    playerOffsetX = 0;
                    Arrays.fill(enemyOffsetX, 0);
                    flashOn = false;
                    animTimer.stop();
                    arena.repaint();
                    if (battle != null) battle.signalAnimationDone();
                    return;
                }
            }
            arena.repaint();
        }

        /**
         * Handles the apply offset behavior.
         *
         * @param offset the offset value
         */
        private void applyOffset(int offset) {
            if (animIsPlayer) {
                playerOffsetX = +offset;
            } else if (slot >= 0 && slot < enemyOffsetX.length) {
                enemyOffsetX[slot] = -offset;
            }
        }
    }


    /**
     * Returns the formation offsets.
     *
     * @param count the count value
     *
     * @return the formation offsets
     */
    private int[][] getFormationOffsets(int count) {
        int hx = ENEMY_SPRITE_W + 18;
        int vy = ENEMY_SPRITE_H + 20;

        switch (count) {
            case 1:
                return new int[][]{ {0, 0} };
            case 2:
                return new int[][]{
                        {0, -vy / 2},
                        {0, +vy / 2}
                };
            case 3:
                return new int[][]{
                        {     0,       -vy / 2},
                        {-hx / 2,  +vy / 2},
                        {+hx / 2,  +vy / 2}
                };
            case 4:
                return new int[][]{
                        {-hx / 2, -vy / 2},
                        {+hx / 2, -vy / 2},
                        {-hx / 2, +vy / 2},
                        {+hx / 2, +vy / 2}
                };
            case 5:
                return new int[][]{
                        {       0,       0},
                        {-hx / 2, -vy / 2},
                        {+hx / 2, -vy / 2},
                        {-hx / 2, +vy / 2},
                        {+hx / 2, +vy / 2}
                };
            default:
                // 3-column grid fallback for 6
                int cols = 3;
                int[][] offsets = new int[count][2];
                for (int i = 0; i < count; i++) {
                    int col = i % cols;
                    int row = i / cols;
                    int totalRows = (int) Math.ceil((double) count / cols);
                    offsets[i][0] = (col - (cols - 1) / 2) * hx;
                    offsets[i][1] = (row - (totalRows - 1) / 2) * vy;
                }
                return offsets;
        }
    }


    private class Arena extends JPanel {
        /**
         * Creates a new Arena object.
         */
        public Arena() {
            setOpaque(false);
            setToolTipText("");
        }

        @Override
        /**
         * Handles the paint component behavior.
         *
         * @param g the g value
         */
        protected void paintComponent(Graphics g) {
            drawPlayerSprite(g);
            drawEnemySprites(g);
        }

        @Override
        /**
         * Returns the tool tip text.
         *
         * @param event the event value
         *
         * @return the tool tip text
         */
        public String getToolTipText(MouseEvent event) {
            Enemy e = getEnemyAtPoint(event.getPoint());
            if (e == null) return null;
            return "<html>" + e.getName()
                    + "<br>HP: " + UIHelpers.formatBigNumber(e.getHp())
                    + " / " + UIHelpers.formatBigNumber(e.getMaxHp())
                    + "</html>";
        }

        /**
         * Draws the player sprite.
         *
         * @param g the g value
         */
        private void drawPlayerSprite(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int panelH = getHeight();
            int baseX  = PLAYER_X_PAD;
            int baseY  = (panelH - PLAYER_SPRITE_H) / 2 + 100;
            int drawX  = baseX + playerOffsetX;

            Image spr = player.getSprite();
            if (spr != null) {
                g2.drawImage(spr, drawX, baseY,
                        PLAYER_SPRITE_W, PLAYER_SPRITE_H, this);
            } else {
                g2.setColor(new Color(70, 70, 180, 200));
                g2.fillRoundRect(drawX, baseY,
                        PLAYER_SPRITE_W, PLAYER_SPRITE_H, 10, 10);
            }
            if (flashOn && flashIdx == -1) {
                g2.setColor(UIHelpers.FLASH_RED);
                g2.fillRoundRect(drawX, baseY,
                        PLAYER_SPRITE_W, PLAYER_SPRITE_H, 10, 10);
            }
            UIHelpers.drawHpBar(g2, baseX, baseY + PLAYER_SPRITE_H + 10,
                    PLAYER_SPRITE_W, player.getHp(), player.getMaxHp(),
                    player.getName());
        }

        /**
         * Draws the enemy sprites.
         *
         * @param g the g value
         */
        private void drawEnemySprites(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            if (currentEnemies.isEmpty()) return;

            int panelW = getWidth();
            int panelH = getHeight();
            int count  = currentEnemies.size();

            int anchorX = panelW - ENEMY_X_PAD - ENEMY_SPRITE_W;
            int anchorY = (panelH - ENEMY_SPRITE_H) / 2 + 150;
            if (count > 1) {
                anchorX += MULTI_ENEMY_SHIFT_X;
                anchorY += MULTI_ENEMY_SHIFT_Y;
            }

            if (enemyOffsetX.length != count) enemyOffsetX = new int[count];
            int[][] formation = getFormationOffsets(count);

            for (int i = 0; i < count; i++) {
                Enemy e = currentEnemies.get(i);
                if (!e.isAlive()) continue;     // dead enemies disappear

                int baseX = anchorX + formation[i][0];
                int baseY = anchorY + formation[i][1];
                int drawX = baseX + enemyOffsetX[i];

                Image spr = e.getSprite();
                if (spr != null) {
                    g2.drawImage(spr, drawX, baseY,
                            ENEMY_SPRITE_W, ENEMY_SPRITE_H, this);
                } else {
                    g2.setColor(new Color(150, 30, 30, 200));
                    g2.fillRoundRect(drawX, baseY,
                            ENEMY_SPRITE_W, ENEMY_SPRITE_H, 10, 10);
                }
                if (flashOn && flashIdx == i) {
                    g2.setColor(UIHelpers.FLASH_RED);
                    g2.fillRoundRect(drawX, baseY,
                            ENEMY_SPRITE_W, ENEMY_SPRITE_H, 10, 10);
                }

                // Wider HP bar so the name+numbers fit
                int barX = baseX - 20;
                int barW = ENEMY_SPRITE_W + 40;
                UIHelpers.drawHpBar(g2, barX, baseY + ENEMY_SPRITE_H + 6,
                        barW, e.getHp(), e.getMaxHp(), e.getName());
            }
        }

        /**
         * Returns the enemy at point.
         *
         * @param p the p value
         *
         * @return the enemy at point
         */
        private Enemy getEnemyAtPoint(Point p) {
            for (int i = currentEnemies.size() - 1; i >= 0; i--) {
                Enemy e = currentEnemies.get(i);
                if (!e.isAlive()) continue;
                Rectangle bounds = getEnemyHoverBounds(i);
                if (bounds != null && bounds.contains(p)) return e;
            }
            return null;
        }

        /**
         * Returns the enemy hover bounds.
         *
         * @param index the index value
         *
         * @return the enemy hover bounds
         */
        private Rectangle getEnemyHoverBounds(int index) {
            int count = currentEnemies.size();
            if (index < 0 || index >= count) return null;

            int anchorX = getWidth() - ENEMY_X_PAD - ENEMY_SPRITE_W;
            int anchorY = (getHeight() - ENEMY_SPRITE_H) / 2 + 150;
            if (count > 1) {
                anchorX += MULTI_ENEMY_SHIFT_X;
                anchorY += MULTI_ENEMY_SHIFT_Y;
            }

            int[][] formation = getFormationOffsets(count);
            int baseX = anchorX + formation[index][0];
            int baseY = anchorY + formation[index][1];
            int drawX = baseX + enemyOffsetX[index];

            int barX = baseX - 20;
            int barY = baseY + ENEMY_SPRITE_H + 6;
            int barW = ENEMY_SPRITE_W + 40;
            int barH = 24;

            Rectangle spriteBounds = new Rectangle(drawX, baseY,
                    ENEMY_SPRITE_W, ENEMY_SPRITE_H);
            Rectangle barBounds = new Rectangle(barX, barY, barW, barH);
            return spriteBounds.union(barBounds);
        }
    }

    private static class ArenaRoot extends JPanel {
        private final Image bg = UIHelpers.loadSprite("BattleScreen.png");

        @Override
        /**
         * Handles the paint component behavior.
         *
         * @param g the g value
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0, 0, 0, 80));
                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g.setColor(new Color(8, 5, 14));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }


    private class TargetClickListener implements ActionListener {
        private final int          targetIndex;
        private final IntConsumer  submit;

        /**
         * Creates a new TargetClickListener object.
         *
         * @param targetIndex the target index value
         * @param submit the submit value
         */
        public TargetClickListener(int targetIndex, IntConsumer submit) {
            this.targetIndex = targetIndex;
            this.submit      = submit;
        }

        @Override
        /**
         * Handles the action performed behavior.
         *
         * @param e the e value
         */
        public void actionPerformed(ActionEvent e) {
            showWaiting();
            submit.accept(targetIndex);
        }
    }
}
