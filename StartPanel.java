import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import java.awt.geom.Path2D;

public class StartPanel extends JPanel implements ActionListener {
    private CustomButton serverBtn;
    private CustomButton clientBtn;
    private CustomButton exitBtn;
    private BackGroundPanel bgPanel;

    private GameFrame f;
    // Null Pointer Exception回避のため、引数にGameFrameを指定
    public StartPanel(GameFrame f) {
        this.f = f;
        Font pixelFont = FontLoader.loadFont("/Fonts/DotGothic16-Regular.ttf", 40f);

        // --- 1. 　レ　イ　ア　ウ　ト　設　定　 ---
        this.setLayout(new BorderLayout());

        // --- 2. 　構　成　要　素　 ---
        bgPanel = new BackGroundPanel("/images/start_image.jpg", "/images/start_image_flashed.jpg");
        bgPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel title = new JLabel(new ImageIcon(getClass().getResource("/images/title_image_transparent.png")));
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 20));
        serverBtn = new CustomButton("CREATE", pixelFont);
        clientBtn = new CustomButton("JOIN", pixelFont);
        exitBtn = new CustomButton("EXIT", pixelFont);

        JLabel credit = new JLabel("SE, 一部BGM: 魔王魂様");

        // --- 3. 　装　飾　 ----
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        btnPanel.setOpaque(false); // ボタンはデフォルトだと背景が不透明(Opaque)
        credit.setForeground(Color.ORANGE);
        credit.setFont(pixelFont.deriveFont(12f));

        serverBtn.setAction(isHover -> bgPanel.flashImage(isHover));
        serverBtn.setSound(f);
        clientBtn.setAction(isHover -> bgPanel.flashImage(isHover));
        clientBtn.setSound(f);
        exitBtn.setAction(isHover -> bgPanel.flashImage(isHover));
        exitBtn.setSound(f);
            // ---- 3.1. 　ボ　タ　ン　の　サ　イ　ズ　を　調　整　 ----
        Dimension btnSize = new Dimension(400, 100);
        serverBtn.setPreferredSize(btnSize);
        clientBtn.setPreferredSize(btnSize);
        exitBtn.setPreferredSize(btnSize);
            // ---- 3.2. 　タ　イ　ト　ル　部　分　の　設　定　 ----
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8; // 上部の空き・画像スペースの比率（大きくすると下に押し下げられる）
        gbc.anchor = GridBagConstraints.CENTER;
        
        bgPanel.add(title, gbc);
            // ---- 3.3. 　ボ　タ　ン　部　分　の　設　定　 ----
        gbc.gridy = 1;
        gbc.weighty = 0.4; // 下部のボタンエリアの比率
        gbc.anchor = GridBagConstraints.CENTER;

        bgPanel.add(btnPanel, gbc);
            // ---- 3.4. 　ク　レ　ジ　ッ　ト　部　分　の　設　定　 ----
        gbc.anchor = GridBagConstraints.SOUTHEAST;

        bgPanel.add(credit, gbc);
        // --- 4. 　リ　ス　ナ　ー　の　登　録　 ---
        serverBtn.addActionListener(this);
        clientBtn.addActionListener(this);
        exitBtn.addActionListener(this);

        // --- 5. 　構　成　要　素　を　載　せ　る　 ---
        this.add(bgPanel);
        btnPanel.add(serverBtn);
        btnPanel.add(clientBtn);
        btnPanel.add(exitBtn);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == serverBtn) {
            f.playSE("music/go.wav", 1);
            f.stopBGM();
            f.playBGM("music/waiting.wav", 0.3f);
            f.showCard("SERVER");
        } else if (e.getSource() == clientBtn) {
            f.playSE("music/go.wav", 1);
            f.stopBGM();
            f.playBGM("music/waiting.wav", 0.3f);
            f.showCard("CLIENT");
        } else if (e.getSource() == exitBtn) {
            f.playSE("music/back.wav", 1);
            System.out.println("EXITボタンが選択されました.");
            f.stopBGM();
            System.exit(0);
        }
    }
}