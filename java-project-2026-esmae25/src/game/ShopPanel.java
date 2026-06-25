package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class ShopPanel extends JPanel {

    public static final int speedPotion  = 0;
    public static final int healthPotion = 1;
    public static final int damagePotion = 2;

    private static final int[] potionCost  = {3, 3, 5};
    private static final String[] potionNames = {"Speed Elixir", "Health Brew", "Damage Tonic"};
    private static final String[] potionEffect = {"Speed for 10s", "+20 HP", "Damage for 10s"};
    private static final String[] imgPaths = {"data/purplePotion.png", "data/greenPotion.png", "data/orangePotion.png"};

    private Image menuBg;
    private Image shopBg;
    private Image gemIcon;
    private Image[] potionImages = new Image[3];
    private Font titleFont;
    private Font bodyFont;

    private Player player;
    private Runnable onClose;
    private GameView gameView;

    private Rectangle[] rowBounds = new Rectangle[3];
    private Rectangle closeBounds;

    public ShopPanel(Player player, Runnable onClose, GameView gameView) {
        this.player = player;
        this.onClose = onClose;
        this.gameView = gameView;

        setPreferredSize(new Dimension(800, 500));
        setOpaque(false);

        menuBg = new ImageIcon("data/menuBg.jpg").getImage();
        shopBg = new ImageIcon("data/shopBackground.png").getImage();
        gemIcon = new ImageIcon("data/gem.PNG").getImage();
        for (int i = 0; i < 3; i++) {
            potionImages[i] = new ImageIcon(imgPaths[i]).getImage();
        }

        titleFont = loadFont("data/bloody.ttf", 36f);
        bodyFont = loadFont("data/maraFont.ttf", 24f);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                handleClick(e.getPoint());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g2d) {
        super.paintComponent(g2d);
        Graphics2D g = (Graphics2D) g2d;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth(), height = getHeight();

        g.drawImage(menuBg, 0, 0, width, height, null);
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, width, height);

        int panelW = 560, panelH = 380;
        int panelX = (width - panelW) / 2, panelY = (height - panelH) / 2;
        g.drawImage(shopBg, panelX, panelY, panelW, panelH, null);

        // title
        g.setFont(titleFont.deriveFont(38f));
        g.setColor(new Color(200, 160, 80));
        String title = "THE SHOP";
        int titleW = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - titleW) / 2, panelY + 52);

        // potion rows
        int rowH = 80;
        int startY = panelY + 80;
        for (int i = 0; i < 3; i++) {
            int rowY = startY + i * (rowH + 10);
            rowBounds[i] = new Rectangle(panelX + 20, rowY, panelW - 40, rowH);
            drawRow(g, i, panelX + 20, rowY, panelW - 40, rowH);
        }

        // Close button
        int closeW = 120, closeH = 36;
        int closeX = panelX + (panelW - closeW) / 2;
        int closeY = panelY + panelH - 52;
        closeBounds = new Rectangle(closeX, closeY, closeW, closeH);
        g.setColor(new Color(160, 50, 20));
        g.fillRoundRect(closeX, closeY, closeW, closeH, 10, 10);
        g.setColor(new Color(230, 200, 150));
        g.setFont(bodyFont.deriveFont(18f));
        String back = "Back";
        int backW = g.getFontMetrics().stringWidth(back);
        g.drawString(back, closeX + (closeW - backW) / 2, closeY + 24);

        // Gem balance
        g.setFont(bodyFont.deriveFont(18f));
        g.setColor(new Color(166, 216, 230));
        String balance = "Gems: " + player.getGems();
        int balW = g.getFontMetrics().stringWidth(balance);
        g.drawImage(gemIcon, panelX + panelW - balW - 46, panelY + 14, 22, 28, null);
        g.drawString(balance, panelX + panelW - balW - 18, panelY + 36);
    }

    private void drawRow(Graphics2D g, int index, int x, int y, int w, int h) {
        boolean canAfford = player.getGems() >= potionCost[index];

        g.setColor(new Color(140, 100, 40, 100));
        g.drawRoundRect(x, y, w, h, 8, 8);

        g.drawImage(potionImages[index], x + 10, y + (h - 54) / 2, 54, 54, null);

        g.setFont(bodyFont.deriveFont(Font.BOLD, 20f));
        g.setColor(new Color(240, 220, 170));
        g.drawString(potionNames[index], x + 76, y + 30);

        g.setFont(bodyFont.deriveFont(15f));
        g.setColor(new Color(190, 170, 130));
        g.drawString(potionEffect[index], x + 76, y + 52);

        int costX = x + w - 110;
        int costY = y + h / 2 + 8;
        g.drawImage(gemIcon, costX, costY - 22, 22, 28, null);
        g.setFont(bodyFont.deriveFont(Font.BOLD, 20f));
        g.setColor(canAfford ? new Color(80, 200, 80) : new Color(200, 60, 60));
        g.drawString(String.valueOf(potionCost[index]), costX + 28, costY);

        int owned = player.getPotionCount(index);
        if (owned > 0) {
            g.setFont(bodyFont.deriveFont(13f));
            g.setColor(new Color(255, 240, 180));
            g.drawString("x" + owned + " owned", costX - 10, y + h - 10);
        }
    }

    private void handleClick(Point p) {
        if (closeBounds != null && closeBounds.contains(p)) { onClose.run(); return; }
        for (int i = 0; i < 3; i++) {
            if (rowBounds[i] != null && rowBounds[i].contains(p)) { attemptBuy(i); return; }
        }
    }

    private void attemptBuy(int type) {
        if (player.getGems() < potionCost[type]) return;
        player.addGems(-potionCost[type]);
        player.addPotion(type);
        if (gameView != null) gameView.addNotification("Bought " + potionNames[type] + "!");
        repaint();
    }

    private Font loadFont(String path, float size) {
        try { return Font.createFont(Font.TRUETYPE_FONT, new File(path)).deriveFont(size); }
        catch (Exception e) { return new Font("Serif", Font.BOLD, (int) size); }
    }
}