import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

//  リザルト画面（宇宙背景バージョン）
public class ResultPanel extends StarAnimPanel implements ActionListener {
    private boolean isWin;
    private JLabel resLabel;
    private JButton retryButton;
    private JButton quitButton;
    private GameFrame f;

    //  コンストラクタ: ウィンドウの初期設定
    public ResultPanel(GameFrame f, boolean isWin) {
        this.f = f;

        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout()); // BorderLayoutを採用

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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // ボタンが押された場合
        if (e.getSource() == this.retryButton) {
            f.playSE("music/go.wav", 1);
            // 連打防止＆状態表示
            this.retryButton.setEnabled(false);
            this.quitButton.setEnabled(false);
            this.retryButton.setText("相手の応答待ち...");

            System.out.println("ゲームをもう一度行います");
            f.tryRetry();
        } else if (e.getSource() == this.quitButton) {
            f.playSE("music/back.wav", 1);
            System.out.println("ゲームを終了します");
            f.sendMessage("QUIT");  //  backToStart()内に記述すると、リトライ失敗後にbackToStart()するので余計なメッセージ送信をしてしまう
            f.backToStart();
        }
        //  親アニメーション(星)を動かす
        super.actionPerformed(e);
    }
}