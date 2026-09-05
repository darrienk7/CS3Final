import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Builds enemy waves, loot drops, and boss encounters.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class WaveDefinition {
    public static final Resource FISH_SCALES  = new Resource("Fish Scales");
    public static final Resource RAW_FISH     = new Resource("Raw Fish");
    public static final Resource CRAB_CLAW    = new Resource("Crab Claw");
    public static final Resource ROYAL_CHITIN = new Resource("Royal Chitin");
    public static final Resource TIDE_CRYSTAL = new Resource("Tide Crystal");
    public static final Resource DEEP_KELP    = new Resource("Deep Kelp");
    public static final Resource HEALTH_ORB   = new Resource("Health Orb");

    private static final double HEALTH_ORB_COMMON_CHANCE = 0.06;
    private static final double HEALTH_ORB_BOSS_CHANCE   = 0.25;
    private static final java.util.Random RNG = new java.util.Random();

    private int          waveNumber;
    private String       displayName;
    private List<Enemy>  enemies;
    private boolean      isBoss;

    /**
     * Handles the for wave behavior.
     *
     * @param wave the wave value
     *
     * @return the generated wave definition
     */
    public WaveDefinition forWave(int wave) {
        this.waveNumber = wave;
        this.isBoss     = ScalingFormulas.isBossWave(wave);
        this.enemies    = buildEnemiesForWave(wave);
        this.displayName = buildDisplayName(wave);
        return this;
    }

    /**
     * Builds the display name.
     *
     * @param wave the wave value
     *
     * @return the build display name text
     */
    private String buildDisplayName(int wave) {
        if (ScalingFormulas.isFinalBoss(wave)) return "Ancient King Crab";
        if (isBoss) return "Boss Wave";
        if (wave <= 10)  return "Shallows";
        if (wave <= 25)  return "The Reef";
        if (wave <= 50)  return "Kelp Forest";
        if (wave <= 100)  return "Dark Grotto";
        if (wave <= 250)  return "Monumental Depths";
        if (wave <= 500) return "Abyssal Trench";
        return "Beyond the Veil";
    }

    /**
     * Builds the enemies for wave.
     *
     * @param wave the wave value
     *
     * @return the list of matching items
     */
    private List<Enemy> buildEnemiesForWave(int wave) {
        List<Enemy> list = new ArrayList<>();

        if (ScalingFormulas.isFinalBoss(wave)) {
            list.add(makeAncientKingCrab(wave));
            return list;
        }
        if (isBoss) {
            list.add(makeRandomRegularBoss(wave));
            return list;
        }

        int maxCount = ScalingFormulas.enemyCountForWave(wave);
        int count = 1 + RNG.nextInt(maxCount);

        for (int i = 0; i < count; i++) {
            list.add(makeRandomNormalEnemy(wave));
        }
        return list;
    }

    /**
     * Creates the random normal enemy.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeRandomNormalEnemy(int wave) {
        List<EnemyFactory> pool = new ArrayList<>();
        pool.add(this::makeBasicFish);
        pool.add(this::makeMinnow);
        pool.add(this::makeSpeedyFish);
        if (wave >= 4)  pool.add(this::makeCrabMinion);
        if (wave >= 7)  pool.add(this::makeCrystalFish);
        if (wave >= 10) pool.add(this::makeArmouredFish);
        if (wave >= 14) pool.add(this::makeDeepCrab);
        if (wave >= 18) pool.add(this::makeReefBrute);
        if (wave >= 24) pool.add(this::makeArmoredCrab);
        if (wave >= ScalingFormulas.ANCIENT_BOSS_THRESHOLD) {
            pool.add(this::makeKingCrab);
            pool.add(this::makeTentacleBoss);
        }

        return pool.get(RNG.nextInt(pool.size())).make(wave);
    }

    /**
     * Creates the random regular boss.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeRandomRegularBoss(int wave) {
        return RNG.nextBoolean() ? makeKingCrab(wave) : makeTentacleBoss(wave);
    }

    @FunctionalInterface
    private interface EnemyFactory {
        /**
         * Creates the value.
         *
         * @param wave the wave value
         *
         * @return the created enemy
         */
        Enemy make(int wave);
    }


    /**
     * Creates the fish.
     *
     * @param name the name value
     * @param wave the wave value
     * @param hpMul the hp mul value
     * @param atkMul the atk mul value
     * @param defMul the def mul value
     * @param spdMul the spd mul value
     * @param xpMul the xp mul value
     * @param spritePath the sprite path value
     * @param loot the loot value
     *
     * @return the created enemy
     */
    private Enemy makeFish(String name, int wave, float hpMul, float atkMul,
                           float defMul, float spdMul, float xpMul,
                           String spritePath, Map<Resource, Integer> loot) {
        return new Enemy(
                name,
                ScalingFormulas.enemyHp(wave) * hpMul,
                ScalingFormulas.enemyAttack(wave) * atkMul,
                ScalingFormulas.enemyDefense(wave) * defMul,
                ScalingFormulas.enemySpeed(wave) * spdMul,
                spritePath,
                ScalingFormulas.enemyXp(wave) * xpMul,
                loot
        );
    }

    /**
     * Creates the basic fish.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeBasicFish(int wave) {
        return makeFish("Basic Fish", wave, 1f, 1f, 1f, 1f, 1f,
                "Sprites/BasicFish.png", basicLoot(wave));
    }

    /**
     * Creates the minnow.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeMinnow(int wave) {
        return makeFish("Minnow", wave, 1f, 1f, 1f, 1f, 1f,
                "Sprites/Minnow.png", basicLoot(wave));
    }

    /**
     * Creates the speedy fish.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeSpeedyFish(int wave) {
        return makeFish("Speedy Fish", wave, 0.75f, 0.85f, 0.5f, 1.3f, 0.9f,
                "Sprites/SpeedyFish.png", basicLoot(wave));
    }

    /**
     * Creates the armoured fish.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeArmouredFish(int wave) {
        return makeFish("Armored Fish", wave, 1.5f, 1.1f, 2f, 0.85f, 1.4f,
                "Sprites/ArmoredFish.png", armouredLoot(wave));
    }

    /**
     * Creates the crab minion.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeCrabMinion(int wave) {
        return makeFish("Crab Minion", wave, 0.75f, 1f, 1.8f, 1f, 1.05f,
                "Sprites/CrabMinion.png", crabLoot(wave));
    }

    /**
     * Creates the deep crab.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeDeepCrab(int wave) {
        return makeFish("Deep Crab", wave, 1.15f, 1.25f, 2.4f, 0.95f, 1.3f,
                "Sprites/DeepCrab.png", crabLoot(wave));
    }

    /**
     * Creates the crystal fish.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeCrystalFish(int wave) {
        return makeFish("Crystal Fish", wave, 0.45f, 1.9f, 0.35f, 1.15f, 1.25f,
                "Sprites/CrystalFish.png", crystalLoot(wave));
    }

    /**
     * Creates the reef brute.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeReefBrute(int wave) {
        return makeFish("Reef Brute", wave, 2.0f, 1.35f, 2.3f, 0.85f, 1.7f,
                "Sprites/ReefBrute.png", armouredLoot(wave));
    }

    /**
     * Creates the armored crab.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeArmoredCrab(int wave) {
        return makeFish("Armored Crab", wave, 1.45f, 1.35f, 3.0f, 0.7f, 1.55f,
                "Sprites/ArmoredCrab.png", crabLoot(wave));
    }


    /**
     * Creates the king crab.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeKingCrab(int wave) {
        double mult = ScalingFormulas.rewardMultiplier(wave);
        float hp  = ScalingFormulas.enemyHp(wave)      * 5f;
        float atk = ScalingFormulas.enemyAttack(wave)  * 1.4f;
        float def = ScalingFormulas.enemyDefense(wave) * 2.5f;
        float spd = ScalingFormulas.enemySpeed(wave)   * 0.7f;
        float xp  = (float)(ScalingFormulas.enemyXp(wave) * mult);
        Map<Resource, Integer> loot = bossLoot(wave, mult);
        return new Enemy("King Crab", hp, atk, def, spd,
                "Sprites/KingCrab.png", xp, loot);
    }

    /**
     * Creates the tentacle boss.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeTentacleBoss(int wave) {
        double mult = ScalingFormulas.rewardMultiplier(wave);
        float hp  = ScalingFormulas.enemyHp(wave)      * 4.0f;
        float atk = ScalingFormulas.enemyAttack(wave)  * 1.55f;
        float def = ScalingFormulas.enemyDefense(wave) * 2.0f;
        float spd = ScalingFormulas.enemySpeed(wave)   * 0.95f;
        float xp  = (float)(ScalingFormulas.enemyXp(wave) * mult);
        Map<Resource, Integer> loot = bossLoot(wave, mult);
        return new Enemy("Tentacle Boss", hp, atk, def, spd,
                "Sprites/TentaclesBoss.png", xp, loot);
    }

    /**
     * Creates the ancient king crab.
     *
     * @param wave the wave value
     *
     * @return the created enemy
     */
    private Enemy makeAncientKingCrab(int wave) {
        double mult = ScalingFormulas.rewardMultiplier(wave);
        float hp  = ScalingFormulas.enemyHp(wave)      * 12f;
        float atk = ScalingFormulas.enemyAttack(wave)  * 2.0f;
        float def = ScalingFormulas.enemyDefense(wave) * 3.5f;
        float spd = ScalingFormulas.enemySpeed(wave)   * 0.85f;
        float xp  = (float)(ScalingFormulas.enemyXp(wave) * mult * 2);
        Map<Resource, Integer> loot = ancientLoot(wave, mult);
        return new Enemy("Ancient King Crab", hp, atk, def, spd,
                "Sprites/AncientCrab.png", xp, loot);
    }

    /**
     * Handles the basic loot behavior.
     *
     * @param wave the wave value
     *
     * @return the resource cost or loot map
     */
    private Map<Resource, Integer> basicLoot(int wave) {
        Map<Resource, Integer> m = new HashMap<>();
        m.put(FISH_SCALES.clone(), ScalingFormulas.resourceDropAmount(wave, 1));
        if (wave >= 5)  m.put(RAW_FISH.clone(),  ScalingFormulas.resourceDropAmount(wave, 1));
        if (wave >= 8)  m.put(DEEP_KELP.clone(), 1);
        maybeAddHealthOrb(m, HEALTH_ORB_COMMON_CHANCE);
        return m;
    }

    /**
     * Handles the armoured loot behavior.
     *
     * @param wave the wave value
     *
     * @return the resource cost or loot map
     */
    private Map<Resource, Integer> armouredLoot(int wave) {
        Map<Resource, Integer> m = new HashMap<>();
        m.put(FISH_SCALES.clone(), ScalingFormulas.resourceDropAmount(wave, 2));
        m.put(RAW_FISH.clone(),    ScalingFormulas.resourceDropAmount(wave, 1));
        if (wave >= 8)  m.put(DEEP_KELP.clone(), ScalingFormulas.resourceDropAmount(wave, 1));
        if (wave >= 12) m.put(ROYAL_CHITIN.clone(), 1);
        maybeAddHealthOrb(m, HEALTH_ORB_COMMON_CHANCE);
        return m;
    }

    /**
     * Handles the crab loot behavior.
     *
     * @param wave the wave value
     *
     * @return the resource cost or loot map
     */
    private Map<Resource, Integer> crabLoot(int wave) {
        Map<Resource, Integer> m = new HashMap<>();
        m.put(CRAB_CLAW.clone(), ScalingFormulas.resourceDropAmount(wave, 1));
        if (wave >= 12) m.put(ROYAL_CHITIN.clone(), 1);
        if (wave >= 15) m.put(TIDE_CRYSTAL.clone(), 1);
        maybeAddHealthOrb(m, HEALTH_ORB_COMMON_CHANCE);
        return m;
    }

    /**
     * Handles the crystal loot behavior.
     *
     * @param wave the wave value
     *
     * @return the resource cost or loot map
     */
    private Map<Resource, Integer> crystalLoot(int wave) {
        Map<Resource, Integer> m = new HashMap<>();
        m.put(TIDE_CRYSTAL.clone(), 1);
        if (wave >= 12) m.put(DEEP_KELP.clone(), ScalingFormulas.resourceDropAmount(wave, 1));
        if (wave >= 18) m.put(ROYAL_CHITIN.clone(), 1);
        maybeAddHealthOrb(m, HEALTH_ORB_COMMON_CHANCE);
        return m;
    }

    /**
     * Handles the boss loot behavior.
     *
     * @param wave the wave value
     * @param mult the mult value
     *
     * @return the resource cost or loot map
     */
    private Map<Resource, Integer> bossLoot(int wave, double mult) {
        Map<Resource, Integer> m = new HashMap<>();
        m.put(CRAB_CLAW.clone(),    (int)(ScalingFormulas.resourceDropAmount(wave, 2) * mult));
        m.put(ROYAL_CHITIN.clone(), (int)(ScalingFormulas.resourceDropAmount(wave, 1) * mult));
        if (wave >= 10) m.put(TIDE_CRYSTAL.clone(), (int)(1 * mult));
        if (wave >= 8)  m.put(DEEP_KELP.clone(), (int)(ScalingFormulas.resourceDropAmount(wave, 1) * mult));
        maybeAddHealthOrb(m, HEALTH_ORB_BOSS_CHANCE);
        return m;
    }

    /**
     * Handles the ancient loot behavior.
     *
     * @param wave the wave value
     * @param mult the mult value
     *
     * @return the resource cost or loot map
     */
    private Map<Resource, Integer> ancientLoot(int wave, double mult) {
        Map<Resource, Integer> m = new HashMap<>();
        m.put(CRAB_CLAW.clone(),    (int)(ScalingFormulas.resourceDropAmount(wave, 5) * mult));
        m.put(ROYAL_CHITIN.clone(), (int)(ScalingFormulas.resourceDropAmount(wave, 3) * mult));
        m.put(TIDE_CRYSTAL.clone(), (int)(ScalingFormulas.resourceDropAmount(wave, 2) * mult));
        m.put(DEEP_KELP.clone(),    (int)(ScalingFormulas.resourceDropAmount(wave, 2) * mult));
        maybeAddHealthOrb(m, HEALTH_ORB_BOSS_CHANCE);
        return m;
    }

    /**
     * Handles the maybe add health orb behavior.
     *
     * @param loot the loot value
     * @param chance the chance value
     */
    private void maybeAddHealthOrb(Map<Resource, Integer> loot, double chance) {
        if (RNG.nextDouble() < chance) {
            loot.put(HEALTH_ORB.clone(), 1);
        }
    }

    /**
     * Returns the display name.
     *
     * @param getTotalWaves the get total waves value
     *
     * @return the display name
     */
    public String      getDisplayName() { return displayName; }
    public List<Enemy> getEnemies()     { return new ArrayList<>(enemies); }

    public int getTotalWaves() {
        return Integer.MAX_VALUE;
    }

    /**
     * Returns the wave.
     *
     * @param wave the wave value
     *
     * @return the wave
     */
    public WaveDefinition getWave(int wave) {
        return new WaveDefinition().forWave(wave);
    }

    /**
     * Resets the enemies.
     */
    public void resetEnemies() {
        for (Enemy e : enemies) e.resetForWave();
    }
}
