import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;


//  リザルト画面
public class ResultPanel extends JPanel {
    private boolean isWin;
    private JLabel resLabel;
    private JButton retryButton;
    private JButton quitButton;

    //  コンストラクタ: ウィンドウの初期設定
    public ResultPanel(boolean isWin) {
        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout()); // BorderLayoutを採用
        this.setBackground(new Color(0, 0, 0, 150)); // 透過背景色
        this.setBackground(Color.DARK_GRAY);

        // --- 2. 結果表示ラベルの作成 ---
        String message;
        String colorCode;

        this.isWin = isWin;
        if (isWin) {
            message = "You win!";
            colorCode = "RED";
        } else {
            message = "You lose...";
            colorCode = "BLUE";
        }
        String resMessage = "<html><span style='font-size:50px; color:"+colorCode+";'>"+message+"</span></html>";
        this.resLabel = new JLabel(resMessage, JLabel.CENTER);
        this.add(resLabel, BorderLayout.CENTER);

        // --- 3. ボタンの作成 ---
        JPanel buttonPanel;
        this.retryButton = new JButton("リトライ");
        this.quitButton = new JButton("ゲームをやめる");
        buttonPanel = new JPanel();
        buttonPanel.add(this.retryButton);
        buttonPanel.add(this.quitButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
}
