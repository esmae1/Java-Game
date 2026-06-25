package game;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;

public class Bull extends Enemy {
    public Bull(World world, Player player, GameView gameView) {
        super(world, player, gameView, 150, 2.4f);

        BodyImage bullRightWalk = new BodyImage("data/enemyBoss1.png", 5.8f);
        BodyImage bullLeftWalk = new BodyImage("data/bullLeftWalk.png", 5.8f);
        BodyImage bullRightAttack = new BodyImage("data/enemyBoss3.png", 5.8f);
        BodyImage bullLeftAttack = new BodyImage("data/bullLeftAttack.png", 5.8f);
        BodyImage bullIdle = new BodyImage("data/enemyBoss2.png", 5.8f);

        addImage(bullIdle);

        world.addStepListener(new StepListener() {
            @Override
            public void preStep(StepEvent e) {
                if (health <= 0) return;
                Vec2 vel = getLinearVelocity();
                if (vel.length() > 0.5f) {
                    if (vel.x < 0) {
                        removeAllImages();
                        addImage(bullLeftWalk);
                    } else {
                        removeAllImages();
                        addImage(bullRightWalk);
                    }
                }
            }
            @Override
            public void postStep(StepEvent e) {}
        });

        this.addCollisionListener(new PlayerCollision(player, this, gameView, 20));

        this.addCollisionListener(new CollisionListener() {
            @Override
            public void collide(CollisionEvent e) {
                if (e.getOtherBody() instanceof Player && health > 0) {
                    Vec2 direction = player.getPosition().sub(getPosition());
                    removeAllImages();
                    if (direction.x < 0) {
                        addImage(bullLeftAttack);
                    } else {
                        addImage(bullRightAttack);
                    }
                    new javax.swing.Timer(1500, event -> {
                        removeAllImages();
                        if (direction.x < 0) {
                            addImage(bullLeftWalk);
                        } else {
                            addImage(bullRightWalk);
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