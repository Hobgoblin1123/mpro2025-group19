import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class StartPanel extends JPanel implements ActionListener {
    private JButton serverBtn;
    private JButton clientBtn;

    private GameFrame f;
    // Null Pointer Exception回避のため、引数にGameFrameを指定
    public StartPanel(GameFrame f) {
        this.f = f;

        // --- 1. レイアウト設定 ---
        this.setLayout(new GridLayout(3,1));

        // --- 2. 構成要素 ---
        JLabel title = new JLabel("Shooting Game", JLabel.CENTER);
        serverBtn = new JButton("ルーム作成");
        clientBtn = new JButton("ルーム参加");

        // --- 3. リスナーの登録 ---
        serverBtn.addActionListener(this);
        clientBtn.addActionListener(this);

        // --- 4. 構成要素を載せる ---
        this.add(title);
        this.add(serverBtn);
        this.add(clientBtn);
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