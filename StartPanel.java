import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class StartPanel extends JPanel implements ActionListener {
    private CustomButton serverBtn;
    private CustomButton clientBtn;
    private CustomButton exitBtn;
    private BackGroundPanel bgPanel;

    private GameFrame f;
    // Null Pointer Exception回避のため、引数にGameFrameを指定
    public StartPanel(GameFrame f) {
        this.f = f;

        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout());

        // --- 2. 構成要素 ---
        bgPanel = new BackGroundPanel();
        bgPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel title = new JLabel(new ImageIcon("images/title_image_transparent.png"), JLabel.CENTER);
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 20));
        serverBtn = new CustomButton("CREATE");
        clientBtn = new CustomButton("JOIN");
        exitBtn = new CustomButton("EXIT");

        // --- 3. 装飾 ----
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        btnPanel.setOpaque(false); // ボタンはデフォルトだと背景が不透明(Opaque)
        // ---- 3.1. ボタンのサイズ感を調整 ----
        Dimension btnSize = new Dimension(400, 100);
        serverBtn.setPreferredSize(btnSize);
        clientBtn.setPreferredSize(btnSize);
        exitBtn.setPreferredSize(btnSize);

        bgPanel.add(title, gbc);
        // ---- 3.2. タイトル部分の設定 ----
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8; // 上部の空き・画像スペースの比率（大きくすると下に押し下げられる）
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(100, 0, 0, 0);
        // ---- 3.3. ボタン部分の設定 ----
        gbc.gridy = 1;
        gbc.weighty = 0.4; // 下部のボタンエリアの比率
        gbc.anchor = GridBagConstraints.CENTER;

        bgPanel.add(btnPanel, gbc);
        
        // --- 4. リスナーの登録 ---
        serverBtn.addActionListener(this);
        clientBtn.addActionListener(this);

        // --- 5. 構成要素を載せる ---
        this.add(bgPanel);
        btnPanel.add(serverBtn);
        btnPanel.add(clientBtn);
        btnPanel.add(exitBtn);
    }
    //  flashImageの中継関数(ボタンから呼び出すため)
    public void flash(boolean isflashed) {
        bgPanel.flashImage(isflashed);
    }  

    //  背景カスタム
    class BackGroundPanel extends StarAnimPanel {
        private Image bgImage_original;
        private Image bgImage_flashed;
        private Color baseColor = Color.BLUE;
        private boolean isflashed;

        public BackGroundPanel() {
            this.starMaxSize = 10.0f;
            this.starMaxBrightness = 300;
            try {
                bgImage_original = ImageIO.read(new File("images/start_image.jpg"));
                bgImage_flashed = ImageIO.read(new File("images/start_image_flashed.jpg"));
            } catch (IOException e) {
                System.out.println("画像が見つかりません.");
            }
        }
        //  外部からの呼び出し関数
        public void flashImage(boolean isflashed) {
            this.isflashed = isflashed;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            //  --- メイン背景を描画 ---
            Image bgImage = isflashed ? bgImage_flashed : bgImage_original;
            if (bgImage != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER /*重ね塗りモード */, 0.7f));    //  透化処理
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));    //  透化リセット
            }
        }
    }

    //  ボタンカスタム
    class CustomButton extends JButton {
        // 調整用パラメータ
        private int offset = 8;         // 枠のずれ幅
        private Color lineColor = new Color(147, 234, 237, 100); // 線の色
        private Color shadowColor = new Color(36, 46, 133, 220); // 影の色
        private float strokeWidth = 2.5f; // 線の太さ
        private Font pixelFont;
        private boolean isHover = false;    
    
        public CustomButton(String text) {
            super(text);
            // 標準のボタンの見た目をオフにする設定
            setContentAreaFilled(false);   // 背景の塗りつぶし
            setBorderPainted(false);       // 標準の枠線
            setFocusPainted(false);        // フォーカス時の枠線
            setOpaque(false);       // ボタンの背景を透かす

            //  フォントの登録
            try {
                // fontsフォルダに入れたファイル名を指定
                File fontFile = new File("Fonts/DotGothic16-Regular.ttf");
                // フォントを作成 (サイズは後で deriveFont で変えられる)
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                // 太字(BOLD)でサイズ24にする
                pixelFont = baseFont.deriveFont(Font.BOLD, 40); 
                // グラフィックス環境に登録（これをしておくとシステム全体で認識されることもある）
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(baseFont);
    
            } catch (FontFormatException | IOException e) {
                e.printStackTrace();
                // 読み込み失敗時はデフォルトのフォントを使うなどの保険
                pixelFont = new Font("Monospaced", Font.BOLD, 40); 
            }

            //  ホバー時の設定(リスナーの登録と処理の記述を一括)(paintComponent内でフラグの値によって色が決まる)
            this.addMouseListener(new MouseAdapter() {
                // hover
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHover = true;
                    StartPanel.this.flash(isHover);
                    repaint();
                }
                // leave
                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    StartPanel.this.flash(isHover);
                    repaint();
                }
            }); 
        }
    
        // --------------- 実際の描画 ----------------
        @Override
        protected void paintComponent(Graphics g) {
            //  --- 1. 準備 ---
            Graphics2D g2d = (Graphics2D) g.create();   //  小数点(float, double)を使えるような描画設定をコピー
    
                // アンチエイリアス（線を滑らかにする設定）
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (pixelFont != null) {
                g2d.setFont(pixelFont);
            }
    
            int w = getWidth();
            int h = getHeight();

                // 実際に描画できる領域の計算（線の太さ分少し内側にする）
            int pad = (int)strokeWidth;     //  線の幅は座標を中心に両側に広がる
            int skew = w / 7;
            int rectW = w - offset - pad * 2 - offset -skew;
            int rectH = h - offset - pad * 2 - offset;
    
                // --- 共通のサイズを計算する ---
            float shapeW = rectW - offset;
            float shapeH = rectH - offset;
                // --- ホバー時の色の変換
            Color currentLineColor = !isHover ? lineColor : shadowColor;
            Color currentShadowColor = !isHover ? shadowColor : lineColor;

            // --- 2. 影の描画 (座標を +offset するだけ) ---
            Path2D.Float p = new Path2D.Float();
            p.moveTo(skew + offset, offset);            // 左上
            p.lineTo(skew + shapeW + offset, offset);   // 右上
            p.lineTo(shapeW + offset, shapeH + offset); // 右下
            p.lineTo(offset, shapeH + offset);          // 左下
            p.closePath();

            g2d.setColor(currentShadowColor); // 影の色
            g2d.fill(p);

            // --- 3. 手前の枠を描く ---
            Path2D.Float q = new Path2D.Float();
            q.moveTo(skew, 0);              // 左上
            q.lineTo(skew + shapeW, 0);     // 右上
            q.lineTo(shapeW, shapeH);   // 右下
            q.lineTo(0, shapeH);    // 左下
            q.closePath();

            g2d.setColor(currentLineColor); // 手前の色（枠線など）
            g2d.fill(q);
    
            // --- 4. テキストを描く ---
            String text = getText();
            FontMetrics fm = g2d.getFontMetrics();
            
            // 「手前の緑色のエリア」の中心を計算
            // 左端(pad) + 傾き半分(skew/2) + 幅半分(shapeW/2)
            float centerX = pad + skew / 2.0f + (shapeW / 2.0f);
            float centerY = pad + shapeH / 2.0f;

            int textX = (int)(centerX - fm.stringWidth(text) / 2);
            int textY = (int)(centerY - fm.getAscent() / 2) + fm.getAscent() - offset;
            
            g2d.setColor(currentShadowColor); // 文字色
            g2d.drawString(text, textX, textY);

            g2d.dispose();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == serverBtn) {
            f.showCard("SERVER");
        } else if (e.getSource() == clientBtn) {
            f.showCard("CLIENT");
        }
    }
}