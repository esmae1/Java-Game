package game;

import city.cs.engine.SoundClip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

public class MenuPanel extends JPanel {

    private Image menuBg;
    private Image startButtonImage;
    private Rectangle startButtonSize;
    private Image storeButtonImage;
    private Rectangle storeButtonSize;
    private Image settingsButtonImage;
    private Rectangle settingsButtonSize;
    private Font maraFont;
    private SoundClip menuMusic;
    private Game game;

    public MenuPanel(Game game) {
        this.game = game;

        try {
            menuMusic = new SoundClip("data/menuTrack.wav");
            menuMusic.loop();
        } catch(Exception e) {
            System.out.println("Music failed to load");
        }

        try {
            maraFont = Font.createFont(Font.TRUETYPE_FONT, new File("data/maraFont.ttf"));
            maraFont = maraFont.deriveFont(Font.PLAIN, 50f);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }

        menuBg = new ImageIcon("data/menuBg.jpg").getImage();
        startButtonImage = new ImageIcon("data/playButton1.png").getImage();
        startButtonSize = new Rectangle(310, 185, 175, 90);

        storeButtonImage = new ImageIcon("data/storeButton.png").getImage();
        storeButtonSize = new Rectangle(310, 277, 175, 74);

        settingsButtonImage = new ImageIcon("data/settingsButton.png").getImage();
        settingsButtonSize = new Rectangle(310, 340, 175, 87);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (startButtonSize.contains(e.getPoint())) {
                    game.startGame();
                    menuMusic.stop();
                } else if (storeButtonSize.contains(e.getPoint())) {
                    game.openMenuShop();
                } else if (settingsButtonSize.contains(e.getPoint())) {
                game.openMenuSettings(menuMusic);
            }


            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(menuBg, 0, 0, getWidth(), getHeight(), null);

        g.setColor(new Color(244, 222, 200, 255));
        g.setFont(maraFont.deriveFont(36));
        g.drawString("COLOSSEUM", 205, 130);
        g.setColor(new Color(45, 33, 6, 255));
        g.setFont(maraFont.deriveFont(36f));
        g.drawString("WRECK", 310, 180);



        g.drawImage(startButtonImage, startButtonSize.x, startButtonSize.y,
                startButtonSize.width, startButtonSize.height, null);
        g.drawImage(storeButtonImage, storeButtonSize.x, storeButtonSize.y,
                storeButtonSize.width, storeButtonSize.height, null);
        g.drawImage(settingsButtonImage, settingsButtonSize.x, settingsButtonSize.y,
                settingsButtonSize.width, settingsButtonSize.height, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 500);
    }

    public SoundClip getMenuMusic() {
        return menuMusic;
    }
}