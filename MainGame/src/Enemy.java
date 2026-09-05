import java.util.HashMap;
import java.util.Map;

/**
 * Represents the enemy part of the game.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */
public class Enemy extends Entity {

    private final float xpGive;
    private final Map<Resource, Integer> resGive;


    private final float startingHp;
    private final float startingAttack;
    private final float startingDefense;
    private final float startingSpeed;


    /**
     * Creates a new Enemy object.
     *
     * @param name the name value
     * @param hp the hp value
     * @param attack the attack value
     * @param defense the defense value
     * @param speed the speed value
     * @param imagePath the image path value
     * @param xpGive the xp give value
     * @param resGive the res give value
     */
    public Enemy(String name, float hp, float attack, float defense,
                 float speed, String imagePath,
                 float xpGive, Map<Resource, Integer> resGive) {
        super(name, hp, attack, defense, speed, imagePath);
        this.xpGive          = xpGive;
        this.resGive         = new HashMap<>(resGive);
        this.startingHp      = hp;
        this.startingAttack  = attack;
        this.startingDefense = defense;
        this.startingSpeed   = speed;
    }


    /**
     * Handles the choose action behavior.
     *
     * @param player the player value
     *
     * @return the choose action text
     */
    public String chooseAction(Player player) {
        float dmg = attack(player, attack);
        return String.format("%s attacks %s for %.0f damage!", name, player.getName(), dmg);
    }

    /**
     * Resets the for wave.
     */
    public void resetForWave() {
        this.hp             = startingHp;
        this.maxHp          = startingHp;
        this.attack         = startingAttack;
        this.defense        = startingDefense;
        this.speed          = startingSpeed;
        this.nextActionTick = 1.0 / startingSpeed;
    }


    @Override
    /**
     * Updates the value.
     *
     * @param onDeath the on death value
     */
    public void update() {  }

    @Override
    public void onDeath() {
        System.out.printf("%s has been defeated!%n", name);
    }

    public float                  getXpGive()  { return xpGive; }
    public Map<Resource, Integer> getResGive() { return resGive; }
}