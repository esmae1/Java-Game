package game;

import city.cs.engine.SoundClip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class SettingsPanel extends JPanel {

    private Image menuBg;
    private Image paperBg;
    private Font titleFont;
    private Font bodyFont;

    // static so the settings save between menu and game
    static boolean musicOn = true;
    static boolean sfxOn = true;
    static boolean bloodOn = true;

    // click regions set in constructor so first click works
    private Rectangle musicBounds;
    private Rectangle sfxBounds;
    private Rectangle bloodBounds;
    private Rectangle closeBounds;

    // null when opened from menu since there is no active game
    private GameView gameView;
    private SoundClip gameMusic;

    // what the back button does depends on where settings was opened from
    private Runnable onClose;

    public SettingsPanel(GameView gameView, SoundClip gameMusic) {
        this.gameView = gameView;
        this.gameMusic = gameMusic;

        setPreferredSize(new Dimension(800, 500));
        setOpaque(false);

        menuBg  = new ImageIcon("data/menuBg.jpg").getImage();
        paperBg = new ImageIcon("data/paperBackground.png").getImage();

        titleFont = loadFont("data/bloody.ttf", 36f);
        bodyFont  = loadFont("data/maraFont.ttf", 22f);

        musicBounds = new Rectangle(360, 160, 80, 34);
        sfxBounds   = new Rectangle(360, 240, 80, 34);
        bloodBounds = new Rectangle(360, 320, 80, 34);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getPoint());
            }
        });
    }

    // called after game music loads so the toggle can actually control it
    public void setMusic(SoundClip music) {
        this.gameMusic = music;
    }

    // set by game when opening settings from menu so back button knows where to go
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    private void handleClick(Point p) {
        // music toggle
        // stops or loops depending on current state
        if (musicBounds != null && musicBounds.contains(p)) {
            musicOn = !musicOn;
            if (gameMusic != null) {
                if (musicOn) {
                    gameMusic.setVolume(0.3f);
                    gameMusic.loop();
                } else {
                    gameMusic.stop();
                }
            }
            repaint();
        }

        // sfx toggle
        // updates static flag read by enemy and gem classes
        if (sfxBounds != null && sfxBounds.contains(p)) {
            sfxOn = !sfxOn;
            GameView.sfxEnabled = sfxOn;
            repaint();
        }

        // blood toggle
        // clears existing blood if turned off
        if (bloodBounds != null && bloodBounds.contains(p)) {
            bloodOn = !bloodOn;
            if (gameView != null) gameView.setBloodEnabled(bloodOn);
            repaint();
        }

        // back button
        // uses onClose if set, otherwise closes in game settings
        if (closeBounds != null && closeBounds.contains(p)) {
            if (onClose != null) {
                onClose.run();
            } else if (gameView != null) {
                gameView.closeSettings();
            }
        }
    }

    public boolean isSfxOn()   {
        return sfxOn; }
    public boolean isBloodOn() {
        return bloodOn; }

    @Override
    protected void paintComponent(Graphics g2d) {
        super.paintComponent(g2d);
        Graphics2D g = (Graphics2D) g2d;

        // draw menu background with dark overlay so panel is readable
        g.drawImage(menuBg, 0, 0, getWidth(), getHeight(), null);
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, getWidth(), getHeight());

        // paper background for the settings panel
        g.drawImage(paperBg, 190, 90, 420, 320, null);

        // title
        g.setFont(titleFont.deriveFont(34f));
        g.setColor(new Color(246, 221, 196));
        g.drawString("SETTINGS", 290, 75);

        // music row
        // green if on red if off
        g.setFont(bodyFont.deriveFont(20f));
        g.setColor(new Color(50, 30, 10));
        g.drawString("Music", 367, 150);
        g.setColor(musicOn ? new Color(60, 150, 60) : new Color(160, 50, 30));
        g.fillRoundRect(360, 160, 80, 34, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(bodyFont.deriveFont(Font.BOLD, 16f));
        g.drawString(musicOn ? "ON" : "OFF", musicOn ? 378 : 374, 183);

        // sound effects row
        g.setFont(bodyFont.deriveFont(20f));
        g.setColor(new Color(50, 30, 10));
        g.drawString("Sound Effects", 334, 230);
        g.setColor(sfxOn ? new Color(60, 150, 60) : new Color(160, 50, 30));
        g.fillRoundRect(360, 240, 80, 34, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(bodyFont.deriveFont(Font.BOLD, 16f));
        g.drawString(sfxOn ? "ON" : "OFF", sfxOn ? 378 : 374, 263);

        // blood effects row
        g.setFont(bodyFont.deriveFont(20f));
        g.setColor(new Color(50, 30, 10));
        g.drawString("Blood Effects", 340, 310);
        g.setColor(bloodOn ? new Color(60, 150, 60) : new Color(160, 50, 30));
        g.fillRoundRect(360, 320, 80, 34, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(bodyFont.deriveFont(Font.BOLD, 16f));
        g.drawString(bloodOn ? "ON" : "OFF", bloodOn ? 378 : 374, 343);

        // invisible clickable back button area with visible text over it
        closeBounds = new Rectangle(350, 418, 100, 34);
        g.setColor(new Color(39, 24, 7, 0));
        g.fillRect(350, 418, 100, 34);
        g.setColor(new Color(240, 215, 170));
        g.setFont(bodyFont.deriveFont(18f));
        g.drawString("Back", 376, 440);
    }

    // loads custom font, falls back to serif if file not found
    private Font loadFont(String path, float size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File(path)).deriveFont(size); }
        catch (Exception e) {
            return new Font("Serif", Font.BOLD, (int) size); }
    }
}