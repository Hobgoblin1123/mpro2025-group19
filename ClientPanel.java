import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class ClientPanel extends JPanel implements ActionListener {
    private JButton closeBtn;
    private JButton connectBtn;
    private JTextField idField;
    private JTextField ipField; // ipアドレス用
    private JLabel statusLbl;
    private GameFrame f;

    public ClientPanel(GameFrame f) {
        this.f = f;
        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout());

        // --- 2. 構成要素 ---
        JPanel centerPanel = new JPanel(new FlowLayout());
        JLabel instruct = new JLabel("⇩ルームIDを入力してください⇩");
        JLabel ipLabel = new JLabel("IPアドレス:");
        ipField = new JTextField("localhost", 10); // デフォルト値をlocalhostにしておく
        statusLbl = new JLabel(" ", JLabel.CENTER);
        idField = new JTextField(10);
        connectBtn = new JButton("接続");
        closeBtn = new JButton("戻る");

        // --- 3. リスナーの登録 ---
        connectBtn.addActionListener(this);
        closeBtn.addActionListener(this);
        idField.addActionListener(this);
        ipField.addActionListener(this);

        // --- 4. 構成要素を載せる ---
        this.add(centerPanel, BorderLayout.CENTER);
        centerPanel.add(ipLabel);
        centerPanel.add(ipField);
        centerPanel.add(instruct);
        centerPanel.add(idField);
        centerPanel.add(connectBtn);
        this.add(statusLbl, BorderLayout.NORTH);
        this.add(closeBtn, BorderLayout.SOUTH);

        // --- +α. 画面が表示されるたびに状態をリセットするリスナー ---
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // リセットする処理
                statusLbl.setText(" ");
                statusLbl.setForeground(Color.BLACK);
                connectBtn.setEnabled(true);
                idField.setText("");
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeBtn) {
            f.playSE("music/back.wav", 1);
            f.showCard("START");
            f.stopBGM();
            f.playBGM("music/main.wav", 1);
        } else if (e.getSource() == connectBtn || e.getSource() == idField) {      //  JTextFieldは、Enter入力の機能を潜在的に持っているのでActionListenerでよい
            f.playSE("music/go.wav", 1);
            String id = idField.getText();
            String ip = ipField.getText();

            //  入力チェック
            if (id.isEmpty()) return;
            try {
                int port = Integer.parseInt(id);
                connecToServer(ip, port);
            } catch (NumberFormatException ex) {
                //  数字以外の文字が入力された場合の処理
                statusLbl.setText("数字を入力してください");
                statusLbl.setForeground(Color.RED);
            }
        }
    }

    //  クライアント接続ロジック
    private void connecToServer(String ip, int port) {
        statusLbl.setText("接続中・・・(" + ip + ":" + port + ")");
        statusLbl.setForeground(Color.GREEN);
        connectBtn.setEnabled(false);

        new Thread(() -> {
            try {
                //  ここでCommClientをインスタンス化
                CommClient cl = new CommClient(ip, port);
                
                System.out.println("接続に成功しました");

                SwingUtilities.invokeLater(() -> {
                    f.startGame(false, cl);
                    connectBtn.setEnabled(true);    //  ゲーム終了後にもう一度ClientPanelに戻ってきた場合の処理
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    statusLbl.setText("接続に失敗しました");
                    statusLbl.setForeground(Color.RED);
                    connectBtn.setEnabled(true);
                });
            }
        }).start();
    }
}
