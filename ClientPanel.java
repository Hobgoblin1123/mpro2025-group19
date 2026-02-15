import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// ロジックはそのまま、見た目を宇宙船コックピット風に改装
public class ClientPanel extends StarAnimPanel implements ActionListener {
    private CustomButton closeBtn;
    private CustomButton connectBtn;
    private JTextField idField;
    private JTextField ipField;
    private JLabel statusLbl;
    private GameFrame f;
    private Image bgImage;

    public ClientPanel(GameFrame f) {
        super(); // 星のアニメーション開始
        this.f = f;
        Font pixelFont = FontLoader.loadFont("/Fonts/DotGothic16-Regular.ttf", 40f);
        Font titleFont = FontLoader.loadFont("/Fonts/DotGothic16-Regular.ttf", 24f);
        Font statusFont = FontLoader.loadFont("/Fonts/DotGothic16-Regular.ttf", 14f);
        
        // --- 1. 　レ　イ　ア　ウ　ト　設　定　 ---
        this.setLayout(new GridBagLayout()); // 中央寄せしやすいGridBagLayoutに変更

        try {
            bgImage = new ImageIcon(getClass().getResource("/images/server-clientpanel.png")).getImage();
        } catch (Exception e) {
            System.out.println("背景画像が見つかりません: " + e.getMessage());
        }

        // --- コックピット風パネルの作成 ---
        JPanel consolePanel = new JPanel();
        consolePanel.setLayout(new GridBagLayout());
        consolePanel.setOpaque(false);
        // 背景に半透明の枠を描く
        consolePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        consolePanel.setBackground(new Color(0, 0, 30, 200)); // 半透明黒

        // --- 2. 　構　成　要　素　の　作　成　と　デ　ザ　イ　ン　 ---
        JLabel titleLabel = new JLabel("ESTABLISH CONNECTION");
        titleLabel.setForeground(Color.CYAN);
        titleLabel.setFont(titleFont);

        JLabel ipLabel = new JLabel("TARGET IP:");
        ipLabel.setForeground(Color.WHITE);
        titleLabel.setFont(pixelFont);
        
        JLabel idLabel = new JLabel("ROOM ID (PORT):");
        idLabel.setForeground(Color.WHITE);
        titleLabel.setFont(pixelFont);

        // テキストフィールドのカスタムデザイン
        ipField = createCustomTextField("localhost");
        idField = createCustomTextField("");

        statusLbl = new JLabel("READY", JLabel.CENTER);
        statusLbl.setForeground(Color.GRAY);
        statusLbl.setFont(statusFont);
        statusLbl.setPreferredSize(new Dimension(300, 30));

        connectBtn = new CustomButton("CONNECT", titleFont);
        connectBtn.setPreferredSize(new Dimension(200, 75));
        closeBtn = new CustomButton("BACK", titleFont);
        closeBtn.setPreferredSize(new Dimension(200, 75));

        connectBtn.setSound(f);
        closeBtn.setSound(f);
        // --- 3. リスナーの登録 ---
        connectBtn.addActionListener(this);
        closeBtn.addActionListener(this);
        idField.addActionListener(this);
        ipField.addActionListener(this);

        // --- 4. レイアウト配置 (GridBag) ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        consolePanel.add(titleLabel, gbc);

        // IP
        gbc.gridy++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        consolePanel.add(ipLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        consolePanel.add(ipField, gbc);

        // ID (PORT)
        gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
        consolePanel.add(idLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        consolePanel.add(idField, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        consolePanel.add(statusLbl, gbc);

        // Buttons
        gbc.gridy++;
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setOpaque(false);
        btnPanel.add(connectBtn);
        btnPanel.add(closeBtn);
        consolePanel.add(btnPanel, gbc);

        // パネルを画面に追加
        // パネル自体の背景描画のために paintComponent をオーバーライドした無名クラスでラップしても良いが
        // 簡易的に JPanel の背景色と透過設定で対応
        JPanel wrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 20, 40, 200));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(consolePanel);
        this.add(wrapper);

        // --- +α. 画面が表示されるたびに状態をリセット ---
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                statusLbl.setText("READY");
                statusLbl.setForeground(Color.GREEN);
                connectBtn.setEnabled(true);
                idField.setText("");
                idField.requestFocus();
            }
        });
    }

    // デザイン用ヘルパーメソッド
    private JTextField createCustomTextField(String text) {
        JTextField tf = new JTextField(text, 12);
        tf.setBackground(Color.BLACK);
        tf.setForeground(Color.GREEN);
        tf.setCaretColor(Color.GREEN);
        tf.setFont(new Font("Monospaced", Font.BOLD, 18));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return tf;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e); // 星のアニメーション更新

        if (e.getSource() == closeBtn) {
            f.playSE("music/back.wav", 1);
            f.showCard("START");
            f.stopBGM();
            f.playBGM("music/main.wav", 0.3f);
        } else if (e.getSource() == connectBtn || e.getSource() == idField) {      //  JTextFieldは、Enter入力の機能を潜在的に持っているのでActionListenerでよい
            f.playSE("music/go.wav", 1);
            String id = idField.getText();
            String ip = ipField.getText();

            // 入力チェック
            if (id.isEmpty()) return;
            try {
                int port = Integer.parseInt(id);
                connecToServer(ip, port);
            } catch (NumberFormatException ex) {
                statusLbl.setText("ERROR: INVALID NUMBER");
                statusLbl.setForeground(Color.RED);
            }
        }
    }

    // クライアント接続ロジック
    private void connecToServer(String ip, int port) {
        statusLbl.setText("CONNECTING (" + ip + ":" + port + ")...");
        statusLbl.setForeground(Color.YELLOW);
        connectBtn.setEnabled(false);

        new Thread(() -> {
            try {
                // ここでCommClientをインスタンス化
                CommClient cl = new CommClient(ip, port);
                
                System.out.println("接続に成功しました");

                SwingUtilities.invokeLater(() -> {
                    f.startGame(false, cl);
                    connectBtn.setEnabled(true);
                    statusLbl.setText("CONNECTED!");
                    statusLbl.setForeground(Color.GREEN);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    statusLbl.setText("CONNECTION FAILED");
                    statusLbl.setForeground(Color.RED);
                    connectBtn.setEnabled(true);
                });
            }
        }).start();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 星を描画
        Graphics2D g2d = (Graphics2D) g;

        // 画像があれば半透明で描画 (StartPanelと同じ処理)
        if (bgImage != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)); // 透明度0.6
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // 戻す
        }
    }
}