import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientPanel extends JPanel implements ActionListener, KeyListener {
    private JButton closeBtn;
    private JTextField idField;
    private GameFrame f;

    public ClientPanel(GameFrame f) {
        this.f = f;
        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout());

        // --- 2. 構成要素 ---
        // 案内ラベルを見やすく
        JLabel instruct = new JLabel("⇩ ルームID(ポート番号)を入力してEnter ⇩", JLabel.CENTER);
        instruct.setFont(new Font("MS Gothic", Font.BOLD, 16));
        
        idField = new JTextField(10);
        idField.setFont(new Font("Arial", Font.PLAIN, 20)); // 入力文字を大きく
        idField.setHorizontalAlignment(JTextField.CENTER);  // 真ん中に文字が出るように
        
        closeBtn = new JButton("戻る");

        // --- 3. リスナーの登録 ---
        closeBtn.addActionListener(this);
        idField.addKeyListener(this);

        // --- 4. 構成要素を載せる ---
        // レイアウトを少し整えるためのパネル
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(idField);

        this.add(instruct, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(closeBtn, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeBtn) {
            f.showCard("START");
        }
    }

    // Enterキー押下時(ルームID確定)
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            String idText = idField.getText().trim(); // 空白除去
            
            if (idText.isEmpty()) return;

            try {
                // 1. 入力されたID（ポート番号）を整数に変換
                int port = Integer.parseInt(idText);

                // 2. 通信クライアントを作成 (ホストは自分のPC内なので "localhost")
                System.out.println("Connecting to localhost:" + port);
                CommClient client = new CommClient("localhost", port);

                // 3. ゲーム開始 (false = クライアントモード)
                // SwingUtilities.invokeLaterを使って、安全に画面遷移を行う
                SwingUtilities.invokeLater(() -> {
                    f.startGame(false, client);
                    
                    // 入力欄をクリアしておく
                    idField.setText("");
                });

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "数字のみを入力してください！");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "接続に失敗しました。\nIDが間違っているか、サーバーが起動していません。");
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}
