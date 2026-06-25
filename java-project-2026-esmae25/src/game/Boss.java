package game;
import city.cs.engine.*;
import org.jbox2d.common.Vec2;

public class Boss extends Enemy {
    public Boss(World world, Player player, GameView gameView) {
        super(world, player, gameView, 500, 1f);

        BodyImage bossRunRight = new BodyImage("data/bossRightWalk.png", 6);
        BodyImage bossRunLeft = new BodyImage("data/bossLeftWalk.png", 6);
        BodyImage bossAttackRight = new BodyImage("data/bossAttackRight.png", 6);
        BodyImage bossAttackLeft = new BodyImage("data/bossAttackLeft.png", 6);
        BodyImage bossIdle = new BodyImage("data/bossIdle.png", 6);

        addImage(bossRunRight);

        world.addStepListener(new StepListener() {
            @Override
            public void preStep(StepEvent e) {
                if (health <= 0) return;
                Vec2 vel = getLinearVelocity();
                if (vel.length() > 0.5f) {
                    if (vel.x < 0) {
                        removeAllImages();
                        addImage(bossRunLeft);
                    } else {
                        removeAllImages();
                        addImage(bossRunRight);
                    }
                }
            }
            @Override
            public void postStep(StepEvent e) {}
        });

        this.addCollisionListener(new PlayerCollision(player, this, gameView, 40));

        this.addCollisionListener(new CollisionListener() {
            @Override
            public void collide(CollisionEvent e) {
                if (e.getOtherBody() instanceof Player && health > 0) {
                    Vec2 direction = player.getPosition().sub(getPosition());
                    removeAllImages();
                    if (direction.x < 0) {
                        addImage(bossAttackLeft);
                    } else {
                        addImage(bossAttackRight);
                    }
                    new javax.swing.Timer(1500, event -> {
                        removeAllImages();
                        if (direction.x < 0) {
                            addImage(bossRunLeft);
                        } else {
                            addImage(bossRunRight);
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