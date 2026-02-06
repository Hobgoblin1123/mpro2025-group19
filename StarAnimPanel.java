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

    public StarAnimPanel() {
        this.setOpaque(true);
        this.setBackground(Color.BLACK);

        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star());
        }

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 背景の描画
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
            this.speedFactor = 5 + rand.nextInt(10); // スピードのばらつき
        }

        // 位置の更新
        void update() {
            z -= starMaxSpeed * speedFactor; // 手前に近づく
            if (z <= 0) {
                reset(false); // 手前まで来たら奥に戻す
            }
        }

        // 描画
        void draw(Graphics2D g2d, int centerX, int centerY) {
            // 遠近法の計算
            if (z <= 0) z = 0.1; // ゼロ除算防止
            
            double sx = (x / z) * 150 + centerX;
            double sy = (y / z) * 150 + centerY;

            // 近いほど大きく、遠いほど小さく
            double size = (1000 - z) / 1000 * starMaxSize; 
            // 最小サイズ保証はないほうが遠くで消えて綺麗に見える場合もありますが、
            // 元のロジックに合わせて調整してください

            int brightness = (int) ((1000 - z) / 1000 * starMaxBrightness);

            if (brightness < 0) brightness = 0;
            if (brightness > 255) brightness = 255;
            
            g2d.setColor(new Color(255, 255, 255, brightness));
            g2d.fillOval((int) sx, (int) sy, (int) size, (int) size);
        }
    }
}