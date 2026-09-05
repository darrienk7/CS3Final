import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * Represents the skill tree part of the game.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */

public class SkillTree {

    private static final Resource FISH_SCALES  = WaveDefinition.FISH_SCALES;
    private static final Resource RAW_FISH     = WaveDefinition.RAW_FISH;
    private static final Resource CRAB_CLAW    = WaveDefinition.CRAB_CLAW;
    private static final Resource ROYAL_CHITIN = WaveDefinition.ROYAL_CHITIN;
    private static final Resource TIDE_CRYSTAL = WaveDefinition.TIDE_CRYSTAL;
    private static final Resource DEEP_KELP    = WaveDefinition.DEEP_KELP;

    private final Map<String, SkillNode> nodes = new LinkedHashMap<>();

    /**
     * Creates a new SkillTree object.
     */
    public SkillTree() {
        buildNodes();
    }

    private static class HealthEffect implements NodeEffect {
        @Override
        /**
         * Handles the apply behavior.
         *
         * @param player the player value
         * @param tier the tier value
         */
        public void apply(Player player, int tier) {
            float bonus = ScalingFormulas.healthBonusForTier(30f, tier);
            player.addMaxHp(bonus);
        }
    }

    private static class AttackEffect implements NodeEffect {
        @Override
        /**
         * Handles the apply behavior.
         *
         * @param player the player value
         * @param tier the tier value
         */
        public void apply(Player player, int tier) {
            float bonus = ScalingFormulas.attackBonusForTier(4f, tier);
            player.addBonusDamage(bonus);
        }
    }

    private static class DefenseEffect implements NodeEffect {
        @Override
        /**
         * Handles the apply behavior.
         *
         * @param player the player value
         * @param tier the tier value
         */
        public void apply(Player player, int tier) {
            float bonus = ScalingFormulas.defenseBonusForTier(3f, tier);
            player.addBonusDefense(bonus);
        }
    }

    private static class SpeedEffect implements NodeEffect {
        @Override
        /**
         * Handles the apply behavior.
         *
         * @param player the player value
         * @param tier the tier value
         */
        public void apply(Player player, int tier) {
            float bonus = ScalingFormulas.speedBonusForTier(0.10f, tier);
            player.addBonusSpeed(bonus);
        }
    }

    private static class ResourceEffect implements NodeEffect {
        @Override
        /**
         * Handles the apply behavior.
         *
         * @param player the player value
         * @param tier the tier value
         */
        public void apply(Player player, int tier) {
            player.addResourceGainMultiplier(0.15f);
        }
    }

    private static class WeaponEffect implements NodeEffect {
        @Override
        /**
         * Handles the apply behavior.
         *
         * @param player the player value
         * @param tier the tier value
         */
        public void apply(Player player, int tier) {
            player.upgradeWeaponTier(tier + 1);
        }
    }

    /**
     * Builds the nodes.
     */
    private void buildNodes() {
        nodes.put("HEALTH", new SkillNode(
                "HEALTH", "Health", SkillNode.NodeType.HEALTH,
                FISH_SCALES, 3,        // base 3 scales scaled exponentially
                RAW_FISH,    1,         // and 1 raw fish scaled too
                new HealthEffect()
        ));


        nodes.put("ATTACK", new SkillNode(
                "ATTACK", "Attack", SkillNode.NodeType.ATTACK,
                FISH_SCALES, 4,
                DEEP_KELP,   1,
                new AttackEffect()
        ));

        nodes.put("DEFENSE", new SkillNode(
                "DEFENSE", "Defense", SkillNode.NodeType.DEFENSE,
                FISH_SCALES, 3,
                CRAB_CLAW,   1,
                new DefenseEffect()
        ));


        nodes.put("SPEED", new SkillNode(
                "SPEED", "Speed", SkillNode.NodeType.SPEED,
                FISH_SCALES, 6,
                DEEP_KELP,   1,
                new SpeedEffect()
        ));

        nodes.put("RESOURCE", new SkillNode(
                "RESOURCE", "Scavenger", SkillNode.NodeType.RESOURCE,
                FISH_SCALES, 5,
                null, 0,
                new ResourceEffect()
        ));

        String[] weaponTierNames = new String[]{
                "Iron Sword",
                "Coral Blade",
                "Tide Blade",
                "Abyssal Edge",
                "Flamewall"
        };
        Map<Resource, Integer>[] weaponCosts = new Map[]{
                ScalingFormulas.buildCost(FISH_SCALES, 15, CRAB_CLAW, 3),
                ScalingFormulas.buildCost(CRAB_CLAW,   8,  DEEP_KELP, 6),
                ScalingFormulas.buildCost(CRAB_CLAW,   15, ROYAL_CHITIN, 4),
                ScalingFormulas.buildCost(ROYAL_CHITIN, 10, TIDE_CRYSTAL, 5),
                ScalingFormulas.buildCost(TIDE_CRYSTAL, 15, ROYAL_CHITIN, 12)
        };
        nodes.put("WEAPON", new SkillNode(
                "WEAPON", "Weapon",
                weaponTierNames, weaponCosts,
                new WeaponEffect()
        ));
    }


    /**
     * Returns the node.
     *
     * @param id the id of the node
     *
     * @return the node
     */
    public SkillNode getNode(String id) { return nodes.get(id); }

    public List<SkillNode> getAllNodes() { return new ArrayList<>(nodes.values()); }

    public void resetAll() {
        for (SkillNode node : nodes.values()) {
            node.reset();
        }
    }
}
