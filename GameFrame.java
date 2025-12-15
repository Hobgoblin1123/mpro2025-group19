import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//  動作確認用
public class GameFrame extends JFrame {
    public GameFrame() {
        this.setTitle("GAME");
        this.setSize(1200,800);

        // ダミーのベースパネルを作成（ゲーム画面の代わり）
        JPanel gameScreen = new JPanel();
        gameScreen.setBackground(Color.BLACK);
        gameScreen.setPreferredSize(new Dimension(800, 600));

        boolean isWin = true;
        ResultPanel result = new ResultPanel(isWin);
        this.setLayout(new BorderLayout());
        this.add(result, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    // ×を押したら閉じる
        this.setVisible(true);
    }
    public static void main(String argv[]) {
        new GameFrame();
    }
}