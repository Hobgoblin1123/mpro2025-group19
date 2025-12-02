import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class StarShooterVS extends JPanel implements ActionListener, KeyListener {

    // ゲーム設定
    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private static final int DELAY = 16; // 約60FPS

    // プレイヤーオブジェクト
    private Player player1;
    private Player player2;

    // 弾のリスト
    private ArrayList<Bullet> bullets;

    // ゲーム状態
    private boolean isRunning = true;
    private Timer timer;
    private String winner = "";

    // 入力管理
    private boolean[] keys;

    public StarShooterVS() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        keys = new boolean[256]; // キー入力の状態保存
        bullets = new ArrayList<>();

        // プレイヤーの初期化 (x, y, 色, 方向1=下から上, -1=上から下)
        player1 = new Player(WIDTH / 2 - 20, HEIGHT - 100, Color.RED, -1);
        player2 = new Player(WIDTH / 2 - 20, 50, Color.CYAN, 1);

        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGame(g);
    }

    private void drawGame(Graphics g) {
        if (isRunning) {
            // プレイヤーの描画
            player1.draw(g);
            player2.draw(g);

            // 弾の描画
            g.setColor(Color.YELLOW);
            for (Bullet b : bullets) {
                b.draw(g);
            }

            // UI（HPバー）の描画
            drawHealthBar(g, player1, 20, HEIGHT - 40);
            drawHealthBar(g, player2, 20, 20);

        } else {
            // ゲームオーバー画面
            drawGameOver(g);
        }
    }

    private void drawHealthBar(Graphics g, Player p, int x, int y) {
        g.setColor(Color.WHITE);
        g.drawString("HP", x, y);
        g.setColor(Color.GRAY);
        g.fillRect(x + 30, y - 10, 100, 10);
        g.setColor(p.color);
        int hpWidth = (int)((p.hp / (double)p.maxHp) * 100);
        g.fillRect(x + 30, y - 10, hpWidth, 10);
    }

    private void drawGameOver(Graphics g) {
        String msg = "GAME OVER - Winner: " + winner;
        Font font = new Font("Arial", Font.BOLD, 30);
        FontMetrics metrics = getFontMetrics(font);

        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString(msg, (WIDTH - metrics.stringWidth(msg)) / 2, HEIGHT / 2);
        
        String restart = "Press SPACE to Restart";
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString(restart, (WIDTH - g.getFontMetrics().stringWidth(restart)) / 2, HEIGHT / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRunning) {
            updateGame();
        }
        repaint();
    }

    private void updateGame() {
        // --- プレイヤー1の操作 (WASD + Space) ---
        if (keys[KeyEvent.VK_A]) player1.move(-5, 0);
        if (keys[KeyEvent.VK_D]) player1.move(5, 0);
        if (keys[KeyEvent.VK_W]) player1.move(0, -5);
        if (keys[KeyEvent.VK_S]) player1.move(0, 5);
        if (keys[KeyEvent.VK_SPACE]) {
            Bullet b = player1.shoot();
            if (b != null) bullets.add(b);
        }

        // --- プレイヤー2の操作 (矢印キー + Enter) ---
        if (keys[KeyEvent.VK_LEFT]) player2.move(-5, 0);
        if (keys[KeyEvent.VK_RIGHT]) player2.move(5, 0);
        if (keys[KeyEvent.VK_UP]) player2.move(0, -5);
        if (keys[KeyEvent.VK_DOWN]) player2.move(0, 5);
        if (keys[KeyEvent.VK_ENTER]) {
            Bullet b = player2.shoot();
            if (b != null) bullets.add(b);
        }

        // --- 弾の更新と当たり判定 ---
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update();

            // 画面外に出たら削除
            if (b.y < 0 || b.y > HEIGHT) {
                it.remove();
                continue;
            }

            // 当たり判定 (自分自身の弾には当たらない前提)
            if (b.owner != player1 && player1.getBounds().intersects(b.getBounds())) {
                player1.hit();
                it.remove();
            } else if (b.owner != player2 && player2.getBounds().intersects(b.getBounds())) {
                player2.hit();
                it.remove();
            }
        }

        // --- 勝敗判定 ---
        if (player1.hp <= 0) {
            isRunning = false;
            winner = "Player 2 (Blue)";
        } else if (player2.hp <= 0) {
            isRunning = false;
            winner = "Player 1 (Red)";
        }
        
        // --- 画面外に出ないように制限 ---
        player1.clamp(WIDTH, HEIGHT);
        player2.clamp(WIDTH, HEIGHT);
    }

    // --- キー入力処理 ---
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = true;
        
        // リスタート処理
        if (!isRunning && code == KeyEvent.VK_SPACE) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = false;
    }
    
    @Override public void keyTyped(KeyEvent e) {}

    private void resetGame() {
        player1 = new Player(WIDTH / 2 - 20, HEIGHT - 100, Color.RED, -1);
        player2 = new Player(WIDTH / 2 - 20, 50, Color.CYAN, 1);
        bullets.clear();
        isRunning = true;
        winner = "";
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Star Shooter VS");
        StarShooterVS game = new StarShooterVS();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ================= 内部クラス =================

    // プレイヤークラス
    class Player {
        int x, y, width = 40, height = 40;
        int maxHp = 20;
        int hp = maxHp;
        Color color;
        int shootDir; // 弾を撃つ方向
        long lastShotTime = 0;
        int cooldown = 200; // 発射間隔(ミリ秒)

        public Player(int x, int y, Color color, int shootDir) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.shootDir = shootDir;
        }

        public void move(int dx, int dy) {
            x += dx;
            y += dy;
        }

        // 画面外に出ないようにする
        public void clamp(int w, int h) {
            if (x < 0) x = 0;
            if (x > w - width) x = w - width;
            if (y < 0) y = 0;
            if (y > h - height) y = h - height;
        }

        public Bullet shoot() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShotTime > cooldown) {
                lastShotTime = currentTime;
                // プレイヤーの中心から発射
                return new Bullet(x + width / 2 - 5, y + (shootDir == 1 ? height : -10), shootDir, this);
            }
            return null;
        }

        public void hit() {
            hp--;
        }

        public void draw(Graphics g) {
            g.setColor(color);
            // 三角形で描画
            int[] xPoints = {x, x + width / 2, x + width};
            int[] yPoints;
            if (shootDir == -1) { // 上向き（P1）
                 yPoints = new int[]{y + height, y, y + height};
            } else { // 下向き（P2）
                 yPoints = new int[]{y, y + height, y};
            }
            g.fillPolygon(xPoints, yPoints, 3);
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }

    // 弾クラス
    class Bullet {
        int x, y, width = 10, height = 10;
        int dy; // 縦方向の速度
        Player owner;

        public Bullet(int x, int y, int dir, Player owner) {
            this.x = x;
            this.y = y;
            this.dy = dir * 10; // スピード10
            this.owner = owner;
        }

        public void update() {
            y += dy;
        }

        public void draw(Graphics g) {
            g.fillOval(x, y, width, height);
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }
}
