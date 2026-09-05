import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * The Screen Class is the main class that contains
 * all the screen information
 * and is the class you run the game from
 */
public class Screen {

    public static final String CARD_START      = "START";
    public static final String CARD_CHARACTER  = "CHARACTER";
    public static final String CARD_BATTLE     = "BATTLE";
    public static final String CARD_GAME_OVER  = "GAME_OVER";
    public static final String CARD_INVENTORY  = "INVENTORY";
    public static final String CARD_SKILL_TREE = "SKILL_TREE";

    private JFrame     frame;
    private JPanel     cardPanel;
    private CardLayout cardLayout;
    private String     currentCard = CARD_START;
    private BattleScreen battleScreenView;

    private final Player         player;
    private final WaveDefinition waveCatalogue = new WaveDefinition();
    private final SkillTree      skillTree     = new SkillTree();
    private int                  currentWave   = 1;
    private BattleSystem         activeBattle  = null;

    private WaveDefinition lastWave = null;
    private int lastWaveNumber = 0;


    private List<Enemy> currentEnemies = new ArrayList<>();


    private javax.swing.Timer animTimer;

    private static final Color BG_DARK     = new Color( 10,  10,  18);
    private static final Color BG_PANEL    = new Color( 20,  20,  35);
    private static final Color BG_PANEL2   = new Color( 16,  14,  28);
    private static final Color ACCENT_GOLD = new Color(212, 175,  55);
    private static final Color TEXT_LIGHT  = new Color(230, 225, 210);
    private static final Color TEXT_DIM    = new Color(140, 130, 110);
    private static final Color BTN_BASE    = new Color( 28,  24,  44);
    private static final Color BTN_HOVER   = new Color( 55,  48,  82);

    private static final Font FONT_TITLE    = new Font("Georgia",    Font.BOLD,  52);
    private static final Font FONT_HEAD     = new Font("Georgia",    Font.BOLD,  22);
    private static final Font FONT_SUBHEAD  = new Font("Georgia",    Font.BOLD,  16);
    private static final Font FONT_BODY     = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font FONT_BTN      = new Font("Georgia",    Font.BOLD,  16);
    private static final Font FONT_STAT_LBL = new Font("Georgia",    Font.PLAIN, 17);
    private static final Font FONT_STAT_VAL = new Font("Georgia",    Font.BOLD,  20);
    private static final Font FONT_SMALL    = new Font("Monospaced", Font.PLAIN, 11);
    private static final String DEBUG_ADMIN_NAME = "adminDARRIEN727!";

    /**
     * Creates a new Screen object.
     *
     * @param player the player value
     */
    public Screen(Player player) {
        this.player = player;
        SwingUtilities.invokeLater(this::buildFrame);
    }

    /**
     * Builds the frame.
     */
    private void buildFrame() {
        frame = new JFrame("RoVentures");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG_DARK);

        battleScreenView = new BattleScreen(this, player);

        cardPanel.add(new StartScreen(this, player).build(), CARD_START);
        cardPanel.add(buildCharacterScreen(), CARD_CHARACTER);
        cardPanel.add(battleScreenView.build(), CARD_BATTLE);
        cardPanel.add(new GameOverScreen(this, player).build(), CARD_GAME_OVER);
        cardPanel.add(new JPanel(),           CARD_INVENTORY);
        cardPanel.add(new JPanel(),           CARD_SKILL_TREE);

        frame.add(cardPanel);
        frame.setVisible(true);
        installDebugHotkeys();

    }

    /**
     * Handles the install debug hotkeys behavior.
     */
    private void installDebugHotkeys() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(ev -> {
                    if (ev.getID() != KeyEvent.KEY_PRESSED) return false;
                    Component focus = KeyboardFocusManager
                            .getCurrentKeyboardFocusManager()
                            .getFocusOwner();
                    if (focus instanceof javax.swing.text.JTextComponent) return false;

                    if (ev.getKeyCode() == KeyEvent.VK_K) {
                        debugSkipToNextWave();
                        return true;
                    }
                    if (ev.getKeyCode() == KeyEvent.VK_L) {
                        debugGiveAllResources();
                        return true;
                    }
                    return false;
                });
    }

    /**
     * Handles the debug skip to next wave behavior.
     */
    private void debugSkipToNextWave() {
        if (!isDebugAdmin()) return;
        if (currentWave >= waveCatalogue.getTotalWaves()) return;
        int skippedWave = currentWave;
        boolean canRepeatSkippedWave = lastWave != null && lastWaveNumber == skippedWave;
        abortActiveBattle();
        currentWave++;
        refreshCharacterScreen();
        showSkippedWaveMessage(skippedWave, canRepeatSkippedWave);
        showCard(CARD_CHARACTER);
    }

    /**
     * Handles the debug give all resources behavior.
     */
    private void debugGiveAllResources() {
        if (!isDebugAdmin()) return;
        player.debugGiveAllResources();
        refreshCharacterScreen();
        if (CARD_INVENTORY.equals(currentCard)) {
            showInventory();
        }
    }

    /**
     * Checks whether debug admin.
     *
     * @return true if debug admin, false otherwise
     */
    private boolean isDebugAdmin() {
        return DEBUG_ADMIN_NAME.equals(player.getName());
    }

    /**
     * Shows the card.
     *
     * @param card the card value
     */
    public void showCard(String card) {
        if (CARD_INVENTORY.equals(card)) {
            showInventory();
            return;
        }
        if (CARD_SKILL_TREE.equals(card)) {
            showSkillTree();
            return;
        }
        displayCard(card);
    }

    /**
     * Handles the display card behavior.
     *
     * @param card the card value
     */
    private void displayCard(String card) {
        currentCard = card;
        cardLayout.show(cardPanel, card);
    }

    /**
     * Returns the frame.
     *
     * @return the frame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Returns the skill tree.
     *
     * @return the skill tree
     */
    public SkillTree getSkillTree() {
        return skillTree;
    }

    /**
     * Sets the current wave.
     *
     * @param wave the wave value
     */
    public void setCurrentWave(int wave) {
        currentWave = wave;
        if (wave == 1) {
            lastWave = null;
            lastWaveNumber = 0;
        }
    }

    /**
     * Refreshes the after reset.
     */
    public void refreshAfterReset() {
        resetCharacterScreenState();
        refreshCharacterScreen();
    }

    /**
     * Handles the abort active battle behavior.
     */
    public void abortActiveBattle() {
        if (activeBattle != null) {
            activeBattle.abort();
            activeBattle = null;
        }
        if (animTimer != null) animTimer.stop();
        if (battleScreenView != null) battleScreenView.stopAnimation();
    }

    private JLabel charScreenTitle, charWaveVal, charNameLabel;
    private JLabel charLvlVal, charHpVal, charAtkVal, charDefVal, charSpdVal, charExpVal;
    private JPanel charRewardBox, charBtnPanel;
    private JLabel charWeaponName, charWeaponStats;

    /**
     * Builds the character screen.
     *
     * @return the character screen component
     */
    private JPanel buildCharacterScreen() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
                // Subtle diagonal grain lines
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(255,255,255,5));
                for (int i = -getHeight(); i < getWidth()+getHeight(); i += 32)
                    g2.drawLine(i, 0, i+getHeight(), getHeight());
            }
        };
        root.setName(CARD_CHARACTER);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(true);
        top.setBackground(new Color(0, 0, 0));
        top.setBorder(new EmptyBorder(12, 28, 12, 28));

        charScreenTitle = new JLabel("CHARACTER");
        charScreenTitle.setFont(FONT_TITLE.deriveFont(32f));
        charScreenTitle.setForeground(TEXT_LIGHT);
        top.add(charScreenTitle, BorderLayout.WEST);

        charWaveVal = new JLabel("CURRENT WAVE 1");
        charWaveVal.setFont(FONT_SUBHEAD);
        charWaveVal.setForeground(ACCENT_GOLD);
        top.add(charWaveVal, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        centre.setBorder(new EmptyBorder(28, 36, 20, 36));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(0, 14, 0, 14);
        g.weighty = 1.0;

        g.gridx = 0; g.weightx = 0.22;
        centre.add(buildCharSpritePanel(), g);

        g.gridx = 1; g.weightx = 0.40;
        centre.add(buildStatPanel(), g);

        g.gridx = 2; g.weightx = 0.38;
        charBtnPanel = buildCharButtonPanel();
        centre.add(charBtnPanel, g);

        root.add(centre, BorderLayout.CENTER);
        charRewardBox = buildRewardBox();
        charRewardBox.setVisible(false);
        root.add(charRewardBox, BorderLayout.SOUTH);

        return root;
    }

    /**
     * Builds the char sprite panel.
     *
     * @return the char sprite panel component
     */
    private JPanel buildCharSpritePanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);


        JPanel spriteBox = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 48, 100, 55));
                g2.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 20, 20);
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(ACCENT_GOLD);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 16, 16);
            }
        };
        spriteBox.setOpaque(false);
        spriteBox.setPreferredSize(new Dimension(220, 340));

        Image spr = loadSprite("Sprites/Player.png");
        JLabel imgLbl = (spr != null)
                ? new JLabel(new ImageIcon(spr.getScaledInstance(185, 260, Image.SCALE_SMOOTH)))
                : new JLabel("[Player]", SwingConstants.CENTER);
        imgLbl.setForeground(TEXT_DIM);
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setBorder(new EmptyBorder(14, 0, 6, 0));
        spriteBox.add(imgLbl, BorderLayout.CENTER);

        charNameLabel = new JLabel(player.getName(), SwingConstants.CENTER);
        charNameLabel.setFont(FONT_HEAD);
        charNameLabel.setForeground(ACCENT_GOLD);
        charNameLabel.setBorder(new EmptyBorder(4, 0, 12, 0));
        spriteBox.add(charNameLabel, BorderLayout.SOUTH);


        JPanel weaponBox = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(100, 80, 30, 160));  // amber border for equipment
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 12, 12);
            }
        };
        weaponBox.setOpaque(false);
        weaponBox.setBorder(new EmptyBorder(10, 12, 10, 12));
        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        textPanel.setOpaque(false);

        charWeaponName = new JLabel(player.getWeapon().getName());
        charWeaponName.setFont(FONT_SUBHEAD);
        charWeaponName.setForeground(ACCENT_GOLD);

        charWeaponStats = new JLabel(weaponStatsLine());
        charWeaponStats.setFont(FONT_SMALL);
        charWeaponStats.setForeground(TEXT_DIM);

        JLabel descLbl = new JLabel("<html><i>" + player.getWeapon().getDescription() + "</i></html>");
        descLbl.setFont(FONT_SMALL);
        descLbl.setForeground(new Color(120, 110, 90));

        textPanel.add(charWeaponName);
        textPanel.add(charWeaponStats);
        textPanel.add(descLbl);

        weaponBox.add(textPanel, BorderLayout.CENTER);

        outer.add(spriteBox,  BorderLayout.CENTER);
        outer.add(weaponBox,  BorderLayout.SOUTH);
        return outer;
    }

    /**
     * Handles the weapon stats line behavior.
     *
     * @return the weapon stats line text
     */
    private String weaponStatsLine() {
        Weapon w = player.getWeapon();
        String dmg = String.format("DMG ×%.1f", w.getDamageMultiplier());
        String spd = w.getSpeedModifier() == 0f ? ""
                : String.format("  SPD %+.2f", w.getSpeedModifier());
        return dmg + spd;
    }

    /**
     * Builds the stat panel.
     *
     * @return the stat panel component
     */
    private JPanel buildStatPanel() {
        JPanel outer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(80, 70, 120, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 16, 16);
            }
        };
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel rows = new JPanel(new GridLayout(6, 1, 0, 0));
        rows.setOpaque(false);

        charLvlVal = addStatRow(rows, "Level");
        charHpVal  = addStatRow(rows, "HP");
        charAtkVal = addStatRow(rows, "Attack");
        charDefVal = addStatRow(rows, "Defense");
        charSpdVal = addStatRow(rows, "Speed");
        charExpVal = addStatRow(rows, "EXP");

        outer.add(rows, BorderLayout.CENTER);
        return outer;
    }

    /**
     * Adds the stat row.
     *
     * @param parent the parent value
     * @param label the label value
     *
     * @return the created label
     */
    private JLabel addStatRow(JPanel parent, String label) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(80, 70, 120, 50));
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(11, 0, 11, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_STAT_LBL);
        lbl.setForeground(TEXT_DIM);

        JLabel val = new JLabel("—", SwingConstants.RIGHT);
        val.setFont(FONT_STAT_VAL);
        val.setForeground(TEXT_LIGHT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        parent.add(row);
        return val;
    }

    /**
     * Builds the char button panel.
     *
     * @return the char button panel component
     */
    private JPanel buildCharButtonPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JButton battleBtn = makeButton("GO TO NEXT WAVE", 300, 66);
        battleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        battleBtn.addActionListener(e -> startBattle(currentWave, false));

        JButton repeatBtn = makeButton("REPEAT WAVE", 300, 54);
        repeatBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        repeatBtn.setName("repeatBtn");
        repeatBtn.setVisible(false);
        repeatBtn.addActionListener(e -> startBattle(currentWave - 1, true));

        JButton healthOrbBtn = makeButton("", 300, 54);
        healthOrbBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        healthOrbBtn.setName("healthOrbBtn");
        healthOrbBtn.setVisible(false);
        healthOrbBtn.addActionListener(e -> useHealthOrbBetweenRounds());

        JButton invBtn = makeButton("INVENTORY", 300, 54);
        invBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        invBtn.addActionListener(e -> showInventory());

        JButton skillBtn = makeButton("SKILL TREE", 300, 54);
        skillBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        skillBtn.setName("skillBtn");
        skillBtn.addActionListener(e -> showSkillTree());

        inner.add(battleBtn);
        inner.add(Box.createVerticalStrut(18));
        inner.add(repeatBtn);
        inner.add(Box.createVerticalStrut(18));
        inner.add(healthOrbBtn);
        inner.add(Box.createVerticalStrut(18));
        inner.add(invBtn);
        inner.add(Box.createVerticalStrut(18));
        inner.add(skillBtn);

        wrap.add(inner);
        return wrap;
    }



    /**
     * Shows the inventory.
     */
    private void showInventory() {
        for (Component c : cardPanel.getComponents()) {
            if (CARD_INVENTORY.equals(c.getName())) {
                cardPanel.remove(c);
                break;
            }
        }
        JPanel fresh = new InventoryScreen(this, player).build();
        fresh.setName(CARD_INVENTORY);
        cardPanel.add(fresh, CARD_INVENTORY);
        cardPanel.revalidate();
        displayCard(CARD_INVENTORY);
    }


    /**
     * Shows the skill tree.
     */
    private void showSkillTree() {
        for (Component c : cardPanel.getComponents())
            if (CARD_SKILL_TREE.equals(c.getName())) { cardPanel.remove(c); break; }
        JPanel fresh = new SkillTreeScreen(this, player, skillTree).build();
        fresh.setName(CARD_SKILL_TREE);
        cardPanel.add(fresh, CARD_SKILL_TREE);
        cardPanel.revalidate();
        displayCard(CARD_SKILL_TREE);
    }

    /**
     * Builds the reward box.
     *
     * @return the reward box component
     */
    private JPanel buildRewardBox() {
        JPanel p = new JPanel(new BorderLayout(20, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(30, 26, 12, 235));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(ACCENT_GOLD);
                ((Graphics2D)g).setStroke(new BasicStroke(1.5f));
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 32, 14, 32));

        JLabel hdr = new JLabel("⚔  BATTLE REWARDS");
        hdr.setFont(FONT_HEAD);
        hdr.setForeground(ACCENT_GOLD);

        JTextArea body = new JTextArea();
        body.setName("rewardText");
        body.setEditable(false);
        body.setOpaque(false);
        body.setForeground(TEXT_LIGHT);
        body.setFont(FONT_BODY);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);

        p.add(hdr,  BorderLayout.WEST);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    /**
     * Refreshes the character screen.
     */
    public void refreshCharacterScreen() {
        charNameLabel.setText(player.getName());
        charLvlVal.setText(String.valueOf(player.getLevel()));
        charHpVal.setText(UIHelpers.formatBigNumber(player.getHp())
                + " / " + UIHelpers.formatBigNumber(player.getMaxHp()));
        charAtkVal.setText(UIHelpers.formatBigNumber(player.getAttack() + player.getBonusDamage()));
        charDefVal.setText(UIHelpers.formatBigNumber(player.getDefense()));
        charSpdVal.setText(String.format("%.2f", player.getEffectiveSpeed()));
        charExpVal.setText(UIHelpers.formatBigNumber(player.getExp())
                + " / " + UIHelpers.formatBigNumber(player.getXpToNextLevel()));
        charWaveVal.setText("CURRENT WAVE " + currentWave);
        charWeaponName.setText(player.getWeapon().getName());
        charWeaponStats.setText(weaponStatsLine());
        updateHealthOrbButton();
        updateSkillTreeButton();
    }

    /**
     * Shows the rewards.
     *
     * @param summary the summary value
     */
    private void showRewards(String summary) {
        charScreenTitle.setText("WAVE COMPLETE!");
        charScreenTitle.setForeground(ACCENT_GOLD);
        charRewardBox.setVisible(true);
        for (Component c : charRewardBox.getComponents())
            if (c instanceof JTextArea) ((JTextArea) c).setText(summary);
        setRepeatVisible(true);
        setHealthOrbVisible(true);
        updateHealthOrbButton();
    }

    /**
     * Shows the skipped wave message.
     *
     * @param skippedWave the skipped wave value
     * @param canRepeatSkippedWave the can repeat skipped wave value
     */
    private void showSkippedWaveMessage(int skippedWave, boolean canRepeatSkippedWave) {
        charScreenTitle.setText("SKIPPED WAVE");
        charScreenTitle.setForeground(ACCENT_GOLD);
        charRewardBox.setVisible(true);
        String message = canRepeatSkippedWave
                ? "Skipped wave " + skippedWave + ". No rewards gained."
                : "SKIPPED WAVE, NO WAVE TO REPEAT";
        for (Component c : charRewardBox.getComponents())
            if (c instanceof JTextArea) ((JTextArea) c).setText(message);
        setRepeatVisible(canRepeatSkippedWave);
        setHealthOrbVisible(true);
        updateHealthOrbButton();
    }

    /**
     * Resets the character screen state.
     */
    private void resetCharacterScreenState() {
        charScreenTitle.setText("CHARACTER");
        charScreenTitle.setForeground(TEXT_LIGHT);
        charRewardBox.setVisible(false);
        setRepeatVisible(false);
        setHealthOrbVisible(false);
    }

    /**
     * Sets the repeat visible.
     *
     * @param v the v value
     */
    private void setRepeatVisible(boolean v) {
        setRepeatInPanel(charBtnPanel, v);
    }

    /**
     * Sets the health orb visible.
     *
     * @param v the v value
     */
    private void setHealthOrbVisible(boolean v) {
        setNamedButtonVisible(charBtnPanel, "healthOrbBtn", v);
    }

    /**
     * Sets the repeat in panel.
     *
     * @param c the c value
     * @param v the v value
     */
    private void setRepeatInPanel(Container c, boolean v) {
        setNamedButtonVisible(c, "repeatBtn", v);
    }

    /**
     * Sets the named button visible.
     *
     * @param c the c value
     * @param name the name value
     * @param v the v value
     */
    private void setNamedButtonVisible(Container c, String name, boolean v) {
        for (Component ch : c.getComponents()) {
            if (name.equals(ch.getName())) { ch.setVisible(v); return; }
            if (ch instanceof Container) setNamedButtonVisible((Container) ch, name, v);
        }
    }

    /**
     * Updates the health orb button.
     */
    private void updateHealthOrbButton() {
        JButton btn = findNamedButton(charBtnPanel, "healthOrbBtn");
        if (btn == null) return;
        int orbs = player.getInventory().getOrDefault(WaveDefinition.HEALTH_ORB, 0);
        btn.setText("USE HEALTH ORB (" + orbs + ")");
        btn.setEnabled(orbs > 0 && player.getHp() < player.getMaxHp());
    }

    /**
     * Updates the skill tree button.
     */
    private void updateSkillTreeButton() {
        JButton btn = findNamedButton(charBtnPanel, "skillBtn");
        if (btn == null) return;
        btn.setText(hasAvailableSkillUpgrade()
                ? "!   SKILL TREE   !"
                : "SKILL TREE");
    }

    /**
     * Checks whether available skill upgrade.
     *
     * @return true if available skill upgrade, false otherwise
     */
    private boolean hasAvailableSkillUpgrade() {
        for (SkillNode node : skillTree.getAllNodes()) {
            if (node.isMaxed()) continue;
            if (player.getLevel() < node.getNextLvlReq()) continue;
            if (canAfford(node.getNextCost())) return true;
        }
        return false;
    }

    /**
     * Handles the can afford behavior.
     *
     * @param cost the cost value
     *
     * @return true if afford, false otherwise
     */
    private boolean canAfford(Map<Resource, Integer> cost) {
        if (cost == null) return false;
        for (Map.Entry<Resource, Integer> entry : cost.entrySet()) {
            int have = player.getInventory().getOrDefault(entry.getKey(), 0);
            if (have < entry.getValue()) return false;
        }
        return true;
    }

    /**
     * Handles the find named button behavior.
     *
     * @param c the c value
     * @param name the name value
     *
     * @return the configured button
     */
    private JButton findNamedButton(Container c, String name) {
        for (Component ch : c.getComponents()) {
            if (name.equals(ch.getName()) && ch instanceof JButton) return (JButton) ch;
            if (ch instanceof Container) {
                JButton found = findNamedButton((Container) ch, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Handles the use health orb between rounds behavior.
     */
    private void useHealthOrbBetweenRounds() {
        if (!player.useHealthOrb()) return;
        refreshCharacterScreen();
        updateHealthOrbButton();
    }


    /**
     * Handles the start battle behavior.
     *
     * @param wave the wave value
     * @param repeat the repeat value
     */
    private void startBattle(int wave, boolean repeat) {
        if (wave < 1 || wave > waveCatalogue.getTotalWaves()) return;
        if (activeBattle != null) activeBattle.abort();
        if (animTimer   != null && animTimer.isRunning()) animTimer.stop();
        if (battleScreenView != null) battleScreenView.stopAnimation();

        WaveDefinition wd;
        if (repeat && lastWave != null && lastWaveNumber == wave) {
            wd = lastWave;
            wd.resetEnemies();
        } else if (repeat) {
            refreshCharacterScreen();
            showSkippedWaveMessage(wave, false);
            showCard(CARD_CHARACTER);
            return;
        } else {
            wd = waveCatalogue.getWave(wave);
            lastWave = wd;
            lastWaveNumber = wave;
        }

        currentEnemies = wd.getEnemies();

        battleScreenView.resetForWave("WAVE " + wave + " - " + wd.getDisplayName(),
                currentEnemies);
        showCard(CARD_BATTLE);

        activeBattle = new BattleSystem(player, currentEnemies, wave);

        activeBattle.setLogCallback(msg ->
                SwingUtilities.invokeLater(() -> battleScreenView.appendLog(msg)));

        activeBattle.setAnimationCallback((type, attacker, target) ->
                SwingUtilities.invokeLater(() ->
                        battleScreenView.playAnimation(type, attacker, target, activeBattle)));

        activeBattle.setPlayerActionCallback((living, submit) ->
                SwingUtilities.invokeLater(() -> {
                    battleScreenView.repaintArena();
                    battleScreenView.showActionButtons(living, submit);
                }));

        activeBattle.setOnXpChangedCallback(() ->
                SwingUtilities.invokeLater(() -> {
                    battleScreenView.refreshXp();
                    refreshCharacterScreen();
                }));

        activeBattle.setOnVictoryCallback(enemies ->
                SwingUtilities.invokeLater(() -> onBattleWon(wave, repeat)));

        activeBattle.setOnPlayerDeathCallback(() ->
                SwingUtilities.invokeLater(this::onPlayerDied));

        Thread t = new Thread(activeBattle, "Battle-" + wave);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Handles the on battle won behavior.
     *
     * @param waveJustFinished the wave just finished value
     * @param repeat the repeat value
     */
    private void onBattleWon(int waveJustFinished, boolean repeat) {
        SoundPlayer.play("Sounds/Victory.wav");
        if (animTimer != null) animTimer.stop();
        if (battleScreenView != null) battleScreenView.stopAnimation();

        Map<Resource, Integer> gains = player.getSessionGains();
        StringBuilder sb = new StringBuilder();
        gains.forEach((r, q) ->
                sb.append("  ").append(r.getName()).append(" ×").append(q).append("   "));
        String resources = !sb.isEmpty() ? sb.toString().trim() : "none";
        String summary = String.format("Resources:  %s        XP: %s / %s   |   Level %d",
                resources,
                UIHelpers.formatBigNumber(player.getExp()),
                UIHelpers.formatBigNumber(player.getXpToNextLevel()),
                player.getLevel());

        if (!repeat && waveJustFinished < waveCatalogue.getTotalWaves())
            currentWave = waveJustFinished + 1;

        refreshCharacterScreen();
        resetCharacterScreenState();
        showRewards(summary);
        showCard(CARD_CHARACTER);
    }

    /**
     * Handles the on player died behavior.
     */
    private void onPlayerDied() {
        SoundPlayer.play("Sounds/GameOver.wav");
        if (animTimer != null) animTimer.stop();
        if (battleScreenView != null) battleScreenView.stopAnimation();
        showCard(CARD_GAME_OVER);
    }

    /**
     * Creates the button.
     *
     * @param text the text value
     * @param w the w value
     * @param h the h value
     *
     * @return the configured button
     */
    private JButton makeButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? BTN_HOVER : BTN_BASE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(hov ? ACCENT_GOLD : new Color(100, 90, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_LIGHT);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    /**
     * Handles the load sprite behavior.
     *
     * @param path the path value
     *
     * @return the loaded image, or null if unavailable
     */
    private Image loadSprite(String path) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(path);
            if (url == null) return null;
            return new ImageIcon(url).getImage();
        } catch (Exception ignored) { return null; }
    }

}
