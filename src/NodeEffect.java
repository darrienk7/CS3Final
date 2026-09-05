/**
 * Represents the node effect part of the game.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */
public interface NodeEffect {
    /**
     * Handles the apply behavior.
     *
     * @param player the player value
     * @param tier the tier value
     */
    void apply(Player player, int tier);
}