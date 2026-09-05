import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * StartScreen — title screen with background image, name input, and START button.
 *
 * The Screen orchestrator passes itself in so this class can switch cards
 * without holding a direct reference to CardLayout details.
 */
public class StartScreen {

    private final Screen owner;
    private final Player player;
    private JPanel panel;

    /**
     * Creates a new StartScreen object.
     *
     * @param owner the owner value
     * @param player the player value
     */
    public StartScreen(Screen owner, Player player) {
        this.owner  = owner;
        this.player = player;
    }

    /**
     * Builds the value.
     *
     * @return the value component
     */
    public JPanel build() {
        panel = new TitlePanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.gridy  = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("ROVENTURES");
        title.setFont(UIHelpers.FONT_TITLE);
        title.setForeground(UIHelpers.ACCENT_GOLD);
        panel.add(title, gbc);

        JLabel tagline = new JLabel("Wave Based RPG roguelike");
        tagline.setFont(UIHelpers.FONT_BODY);
        tagline.setForeground(UIHelpers.TEXT_DIM);
        panel.add(tagline, gbc);

        panel.add(Box.createVerticalStrut(20), gbc);

        JLabel nameLbl = new JLabel("Enter your name:");
        nameLbl.setFont(UIHelpers.FONT_SUBHEAD);
        nameLbl.setForeground(UIHelpers.TEXT_LIGHT);
        panel.add(nameLbl, gbc);

        final JTextField nameField = new StyledNameField();
        panel.add(nameField, gbc);

        panel.add(Box.createVerticalStrut(6), gbc);

        JButton startBtn = UIHelpers.makeButton("START GAME", 240, 56,
                new StartListener(nameField));
        panel.add(startBtn, gbc);

        return panel;
    }

    private static class TitlePanel extends JPanel {
        private final Image bg = UIHelpers.loadSprite("StartScreen.png");

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
                Graphics2D g2 = (Graphics2D) g;
                RadialGradientPaint vignette = new RadialGradientPaint(
                        getWidth() / 2f, getHeight() / 2f,
                        Math.max(getWidth(), getHeight()) * 0.75f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(0,0,0,0), new Color(0,0,0,200)});
                g2.setPaint(vignette);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g.setColor(UIHelpers.BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private static class StyledNameField extends JTextField {
        /**
         * Creates a new StyledNameField object.
         */
        public StyledNameField() {
            super("Hero", 14);
            setOpaque(false);
            setForeground(UIHelpers.TEXT_LIGHT);
            setCaretColor(UIHelpers.ACCENT_GOLD);
            setFont(UIHelpers.FONT_BTN);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIHelpers.ACCENT_GOLD, 1),
                    new EmptyBorder(6, 12, 6, 12)));
            setPreferredSize(new Dimension(240, 44));
        }

        @Override
        /**
         * Handles the paint component behavior.
         *
         * @param g the g value
         */
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(18, 16, 30));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            super.paintComponent(g);
        }
    }

    private class StartListener implements ActionListener {
        private final JTextField nameField;
        /**
         * Creates a new StartListener object.
         *
         * @param nameField the name field value
         */
        public StartListener(JTextField nameField) {
            this.nameField = nameField;
        }
        @Override
        /**
         * Handles the action performed behavior.
         *
         * @param e the e value
         */
        public void actionPerformed(ActionEvent e) {
            String n = nameField.getText().trim();
            if (n.isEmpty()) n = "Hero";
            player.setName(n);
            player.resetForNewGame();
            owner.getSkillTree().resetAll();
            owner.setCurrentWave(1);
            owner.refreshAfterReset();
            owner.showCard(Screen.CARD_CHARACTER);
        }
    }
}