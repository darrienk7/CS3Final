/**
 * Represents the weapon part of the game.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class Weapon implements Cloneable {

    private final String name;
    private final int    tier;
    private final float  damageMultiplier;
    private final float  speedModifier;
    private final String description;
    private final int    splashTargets;
    private final float  splashChance;
    private final float  lingerDamage;

    /**
     * Creates a new Weapon object.
     *
     * @param name the name value
     * @param tier the tier value
     * @param damageMultiplier the damage multiplier value
     * @param speedModifier the speed modifier value
     * @param description the description value
     * @param splashTargets the splash targets value
     * @param splashChance the splash chance value
     * @param lingerDamage the linger damage value
     */
    public Weapon(String name, int tier, float damageMultiplier, float speedModifier,
                  String description, int splashTargets, float splashChance, float lingerDamage) {
        this.name             = name;
        this.tier             = tier;
        this.damageMultiplier = damageMultiplier;
        this.speedModifier    = speedModifier;
        this.description      = description;
        this.splashTargets    = splashTargets;
        this.splashChance     = splashChance;
        this.lingerDamage     = lingerDamage;
    }


    /**
     * Handles the tier1 behavior.
     *
     * @return the weapon for this tier
     */
    public static Weapon tier1() {
        return new Weapon("Wooden Sword", 1, 1.0f, 0.0f,
                "A shoddy sword carved from sea wood. Better than nothing.",
                0, 0.0f, 0f);
    }

    /**
     * Handles the tier2 behavior.
     *
     * @return the weapon for this tier
     */
    public static Weapon tier2() {
        return new Weapon("Iron Sword", 2, 1.6f, -0.05f,
                "A heavy iron blade. 30% chance to splash 1 adjacent enemy.",
                1, 0.30f, 0f);
    }

    /**
     * Handles the tier3 behavior.
     *
     * @return the weapon for this tier
     */
    public static Weapon tier3() {
        return new Weapon("Coral Blade", 3, 2.0f, 0.0f,
                "A worthy sharp coral blade. 55% chance to splash 1 adjacent foe.",
                1, 0.55f, 0f);
    }

    /**
     * Handles the tier4 behavior.
     *
     * @return the weapon for this tier
     */
    public static Weapon tier4() {
        return new Weapon("Tide Blade", 4, 2.4f, 0.05f,
                "Heavy, strong, and forged from tidal iron. 55% chance to splash 2 adjacent foes.",
                2, 0.55f, 0f);
    }

    /**
     * Handles the tier5 behavior.
     *
     * @return the weapon for this tier
     */
    public static Weapon tier5() {
        return new Weapon("Abyssal Edge", 5, 2.8f, 0.05f,
                "Crafted from the abyss. Guaranteed splash to 2 adjacent foes.",
                2, 1.0f, 0f);
    }

    /**
     * Handles the tier6 behavior.
     *
     * @return the weapon for this tier
     */
    public static Weapon tier6() {
        return new Weapon("Flamewall", 6, 3.2f, 0.10f,
                "Eternal blade embraced by the flames. Splash 4 foes + leaves lingering fire damage.",
                4, 1.0f, 120f);
    }

    /**
     * Handles the for tier behavior.
     *
     * @param tier the tier value
     *
     * @return the weapon for this tier
     */
    public static Weapon forTier(int tier) {
        return switch (tier) {
            case 1  -> tier1();
            case 2  -> tier2();
            case 3  -> tier3();
            case 4  -> tier4();
            case 5  -> tier5();
            case 6  -> tier6();
            default -> tier1();
        };
    }

    /**
     * Returns the name.
     *
     * @param getSplashTargets the get splash targets value
     *
     * @return the name
     */
    public String getName()             { return name; }
    public float  getDamageMultiplier() { return damageMultiplier; }
    /**
     * Returns the speed modifier.
     *
     * @param clone the clone value
     *
     * @return the result of the method
     */
    public float  getSpeedModifier()    { return speedModifier; }
    public String getDescription()      { return description; }
    public int    getSplashTargets()    { return splashTargets; }
    /**
     * Returns the splash chance.
     *
     * @param clone the clone value
     *
     * @return the splash chance
     */
    public float  getSplashChance()     { return splashChance; }
    public float  getLingerDamage()     { return lingerDamage; }
    public boolean hasSplash()          { return splashTargets > 0 && splashChance > 0f; }
    public boolean hasLinger()          { return lingerDamage > 0f; }

    @Override
    public Weapon clone() {
        try { return (Weapon) super.clone(); }
        catch (CloneNotSupportedException e) { throw new AssertionError(); }
    }

    @Override
    /**
     * Handles the to string behavior.
     *
     * @return text description of this object
     */
    public String toString() {
        return String.format("%s [T%d] (×%.1f dmg, splash:%d@%.0f%%)",
                name, tier, damageMultiplier, splashTargets, splashChance * 100);
    }
}
