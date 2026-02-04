import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

// 星のアニメーション機能だけを持つ共通クラス
public class StarAnimPanel extends JPanel implements ActionListener {
    protected Timer timer;
    private ArrayList<Star> stars = new ArrayList<>();
    private final int STAR_COUNT = 200; // 星の数

    protected float starMaxSize = 7.0f;
    protected int starMaxBrightness = 255;
    protected double starMaxSpeed = 1;
    
    // ★追加: 横移動モードかどうかのフラグ
    protected boolean isHorizontal = false; 

    public StarAnimPanel() {
        this.setOpaque(true);
        this.setBackground(Color.BLACK);

        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star());
        }

        timer = new Timer(16, this);
        timer.start();
    }
    
    // ★追加: 横移動モードをセットするメソッド
    public void setHorizontalMode(boolean enable) {
        this.isHorizontal = enable;
        // モード切替時に星の位置を再配置する
        for (Star s : stars) {
            s.reset(true);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 背景の描画 (ShootingViewでsetBackgroundしてあればそれが使われる)
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        for (Star star : stars) {
            star.update();
            star.draw(g2d, centerX, centerY);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    // 内部クラス: 星
    class Star {
        double x, y, z;
        double speedFactor;

        Star() {
            reset(true);
        }

        void reset(boolean randomize) {
            Random rand = new Random();
            
            if (isHorizontal) {
                // --- 横移動用の初期化 ---
                // 画面全体に散らす
                x = rand.nextInt(2000) - 1000; 
                y = rand.nextInt(1200) - 600;
                // zは「奥行き（遠近感）」として使う（1〜1000）
                z = rand.nextInt(1000) + 1; 
            } else {
                // --- ワープ用の初期化 (元のロジック) ---
                x = rand.nextInt(2000) - 1000;
                y = rand.nextInt(2000) - 1000;
                if (randomize) {
                    z = rand.nextInt(1000);
                } else {
                    z = 1000;
                }
            }
            this.speedFactor = 5 + rand.nextInt(10);
        }

        void update() {
            if (isHorizontal) {
                // --- 横移動のロジック ---
                // 近く(zが小さい)ほど速く動く（パララックス効果）
                double parallaxSpeed = starMaxSpeed * speedFactor * (1000.0 / z) * 0.1;
                x -= parallaxSpeed; // 左へ移動

                // 左端(画面外)に行ったら右端に戻す
                if (x < -1000) {
                    x = 1000;
                    y = new Random().nextInt(1200) - 600; // Y座標もランダムに変える
                }
            } else {
                // --- ワープのロジック (元のまま) ---
                z -= starMaxSpeed * speedFactor;
                if (z <= 0) {
                    reset(false);
                }
            }
        }

        void draw(Graphics2D g2d, int centerX, int centerY) {
            double sx, sy, size;
            int brightness;

            if (isHorizontal) {
                // --- 横移動の描画 ---
                // 3D割算をせず、そのまま座標として使う
                sx = x + centerX;
                sy = y + centerY;

                // 遠く(zが大きい)ほど小さく、暗く
                double scale = (1000.0 - z) / 1000.0; // 0.0 ~ 1.0
                size = starMaxSize * scale;
                // 最小サイズ保証
                if (size < 1.0) size = 1.0;

                brightness = (int)(starMaxBrightness * scale);

            } else {
                // --- ワープの描画 (元のまま) ---
                if (z <= 0) z = 0.1; 
                sx = (x / z) * 150 + centerX;
                sy = (y / z) * 150 + centerY;
                size = (1000 - z) / 1000 * starMaxSize;
                brightness = (int) ((1000 - z) / 1000 * starMaxBrightness);
            }

            if (brightness < 0) brightness = 0;
            if (brightness > 255) brightness = 255;

            g2d.setColor(new Color(255, 255, 255, brightness));
            g2d.fillOval((int) sx, (int) sy, (int) size, (int) size);
        }
    }
}