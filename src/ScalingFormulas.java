import java.util.HashMap;
import java.util.Map;

/**
 * The class to contain all the formulas to balance the game
 * and keep progression at a good pace
 */
public class ScalingFormulas {

    private static final float BASE_HP      = 30f;
    private static final float BASE_ATTACK  =  6f;
    private static final float BASE_DEFENSE =  1f;
    private static final float BASE_SPEED   =  0.8f;
    private static final float BASE_XP      = 20f;

    private static final double HP_GROWTH  = 1.075;
    private static final double ATK_GROWTH = 1.055;
    private static final double DEF_GROWTH = 1.035;
    private static final double COST_GROWTH = 1.55;

    public static final int  EARLY_BOSS_INTERVAL = 10;
    public static final int  LATE_BOSS_INTERVAL  = 25;
    public static final int  ANCIENT_BOSS_INTERVAL = 10;
    public static final int  LATE_GAME_THRESHOLD = 100;
    public static final int  ANCIENT_BOSS_THRESHOLD = 500;
    public static final double BOSS_REWARD_MULT  = 10.0;

    /**
     * Handles the enemy hp behavior.
     *
     * @param wave the wave value
     *
     * @return the enemy hp value
     */
    public static float enemyHp(int wave) {
        return (float)(BASE_HP * Math.pow(HP_GROWTH, wave - 1));
    }

    /**
     * Handles the enemy attack behavior.
     *
     * @param wave the wave value
     *
     * @return the enemy attack value
     */
    public static float enemyAttack(int wave) {
        return (float)(BASE_ATTACK * Math.pow(ATK_GROWTH, wave - 1));
    }

    /**
     * Handles the enemy defense behavior.
     *
     * @param wave the wave value
     *
     * @return the enemy defense value
     */
    public static float enemyDefense(int wave) {
        return (float)(BASE_DEFENSE * Math.pow(DEF_GROWTH, wave - 1));
    }

    /**
     * Handles the enemy speed behavior.
     *
     * @param wave the wave value
     *
     * @return the enemy speed value
     */
    public static float enemySpeed(int wave) {
        return Math.min(3.0f, BASE_SPEED + wave * 0.01f);
    }

    /**
     * Handles the enemy xp behavior.
     *
     * @param wave the wave value
     *
     * @return the enemy xp value
     */
    public static float enemyXp(int wave) {
        double lateWaveBoost = 1.0;
        if (wave > LATE_GAME_THRESHOLD) {
            lateWaveBoost += Math.sqrt((wave - LATE_GAME_THRESHOLD) / 400.0);
        }
        return (float)(BASE_XP * wave * lateWaveBoost);
    }

    /**
     * Handles the resource drop amount behavior.
     *
     * @param wave the wave value
     * @param baseAmount the base amount value
     *
     * @return the resource drop amount number
     */
    public static int resourceDropAmount(int wave, int baseAmount) {
        double mult = Math.max(1.0, Math.sqrt(wave));
        return (int)Math.round(baseAmount * mult);
    }


    /**
     * Checks whether boss wave.
     *
     * @param wave the wave value
     *
     * @return true if boss wave, false otherwise
     */
    public static boolean isBossWave(int wave) {
        if (wave >= ANCIENT_BOSS_THRESHOLD) {
            return wave % ANCIENT_BOSS_INTERVAL == 0;
        }
        if (wave < LATE_GAME_THRESHOLD) {
            return wave % EARLY_BOSS_INTERVAL == 0;
        }
        return wave % LATE_BOSS_INTERVAL == 0;
    }

    /**
     * Checks whether final boss.
     *
     * @param wave the wave value
     *
     * @return true if final boss, false otherwise
     */
    public static boolean isFinalBoss(int wave) {
        return wave == LATE_GAME_THRESHOLD
                || (wave >= ANCIENT_BOSS_THRESHOLD
                && wave % ANCIENT_BOSS_INTERVAL == 0);
    }


    /**
     * Handles the enemy count for wave behavior.
     *
     * @param wave the wave value
     *
     * @return the enemy count for wave number
     */
    public static int enemyCountForWave(int wave) {
        if (isBossWave(wave)) {
            return 1;
        }
        if (wave >= 100) return 6;
        if (wave >= 50)  return 5;
        if (wave >= 35)  return 4;
        if (wave >= 25)  return 3;
        if (wave > 10)   return 2;
        return 1;
    }

    /**
     * Handles the reward multiplier behavior.
     *
     * @param wave the wave value
     *
     * @return the reward multiplier value
     */
    public static double rewardMultiplier(int wave) {
        if (isBossWave(wave)) {
            return wave < LATE_GAME_THRESHOLD ? BOSS_REWARD_MULT
                    : BOSS_REWARD_MULT * 2.0;
        }
        return 1.0;
    }


    /**
     * Handles the skill cost behavior.
     *
     * @param tier the tier value
     * @param baseCost the base cost value
     *
     * @return the skill cost number
     */
    public static int skillCost(int tier, int baseCost) {
        return (int)Math.round(baseCost * Math.pow(COST_GROWTH, tier - 1));
    }
    /**
     * Handles the skill level requirement behavior.
     *
     * @param tier the tier value
     *
     * @return the skill level requirement number
     */
    public static int skillLevelRequirement(int tier) {
        return 1 + (tier - 1) * 2;
    }

    /**
     * Handles the health bonus for tier behavior.
     *
     * @param baseBonus the base bonus value
     * @param tier the tier value
     *
     * @return the health bonus for tier value
     */
    public static float healthBonusForTier(float baseBonus, int tier) {
        return (float)(baseBonus * (1.0 + 0.18 * (tier - 1)));
    }
    /**
     * Handles the attack bonus for tier behavior.
     *
     * @param baseBonus the base bonus value
     * @param tier the tier value
     *
     * @return the attack bonus for tier value
     */
    public static float attackBonusForTier(float baseBonus, int tier) {
        return baseBonus * tier * 1.25f;
    }

    /**
     * Handles the defense bonus for tier behavior.
     *
     * @param baseBonus the base bonus value
     * @param tier the tier value
     *
     * @return the defense bonus for tier value
     */
    public static float defenseBonusForTier(float baseBonus, int tier) {
        return (float)(baseBonus * (1.0 + 0.15 * (tier - 1)));
    }
    /**
     * Handles the speed bonus for tier behavior.
     *
     * @param baseBonus the base bonus value
     * @param tier the tier value
     *
     * @return the speed bonus for tier value
     */
    public static float speedBonusForTier(float baseBonus, int tier) {
        return (float)(baseBonus * Math.sqrt(tier));
    }


    /**
     * Builds the cost.
     *
     * @param a the a value
     * @param qa the qa value
     *
     * @return the resource cost or loot map
     */
    public static Map<Resource, Integer> buildCost(Resource a, int qa) {
        Map<Resource, Integer> m = new HashMap<>();
        m.put(a.clone(), qa);
        return m;
    }

    /**
     * Builds the cost.
     *
     * @param a the a value
     * @param qa the qa value
     * @param b the b value
     * @param qb the qb value
     *
     * @return the resource cost or loot map
     */
    public static Map<Resource, Integer> buildCost(Resource a, int qa,
                                                   Resource b, int qb) {
        Map<Resource, Integer> m = new HashMap<>();
        m.put(a.clone(), qa);
        m.put(b.clone(), qb);
        return m;
    }
}
