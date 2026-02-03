import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ShootingView extends JPanel implements Runnable, KeyListener {

    private int shakeX, shakeY;
    private double shakeAngle = 0;
    private int offset = 8;
    private MoveManager manager;
    private Thread gameThread;
    private long beforeShootTime;
    private long nowTime, startedTime;
    private Font pixelFont;
    private boolean[] keys = new boolean[256];

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    public ShootingView(MoveManager manager) {
        this.manager = manager;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        gameThread = new Thread(this);
        gameThread.start();
        startedTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        System.out.println("ゲームループ開始"); // ★デバッグ用
        while (manager.isRunning) {
            try {
                processInput();

                // ★デバッグ用: どこで止まっているか確認
                // System.out.println("受信待機中...");

                // System.out.println("画面更新");
                manager.update();

                this.revalidate(); // レイアウトの再計算

                repaint();
                Thread.sleep(16);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processInput() {
        if (manager.isServer()) {
            if (keys[KeyEvent.VK_LEFT])
                manager.player1.moveLeft();
            if (keys[KeyEvent.VK_RIGHT])
                manager.player1.moveRight();
            if (keys[KeyEvent.VK_UP])
                manager.player1.moveUp();
            if (keys[KeyEvent.VK_DOWN])
                manager.player1.moveDown();
            if (keys[KeyEvent.VK_SPACE]) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - beforeShootTime > 500) {
                    manager.bullets.addAll(manager.player1.tryShoot(0));
                    beforeShootTime = System.currentTimeMillis();
                }
            }
            if (keys[KeyEvent.VK_Z]) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - beforeShootTime > 300) {
                    manager.bullets.addAll(manager.player1.tryShoot(1));
                    beforeShootTime = System.currentTimeMillis();
                }
            }
            if (keys[KeyEvent.VK_Q]) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - beforeShootTime > 300) {
                    manager.bullets.addAll(manager.player1.tryShoot(2));
                    beforeShootTime = System.currentTimeMillis();
                }
            }
            // Serverの発射処理はここに実装
        } else {
            // Client
            if (keys[KeyEvent.VK_LEFT])
                manager.player2.moveLeft();
            if (keys[KeyEvent.VK_RIGHT])
                manager.player2.moveRight();
            if (keys[KeyEvent.VK_UP])
                manager.player2.moveUp();
            if (keys[KeyEvent.VK_DOWN])
                manager.player2.moveDown();
            // ★重要: クライアントは「受信」する前に「送信」すること！
            // そうしないと、サーバー(受信待ち)とクライアント(受信待ち)で睨み合いになる
            boolean isShooting = false;
            int bulletType = 0;
            if (keys[KeyEvent.VK_SPACE]) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - beforeShootTime > 500) {
                    isShooting = true;
                    bulletType = 0;
                    beforeShootTime = System.currentTimeMillis();
                }
            }
            if (keys[KeyEvent.VK_Z]) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - beforeShootTime > 300) {
                    isShooting = true;
                    bulletType = 1;
                    beforeShootTime = System.currentTimeMillis();
                }
            }
            if (keys[KeyEvent.VK_Q]) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - beforeShootTime > 300) {
                    isShooting = true;
                    bulletType = 2;
                    beforeShootTime = System.currentTimeMillis();
                }
            }
            manager.sendClientInput(isShooting, bulletType);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (manager.getPlayer().getShakeTime() > 0) {
            shakeX = (int) (Math.cos(shakeAngle) * 30);
            shakeY = (int) (Math.sin(shakeAngle) * 30);
            shakeAngle += 1.0;
            manager.getPlayer().passShakeTime();
            g2.setColor(new Color(255, 0, 0, 80)); // 半透明赤
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            shakeX = 0;
            shakeY = 0;
        }
        g2.translate(shakeX, shakeY);

        if (manager != null) {
            manager.draw(g2);
            drawUI(g2);

            nowTime = System.currentTimeMillis();
            if (nowTime - startedTime < 800) {
                drawStart(g2);
            }
        }

        g2.translate(-1 * shakeX, -1 * shakeY);
    }

    // UI描画（NetworkShooterから移植）
    private void drawUI(Graphics g) {
        // Player1 (Server) のステータス
        drawPlayerStatus(g, manager.player1, 20, HEIGHT - 100);

        // Player2 (Client) のステータス
        drawPlayerStatus(g, manager.player2, 20, 50);
    }

    private void drawStart(Graphics g) {
        // フォントの登録
        try {
            // fontsフォルダに入れたファイル名を指定
            File fontFile = new File("Fonts/DotGothic16-Regular.ttf");
            // フォントを作成 (サイズは後で deriveFont で変えられる)
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            // 太字(BOLD)でサイズ24にする
            pixelFont = baseFont.deriveFont(Font.BOLD, 40);
            // グラフィックス環境に登録（これをしておくとシステム全体で認識されることもある）
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);

        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // 読み込み失敗時はデフォルトのフォントを使うなどの保険
            pixelFont = new Font("Monospaced", Font.BOLD, 60);
        }

        if (pixelFont != null) {
            g.setFont(pixelFont);
        }

        String text = "START";
        FontMetrics fm = g.getFontMetrics();

        // 「手前の緑色のエリア」の中心を計算
        // 左端(pad) + 傾き半分(skew/2) + 幅半分(shapeW/2)
        float centerX = 600;
        float centerY = 400;

        int textX = (int) (centerX - fm.stringWidth(text) / 2);
        int textY = (int) (centerY - fm.getAscent() / 2) + fm.getAscent() - offset;

        g.setColor(Color.RED);
        g.drawString(text, textX, textY);
    }

    private void drawPlayerStatus(Graphics g, Player p, int x, int y) {
        try {
            // fontsフォルダに入れたファイル名を指定
            File fontFile = new File("Fonts/DotGothic16-Regular.ttf");
            // フォントを作成 (サイズは後で deriveFont で変えられる)
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            // 太字(BOLD)でサイズ24にする
            pixelFont = baseFont.deriveFont(Font.BOLD, 10);
            // グラフィックス環境に登録（これをしておくとシステム全体で認識されることもある）
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);

        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // 読み込み失敗時はデフォルトのフォントを使うなどの保険
            pixelFont = new Font("Monospaced", Font.BOLD, 10);
        }

        if (pixelFont != null) {
            g.setFont(pixelFont);
        }
        g.setColor(Color.WHITE);
        g.drawString("HP: " + p.getHp(), x, y);

        // HPバー
        g.setColor(Color.GRAY);
        g.fillRect(x + 50, y - 10, 200, 10);

        g.setColor(Color.GREEN); // とりあえず緑
        // Playerクラスに getMaxHp() があると仮定して割合計算
        // int hpWidth = (int)((p.getHp() / (double)p.getMaxHp()) * 200);
        int hpWidth = p.getHp() * 20; // 仮計算
        if (hpWidth < 0)
            hpWidth = 0;

        g.fillRect(x + 50, y - 10, hpWidth, 10);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("キーが押されました: " + e.getKeyCode()); // ★デバッグ用
        if (e.getKeyCode() < keys.length)
            keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() < keys.length)
            keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}