import java.util.Map;
/**
 * Represents the skill node part of the game.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class SkillNode {

    public enum NodeType { ATTACK, DEFENSE, HEALTH, SPEED, WEAPON, RESOURCE }

    private final String     id;
    private final String     displayName;
    private final NodeType   type;
    private final NodeEffect effect;

    private final Resource baseResource;   // primary resource used in costs
    private final int      baseCost;       // tier-1 cost
    private final Resource secondaryRes;   // optional secondary (null = none)
    private final int      secondaryBase;  // tier-1 secondary cost

    private final int                       maxTier;       // -1 for infinite
    private final String[]                  tierNames;     // null if infinite
    private final Map<Resource, Integer>[]  fixedCosts;    // null if infinite

    private int currentTier = 0;

    /** Result codes returned from tryUpgrade(). */
    public enum UpgradeResult {
        SUCCESS,
        LEVEL_TOO_LOW,
        NOT_ENOUGH_RESOURCES,
        ALREADY_MAXED;

    }

    /**
     * Creates a new SkillNode object.
     *
     * @param id the id value
     * @param displayName the display name value
     * @param type the type value
     * @param baseResource the base resource value
     * @param baseCost the base cost value
     * @param secondaryRes the secondary res value
     * @param secondaryBase the secondary base value
     * @param effect the effect value
     */
    public SkillNode(String id, String displayName, NodeType type,
                     Resource baseResource, int baseCost,
                     Resource secondaryRes, int secondaryBase,
                     NodeEffect effect) {
        this.id           = id;
        this.displayName  = displayName;
        this.type         = type;
        this.effect       = effect;
        this.baseResource = baseResource;
        this.baseCost     = baseCost;
        this.secondaryRes = secondaryRes;
        this.secondaryBase = secondaryBase;
        this.maxTier      = -1;     // infinite
        this.tierNames    = null;
        this.fixedCosts   = null;
    }

    @SuppressWarnings("unchecked")
    /**
     * Creates a new SkillNode object.
     *
     * @param id the id value
     * @param displayName the display name value
     * @param tierNames the tier names value
     * @param fixedCosts the fixed costs value
     * @param effect the effect value
     */
    public SkillNode(String id, String displayName,
                     String[] tierNames,
                     Map<Resource, Integer>[] fixedCosts,
                     NodeEffect effect) {
        this.id           = id;
        this.displayName  = displayName;
        this.type         = NodeType.WEAPON;
        this.effect       = effect;
        this.baseResource = null;
        this.baseCost     = 0;
        this.secondaryRes = null;
        this.secondaryBase = 0;
        this.maxTier      = tierNames.length;
        this.tierNames    = tierNames;
        this.fixedCosts   = fixedCosts;
    }


    /**
     * Handles the try upgrade behavior.
     *
     * @param player the player value
     *
     * @return the try upgrade value
     */
    public UpgradeResult tryUpgrade(Player player) {
        if (isMaxed()) return UpgradeResult.ALREADY_MAXED;

        int nextTier = currentTier + 1;

        if (player.getLevel() < getNextLvlReq()) {
            return UpgradeResult.LEVEL_TOO_LOW;
        }

        Map<Resource, Integer> cost = getNextCost();
        if (!player.spendResources(cost)) {
            return UpgradeResult.NOT_ENOUGH_RESOURCES;
        }

        effect.apply(player, nextTier);
        currentTier = nextTier;
        return UpgradeResult.SUCCESS;
    }



    public boolean isInfinite() { return maxTier == -1; }
    public boolean isMaxed()    { return !isInfinite() && currentTier >= maxTier; }
    /**
     * Checks whether unlocked.
     *
     * @param getCurrentTierName the get current tier name value
     *
     * @return true if unlocked, false otherwise
     */
    public boolean isUnlocked() { return currentTier > 0; }
    public int     getCurrentTier() { return currentTier; }
    public String  getId()          { return id; }
    public String  getDisplayName() { return displayName; }
    public NodeType getType()       { return type; }

    public String getCurrentTierName() {
        if (currentTier == 0) return "Locked";
        if (tierNames != null) return tierNames[currentTier - 1];
        return "Tier " + currentTier;
    }

    /**
     * Returns the next tier name.
     *
     * @return the next tier name
     */
    public String getNextTierName() {
        if (isMaxed()) return null;
        if (tierNames != null) return tierNames[currentTier];
        return "Tier " + (currentTier + 1);
    }


    /**
     * Returns the next lvl req.
     *
     * @return the next lvl req
     */
    public int getNextLvlReq() {
        if (isMaxed()) return -1;
        return ScalingFormulas.skillLevelRequirement(currentTier + 1);
    }

    /**
     * Returns the next cost.
     *
     * @return the next cost
     */
    public Map<Resource, Integer> getNextCost() {
        if (isMaxed()) return null;
        if (fixedCosts != null) {
            return fixedCosts[currentTier];
        }
        int nextTier = currentTier + 1;
        int primary  = ScalingFormulas.skillCost(nextTier, baseCost);
        if (secondaryRes == null) {
            return ScalingFormulas.buildCost(baseResource, primary);
        }
        int secondary = ScalingFormulas.skillCost(nextTier, secondaryBase);
        return ScalingFormulas.buildCost(baseResource, primary,
                secondaryRes, secondary);
    }

    public void reset() { currentTier = 0; }
}
