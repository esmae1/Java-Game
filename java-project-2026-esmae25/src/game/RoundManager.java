package game;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoundManager {

    private int currentRound = 1;
    private World world;
    private Player player;
    private PlayerController controller;

    // prevents boss from being spawned more than once per round
    private boolean bossSpawned = false;

    private GameView gameView;

    // list of all active enemies in the current round
    private List<Enemy> enemies = new ArrayList<>();

    private int enemiesToSpawn;
    private int enemiesSpawned;

    // timer that spawns enemies at set intervals
    private Timer spawnTimer;
    private Random random = new Random();

    // left and right spawn positions at the arena edges
    private Vec2[] spawnPoints = {
            new Vec2(-16f, 1.7f),
            new Vec2(16f, 1.7f),
    };

    public RoundManager(World world, Player player, PlayerController controller, GameView gameView) {
        this.world = world;
        this.player = player;
        this.controller = controller;
        this.gameView = gameView;
    }

    // sets up enemy count and timer speed depending on which round it is
    public void startRound() {
        enemies.clear();
        enemiesSpawned = 0;
        bossSpawned = false;

        if (currentRound == 1) {
            enemiesToSpawn = 6;
            gameView.setRoundTime(60);
            startSpawning(4300);
        } else if (currentRound == 2) {
            enemiesToSpawn = 9;
            gameView.setRoundTime(90);
            startSpawning(4700);
        } else if (currentRound == 3) {
            enemiesToSpawn = 10;
            gameView.setRoundTime(120);
            startSpawning(3800);
        }

        gameView.setRound(currentRound);
    }

    // used by respawn so the player restarts from the round they died on
    public void startFromRound(int round) {
        currentRound = round;
        startRound();
    }

    // creates a repeating timer that spawns one enemy every intervalMs milliseconds
    // only increments enemiesSpawned if something actually spawned
    private void startSpawning(int intervalMs) {
        spawnTimer = new Timer(intervalMs, e -> {
            if (enemiesSpawned < enemiesToSpawn && !player.isDead()) {
                boolean spawned = spawnNext();
                if (spawned) enemiesSpawned++;
            }
            if (enemiesSpawned >= enemiesToSpawn) {
                spawnTimer.stop();
            }
        });
        // small delay before first spawn so the round transition feels smoother
        spawnTimer.setInitialDelay(1650);
        spawnTimer.start();
    }

    // decides which enemy to spawn, returns false if boss is pending so counter doesnt tick up early
    private boolean spawnNext() {
        Vec2 pos = spawnPoints[random.nextInt(spawnPoints.length)];
        Enemy e;
        if (currentRound == 1) {
            // round 1 - all goblins
            e = new Goblin(world, player, gameView);
        } else if (currentRound == 2) {
            // round 2 - 5 goblins then 4 bulls
            e = (enemiesSpawned < 5) ? new Goblin(world, player, gameView) : new Bull(world, player, gameView);
        } else {
            if (enemiesSpawned < 4) {
                // round 3 - 4 goblins first
                e = new Goblin(world, player, gameView);
            } else if (enemiesSpawned < 9) {
                // then 5 bulls
                e = new Bull(world, player, gameView);
            } else {
                // boss spawns after 4 second delay
                // enemiesSpawned incremented inside timer when boss actually appears
                if (!bossSpawned) {
                    bossSpawned = true;
                    new javax.swing.Timer(4000, event -> {
                        Boss boss = new Boss(world, player, gameView);
                        boss.setPosition(spawnPoints[random.nextInt(spawnPoints.length)]);
                        boss.setGravityScale(0);
                        enemies.add(boss);
                        controller.addEnemy(boss);
                        gameView.addNotification("BOSS ARRIVED");
                        enemiesSpawned++; // count boss as spawned only when it actually appears
                    }) {{ setRepeats(false); start(); }};
                }
                return false; // dont count this as a spawn yet
            }
        }

        e.setPosition(pos);
        e.setGravityScale(0);
        enemies.add(e);
        controller.addEnemy(e);
        return true;
    }

    // called every physics step to check if all enemies are dead
    public void checkRoundOver() {
        if (player.isDead()) return;
        if (enemiesSpawned < enemiesToSpawn) return;

        // dont trigger victory while boss hasnt appeared yet
        if (currentRound == 3 && bossSpawned && enemies.isEmpty()) return;

        // checks if every enemy in the list has zero health
        boolean allDead = enemies.stream().allMatch(e -> e.getEnemyHealth() <= 0);

        if (allDead && !enemies.isEmpty()) {
            enemies.clear();
            if (spawnTimer != null) spawnTimer.stop();

            // remove any static blood or death sprites left in the world
            for (Body b : world.getStaticBodies()) {
                if (b.getImages() != null && !b.getImages().isEmpty()) {
                    b.destroy();
                }
            }

            if (currentRound < 3) {
                int pointsEarned = currentRound * 100;
                int nextRound = currentRound + 1;
                gameView.showRoundComplete(pointsEarned, nextRound);
                gameView.clearBlood();
                currentRound++;
                // 3.6 second pause so the player can read the round complete popup
                new Timer(3630, e -> startRound()) {{
                    setRepeats(false);
                    start();
                }};
            } else {
                // all 3 rounds done
                gameView.clearBlood();
                gameView.showVictory();
            }
        }
    }

    public int getCurrentRound() {
        return currentRound;
    }
}