import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Runs the battle loop and combat rules.
 *
 * Author: Darrien Kwan
 * Collaborators: Lucas Silva, Cowen Chen
 * Period: 3
 * Due Date: 5/10/26
 * Teacher: Bailey
 */
public class BattleSystem implements Runnable {


    private final Player      player;
    private final List<Enemy> enemies;
    private final int         waveNumber;


    private final java.util.Map<Enemy, Integer> lingerTurns = new java.util.HashMap<>();
    private static final int LINGER_DURATION = 2;
    private static final java.util.Random RNG = new java.util.Random();


    private final PriorityQueue<Entity> turnQueue = new PriorityQueue<>();


    private PlayerActionCallback     playerActionCallback;
    private AnimationCallback        animationCallback;
    private Consumer<String>         logCallback;
    private Consumer<List<Enemy>>    onVictoryCallback;
    private Runnable                 onPlayerDeathCallback;
    private Runnable                 onXpChangedCallback;

    private final Object     playerLock       = new Object();
    private volatile int     playerChoice     = -1;
    private volatile boolean waitingForPlayer = false;


    private final Object     animLock      = new Object();
    private volatile boolean waitingForAnim = false;

    private volatile boolean battleOver = false;


    /**
     * Creates a new BattleSystem object.
     *
     * @param player the player value
     * @param enemies the enemies value
     * @param waveNumber the wave number value
     */
    public BattleSystem(Player player, List<Enemy> enemies, int waveNumber) {
        this.player     = player;
        this.enemies    = new ArrayList<>(enemies);
        this.waveNumber = waveNumber;
    }


    public void setPlayerActionCallback(PlayerActionCallback cb) { this.playerActionCallback = cb; }
    public void setAnimationCallback(AnimationCallback cb)        { this.animationCallback = cb; }
    public void setLogCallback(Consumer<String> cb)              { this.logCallback = cb; }
    public void setOnVictoryCallback(Consumer<List<Enemy>> cb)   { this.onVictoryCallback = cb; }
    public void setOnPlayerDeathCallback(Runnable cb)            { this.onPlayerDeathCallback = cb; }
    public void setOnXpChangedCallback(Runnable cb)              { this.onXpChangedCallback = cb; }


    @Override
    /**
     * Handles the run behavior.
     */
    public void run() {
        initQueue();
        player.clearSessionGains();

        log("WAVE " + waveNumber + " — BATTLE START!");

        while (!battleOver) {
            pruneDeadFromQueue();
            if (checkVictory() || checkDefeat()) break;

            Entity current = turnQueue.poll();
            if (current == null) break;
            if (!current.isAlive()) continue;

            if (current instanceof Player) {
                handlePlayerTurn();
            } else if (current instanceof Enemy) {
                handleEnemyTurn((Enemy) current);
            }

            if (current.isAlive()) {
                current.advanceTick();
                turnQueue.offer(current);
            }
        }
    }

    /**
     * Handles the handle player turn behavior.
     */
    private void handlePlayerTurn() {
        List<Enemy> living = getLivingEnemies();
        if (living.isEmpty()) return;
        applyLingerDamage();
        living = getLivingEnemies();
        if (living.isEmpty()) {
            checkVictory();
            return;
        }

        if (playerActionCallback != null) {
            waitingForPlayer = true;
            playerActionCallback.onPlayerTurn(living, this::submitPlayerChoice);
            synchronized (playerLock) {
                while (waitingForPlayer && !battleOver) {
                    try { playerLock.wait(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
            }
            int idx = playerChoice;
            playerChoice = -1;
            if (idx >= 0 && idx < living.size()) {
                Enemy target = living.get(idx);
                fireAnimation(AnimationType.PLAYER_ATTACK, player, target);
                SoundPlayer.play("Sounds/Punch.wav");
                float dmg = player.performAttack(target);
                log(String.format("You strike %s with %s — %s damage!",
                        target.getName(), player.getWeapon().getName(),
                        UIHelpers.formatBigNumber(dmg)));
                if (!target.isAlive()) onEnemyKilled(target);
                Weapon w = player.getWeapon();
                if (w.hasSplash()) {
                    List<Enemy> others = new ArrayList<>(living);
                    others.remove(target);
                    int splashCount = Math.min(w.getSplashTargets(), others.size());
                    for (int s = 0; s < splashCount; s++) {
                        if (others.isEmpty()) break;
                        if (RNG.nextFloat() < w.getSplashChance()) {
                            Enemy splashTarget = others.remove(0);
                            SoundPlayer.play("Sounds/Punch.wav");
                            float splashDmg = Math.max(1f,
                                    player.getTotalAttack()
                                            * w.getDamageMultiplier()
                                            * 0.6f
                                            - splashTarget.getDefense());
                            splashTarget.takeDamage(splashDmg);
                            log(String.format("  Splash hits %s for %s damage!",
                                    splashTarget.getName(),
                                    UIHelpers.formatBigNumber(splashDmg)));
                            if (!splashTarget.isAlive()) onEnemyKilled(splashTarget);
                            if (w.hasLinger() && splashTarget.isAlive()) {
                                lingerTurns.put(splashTarget, LINGER_DURATION);
                                log(String.format(" %s is burning!", splashTarget.getName()));
                            }
                        }
                    }
                    if (w.hasLinger() && target.isAlive()) {
                        lingerTurns.put(target, LINGER_DURATION);
                        log(String.format(" %s is burning!", target.getName()));
                    }
                }
            }
            checkVictory();
        } else {
            Enemy target = living.get(0);
            fireAnimation(AnimationType.PLAYER_ATTACK, player, target);
            SoundPlayer.play("Sounds/Punch.wav");
            float dmg = player.performAttack(target);
            log(String.format("[AUTO] Attack %s — %s damage.",
                    target.getName(), UIHelpers.formatBigNumber(dmg)));
            if (!target.isAlive()) onEnemyKilled(target);
            checkVictory();
        }
    }


    /**
     * Handles the apply linger damage behavior.
     */
    private void applyLingerDamage() {
        if (lingerTurns.isEmpty()) return;
        float baseAttackDamage =
                (player.getAttack() + player.getBonusDamage())
                        * player.getWeapon().getDamageMultiplier();

        float lingerDmg =
                player.getWeapon().getLingerDamage()
                        + baseAttackDamage * 0.25f;

        List<Enemy> burned = new ArrayList<>(lingerTurns.keySet());
        for (Enemy e : burned) {
            if (!e.isAlive()) { lingerTurns.remove(e); continue; }
            e.takeDamage(lingerDmg);
            log(String.format("  %s burns for %s damage!", e.getName(),
                    UIHelpers.formatBigNumber(lingerDmg)));
            if (!e.isAlive()) {
                onEnemyKilled(e);
                lingerTurns.remove(e);
                if (checkVictory()) return;
            } else {
                int remaining = lingerTurns.get(e) - 1;
                if (remaining <= 0) lingerTurns.remove(e);
                else lingerTurns.put(e, remaining);
            }
        }
    }

    /**
     * Handles the handle enemy turn behavior.
     *
     * @param enemy the enemy value
     */
    private void handleEnemyTurn(Enemy enemy) {
        if (!player.isAlive()) return;
        fireAnimation(AnimationType.ENEMY_ATTACK, enemy, player);
        SoundPlayer.play("Sounds/Punch.wav");
        String result = enemy.chooseAction(player);
        log(result);
        if (!player.isAlive()) {
            battleOver = true;
            log("YOU DIED — GAME OVER");
            if (onPlayerDeathCallback != null) onPlayerDeathCallback.run();
        }
    }


    /**
     * Handles the fire animation behavior.
     *
     * @param type the type value
     * @param attacker the attacker value
     * @param target the target value
     */
    private void fireAnimation(AnimationType type, Entity attacker, Entity target) {
        if (animationCallback == null) return;
        waitingForAnim = true;
        animationCallback.onAnimation(type, attacker, target);
        synchronized (animLock) {
            while (waitingForAnim && !battleOver) {
                try { animLock.wait(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        }
    }

    /**
     * Handles the signal animation done behavior.
     */
    public void signalAnimationDone() {
        synchronized (animLock) {
            waitingForAnim = false;
            animLock.notifyAll();
        }
    }

    /**
     * Handles the submit player choice behavior.
     *
     * @param targetIndex the target index value
     */
    public void submitPlayerChoice(int targetIndex) {
        synchronized (playerLock) {
            playerChoice     = targetIndex;
            waitingForPlayer = false;
            playerLock.notifyAll();
        }
    }



    /**
     * Handles the check victory behavior.
     *
     * @return true if the action succeeds, false otherwise
     */
    private boolean checkVictory() {
        if (getLivingEnemies().isEmpty()) {
            battleOver = true;
            log("WAVE " + waveNumber + " COMPLETE!");
            if (onVictoryCallback != null) onVictoryCallback.accept(enemies);
            return true;
        }
        return false;
    }

    /**
     * Handles the check defeat behavior.
     *
     * @return true if the action succeeds, false otherwise
     */
    private boolean checkDefeat() {
        return !player.isAlive();
    }

    /**
     * Handles the on enemy killed behavior.
     *
     * @param e the e value
     */
    private void onEnemyKilled(Enemy e) {
        lingerTurns.remove(e);
        log(e.getName() + " defeated!");
        player.addExperience(e.getXpGive());
        if (onXpChangedCallback != null) onXpChangedCallback.run();
        player.addResources(e.getResGive());
        log(String.format("+%s XP", UIHelpers.formatBigNumber(e.getXpGive())));
    }


    /**
     * Handles the init queue behavior.
     */
    private void initQueue() {
        turnQueue.clear();
        float effSpeed = player.getEffectiveSpeed();
        player.setSpeed(effSpeed);
        player.nextActionTick = 1.0 / effSpeed;
        turnQueue.offer(player);
        for (Enemy e : enemies) {
            e.nextActionTick = 1.0 / e.getSpeed();
            turnQueue.offer(e);
        }
    }

    /**
     * Handles the prune dead from queue behavior.
     */
    private void pruneDeadFromQueue() {
        List<Entity> alive = new ArrayList<>();
        while (!turnQueue.isEmpty()) {
            Entity e = turnQueue.poll();
            if (e.isAlive()) alive.add(e);
        }
        turnQueue.addAll(alive);
    }

    /**
     * Returns the living enemies.
     *
     * @return the living enemies
     */
    private List<Enemy> getLivingEnemies() {
        List<Enemy> out = new ArrayList<>();
        for (Enemy e : enemies) { if (e.isAlive()) out.add(e); }
        return out;
    }


    /**
     * Handles the log behavior.
     *
     * @param msg the msg value
     */
    private void log(String msg) {
        System.out.println(msg);
        if (logCallback != null) logCallback.accept(msg);
    }


    /**
     * Handles the abort behavior.
     */
    public void abort() {
        battleOver = true;
        lingerTurns.clear();
        synchronized (playerLock) { playerLock.notifyAll(); }
        synchronized (animLock)   { animLock.notifyAll(); }
    }


    public enum AnimationType {
        PLAYER_ATTACK,
        ENEMY_ATTACK
    }


    @FunctionalInterface
    public interface PlayerActionCallback {
        /**
         * Handles the on player turn behavior.
         *
         * @param livingEnemies the living enemies value
         * @param submitChoice the submit choice value
         */
        void onPlayerTurn(List<Enemy> livingEnemies, IntConsumer submitChoice);
    }

    @FunctionalInterface
    public interface AnimationCallback {
        /**
         * Handles the on animation behavior.
         *
         * @param type the type value
         * @param attacker the attacker value
         * @param target the target value
         */
        void onAnimation(BattleSystem.AnimationType type, Entity attacker, Entity target);
    }
}