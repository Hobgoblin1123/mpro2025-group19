import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class ServerPanel extends JPanel implements ActionListener {
    private JLabel statusLbl;
    private JLabel portNum;
    private JButton closeBtn;
    private GameFrame f;

    public ServerPanel(GameFrame f) {
        this.f = f;
        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout());

        // --- 2. 構成要素 ---
        portNum = new JLabel("ルームID", JLabel.CENTER);
        statusLbl = new JLabel("待機中・・・", JLabel.CENTER);
        closeBtn = new JButton("戻る");

        // --- 3. リスナーの登録 ---
        closeBtn.addActionListener(this);

        // --- 4. 構成要素を載せる ---
        this.add(statusLbl, BorderLayout.CENTER);
        this.add(portNum, BorderLayout.NORTH);
        this.add(closeBtn, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeBtn) {
            f.showCard("START");
        }
    }

    public void setPort(int port) {
        portNum.setText(String.valueOf(port));
    }

    // サーバーの起動
    private void launchServer() {
        int port = (int) (Math.random() * 64511) + 1024;    // 65535 - 1024 = 64511
        setPort(port);

        System.out.println("サーバ待受開始: " + port);
    }
}
