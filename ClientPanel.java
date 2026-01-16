import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class ClientPanel extends JPanel implements ActionListener, KeyListener {
    private JButton closeBtn;
    private JTextField idField;
    private GameFrame f;

    public ClientPanel(GameFrame f) {
        this.f = f;
        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout());

        // --- 2. 構成要素 ---
        JLabel instruct = new JLabel("⇩ルームIDを入力してください⇩");
        idField = new JTextField(10);
        closeBtn = new JButton("戻る");

        // --- 3. リスナーの登録 ---
        closeBtn.addActionListener(this);
        idField.addKeyListener(this);

        // --- 4. 構成要素を載せる ---
        this.add(idField, BorderLayout.CENTER);
        this.add(instruct, BorderLayout.NORTH);
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
            // String id = idField.getText();
            // connectToServerLogic(id);
            System.out.println("serverID: ");
        }
    }
    public void keyTyped(KeyEvent e) {

    }
    public void keyReleased(KeyEvent e) {
        
    }
}
