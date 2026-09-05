import javax.swing.SwingUtilities;
/**
 * Launches the game application.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class GameRunner {
    /**
     * Starts the game application.
     *
     * @param args the args value
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Screen(new Player("Hero")));
    }
}
