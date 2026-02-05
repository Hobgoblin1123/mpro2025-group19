import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("deprecation")
public class ShootingView extends StarAnimPanel implements Runnable, KeyListener, Observer {
    private int shakeX, shakeY;
    private double shakeAngle = 0;
    private int offset = 8;
    private MoveManager manager;
    private Thread gameThread;
    private long beforeShootTime;
    private long nowTime, startedTime, endTime;
    private Image bgImage;
    private Font fontLarge;
    private Font fontSmall;
    private boolean[] keys = new boolean[256];

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    public ShootingView(MoveManager manager) {
        this.manager = manager;
        manager.addObserver(this);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        this.setHorizontalMode(true);
        this.starMaxSpeed = 0.3;
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        gameThread = new Thread(this);
        gameThread.start();
        startedTime = System.currentTimeMillis();
        try {
            bgImage = ImageIO.read(new File("images/GamePanelBG.jpg"));
        } catch (IOException e) {
            System.out.println("画像が見つかりません.");
        }
        try {
            File fontFile = new File("Fonts/DotGothic16-Regular.ttf");
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);

            fontLarge = baseFont.deriveFont(Font.BOLD, 60f);
            fontSmall = baseFont.deriveFont(Font.BOLD, 10f);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);

        } catch (Exception e) {
            // フォールバック（失敗しても落ちない）
            fontLarge = new Font("Monospaced", Font.BOLD, 200);
            fontSmall = new Font("Monospaced", Font.BOLD, 10);
        }
    }

    @Override
    public void run() {
        System.out.println("ゲームループ開始"); // ★デバッグ用
        long error = 0;
        int fps = 60;
        long idealSleep = 1000 / fps; // 約16ms
        long oldTime;
        long newTime = System.currentTimeMillis();
        while (getpassedEndTime() > 2000) {
            oldTime = newTime;
            try {
                processInput();

                // ここで通信待ちが発生するが、以下の計算でその分スリープを削る
                manager.update();
                repaint();

                newTime = System.currentTimeMillis();
                long processTime = newTime - oldTime; // 処理にかかった時間
                long sleepTime = idealSleep - processTime; // 理想の時間から処理時間を引く

                if (sleepTime < 2) {
                    sleepTime = 2; // 最低でも2msは休ませてCPU負荷を下げる
                }
                Thread.sleep(sleepTime);
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
            if (keys[KeyEvent.VK_X]) {
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
            if (keys[KeyEvent.VK_X]) {
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

        if (manager != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
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
            manager.draw(g2);
            drawUI(g2);

            nowTime = System.currentTimeMillis();
            if (nowTime - startedTime < 800) {

                drawStart(g2);
            }
            if (manager.isRunning == false) {
                drawGameset(g2);
            }
        }

        g2.translate(-1 * shakeX, -1 * shakeY);
    }

    // UI描画（NetworkShooterから移植）
    private void drawUI(Graphics g) {
        // Player1 (Server) のステータス
        drawPlayerStatus(g, manager.player1, 20, HEIGHT - 80);

        // Player2 (Client) のステータス
        drawPlayerStatus(g, manager.player2, 980, 30);
    }

    private void drawStart(Graphics g) {
        // フォントの登録
        if (fontLarge != null) {
            g.setFont(fontLarge);
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

    private void drawGameset(Graphics g) {
        // フォントの登録
        if (fontLarge != null) {
            g.setFont(fontLarge);
        }
        String text = "GAMESET";
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

        g.setColor(Color.WHITE);

        if (fontSmall != null) {
            g.setFont(fontSmall);
        }
        // HPバー
        g.drawString("HP: " + p.getHp(), x, y);
        g.setColor(Color.GRAY);
        g.fillRect(x + 50, y - 10, 200, 10);

        g.setColor(Color.GREEN); // とりあえず緑
        // Playerクラスに getMaxHp() があると仮定して割合計算
        // int hpWidth = (int)((p.getHp() / (double)p.getMaxHp()) * 200);
        int hpWidth = p.getHp() * 20; // 仮計算
        if (hpWidth < 0)
            hpWidth = 0;

        g.fillRect(x + 50, y - 10, hpWidth, 10);
        g.setColor(Color.WHITE);
        g.drawRect(x + 50, y - 10, 200, 10);
    }

    public long getpassedEndTime() {
        nowTime = System.currentTimeMillis();
        return this.nowTime - endTime;
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

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("ゲームが終了しました");
        endTime = System.currentTimeMillis();

        // stage変数を通じてStageクラスのメソッドやフィールドにアクセスできるようにする

    }
}