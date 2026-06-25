package game;

import city.cs.engine.*;
import org.jbox2d.common.Vec2;

public class Enemy extends Walker {

    // shared by all enemy subclasses (goblin, bull, boss)
    protected Player player;
    protected int health;
    protected float speed;
    private GameView gameView;

    // cooldown flag so hit sound doesnt overlap on every attack press
    private boolean hitSoundPlaying = false;

    // preloaded so they dont cause a freeze when played mid game
    private SoundClip goblinHit;
    private SoundClip bullHit;
    private SoundClip bossHit;

    public Enemy(World world, Player player, GameView gameView, int health, float speed) {
        super(world, new BoxShape(1, 2));
        this.player = player;
        this.health = health;
        this.speed = speed;
        this.gameView = gameView;

        // load all hit sounds once at creation
        try {
            goblinHit = new SoundClip("data/goblinHit.wav");
            goblinHit.setVolume(0.3f);
            bullHit = new SoundClip("data/bullHit.wav");
            bullHit.setVolume(0.3f);
            bossHit = new SoundClip("data/bossHit.wav");
            bossHit.setVolume(0.3f);
        } catch (Exception ex) {
            System.out.println("Hit sound failed to load");
        }

        // every physics step, follow the player if alive
        world.addStepListener(new StepListener() {
            @Override
            public void preStep(StepEvent e) {
                if (health > 0) {
                    followPlayer();
                } else {
                    setLinearVelocity(new Vec2(0, 0));
                }
            }

            @Override
            public void postStep(StepEvent e) {
            }
        });
    }

    // moves toward player and stops within 3 units so collision can trigger
    protected void followPlayer() {
        float stopDistance = 3f;
        Vec2 direction = player.getPosition().sub(this.getPosition());
        float distance = direction.length();

        if (distance > stopDistance) {
            direction.normalize();
            setLinearVelocity(direction.mul(speed));
        } else {
            setLinearVelocity(new Vec2(0, 0));
        }
    }

    public int getEnemyHealth() {
        return health;
    }

    // called when player attacks, plays different sounds depending on enemy type
    public void takeDamage(int damage) {
        health -= damage;

        // only play sound if sfx is on and no sound is already playing
        if (!hitSoundPlaying && GameView.sfxEnabled) {
            try {
                hitSoundPlaying = true;
                if (this instanceof Boss) {
                    if (goblinHit != null) goblinHit.play();
                    if (bossHit != null) bossHit.play();
                } else if (this instanceof Bull) {
                    if (bullHit != null) bullHit.play();
                } else {
                    if (goblinHit != null) goblinHit.play();
                }
                // unlock after 600ms so next hit can play cleanly
                new javax.swing.Timer(600, e -> {
                    hitSoundPlaying = false;
                }) {{ setRepeats(false); start(); }};
            } catch (Exception ex) {
                System.out.println("Hit sound failed");
                hitSoundPlaying = false;
            }
        }

        if (health <= 0) {
            health = 0;
            gameView.addNotification("Enemy slain!");
            gameView.addBlood(this.getPosition());

            // 60% chance to drop a gem on death
            if (Math.random() < 0.6) {
                new Gem(getWorld(), player, gameView, this.getPosition());
            }

            this.destroy();
        }
    }
}