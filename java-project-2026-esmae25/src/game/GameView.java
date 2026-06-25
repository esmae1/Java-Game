package game;

import city.cs.engine.UserView;
import city.cs.engine.World;
import org.jbox2d.common.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameView extends UserView {

    // player reference so i can read health, gems, potion state etc
    private final Player player;

    // countdown timer for the round
    private int time = 60;

    // custom fonts loaded from data folder
    private Font maraFont;
    private Font bloodyFont;

    // ticks every second to decrease the timer
    private Timer gameTimer;

    private int currentRound = 1;
    private boolean victory = false;

    // list of floating notifications that fade in and out on screen
    private List<StatNotification> notifications = new ArrayList<>();

    private boolean showRoundComplete = false;
    private int roundCompletePoints = 0;

    // separate fade values for each end screen
    private float timeFade = 0f;
    private float victoryFade = 0f;
    private Timer timeFadeTimer;
    private Timer victoryFadeTimer;

    private int nextRoundNumber = 0;

    // fade value for death screen
    private float fade = 0f;
    private Timer deathFadeTimer;

    // stores positions of blood splats so they persist on the ground
    private List<Vec2> bloodPositions = new ArrayList<>();

    // tracks whether panels are currently open so clicks dont conflict
    private boolean shopOpen = false;
    private ShopPanel shopPanel;

    // potion images preloaded so they dont cause freezing mid game
    private Image[] potionIcons = new Image[3];

    // click regions for buttons and potion slots, set during paint
    private Rectangle shopButtonBounds;
    private Rectangle homeButtonBounds;
    private Rectangle[] potionSlotBounds = new Rectangle[3];
    private Rectangle settingButtonBounds;

    // home confirm dialog state
    private boolean showHomeConfirm = false;
    private Rectangle confirmYesBounds;
    private Rectangle confirmNoBounds;

    // reference to game so i can call returnToMenu and respawn
    private Game game;

    private SettingsPanel settingsPanel;
    private boolean settingsOpen = false;

    // controlled by settings panel toggle
    private boolean bloodEnabled = true;

    // static so enemy and gem classes can check it before playing sounds
    public static boolean sfxEnabled = true;

    public GameView(World world, Player player, Game game) {
        super(world, 800, 500);
        this.player = player;
        this.game = game;

        // load custom fonts, fallback to serif if file not found
        try {
            maraFont = Font.createFont(Font.TRUETYPE_FONT,
                    new File("data/maraFont.ttf")).deriveFont(24f);
        } catch (Exception e) {
            maraFont = new Font("Serif", Font.BOLD, 24);
        }

        try {
            bloodyFont = Font.createFont(Font.TRUETYPE_FONT,
                    new File("data/bloody.ttf")).deriveFont(24f);
        } catch (Exception e) {
            bloodyFont = new Font("Serif", Font.BOLD, 24);
        }

        // timer ticks every second and decreases round time
        gameTimer = new Timer(1000, e -> {
            if (time > 0) {
                time--;
                repaint();
            }
        });
        gameTimer.start();

        // preload potion icons once so they dont reload every frame
        potionIcons[0] = new ImageIcon("data/purplePotion.png").getImage();
        potionIcons[1] = new ImageIcon("data/greenPotion.png").getImage();
        potionIcons[2] = new ImageIcon("data/orangePotion.png").getImage();

        // shop panel sits hidden as a child component, shown when shop button clicked
        shopPanel = new ShopPanel(player, () -> closeShop(), this);
        shopPanel.setBounds(0, 0, 800, 500);
        shopPanel.setVisible(false);
        add(shopPanel);

        // settings panel same approach as shop panel
        settingsPanel = new SettingsPanel(this, null);
        settingsPanel.setBounds(0, 0, 800, 500);
        settingsPanel.setVisible(false);
        add(settingsPanel);

        // read blood setting from settings panel in case player changed it in menu
        bloodEnabled = SettingsPanel.bloodOn;

        // mouse listener handles all button clicks and potion slot clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (shopOpen) return;
                if (settingsOpen) return;

                if (shopButtonBounds != null && shopButtonBounds.contains(e.getPoint())) {
                    openShop(); return;
                }

                if (settingButtonBounds != null && settingButtonBounds.contains(e.getPoint())) {
                    openSettings(); return;
                }

                if (homeButtonBounds != null && homeButtonBounds.contains(e.getPoint())) {
                    showHomeConfirm = true;
                    getWorld().stop();
                    gameTimer.stop();
                    repaint();
                    return;
                }

                // yes/no on home confirm dialog
                if (showHomeConfirm) {
                    if (confirmYesBounds != null && confirmYesBounds.contains(e.getPoint())) {
                        game.returnToMenu();
                    }
                    if (confirmNoBounds != null && confirmNoBounds.contains(e.getPoint())) {
                        showHomeConfirm = false;
                        getWorld().start();
                        gameTimer.start();
                        repaint();
                    }
                    return;
                }

                // clicking a potion slot uses one potion of that type
                String[] names = {"Speed Elixir", "Health Brew", "Damage Tonic"};
                for (int i = 0; i < 3; i++) {
                    if (potionSlotBounds[i] != null && potionSlotBounds[i].contains(e.getPoint())) {
                        boolean used = player.usePotion(i);
                        if (used) addNotification("Used " + names[i] + "!");
                        repaint();
                        return;
                    }
                }
            }
        });
    }

    // pauses world and timer while shop is open
    private void openShop() {
        shopOpen = true;
        getWorld().stop();
        shopPanel.setVisible(true);
        shopPanel.repaint();
        shopPanel.requestFocus();
        gameTimer.stop();
    }

    // resumes world and timer when shop closes
    private void closeShop() {
        shopOpen = false;
        shopPanel.setVisible(false);
        if (!player.isDead()) { getWorld().start(); gameTimer.start(); }
        requestFocus();
        repaint();
    }

    // pauses world and timer while settings is open
    private void openSettings() {
        settingsOpen = true;
        getWorld().stop();
        gameTimer.stop();
        settingsPanel.setVisible(true);
        settingsPanel.repaint();
        settingsPanel.requestFocus();
    }

    // resumes world and timer when settings closes
    public void closeSettings() {
        settingsOpen = false;
        settingsPanel.setVisible(false);
        if (!player.isDead()) { getWorld().start(); gameTimer.start(); }
        requestFocus();
        repaint();
    }

    // called by settings panel when blood toggle is switched
    public void setBloodEnabled(boolean enabled) {
        bloodEnabled = enabled;
        if (!enabled) bloodPositions.clear();
        repaint();
    }

    public SettingsPanel getSettingsPanel() { return settingsPanel; }

    public void setRound(int round) {
        this.currentRound = round;
        repaint();
    }

    public void setRoundTime(int seconds) {
        this.time = seconds;
        if (gameTimer != null) gameTimer.restart();
    }

    public void showVictory() {
        this.victory = true;
        repaint();
    }

    // inner class to track each notification's fade state
    private static class StatNotification {
        String text;
        float alpha;
        boolean fadingIn;
        boolean fadingOut;
        Timer timer;

        StatNotification(String text) {
            this.text = text;
            this.alpha = 0.5f;
            this.fadingIn = true;
            this.fadingOut = false;
        }
    }

    // adds a notification that fades in, stays for 8 seconds, then fades out
    public void addNotification(String text) {
        StatNotification notif = new StatNotification(text);

        Timer fadeTimer = new Timer(30, null);
        fadeTimer.addActionListener(e -> {
            if (notif.fadingIn) {
                notif.alpha = Math.min(200f, notif.alpha + 8f);
                if (notif.alpha >= 200f) {
                    notif.fadingIn = false;
                    new javax.swing.Timer(8000, ev -> {
                        notif.fadingOut = true;
                    }) {{ setRepeats(false); start(); }};
                }
            } else if (notif.fadingOut) {
                notif.alpha = Math.max(0f, notif.alpha - 5f);
                if (notif.alpha <= 0f) {
                    notifications.remove(notif);
                    fadeTimer.stop();
                }
            }
            repaint();
        });
        notif.timer = fadeTimer;
        notifications.add(notif);
        fadeTimer.start();
    }

    // only adds blood if blood effects are enabled in settings
    public void addBlood(Vec2 position) {
        if (!bloodEnabled) return;
        bloodPositions.add(position);
        repaint();
    }

    public void clearBlood() {
        bloodPositions.clear();
        repaint();
    }

    @Override
    protected void paintBackground(Graphics2D g) {
        Image bg = new ImageIcon("data/gamebg.jpg").getImage();
        g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);

        // draw blood splats at stored positions
        Image blood = new ImageIcon("data/blood1.png").getImage();
        for (Vec2 pos : new ArrayList<>(bloodPositions)) {
            int screenX = (int)(getWidth() / 2 + pos.x * 20) - 15;
            int screenY = (int)(getHeight() / 2 - pos.y * 20) - 20;
            g.drawImage(blood, screenX, screenY, 80, 80, null);
        }
    }

    // shows round complete popup for 5 seconds then hides it
    public void showRoundComplete(int points, int nextRound) {
        this.showRoundComplete = true;
        this.roundCompletePoints = points;
        this.nextRoundNumber = nextRound;
        repaint();

        new javax.swing.Timer(5000, e -> {
            showRoundComplete = false;
            repaint();
        }) {{ setRepeats(false); start(); }};
    }

    @Override
    protected void paintForeground(Graphics2D g) {

        // draw HUD banners at top
        Image banner = new ImageIcon("data/banner.png").getImage();
        g.drawImage(banner, 14, 4, 225, 85, null);

        Image banner2 = new ImageIcon("data/banner2.png").getImage();
        g.drawImage(banner, 550, 4, 225, 85, null);
        Image topBanner = new ImageIcon("data/topBanner.png").getImage();
        g.drawImage(topBanner, 300, 0, 200, 85, null);

        // draw all 5 health icons as full first, then draw empty ones over depending on health
        Image emptyHealth = new ImageIcon("data/emptyHealth.png").getImage();
        Image fullHealth = new ImageIcon("data/fullHealth.png").getImage();
        g.drawImage(fullHealth, 592, 36, 28, 28, null);

        Image emptyHealth2 = new ImageIcon("data/emptyHealth.png").getImage();
        Image fullHealth2 = new ImageIcon("data/fullHealth.png").getImage();
        g.drawImage(fullHealth2, 624, 36, 28, 28, null);

        Image emptyHealth3 = new ImageIcon("data/emptyHealth.png").getImage();
        Image fullHealth3 = new ImageIcon("data/fullHealth.png").getImage();
        g.drawImage(fullHealth3, 656, 36, 28, 28, null);

        Image emptyHealth4 = new ImageIcon("data/emptyHealth.png").getImage();
        Image fullHealth4 = new ImageIcon("data/fullHealth.png").getImage();
        g.drawImage(fullHealth4, 687, 36, 28, 28, null);

        Image emptyHealth5 = new ImageIcon("data/emptyHealth.png").getImage();
        Image fullHealth5 = new ImageIcon("data/fullHealth.png").getImage();
        g.drawImage(fullHealth5, 718, 36, 28, 28, null);

        // timer and round number display
        g.setColor(new Color(45, 33, 6));
        g.setFont(maraFont.deriveFont(26f));
        g.drawString("Timer: "+ time, 61, 60);
        g.setFont(maraFont.deriveFont(21f));
        g.drawString("Round: " + currentRound, 349, 38);

        // gem count with icon
        Image gemIcon = new ImageIcon("data/gem.PNG").getImage();
        g.drawImage(gemIcon, 50, 90, 29, 39, null);
        g.setColor(new Color(166, 216, 230));
        g.setFont(maraFont.deriveFont(Font.BOLD, 20f));
        g.drawString("" + player.getGems(), 83, 116);

        // draw notifications stacked downward on the right side
        g.setFont(maraFont.deriveFont(16f));
        int notifYaxis = 100;
        for (StatNotification notif : new ArrayList<>(notifications)) {
            g.setColor(new Color(237, 216, 174, (int) notif.alpha));
            g.drawString(notif.text, 680, notifYaxis);
            notifYaxis += 22;
        }

        // start death fade timer when player health hits 0
        if (player.getHealth() <= 0) {
            gameTimer.stop();
            if (deathFadeTimer == null) {
                deathFadeTimer = new Timer(30, e -> {
                    if (fade < 1f) {
                        fade = Math.min(1f, fade + 0.03f);
                        repaint();
                    } else {
                        deathFadeTimer.stop();
                    }
                });
                deathFadeTimer.start();
            }
        }

        // overlay empty health icons depending on how low health is
        if (player.getHealth() <= 80) {
            g.drawImage(emptyHealth, 718, 37, 28, 26, null);
        }
        if (player.getHealth() <= 60) {
            g.drawImage(emptyHealth4, 687, 37, 28, 26, null);
        }
        if (player.getHealth() <= 40) {
            g.drawImage(emptyHealth3, 656, 37, 28, 26, null);
        }
        if (player.getHealth() <= 20) {
            g.drawImage(emptyHealth2, 624, 37, 28, 26, null);
        }

        // death screen fades in as dark overlay with text
        g.setColor(new Color(0, 0, 0, (int)(240 * fade)));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(145, 32, 17, (int)(255 * fade)));
        g.setFont(bloodyFont.deriveFont(55f));
        g.drawString("You DIED.", 240, 250);

        g.setColor(new Color(217, 207, 190, (int)(255 * fade)));
        g.setFont(maraFont.deriveFont(21.3f));
        g.drawString("Press M to return to menu", 245f, 300);

        // only show respawn option if player hasnt used it yet
        if (!player.hasRespawned() && fade >= 1f) {
            g.setColor(new Color(255, 206, 158, 163));
            g.setFont(maraFont.deriveFont(15.8f));
            g.drawString("Press R to respawn (once only)", 259, 330);
        }

        // time out screen with its own fade
        if (time <= 0) {
            getWorld().stop();
            gameTimer.stop();
            if (timeFadeTimer == null) {
                timeFadeTimer = new Timer(30, e -> {
                    if (timeFade < 1f) { timeFade = Math.min(1f, timeFade + 0.03f); repaint(); }
                    else timeFadeTimer.stop();
                });
                timeFadeTimer.start();
            }
            g.setColor(new Color(0, 0, 0, (int)(240 * timeFade)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(145, 32, 17, (int)(255 * timeFade)));
            g.setFont(bloodyFont.deriveFont(33f));
            g.drawString("You ran out of time...", 220, 250);
            g.setColor(new Color(217, 207, 190, (int)(255 * fade)));
            g.setFont(maraFont.deriveFont(21.3f));
            g.drawString("Press M to return to main menu", 242f, 300);
        }

        // victory screen with its own fade
        if (victory) {
            getWorld().stop();
            if (victoryFadeTimer == null) {
                victoryFadeTimer = new Timer(30, e -> {
                    if (victoryFade < 1f) { victoryFade = Math.min(1f, victoryFade + 0.03f); repaint(); }
                    else victoryFadeTimer.stop();
                });
                victoryFadeTimer.start();
            }
            g.setColor(new Color(0, 0, 0, (int)(240 * victoryFade)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(189, 165, 25, (int)(255 * victoryFade)));
            g.setFont(bloodyFont.deriveFont(40f));
            g.drawString("YOU WON.", 280, 250);
            g.setColor(new Color(189, 165, 25, (int)(255 * victoryFade)));
        }

        // round complete popup drawn on top of game
        if (showRoundComplete) {
            g.setColor(new Color(0, 0, 0, 150));
            Image roundCompleteBackground = new ImageIcon("data/backgroundText.png").getImage();
            g.drawImage(roundCompleteBackground, 150, 171, 500, 150, null);
            g.setColor(new Color(145, 32, 17));
            g.setFont(bloodyFont.deriveFont(32f));
            g.drawString("ENEMIES WIPED!", 240, 220);
            g.setFont(maraFont.deriveFont(22f));
            g.setColor(new Color(34, 19, 5, 255));
            g.drawString("Gems earned this round: " + player.getGems(), 234, 260);
            g.setFont(maraFont.deriveFont(18f));
            g.drawString("Proceeding to Round " + nextRoundNumber + "...", 235, 295);
        }

        // draw potion inventory slots bottom left
        drawInventory(g);

        // show active boost timers above inventory
        if (player.isSpeedBoosted()) {
            g.setColor(new Color(255, 230, 207, 163));
            g.setFont(maraFont.deriveFont(Font.BOLD, 14f));
            g.drawString("SPD: " + player.getSpeedTimeLeft() + "s", 10, 418);
        }

        if (player.isDamageBoosted()) {
            g.setColor(new Color(255, 230, 207, 163));
            g.setFont(maraFont.deriveFont(Font.BOLD, 13f));
            g.drawString("DMG: " + player.getDamageTimeLeft() + "s", 10, 432);
        }

        // bottom right buttons
        Image shopButtonImg = new ImageIcon("data/shopButtonn.png").getImage();
        shopButtonBounds = new Rectangle(726, 454, 42, 42);
        g.drawImage(shopButtonImg, 726, 454, 42, 42, null);
        g.setFont(maraFont.deriveFont(Font.BOLD, 11f));
        g.setColor(new Color(255, 230, 207, 163));
        g.drawString("SHOP", 727, 450);

        Image homeButtonImg = new ImageIcon("data/homeButtonn.png").getImage();
        homeButtonBounds = new Rectangle(669, 457, 39, 39);
        g.drawImage(homeButtonImg, 669, 457, 39, 42, null);
        g.setFont(maraFont.deriveFont(Font.BOLD, 11f));
        g.setColor(new Color(255, 223, 197, 163));
        g.drawString("HOME", 667, 450);

        Image settingsButtonImg = new ImageIcon("data/settingButton.png").getImage();
        settingButtonBounds = new Rectangle(609, 457, 39, 39);
        g.drawImage(settingsButtonImg, 609, 457, 39, 39, null);
        g.setFont(maraFont.deriveFont(Font.BOLD, 11f));
        g.setColor(new Color(255, 235, 216, 163));
        g.drawString("SETTING", 597, 450);

        // home confirm dialog drawn on top of everything else
        if (showHomeConfirm) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, 800, 500);

            g.setFont(maraFont.deriveFont(Font.BOLD, 27));
            g.setColor(new Color(240, 220, 170));
            g.drawString("Return to main menu?", 230, 210);

            g.setFont(maraFont.deriveFont(14f));
            g.setColor(new Color(190, 170, 130));
            g.drawString("Current game progress will be lost.", 270, 235);

            g.setColor(new Color(60, 120, 62));
            g.fillRoundRect(280, 260, 100, 36, 8, 8);
            g.setColor(new Color(244, 248, 244));
            g.setFont(maraFont.deriveFont(Font.BOLD, 18f));
            g.drawString("Yes", 314, 283);
            confirmYesBounds = new Rectangle(280, 260, 100, 36);

            g.setColor(new Color(120, 40, 42));
            g.fillRoundRect(420, 260, 100, 36, 8, 8);
            g.setColor(new Color(246, 243, 243));
            g.setFont(maraFont.deriveFont(Font.BOLD, 18f));
            g.drawString("No", 458, 283);
            confirmNoBounds = new Rectangle(420, 260, 100, 36);
        }
    }

    // draws potion icons bottom left, one slot per potion owned, clicking uses one
    private void drawInventory(Graphics2D g) {
        int slotSize = 46;
        int slotGap  = 6;
        int startX   = 14;
        int startY   = getHeight() - slotSize - 14;

        int[] timesLeft = {
                player.getSpeedTimeLeft(),
                0, // health has no duration
                player.getDamageTimeLeft()
        };
        boolean[] active = {
                player.isSpeedBoosted(),
                false,
                player.isDamageBoosted()
        };

        for (int i = 0; i < 3; i++) potionSlotBounds[i] = null;

        int[] typeStartX = new int[3];
        int[] typeEndX   = new int[3];
        int cursor = startX;

        // loop through each potion type and draw one slot per potion owned
        for (int type = 0; type < 3; type++) {
            int count = player.getPotionCount(type);
            typeStartX[type] = cursor;

            for (int n = 0; n < count; n++) {
                int slotX = cursor;

                // gold glow if boost is active, dark background if not
                g.setColor(active[type] ? new Color(255, 200, 60, 170) : new Color(30, 20, 10, 180));
                g.fillRoundRect(slotX, startY, slotSize, slotSize, 7, 7);
                g.setColor(new Color(140, 100, 40, 200));
                g.drawRoundRect(slotX, startY, slotSize, slotSize, 7, 7);

                g.drawImage(potionIcons[type], slotX + 4, startY + 4, slotSize - 8, slotSize - 8, null);

                // label above each slot
                String[] labels = {"SPD", "HP", "DMG"};
                g.setFont(maraFont.deriveFont(Font.BOLD, 10f));
                g.setColor(new Color(255, 230, 180, 200));
                g.drawString(labels[type], slotX + 12, startY - 4);

                cursor += slotSize + slotGap;
            }

            typeEndX[type] = cursor - slotGap;
            if (count > 0) cursor += 4;
        }

        // set click bounds for each type covering its full row of icons
        for (int type = 0; type < 3; type++) {
            if (player.getPotionCount(type) > 0) {
                int w = typeEndX[type] - typeStartX[type];
                potionSlotBounds[type] = new Rectangle(typeStartX[type], startY, w, slotSize);
            }
        }
    }
}