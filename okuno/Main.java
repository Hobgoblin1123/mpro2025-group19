import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // SwingのGUI処理はイベントディスパッチスレッドで行うのが安全です
        SwingUtilities.invokeLater(() -> {
            // 1. ウィンドウ(JFrame)の作成
            JFrame frame = new JFrame("ゲーム結果");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 閉じた時にプログラムを終了
            frame.setSize(400, 300); // ウィンドウのサイズ（適宜変更可）
            frame.setLocationRelativeTo(null); // 画面中央に表示

            // 2. あなたが作ったResultPanelを作成
            // 引数に true を渡すと「Win」、false を渡すと「Lose」になります
            ResultPanel resultPanel = new ResultPanel(true); 

            // 3. ウィンドウにパネルを追加
            frame.add(resultPanel);

            // 4. ウィンドウを表示
            frame.setVisible(true);
        });
    }
}