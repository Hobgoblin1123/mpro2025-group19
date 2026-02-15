import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.IOException;
import javax.swing.*;

public class ResultPanel extends JPanel implements ActionListener {
    private CustomButton retryButton;
    private CustomButton quitButton;
    private BackGroundPanel bgPanel;
    private GameFrame f;
    private boolean isWin;

    public ResultPanel(GameFrame f, boolean isWin) {
        this.f = f;
        Font pixelFont = FontLoader.loadFont("/Fonts/DotGothic16-Regular.ttf", 40f);
        Font resFont = FontLoader.loadFont("/Fonts/DotGothic16-Regular.ttf", 80f);
        this.isWin = isWin;

        // --- 1. レ　イ　ア　ウ　ト　設　定　 ---
        this.setLayout(new BorderLayout());

        // --- 2. 構　成　要　素　 (背景パネル) ---
        bgPanel = new BackGroundPanel("/images/result.png", "/images/resultflashed.png");
        bgPanel.setLayout(new GridBagLayout()); // 中央寄せ用
        GridBagConstraints gbc = new GridBagConstraints();

        // --- 3. 勝　敗　表　示　ラ　ベ　ル　 ---
        String message = isWin ? "YOU WIN!" : "YOU LOSE...";
        Color textColor = isWin ? new Color(255, 50, 50) : new Color(50, 50, 255); // 勝ちは赤、負けは青

        JLabel resLabel = new JLabel(message, JLabel.CENTER);
        resLabel.setFont(resFont);
        resLabel.setForeground(textColor);
        
        // --- 4. ボ　タ　ン　パ　ネ　ル　 ---
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 20)); // 縦に並べる
        retryButton = new CustomButton("RETRY", pixelFont);
        quitButton = new CustomButton("QUIT", pixelFont);

        // ボ　タ　ン　設　定
        btnPanel.setOpaque(false);
        Dimension btnSize = new Dimension(400, 100);
        retryButton.setPreferredSize(btnSize);
        quitButton.setPreferredSize(btnSize);
        retryButton.setAction(isHover -> bgPanel.flashImage(isHover));
        retryButton.setSound(f);
        quitButton.setAction(isHover -> bgPanel.flashImage(isHover));
        quitButton.setSound(f);

        // --- 5. 配　置 (GridBagLayout) ---
        
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

        // --- 6. リ　ス　ナ　ー　登　録　 ---
        retryButton.addActionListener(this);
        quitButton.addActionListener(this);

        // --- 7. パ　ネ　ル　に　追　加　 ---
        btnPanel.add(retryButton);
        btnPanel.add(quitButton);
        this.add(bgPanel);
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
}