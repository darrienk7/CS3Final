import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Builds and manages the game over screen.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */
public class GameOverScreen {

    private final Screen owner;
    private final Player player;

    /**
     * Creates a new GameOverScreen object.
     *
     * @param owner the owner value
     * @param player the player value
     */
    public GameOverScreen(Screen owner, Player player) {
        this.owner  = owner;
        this.player = player;
    }

    /**
     * Builds the value.
     *
     * @return the value component
     */
    public JPanel build() {
        BackgroundPanel panel = new BackgroundPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.gridy  = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(14, 0, 14, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("YOU DIED");
        title.setFont(UIHelpers.FONT_TITLE.deriveFont(Font.BOLD, 72f));
        title.setForeground(UIHelpers.ACCENT_RED);
        panel.add(title, gbc);

        JLabel sub = new JLabel("You fought well.");
        sub.setFont(UIHelpers.FONT_BODY);
        sub.setForeground(UIHelpers.TEXT_DIM);
        panel.add(sub, gbc);

        JButton restart = UIHelpers.makeButton("RESTART", 220, 54,
                new RestartListener());
        panel.add(restart, gbc);

        return panel;
    }

    private static class BackgroundPanel extends JPanel {
        @Override
        /**
         * Handles the paint component behavior.
         *
         * @param g the g value
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(8, 0, 0));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(160, 0, 0, 20));
            for (int y = 0; y < getHeight(); y += 4) {
                g.drawLine(0, y, getWidth(), y);
            }
        }
    }

    private class RestartListener implements ActionListener {
        @Override
        /**
         * Handles the action performed behavior.
         *
         * @param e the e value
         */
        public void actionPerformed(ActionEvent e) {
            owner.abortActiveBattle();
            player.resetForNewGame();
            owner.getSkillTree().resetAll();
            owner.setCurrentWave(1);
            owner.refreshAfterReset();
            owner.showCard(Screen.CARD_START);
        }
    }
}