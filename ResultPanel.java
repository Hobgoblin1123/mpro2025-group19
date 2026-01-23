import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

//  リザルト画面（宇宙背景バージョン）
public class ResultPanel extends JPanel implements ActionListener {
    private boolean isWin;
    private JLabel resLabel;
    private JButton retryButton;
    private JButton quitButton;

    // --- アニメーション用の変数 ---
    private Timer timer;
    private ArrayList<Star> stars = new ArrayList<>();
    private final int STAR_COUNT = 100; // 星の数

    //  コンストラクタ: ウィンドウの初期設定
    public ResultPanel(boolean isWin) {
        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout());
        
        // 背景色は描画メソッド(paintComponent)で塗るので、ここでは設定不要ですが、
        // コンポーネント自体を不透明にしておきます
        this.setOpaque(true);

        // --- 星の初期化 ---
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star());
        }

        // --- 2. 結果表示ラベルの作成 ---
        String message;
        String colorCode;
        this.isWin = isWin;

        if (isWin) {
            message = "You win!";
            colorCode = "#FF3333"; // 明るい赤
        } else {
            message = "You lose...";
            colorCode = "#3333FF"; // 明るい青
        }
        
        // 文字を見やすくするために影をつけたりフォントを工夫
        String resMessage = "<html><div style='text-align:center;'>"
                + "<span style='font-size:70px; color:"+colorCode+"; font-weight:bold;'>" + message + "</span>"
                + "</div></html>";
        
        this.resLabel = new JLabel(resMessage, JLabel.CENTER);
        this.add(resLabel, BorderLayout.CENTER);

        // --- 3. ボタンの作成 ---
        this.retryButton = new JButton("リトライ");
        this.quitButton = new JButton("ゲームをやめる");
        
        // パネルを作ってボタンを乗せる
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // ★重要: ボタンの裏パネルを透明にして星が見えるようにする
        buttonPanel.add(this.retryButton);
        buttonPanel.add(this.quitButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // --- 4. リスナーの登録 ---
        this.retryButton.addActionListener(this);
        this.quitButton.addActionListener(this);

        // --- 5. アニメーションタイマーの開始 ---
        // 16ミリ秒ごとに actionPerformed を呼び出す (約60FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    // --- ★描画処理（ここで背景を描く） ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Graphics2Dにキャストして画質を良くする
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 背景を真っ黒（宇宙）に塗りつぶす
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 2. 星を描画する
        g2d.setColor(Color.WHITE);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        for (Star star : stars) {
            star.draw(g2d, centerX, centerY);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // タイマーによって呼び出された場合（アニメーション更新）
        if (e.getSource() == timer) {
            for (Star star : stars) {
                star.update();
            }
            repaint(); // 再描画を要求 -> paintComponentが呼ばれる
        } 
        // ボタンが押された場合
        else if (e.getSource() == this.retryButton) {
            System.out.println("ゲームをもう一度行います");
        } else if (e.getSource() == this.quitButton) {
            System.out.println("ゲームを終了します");
        }
    }

    // --- ★内部クラス：星の定義 ---
    private class Star {
        double x, y, z; // 3次元座標的な扱い
        double speed;
        
        Star() {
            reset(true);
        }

        // 星の位置を初期化（randomize: 最初から散らばらせるかどうか）
        void reset(boolean randomize) {
            Random rand = new Random();
            x = rand.nextInt(2000) - 1000; // -1000 ~ 1000
            y = rand.nextInt(2000) - 1000;
            
            if (randomize) {
                z = rand.nextInt(1000); // 奥行き
            } else {
                z = 1000; // 遠くから出現
            }
            speed = 5 + rand.nextInt(15); // スピードのばらつき
        }

        // 位置の更新
        void update() {
            z -= speed; // 手前に近づく
            if (z <= 0) {
                reset(false); // 手前まで来たら奥に戻す
            }
        }

        // 描画
        void draw(Graphics2D g2d, int centerX, int centerY) {
            // 遠近法の計算
            double sx = (x / z) * 150 + centerX;
            double sy = (y / z) * 150 + centerY;

            // 近いほど大きく、遠いほど小さく
            double size = (1000 - z) / 1000 * 7; 

            // 画面範囲内なら描画
            if (sx > 0 && sx < getWidth() && sy > 0 && sy < getHeight()) {
                // 星の明るさ（近いほど白く、遠いほど暗く）
                int alpha = (int) ((1000 - z) / 1000 * 255);
                if (alpha < 0) alpha = 0;
                if (alpha > 255) alpha = 255;
                g2d.setColor(new Color(255, 255, 255, alpha));
                g2d.fillOval((int) sx, (int) sy, (int) size, (int) size);
            }
        }
    }
}