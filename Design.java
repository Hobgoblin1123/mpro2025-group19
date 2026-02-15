import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import java.util.function.Consumer;
import java.awt.geom.Path2D;

public interface Design {
    default void flashImage(boolean isflashed) {};
    default void setSound(GameFrame f) {};
    default void setAction(java.util.function.Consumer<Boolean> action) {};
}

//  --- 　背　景　装　飾　 ---

class BackGroundPanel extends StarAnimPanel implements Design {
    private Image bgImage_original;
    private Image bgImage_flashed;
    private boolean isFlashed;
    public BackGroundPanel(String ori_path, String flash_path) {
        this.starMaxSize = 12.0f;
        this.starMaxBrightness = 300;
        try {
            bgImage_original = new ImageIcon(getClass().getResource(ori_path)).getImage();
            bgImage_flashed = new ImageIcon(getClass().getResource(flash_path)).getImage();
        } catch (Exception e) {
            System.out.println("画像が見つかりません.");
        }
    }

    //  外部からの呼び出し関数
    @Override
    public void flashImage(boolean isFlashed) {
        this.isFlashed = isFlashed;
        if (isFlashed) {
            starMaxSpeed = 0.1f;
        } else {
            starMaxSpeed = 1f;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        //  --- メイン背景を描画 ---
        Image bgImage = isFlashed ? bgImage_flashed : bgImage_original;
        if (bgImage != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER /*重ね塗りモード */, 0.7f));    //  透化処理
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));    //  透化リセット
        }
    }
}

//  --- 　ボ　タ　ン　装　飾　 ---

class CustomButton extends JButton implements Design {
    //  ActionPerformedの処理を入れる配列
    private Consumer<Boolean> actionBox;
    private GameFrame f;

    //  調整用パラメータ
    private int offset = 8;         // 枠のずれ幅
    private Color lineColor = new Color(147, 234, 237, 100); // 線の色
    private Color shadowColor = new Color(36, 46, 133, 220); // 影の色
    private float strokeWidth = 2.5f; // 線の太さ
    private boolean isHover = false; 

    public CustomButton(String text, Font font) {
        super(text);
        this.setFont(font);
        //  標準のボタンの見た目をオフにする設定
        setContentAreaFilled(false);   // 背景の塗りつぶし
        setBorderPainted(false);       // 標準の枠線
        setFocusPainted(false);        // フォーカス時の枠線
        setOpaque(false);       // ボタンの背景を透かす

        //  ホバー時の設定(リスナーの登録と処理の記述を一括)(paintComponent内でフラグの値によって色が決まる)
        this.addMouseListener(new MouseAdapter() {
            // hover
            @Override
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                f.playSE("music/hover.wav", 1);
                if (actionBox != null) {    //  bgPanelを使っていない場合の処理
                    actionBox.accept(true);
                }
                repaint();
            }
            // leave
            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
                if (actionBox != null) {
                    actionBox.accept(false);
                }
                repaint();
            }
        });
    }

    // --------------- 実際の描画 ----------------
    @Override
    protected void paintComponent(Graphics g) {
        //  --- 1. 準備 ---
        Graphics2D g2d = (Graphics2D) g.create();   //  小数点(float, double)を使えるような描画設定をコピー

            // ---- 1.1 　ア　ン　チ　エ　イ　リ　ア　ス（線を滑らかにする設定） ----
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

            // ---- 1.2 　実　際　に　描　画　で　き　る　領　域　の　計　算　（線の太さ分少し内側にする） ----
        int pad = (int)strokeWidth;     //  線の幅は座標を中心に両側に広がる
        int skew = w / 7;
        int rectW = w - offset - pad * 2 - offset -skew;
        int rectH = h - offset - pad * 2 - offset;

            // ---- 1.3 　共　通　の　サ　イ　ズ　を　計　算　す　る　 ----
        float shapeW = rectW - offset;
        float shapeH = rectH - offset;
            // ---- 1.4 　ホ　バ　ー　時　の　色　の　変　換　 ----
        Color currentLineColor = !isHover ? lineColor : shadowColor;
        Color currentShadowColor = !isHover ? shadowColor : lineColor;
            //  ---- 1.5 　フ　ォ　ン　ト　の　読　み　込　み　 ----
        if (getFont() != null) {
            g2d.setFont(getFont());
        }

        // --- 2. 影　の　描　画　 (座標を +offset するだけ)　 ---
        Path2D.Float p = new Path2D.Float();
        p.moveTo(skew + offset, offset);            // 左上
        p.lineTo(skew + shapeW + offset, offset);   // 右上
        p.lineTo(shapeW + offset, shapeH + offset); // 右下
        p.lineTo(offset, shapeH + offset);          // 左下
        p.closePath();

        g2d.setColor(currentShadowColor); // 影の色
        g2d.fill(p);

        // --- 3. 　手　前　の　枠　を　描　く　 ---
        Path2D.Float q = new Path2D.Float();
        q.moveTo(skew, 0);              // 左上
        q.lineTo(skew + shapeW, 0);     // 右上
        q.lineTo(shapeW, shapeH);   // 右下
        q.lineTo(0, shapeH);    // 左下
        q.closePath();

        g2d.setColor(currentLineColor); // 手前の色（枠線など）
        g2d.fill(q);

        // --- 4. テ　キ　ス　ト　を　描　く　 ---
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

    //  インターフェースの実装
    @Override
    public void setSound(GameFrame f) { this.f = f;}
    @Override
    public void setAction(Consumer<Boolean> action) { this.actionBox = action;}
}