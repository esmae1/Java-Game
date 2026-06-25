package game;

import city.cs.engine.*;
import org.jbox2d.common.Vec2;

public class Gem extends StaticBody {
    private GameView gameView;
    private Player player;
    private SoundClip gemSound;

    public Gem(World world, Player player, GameView gameView, Vec2 position) {
        super(world, new CircleShape(0.5f));
        this.player = player;
        this.gameView = gameView;
        setPosition(position);
        addImage(new BodyImage("data/gem.png", 1.5f));

        try {
            gemSound = new SoundClip("data/gemCollectSound.wav");
            gemSound.setVolume(0.1f);
        } catch (Exception ex) {
            System.out.println("Gem pickup sound failed to load");
        }

        addCollisionListener(e -> {
            if (e.getOtherBody() instanceof Player) {
                player.addGems(1);
                if (GameView.sfxEnabled && gemSound != null) {
                    gemSound.play();
                }
                gameView.addNotification("+1 gem ");
                destroy();
            }
        });
    }
}