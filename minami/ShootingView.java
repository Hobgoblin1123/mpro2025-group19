import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ShootingView extends JPanel implements Runnable, KeyListener {

    private MoveManager manager;
    private Thread gameThread;
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
            if (keys[KeyEvent.VK_LEFT]) manager.player1.moveLeft();
            if (keys[KeyEvent.VK_RIGHT]) manager.player1.moveRight();
            // Serverの発射処理はここに実装
        } else {
            // Client
            if (keys[KeyEvent.VK_LEFT]) manager.player2.moveLeft();
            if (keys[KeyEvent.VK_RIGHT]) manager.player2.moveRight();

            // ★重要: クライアントは「受信」する前に「送信」すること！
            // そうしないと、サーバー(受信待ち)とクライアント(受信待ち)で睨み合いになる
            boolean isShooting = keys[KeyEvent.VK_SPACE];
            manager.sendClientInput(isShooting, 0); 
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
    public void keyTyped(KeyEvent e) {}
}