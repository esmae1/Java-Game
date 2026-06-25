package game;

import city.cs.engine.*;
import org.jbox2d.common.Vec2;

public class Goblin extends Enemy {

    public Goblin(World world, Player player, GameView gameView) {
        super(world, player, gameView, 100, 2f);

        BodyImage goblinAttackRight = new BodyImage("data/goblinAttack.png", 4);
        BodyImage goblinAttackLeft = new BodyImage("data/goblinAttackLeft.png", 4);
        BodyImage goblinRunRight = new BodyImage("data/goblinRun.png", 4);
        BodyImage goblinRunLeft = new BodyImage("data/goblinRunLeft.PNG", 4);
        BodyImage goblinIdle = new BodyImage("data/goblinIdle.png", 4);

        addImage(goblinIdle);

        world.addStepListener(new StepListener() {
            @Override
            public void preStep(StepEvent e) {
                if (health <= 0) return;
                Vec2 vel = getLinearVelocity();
                if (vel.length() > 0.5f) {
                    if (vel.x < 0) {
                        removeAllImages();
                        addImage(goblinRunLeft);
                    } else {
                        removeAllImages();
                        addImage(goblinRunRight);
                    }
                }
            }
            @Override
            public void postStep(StepEvent e) {}
        });

        this.addCollisionListener(new PlayerCollision(player, this, gameView, 15));

        this.addCollisionListener(new CollisionListener() {
            @Override
            public void collide(CollisionEvent e) {
                if (e.getOtherBody() instanceof Player && health > 0) {
                    Vec2 direction = player.getPosition().sub(getPosition());
                    removeAllImages();
                    if (direction.x < 0) {
                        addImage(goblinAttackLeft);
                    } else {
                        addImage(goblinAttackRight);
                    }
                    new javax.swing.Timer(1500, event -> {
                        removeAllImages();
                        if (direction.x < 0) {
                            addImage(goblinRunLeft);
                        } else {
                            addImage(goblinRunRight);
                        }
                    }) {{ setRepeats(false); start(); }};
                }
            }
        });

        setPosition(new Vec2(-100, -100));
        setGravityScale(0);
        setLinearVelocity(new Vec2(0, 0));
        setAngularVelocity(0);
    }
}