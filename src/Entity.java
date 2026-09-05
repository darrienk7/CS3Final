import javax.swing.ImageIcon;
import java.awt.Image;

/**
 * Base class for all combatants (Player, Enemy).
 * Implements Comparable<Entity> so that a PriorityQueue sorts by
 * each entity's nextActionTick — whoever acts soonest goes first.
 */
public abstract class Entity implements Comparable<Entity> {

    protected String name;
    protected Image  sprite;


    protected float maxHp;
    protected float hp;
    protected float attack;
    protected float defense;
    protected float speed;

    protected double nextActionTick;

    /**
     * Creates a new Entity object.
     *
     * @param name the name value
     * @param hp the hp value
     * @param attack the attack value
     * @param defense the defense value
     * @param speed the speed value
     * @param imagePath the image path value
     */
    public Entity(String name, float hp, float attack, float defense,
                  float speed, String imagePath) {
        this.name           = name;
        this.maxHp          = hp;
        this.hp             = hp;
        this.attack         = attack;
        this.defense        = defense;
        this.speed          = Math.max(speed, 0.01f); // guard against /0
        this.nextActionTick = 1.0 / this.speed;       // first turn offset
        this.sprite         = loadSprite(imagePath);
    }

    @Override
    /**
     * Handles the compare to behavior.
     *
     * @param other the other value
     *
     * @return comparison value used to order entities
     */
    public int compareTo(Entity other) {
        return Double.compare(this.nextActionTick, other.nextActionTick);
    }

    /** Call after this entity acts to schedule its next action. */
    public void advanceTick() {
        this.nextActionTick += 1.0 / this.speed;
    }

    /**
     * Handles the attack behavior.
     *
     * @param target the target value
     * @param damage the damage value
     *
     * @return the attack value
     */
    public float attack(Entity target, float damage) {
        float reduced = Math.max(1f, damage - target.defense);
        target.takeDamage(reduced);
        return reduced;
    }

    /**
     * Handles the take damage behavior.
     *
     * @param damage the damage value
     */
    public void takeDamage(float damage) {
        this.hp = Math.max(0, this.hp - damage);
        if (this.hp <= 0) {
            onDeath();
        }
    }

    /**
     * Handles the heal behavior.
     *
     * @param amount the amount value
     */
    public void heal(float amount) {
        this.hp = Math.min(maxHp, this.hp + amount);
    }

    /**
     * Checks whether alive.
     *
     * @return true if alive, false otherwise
     */
    public boolean isAlive() {
        return this.hp > 0;
    }

    public String getName()           { return name; }
    public float  getHp()             { return hp; }
    public float  getMaxHp()          { return maxHp; }
    public float  getAttack()         { return attack; }
    public float  getDefense()        { return defense; }
    public float  getSpeed()          { return speed; }
    public Image  getSprite()         { return sprite; }

    public void setSpeed(float speed)     { this.speed = Math.max(speed, 0.01f); }


    /**
     * Updates the value.
     */
    public abstract void update();

    /**
     * Handles the on death behavior.
     */
    public abstract void onDeath();

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
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    /**
     * Handles the to string behavior.
     *
     * @return text description of this object
     */
    public String toString() {
        return String.format("%s [HP %.0f/%.0f | ATK %.1f | DEF %.1f | SPD %.2f]",
                name, hp, maxHp, attack, defense, speed);
    }
}