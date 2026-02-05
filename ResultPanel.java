import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ResultPanel extends JPanel implements ActionListener {
    private CustomButton retryButton;
    private CustomButton quitButton;
    private BackGroundPanel bgPanel;
    private GameFrame f;
    private boolean isWin;

    public ResultPanel(GameFrame f, boolean isWin) {
        this.f = f;
        this.isWin = isWin;

        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout());

        // --- 2. 構成要素 (背景パネル) ---
        bgPanel = new BackGroundPanel();
        bgPanel.setLayout(new GridBagLayout()); // 中央寄せ用
        GridBagConstraints gbc = new GridBagConstraints();

        // --- 3. 勝敗表示ラベル ---
        String message = isWin ? "YOU WIN!" : "YOU LOSE...";
        Color textColor = isWin ? new Color(255, 50, 50) : new Color(50, 50, 255); // 勝ちは赤、負けは青

        JLabel resLabel = new JLabel(message, JLabel.CENTER);
        resLabel.setFont(new Font("Monospaced", Font.BOLD, 80)); // フォントサイズを大きく
        resLabel.setForeground(textColor);
        
        // --- 4. ボタンパネル ---
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 20)); // 縦に並べる
        retryButton = new CustomButton("RETRY");
        quitButton = new CustomButton("QUIT");

        // ボタン設定
        btnPanel.setOpaque(false);
        Dimension btnSize = new Dimension(400, 100);
        retryButton.setPreferredSize(btnSize);
        quitButton.setPreferredSize(btnSize);

        // --- 5. 配置 (GridBagLayout) ---
        
        // (1) 勝敗ラベル
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.6; // 上側の比率
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        bgPanel.add(resLabel, gbc);

        // (2) ボタンエリア
        gbc.gridy = 1;
        gbc.weighty = 0.4; // 下側の比率
        gbc.anchor = GridBagConstraints.NORTH; // 中央より少し上に寄せる
        bgPanel.add(btnPanel, gbc);

        // --- 6. リスナー登録 ---
        retryButton.addActionListener(this);
        quitButton.addActionListener(this);

        // --- 7. パネルに追加 ---
        btnPanel.add(retryButton);
        btnPanel.add(quitButton);
        this.add(bgPanel);
    }

    // --- フラッシュ演出の中継メソッド ---
    public void flash(boolean isflashed) {
        bgPanel.flashImage(isflashed);
    }

    // =========================================================================
    //  内部クラス: 背景カスタム (StartPanelと同じ仕組み)
    // =========================================================================
    class BackGroundPanel extends StarAnimPanel {
        private Image bgImage_original;
        private Image bgImage_flashed;
        private boolean isflashed;

        public BackGroundPanel() {
            // リザルト画面なので星は少し派手または落ち着いた設定に
            this.starMaxSize = 12.0f;
            this.starMaxBrightness = 300;
            
            try {
                // スタート画面と同じ画像を使うか、リザルト用の画像があれば変更してください
                bgImage_original = new ImageIcon(getClass().getResource("./images/result.png")).getImage();
                bgImage_flashed = new ImageIcon(getClass().getResource("./images/resultflashed.png")).getImage();
            } catch (Exception e) {
                System.out.println("背景画像が見つかりません (ResultPanel)");
            }
        }

        // 外部からの呼び出し関数
        public void flashImage(boolean isflashed) {
            this.isflashed = isflashed;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // 画像の描画
            Image bgImage = isflashed ? bgImage_flashed : bgImage_original;
            if (bgImage != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f)); // 透化
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // リセット
            }
        }
    }

    // =========================================================================
    //  内部クラス: カスタムボタン (StartPanelと全く同じデザイン)
    // =========================================================================
    class CustomButton extends JButton {
        private int offset = 8;
        private Color lineColor = new Color(147, 234, 237, 100);
        private Color shadowColor = new Color(36, 46, 133, 220);
        private float strokeWidth = 2.5f;
        private Font pixelFont;
        private boolean isHover = false;

        public CustomButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);

            // フォント設定
            try {
                File fontFile = new File("Fonts/DotGothic16-Regular.ttf");
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                pixelFont = baseFont.deriveFont(Font.BOLD, 40);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(baseFont);
            } catch (FontFormatException | IOException e) {
                pixelFont = new Font("Monospaced", Font.BOLD, 40);
            }

            // マウスリスナー (ホバー時に親パネルのflashを呼ぶ)
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHover = true;
                    f.playSE("music/hover.wav", 1);
                    ResultPanel.this.flash(isHover); // ★ここが重要
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    ResultPanel.this.flash(isHover); // ★ここが重要
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (pixelFont != null) g2d.setFont(pixelFont);

            int w = getWidth();
            int h = getHeight();
            int pad = (int)strokeWidth;
            int skew = w / 7;
            int rectW = w - offset - pad * 2 - offset - skew;
            int rectH = h - offset - pad * 2 - offset;
            float shapeW = rectW - offset;
            float shapeH = rectH - offset;

            Color currentLineColor = !isHover ? lineColor : shadowColor;
            Color currentShadowColor = !isHover ? shadowColor : lineColor;

            // 影
            Path2D.Float p = new Path2D.Float();
            p.moveTo(skew + offset, offset);
            p.lineTo(skew + shapeW + offset, offset);
            p.lineTo(shapeW + offset, shapeH + offset);
            p.lineTo(offset, shapeH + offset);
            p.closePath();
            g2d.setColor(currentShadowColor);
            g2d.fill(p);

            // 枠
            Path2D.Float q = new Path2D.Float();
            q.moveTo(skew, 0);
            q.lineTo(skew + shapeW, 0);
            q.lineTo(shapeW, shapeH);
            q.lineTo(0, shapeH);
            q.closePath();
            g2d.setColor(currentLineColor);
            g2d.fill(q);

            // テキスト
            String text = getText();
            FontMetrics fm = g2d.getFontMetrics();
            float centerX = pad + skew / 2.0f + (shapeW / 2.0f);
            float centerY = pad + shapeH / 2.0f;
            int textX = (int)(centerX - fm.stringWidth(text) / 2);
            int textY = (int)(centerY - fm.getAscent() / 2) + fm.getAscent() - offset;

            g2d.setColor(currentShadowColor);
            g2d.drawString(text, textX, textY);
            g2d.dispose();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // ボタンが押された場合
        if (e.getSource() == this.retryButton) {
            f.playSE("music/go.wav", 1);
            // 連打防止＆状態表示
            this.retryButton.setEnabled(false);
            this.quitButton.setEnabled(false);
            this.retryButton.setText("WAITING...");

            System.out.println("ゲームをもう一度行います");
            f.tryRetry();
        } else if (e.getSource() == this.quitButton) {
            f.playSE("music/back.wav", 1);
            System.out.println("ゲームを終了します");
            f.sendMessage("QUIT");  //  backToStart()内に記述すると、リトライ失敗後にbackToStart()するので余計なメッセージ送信をしてしまう
            f.backToStart();
        }
        
    }
    public ResultPanel(){}
}