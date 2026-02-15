import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;

import javax.imageio.ImageIO;
import javax.swing.*;

// ロジックはそのまま、見た目を宇宙・レーダー風に改装
public class ServerPanel extends StarAnimPanel implements ActionListener {
    private CustomButton closeBtn;
    private GameFrame f;
    private boolean isWaiting = false;

    // 描画用データ保持変数
    private String displayIP = "取得中...";
    private String displayPort = "---";
    private String displayStatus = "INITIALIZING...";
    
    // アニメーション用
    private double radarAngle = 0;
    private Image bgImage;

    public ServerPanel(GameFrame f) {
        super(); // 星のアニメーション開始
        this.f = f;
        Font pixelFont = FontLoader.loadFont("/Fonts/DotGothic16-Regular.ttf", 40f);
        
        // --- 1. 　レ　イ　ア　ウ　ト　設　定　 ---
        this.setLayout(new BorderLayout());
        
        try {
            bgImage = new ImageIcon(getClass().getResource("/images/server-clientpanel.png")).getImage();
        } catch (Exception e) {
            System.out.println("背景画像が見つかりません: " + e.getMessage());
        }


        // --- 2. 　構　成　要　素　 (ボタンのみ配置、テキストはpaintComponentで描画) ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);

        closeBtn = new CustomButton("CANCEL", pixelFont); // "戻る" -> 英語表記に変更（機能は同じ）
        closeBtn.setPreferredSize(new Dimension(300, 100));
        closeBtn.setSound(f);
        bottomPanel.add(closeBtn);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0)); // 下に余白
        this.add(bottomPanel, BorderLayout.SOUTH);

        // --- 3. 　リ　ス　ナ　ー　の　登　録　 ---
        closeBtn.addActionListener(this);

        // --- 4. 　画　面　表　示　時　の　イ　ベ　ン　ト　 ---
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                serverWait();
            }
        });
    }

    // サーバー待機ロジック
    public void serverWait() {
        if (isWaiting) return;  // 二重起動防止
        isWaiting = true;

        // --- 1. ラ　ン　ダ　ム　ポ　ー　ト　番　号　設　定　 ---
        int port = (int) (Math.random() * 64511) + 1024;    // 65535 - 1024 = 64511
        
        // ラベルではなく描画用変数にセット
        this.displayPort = String.valueOf(port);
        this.displayStatus = "WAITING FOR CONNECTION..."; 

        // --- 2. 自　分　の　I　P　ア　ド　レ　ス　を　取　得　 ---
        String myIP = "UNKNOWN";
        try {
            myIP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.displayIP = myIP;

        // --- 3. 別　ス　レ　ッ　ド　で　接　続　待　ち　を　開　始　 ---
        new Thread(() -> {
            try {
                // --- 4. Commサーバーをインスタンス化 ---
                CommServer sv = new CommServer(port);
                f.setCommSV(sv);

                System.out.println("接続に成功しました");
                
                // 状態更新
                displayStatus = "CONNECTION ESTABLISHED!";
                repaint();

                // --- 5. 接続成功後、ゲームを実行 ---
                SwingUtilities.invokeLater(() -> {
                    f.startGame(true, sv);
                    isWaiting = false;
                });
            } catch (Exception e){      // エラー処理
                e.printStackTrace();
                isWaiting = false;
                SwingUtilities.invokeLater(() -> {
                    displayStatus = "CONNECTION ERROR";
                    repaint();
                });
            }
        }).start();     // Threadの起動
    }

    // --- 描画処理 (ラベルの代わりに直接描画) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 星背景を描画
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        int cx = getWidth() / 2;
        int cy = getHeight() / 2 - 30;
        int r = 130; // レーダー半径

        // 1. レーダー円
        g2d.setColor(new Color(0, 255, 100, 100)); // サイバーグリーン
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(cx - r, cy - r, r * 2, r * 2);
        g2d.drawOval(cx - r/2, cy - r/2, r, r);
        g2d.drawLine(cx - r, cy, cx + r, cy);
        g2d.drawLine(cx, cy - r, cx, cy + r);

        // 2. 走査線（くるくる回る）
        if (isWaiting) {
            int lx = cx + (int)(r * Math.cos(radarAngle));
            int ly = cy + (int)(r * Math.sin(radarAngle));
            GradientPaint gp = new GradientPaint(cx, cy, new Color(0, 255, 100, 0), lx, ly, new Color(0, 255, 100, 180));
            g2d.setPaint(gp);
            g2d.fillArc(cx - r, cy - r, r * 2, r * 2, (int)Math.toDegrees(-radarAngle), 60);
        }

        // 3. 情報テキスト描画
        g2d.setColor(Color.WHITE);
        
        // ステータス
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        drawCenteredString(g2d, displayStatus, cx, cy - r - 30);

        // IPとポート
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.setColor(Color.CYAN);
        drawCenteredString(g2d, "SERVER IP : " + displayIP, cx, cy + r + 40);
        drawCenteredString(g2d, "ROOM ID (PORT) : " + displayPort, cx, cy + r + 70);
    }

    private void drawCenteredString(Graphics2D g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x - fm.stringWidth(text) / 2, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeBtn) {
            // 待機キャンセル等の処理が必要ならここに記述
            f.playSE("music/back.wav", 1);
            f.showCard("START");
            f.stopBGM();
            f.playBGM("music/main.wav", 0.3f);
        }
        // 星のアニメーション用
        updateStars();
        
        // レーダーの回転
        radarAngle += 0.05;
        if(radarAngle > Math.PI * 2) radarAngle = 0;
        repaint();
    }
}