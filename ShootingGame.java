import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ShootingGame extends JPanel implements ActionListener, KeyListener {

 // --- ゲーム設定 ---
 private static final int WIDTH = 400;
 private static final int HEIGHT = 600;
 private Timer timer;
 private boolean isGameOver = false;
 private int score = 0;
 private int difficultyTimer = 0; // 難易度上昇用

 // --- キー入力状態 ---
 private boolean[] keys = new boolean[256];

 // --- オブジェクト群 ---
 private Player player;
 private ArrayList<Bullet> bullets;
 private ArrayList<Enemy> enemies;
 private ArrayList<Explosion> explosions;
 private ArrayList<Star> stars; // 背景の星

 public ShootingGame() {
 this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
 this.setBackground(new Color(10, 10, 30)); // 宇宙っぽい紺色
 this.setFocusable(true);
 this.addKeyListener(this);

 initGame();

 timer = new Timer(16, this); // 約60FPS
 timer.start();
 }

 private void initGame() {
 player = new Player(WIDTH / 2, HEIGHT - 80);
 bullets = new ArrayList<>();
 enemies = new ArrayList<>();
 explosions = new ArrayList<>();
 stars = new ArrayList<>();
 score = 0;
 isGameOver = false;

 // 星を初期生成
 for (int i = 0; i < 50; i++) {
 stars.add(new Star(true));
 }
 }

 @Override
 public void paintComponent(Graphics g) {
 super.paintComponent(g);
 Graphics2D g2d = (Graphics2D) g;
 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

 // 1. 背景（星）の描画
 g2d.setColor(Color.WHITE);
 for (Star star : stars) star.draw(g2d);

 // 2. プレイヤー描画
 if (!isGameOver) player.draw(g2d);

 // 3. 弾の描画
 g2d.setColor(Color.ORANGE);
 for (Bullet b : bullets) b.draw(g2d);

 // 4. 敵の描画
 for (Enemy e : enemies) e.draw(g2d);

 // 5. 爆発エフェクト描画
 for (Explosion exp : explosions) exp.draw(g2d);

 // 6. UI描画
 g2d.setColor(Color.WHITE);
 g2d.setFont(new Font("Consolas", Font.BOLD, 18));
 g2d.drawString("SCORE: " + score, 10, 25);

 if (isGameOver) {
 g2d.setColor(Color.RED);
 g2d.setFont(new Font("Arial", Font.BOLD, 40));
 String msg = "GAME OVER";
 FontMetrics fm = g2d.getFontMetrics();
 g2d.drawString(msg, WIDTH / 2 - fm.stringWidth(msg) / 2, HEIGHT / 2);
 
 g2d.setFont(new Font("Arial", Font.BOLD, 20));
 g2d.setColor(Color.WHITE);
 String sub = "Press R to Retry";
 fm = g2d.getFontMetrics();
 g2d.drawString(sub, WIDTH / 2 - fm.stringWidth(sub) / 2, HEIGHT / 2 + 40);
 }
 }

 @Override
 public void actionPerformed(ActionEvent e) {
 // 星はゲームオーバーでも動かす（雰囲気重視）
 updateStars();
 
 if (isGameOver) return;

 // --- 更新処理 ---
 player.update();
 updateBullets();
 updateEnemies();
 updateExplosions();
 checkCollisions();

 // 難易度調整（敵の出現率）
 difficultyTimer++;
 int spawnRate = Math.max(20, 60 - (score / 500)); // スコアが上がると敵が増える
 if (difficultyTimer % spawnRate == 0) {
 enemies.add(new Enemy());
 }

 repaint();
 }

 private void updateStars() {
 for (Star star : stars) star.update();
 }

 private void updateBullets() {
 Iterator<Bullet> it = bullets.iterator();
 while (it.hasNext()) {
 Bullet b = it.next();
 b.update();
 if (b.y < 0) it.remove();
 }
 }

 private void updateEnemies() {
 Iterator<Enemy> it = enemies.iterator();
 while (it.hasNext()) {
 Enemy e = it.next();
 e.update();
 if (e.y > HEIGHT) it.remove();
 }
 }

 private void updateExplosions() {
 Iterator<Explosion> it = explosions.iterator();
 while (it.hasNext()) {
 Explosion exp = it.next();
 exp.update();
 if (!exp.active) it.remove();
 }
 }

 private void checkCollisions() {
 Rectangle pRect = player.getBounds();

 // 敵とプレイヤー/弾の判定
 Iterator<Enemy> eIt = enemies.iterator();
 while (eIt.hasNext()) {
 Enemy enemy = eIt.next();
 Rectangle eRect = enemy.getBounds();

 // 1. 敵 vs プレイヤー
 if (pRect.intersects(eRect)) {
 isGameOver = true;
 explosions.add(new Explosion(player.x, player.y, Color.YELLOW));
 explosions.add(new Explosion(enemy.x, enemy.y, Color.RED));
 }

 // 2. 敵 vs 弾
 Iterator<Bullet> bIt = bullets.iterator();
 while (bIt.hasNext()) {
 Bullet b = bIt.next();
 if (eRect.intersects(b.getBounds())) {
 explosions.add(new Explosion(enemy.x, enemy.y, Color.ORANGE));
 score += 100;
 eIt.remove();
 bIt.remove();
 break; // 弾が当たったらその弾ループは抜ける
 }
 }
 }
 }

 // --- キー入力 ---
 @Override
 public void keyPressed(KeyEvent e) {
 int code = e.getKeyCode();
 if (code < keys.length) keys[code] = true;
 
 if (code == KeyEvent.VK_SPACE && !isGameOver) {
 player.shoot();
 }
 if (code == KeyEvent.VK_R && isGameOver) {
 initGame();
 }
 }
 @Override
 public void keyReleased(KeyEvent e) {
 int code = e.getKeyCode();
 if (code < keys.length) keys[code] = false;
 }
 @Override public void keyTyped(KeyEvent e) {}

 // --- メインメソッド ---
 public static void main(String[] args) {
 JFrame frame = new JFrame("Java Space Shooter");
 ShootingGame game = new ShootingGame();
 frame.add(game);
 frame.pack();
 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 frame.setLocationRelativeTo(null);
 frame.setVisible(true);
 }

 // --- 内部クラス群 ---

 class Player {
 int x, y;
 int speed = 5;
 int width = 30, height = 30;

 public Player(int x, int y) { this.x = x; this.y = y; }

 public void update() {
 if (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]) x -= speed;
 if (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]) x += speed;
 if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) y -= speed;
 if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) y += speed;

 // 画面外に出ない
 x = Math.max(width/2, Math.min(WIDTH - width/2, x));
 y = Math.max(height/2, Math.min(HEIGHT - height/2, y));
 }

 public void shoot() {
 bullets.add(new Bullet(x, y - 10));
 }

 public Rectangle getBounds() { return new Rectangle(x - width/2, y - height/2, width, height); }

 public void draw(Graphics2D g) {
 // 戦闘機っぽい三角形
 int[] xPoints = {x, x - 15, x + 15};
 int[] yPoints = {y - 20, y + 15, y + 15};
 g.setColor(Color.CYAN);
 g.fillPolygon(xPoints, yPoints, 3);
 
 // エンジンの火
 g.setColor(Color.ORANGE);
 g.fillRect(x - 5, y + 15, 10, 5);
 }
 }

 class Bullet {
 int x, y;
 int speed = 10;
 int width = 4, height = 10;

 public Bullet(int x, int y) { this.x = x; this.y = y; }
 public void update() { y -= speed; }
 public Rectangle getBounds() { return new Rectangle(x - width/2, y - height/2, width, height); }
 public void draw(Graphics2D g) { g.fillRect(x - width/2, y - height/2, width, height); }
 }

 class Enemy {
 int x, y;
 int speed;
 int size = 30;

 public Enemy() {
 Random rand = new Random();
 x = rand.nextInt(WIDTH - 40) + 20;
 y = -size;
 speed = rand.nextInt(3) + 2; // ランダムな速度
 }
 public void update() { y += speed; }
 public Rectangle getBounds() { return new Rectangle(x - size/2, y - size/2, size, size); }
 public void draw(Graphics2D g) {
 g.setColor(Color.MAGENTA);
 // 敵っぽい形（ギザギザ）
 int[] xp = {x, x-15, x, x+15};
 int[] yp = {y+15, y-10, y-15, y-10};
 g.fillPolygon(xp, yp, 4);
 }
 }

 class Explosion {
 int x, y;
 int life = 10; // 爆発の持続時間
 int maxRadius = 40;
 Color color;

 public Explosion(int x, int y, Color c) { this.x = x; this.y = y; this.color = c; }
 public boolean active = true;

 public void update() {
 life--;
 if (life <= 0) active = false;
 }

 public void draw(Graphics2D g) {
 int radius = maxRadius - (life * 3);
 g.setColor(color);
 g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, life / 10f)); // 透明度
 g.fillOval(x - radius/2, y - radius/2, radius, radius);
 g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // 戻す
 }
 }
 
 class Star {
 int x, y, speed, size;
 public Star(boolean randomY) {
 reset(randomY);
 }
 void reset(boolean randomY) {
 Random r = new Random();
 x = r.nextInt(WIDTH);
 y = randomY ? r.nextInt(HEIGHT) : 0;
 speed = r.nextInt(3) + 1; // 遠近感のために速度を変える
 size = r.nextInt(2) + 1;
 }
 void update() {
 y += speed;
 if (y > HEIGHT) reset(false);
 }
 void draw(Graphics2D g) {
 g.fillRect(x, y, size, size);
 }
 }
}
