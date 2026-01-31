// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.*;

// // NetworkShooterの機能を移植した ShootingView
// public class ShootingView extends JPanel implements ActionListener, KeyListener {

//     // ロジック部分（前のShooterModelの代わり）
//     private MoveManager manager;

//     // ゲームループ用タイマー
//     private Timer timer;

//     // キー入力管理（滑らかな移動のため）
//     private boolean[] keys = new boolean[256];

//     // 画面サイズ（GameFrameに合わせるなら不要かもしれないが念のため）
//     private static final int WIDTH = 1200; // GameFrameのサイズに合わせました
//     private static final int HEIGHT = 800;

//     // コンストラクタ：GameFrameから manager を受け取る！
//     public ShootingView(MoveManager manager) {
//         this.manager = manager;

//         // パネルの設定
//         setPreferredSize(new Dimension(WIDTH, HEIGHT));
//         setBackground(Color.BLACK);
//         setFocusable(true);
//         addKeyListener(this); // キー入力を受け付ける

//         // タイマー開始（約60FPS = 16ms, 余裕を見て20ms）
//         timer = new Timer(20, this);
//         timer.start();
//     }

//     // --- ゲームループ（タイマーがカチッとなるたびに呼ばれる） ---
//     @Override
//     public void actionPerformed(ActionEvent e) {
//         // 入力処理とモデルの更新
//         processInput();

//         // Managerの更新処理（通信や移動計算）
//         manager.update();

//         // 画面の再描画
//         repaint();
//     }

//     // --- 入力処理（NetworkShooterのロジックを移植） ---
//     private void processInput() {
//         int speed = 5; // プレイヤースピード
//         int courtW = manager.court_size_x; // コートの幅

//         // 自分がサーバー(Player1)の場合
//         if (manager.isServer()) { // ManagerにisServer()メソッドが必要（後述）
//             if (keys[KeyEvent.VK_LEFT]) {
//                 // Manager内のPlayer1を動かす
//                 manager.player1.moveLeft();
//             }
//             if (keys[KeyEvent.VK_RIGHT]) {
//                 manager.player1.moveRight();
//             }
//             if (keys[KeyEvent.VK_SPACE]) {
//                 // 弾の発射ロジック (Manager側に実装が必要)
//                 // manager.player1.shoot();
//             }
//         }
//         // 自分がクライアント(Player2)の場合
//         else {
//             if (keys[KeyEvent.VK_LEFT]) {
//                 manager.player2.moveLeft();
//             }
//             if (keys[KeyEvent.VK_RIGHT]) {
//                 manager.player2.moveRight();
//             }

//             // クライアントの入力を送信する処理など
//             boolean isShooting = keys[KeyEvent.VK_SPACE];
//             // manager.sendClientInput(isShooting); // 必要なら実装
//         }
//     }

//     // --- 描画処理 ---
//     @Override
//     protected void paintComponent(Graphics g) {
//         super.paintComponent(g);

//         // 1. ゲーム画面（プレイヤーや弾）を描画
//         // Managerが持っているdrawメソッドに任せる
//         manager.draw(g);

//         // 2. UI（HPバーなど）をその上に描画
//         drawUI(g);
//     }

//     // UI描画（NetworkShooterから移植）
//     private void drawUI(Graphics g) {
//         // Player1 (Server) のステータス
//         drawPlayerStatus(g, manager.player1, 20, HEIGHT - 100);

//         // Player2 (Client) のステータス
//         drawPlayerStatus(g, manager.player2, 20, 50);
//     }

//     private void drawPlayerStatus(Graphics g, Player p, int x, int y) {
//         g.setColor(Color.WHITE);
//         g.drawString("HP: " + p.getHp(), x, y);

//         // HPバー
//         g.setColor(Color.GRAY);
//         g.fillRect(x + 50, y - 10, 200, 10);

//         g.setColor(Color.GREEN); // とりあえず緑
//         // Playerクラスに getMaxHp() があると仮定して割合計算
//         // int hpWidth = (int)((p.getHp() / (double)p.getMaxHp()) * 200);
//         int hpWidth = p.getHp() * 10; // 仮計算
//         if (hpWidth < 0)
//             hpWidth = 0;

//         g.fillRect(x + 50, y - 10, hpWidth, 10);
//     }

//     // --- キー入力イベント ---
//     @Override
//     public void keyPressed(KeyEvent e) {
//         if (e.getKeyCode() < keys.length)
//             keys[e.getKeyCode()] = true;
//     }

//     @Override
//     public void keyReleased(KeyEvent e) {
//         if (e.getKeyCode() < keys.length)
//             keys[e.getKeyCode()] = false;
//     }

//     @Override
//     public void keyTyped(KeyEvent e) {
//     }
// }
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ShootingView extends JPanel implements Runnable, KeyListener {

    private MoveManager manager;
    private Thread gameThread;
    private long beforeShootTime;
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
    }

    @Override
    public void run() {
        System.out.println("ゲームループ開始"); // ★デバッグ用
        while (manager.isRunning) {
            try {
                processInput();

                // ★デバッグ用: どこで止まっているか確認
                // System.out.println("受信待機中...");

                manager.update();

                // System.out.println("画面更新");

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
                if (currentTime - beforeShootTime > 500) {
                    manager.bullets.addAll(manager.player1.tryShoot(1));
                    beforeShootTime = System.currentTimeMillis();
                }
            }
            if (keys[KeyEvent.VK_Q]) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - beforeShootTime > 500) {
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
                if (currentTime - beforeShootTime > 500) {
                    isShooting = true;
                    bulletType = 1;
                    beforeShootTime = System.currentTimeMillis();
                }
            }
            if (keys[KeyEvent.VK_Q]) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - beforeShootTime > 500) {
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
        if (manager != null) {
            manager.draw(g);
            drawUI(g);
        }
    }

    // UI描画（NetworkShooterから移植）
    private void drawUI(Graphics g) {
        // Player1 (Server) のステータス
        drawPlayerStatus(g, manager.player1, 20, HEIGHT - 100);

        // Player2 (Client) のステータス
        drawPlayerStatus(g, manager.player2, 20, 50);
    }

    private void drawPlayerStatus(Graphics g, Player p, int x, int y) {
        g.setColor(Color.WHITE);
        g.drawString("HP: " + p.getHp(), x, y);

        // HPバー
        g.setColor(Color.GRAY);
        g.fillRect(x + 50, y - 10, 200, 10);

        g.setColor(Color.GREEN); // とりあえず緑
        // Playerクラスに getMaxHp() があると仮定して割合計算
        // int hpWidth = (int)((p.getHp() / (double)p.getMaxHp()) * 200);
        int hpWidth = p.getHp() * 10; // 仮計算
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