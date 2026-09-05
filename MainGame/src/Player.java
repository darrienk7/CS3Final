import java.util.HashMap;
import java.util.Map;
/**
 * Represents the player part of the game.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class Player extends Entity {

    private double exp         = 0;
    private int    level       = 1;
    private int    skillPoints = 0;


    private static final int XP_PER_LEVEL = 100;

    private Weapon weapon;
    private int    weaponTier = 1;


    private final Map<Resource, Integer> inventory = new HashMap<>();


    private final Map<Resource, Integer> sessionGains = new HashMap<>();

    private float bonusDamage  = 0f;
    private float bonusDefense = 0f;
    private float bonusSpeed   = 0f;
    private float resourceGainMultiplier = 1.0f;


    private boolean isDead = false;

    private static final float BASE_HP      = 100f;
    private static final float BASE_ATTACK  =  12f;
    private static final float BASE_DEFENSE =   2f;
    private static final float BASE_SPEED   =   1.0f;
    private static final float MAX_EFFECTIVE_SPEED = 15.0f;


    /**
     * Creates a new Player object.
     *
     * @param name the name value
     */
    public Player(String name) {
        super(name, BASE_HP, BASE_ATTACK, BASE_DEFENSE, BASE_SPEED,
                "Sprites/Player.png");
        this.weapon = Weapon.tier1();
    }


    /**
     * Handles the perform attack behavior.
     *
     * @param target the target value
     *
     * @return the perform attack value
     */
    public float performAttack(Entity target) {
        float raw = getTotalAttack() * weapon.getDamageMultiplier();
        return attack(target, raw);
    }

    /**
     * Returns the effective speed.
     *
     * @return the effective speed
     */
    public float getEffectiveSpeed() {
        return Math.min(MAX_EFFECTIVE_SPEED, speed + weapon.getSpeedModifier() + bonusSpeed);
    }



    /**
     * Adds the experience.
     *
     * @param amount the amount value
     */
    public void addExperience(double amount) {
        exp += amount;
        checkLevelUp();
    }

    /**
     * Handles the check level up behavior.
     */
    private void checkLevelUp() {
        int xpNeeded = level * XP_PER_LEVEL;
        while (exp >= xpNeeded) {
            exp      -= xpNeeded;
            level++;
            skillPoints++;
            onLevelUp();
            xpNeeded  = level * XP_PER_LEVEL;
        }
    }

    /**
     * Handles the on level up behavior.
     */
    private void onLevelUp() {
        maxHp   += 14f;
        hp       = maxHp;          //level up makes player health go to max
        attack  += 3.0f;
        defense += 0.8f;
        SoundPlayer.play("Sounds/Heal.wav");
        System.out.printf("[LEVEL UP] %s is now level %d! HP fully restored.%n", name, level);
    }


    /**
     * Adds the resources.
     *
     * @param resources the resources value
     */
    public void addResources(Map<Resource, Integer> resources) {
        resources.forEach((res, qty) -> {
            int gained = Math.round(qty * resourceGainMultiplier);
            inventory.merge(res, gained, Integer::sum);
            sessionGains.merge(res, gained, Integer::sum);
        });
    }

    /**
     * Handles the clear session gains behavior.
     */
    public void clearSessionGains() {
        sessionGains.clear();
    }

    /**
     * Handles the spend resources behavior.
     *
     * @param cost the cost value
     *
     * @return true if the action succeeds, false otherwise
     */
    public boolean spendResources(Map<Resource, Integer> cost) {
        for (Map.Entry<Resource, Integer> e : cost.entrySet()) {
            int have = inventory.getOrDefault(e.getKey(), 0);
            if (have < e.getValue()) return false;
        }
        cost.forEach((res, qty) -> inventory.merge(res, -qty, Integer::sum));
        return true;
    }

    /**
     * Handles the use health orb behavior.
     *
     * @return true if the action succeeds, false otherwise
     */
    public boolean useHealthOrb() {
        Resource orb = WaveDefinition.HEALTH_ORB;
        int have = inventory.getOrDefault(orb, 0);
        if (have <= 0 || hp >= maxHp) return false;
        inventory.merge(orb, -1, Integer::sum);
        if (inventory.getOrDefault(orb, 0) <= 0) inventory.remove(orb);
        heal(maxHp);
        SoundPlayer.play("Sounds/Heal.wav");
        return true;
    }


    /**
     * Handles the debug give all resources behavior.
     */
    public void debugGiveAllResources() {
        int amount = Integer.MAX_VALUE/2;
        maxHp = Float.MAX_VALUE;
        hp = maxHp;
        attack = Float.MAX_VALUE;
        defense = Float.MAX_VALUE;
        speed = Float.MAX_VALUE;
        inventory.put(WaveDefinition.FISH_SCALES.clone(), amount);
        inventory.put(WaveDefinition.RAW_FISH.clone(), amount);
        inventory.put(WaveDefinition.CRAB_CLAW.clone(), amount);
        inventory.put(WaveDefinition.ROYAL_CHITIN.clone(), amount);
        inventory.put(WaveDefinition.TIDE_CRYSTAL.clone(), amount);
        inventory.put(WaveDefinition.DEEP_KELP.clone(), amount);
        inventory.put(WaveDefinition.HEALTH_ORB.clone(), amount);
    }

    public void addBonusDamage(float amount)  { bonusDamage  += amount; }
    public void addBonusDefense(float amount) { bonusDefense += amount; defense += amount; }
    public void addBonusSpeed(float amount)   { bonusSpeed   += amount; }
    public void addMaxHp(float amount)        { maxHp += amount; hp += amount; }
    public void addResourceGainMultiplier(float delta) { resourceGainMultiplier += delta; }


    @Override
    /**
     * Updates the value.
     */
    public void update() {

    }

    @Override
    /**
     * Handles the on death behavior.
     */
    public void onDeath() {
        isDead = true;
    }

    /**
     * Resets the for new game.
     */
    public void resetForNewGame() {
        hp         = BASE_HP;
        maxHp      = BASE_HP;
        attack     = BASE_ATTACK;
        defense    = BASE_DEFENSE;
        speed      = BASE_SPEED;
        exp        = 0;
        level      = 1;
        skillPoints = 0;
        bonusDamage = 0f;
        bonusDefense = 0f;
        bonusSpeed  = 0f;
        resourceGainMultiplier = 1.0f;
        isDead      = false;
        weapon      = Weapon.tier1();
        weaponTier  = 1;
        inventory.clear();
        sessionGains.clear();
        nextActionTick = 1.0 / speed;
    }

    public double getExp()             { return exp; }
    public int getLevel()           { return level; }
    public Weapon getWeapon()          { return weapon; }
    public float  getBonusDamage()     { return bonusDamage; }
    public float  getTotalAttack()     { return attack + bonusDamage; }

    public Map<Resource, Integer> getInventory()    { return inventory; }
    public Map<Resource, Integer> getSessionGains() { return sessionGains; }

    public int getXpToNextLevel() { return level * XP_PER_LEVEL; }

    public void setName(String n)      { this.name = n; }

    /**
     * Handles the upgrade weapon tier behavior.
     *
     * @param tier the tier value
     */
    public void upgradeWeaponTier(int tier) {
        this.weaponTier = tier;
        this.weapon     = Weapon.forTier(tier);
    }
}
