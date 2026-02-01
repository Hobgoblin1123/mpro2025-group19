import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

// 星のアニメーション機能だけを持つ共通クラス
public class StarAnimPanel extends JPanel implements ActionListener {
    // --- アニメーション用の変数 ---
    protected Timer timer;
    private ArrayList<Star> stars = new ArrayList<>();
    private final int STAR_COUNT = 100; // 星の数

    // ★追加: 子クラスから変更できるように protected な変数にする
    protected float starMaxSize = 7.0f;   // 星の最大サイズ (デフォルト: 7px)
    protected int starMaxBrightness = 255; // 星の最大明るさ (0~255, デフォルト: 255)

    public StarAnimPanel() {
        // 背景色は描画メソッド(paintComponent)で塗るので、ここでは設定不要ですが、
        // コンポーネント自体を不透明にしておきます
        this.setOpaque(true);
        this.setBackground(Color.BLACK);

        // --- 星の初期化 ---
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star());
        }

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
    }

    // --- ★内部クラス：星の定義 ---
    // (継承先でクラス名が見えなくても動作はするのでprivateでも良いですが、もし調整したいならprotectedにします)
    protected class Star {
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
            double size = (1000 - z) / 1000 * starMaxSize; 

            // 画面範囲内なら描画
            if (sx > 0 && sx < getWidth() && sy > 0 && sy < getHeight()) {
                // 星の明るさ（近いほど白く、遠いほど暗く）
                int alpha = (int) ((1000 - z) / 1000 * starMaxBrightness);
                if (alpha < 0) alpha = 0;
                if (alpha > 255) alpha = 255;
                g2d.setColor(new Color(255, 255, 255, alpha));
                g2d.fillOval((int) sx, (int) sy, (int) size, (int) size);
            }
        }
    }
}