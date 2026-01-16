import java.awt.*;
import java.awt.event.*;

import javax.smartcardio.Card;
import javax.swing.*;
import java.util.*;

//  動作確認用
@SuppressWarnings("deprecation")
class Stage extends Observable implements KeyListener {
    private boolean gameOver = false;

    // gameOverのgetterメソッド
    public boolean isGameOver() {
        return this.gameOver;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.gameOver = true;

            setChanged();
            notifyObservers();
        }
    }
    public void keyTyped(KeyEvent e) {

    }
    public void keyReleased(KeyEvent e) {
        
    }
}

@SuppressWarnings("deprecation")
public class GameFrame extends JFrame implements Observer {
    private JPanel mainPanel;
    private JPanel gamePanel;
    private Object commModule;

    public GameFrame() {
        this.setTitle("GAME");
        this.setSize(1200,800);
        this.setLayout(new BorderLayout());

        // ダミーのベースパネルを作成（ゲーム画面の代わり）
        JPanel gamePanel = new JPanel();
        gamePanel.setBackground(Color.BLACK);
        gamePanel.setPreferredSize(new Dimension(800, 600));
        gamePanel.setFocusable(true);

        // Start, Server, Clientの各パネルを切り替えられる、土台となるmainPanelを使用
        mainPanel = new JPanel(new CardLayout());

        // mainPanelで切り替えるパネルを追加
        mainPanel.add(new StartPanel(this), "START");
        mainPanel.add(new ServerPanel(this), "SERVER");
        mainPanel.add(new ClientPanel(this), "CLIENT");
        mainPanel.add(gamePanel, "GAME");

        this.add(mainPanel, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    // ×を押したら閉じる
        ((CardLayout) mainPanel.getLayout()).show(mainPanel,"START");
        this.setVisible(true);
    }

    //  画面切り替え用メソッド
    public void showCard(String key) {
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, key);
    }

    public void startGame(boolean isServer, int port, String host) {
        System.out.println("ゲーム開始: " + (isServer ? "Server" : "Client"));

        //  ゲームのステージ
        Stage stage = new Stage();
        stage.addObserver(this);
        
        //  リトライ後を考慮して古いキーリスナーは削除
        for (KeyListener kl : gamePanel.getKeyListeners()) {
            gamePanel.removeKeyListener(kl);
        }
        
        gamePanel.addKeyListener(stage);

        showCard("GAME");

        SwingUtilities.invokeLater(() -> {      //  gamePanelの描画後に実行(実行予約リストの最後尾に回す)
            gamePanel.requestFocusInWindow();   //  キーボードの入力先をgamePanelに設定
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("ゲームが終了しました");
        //  stage変数を通じてStageクラスのメソッドやフィールドにアクセスできるようにする
        Stage stage = (Stage)o;

        if(stage.isGameOver()) {
            this.getContentPane().removeAll();
            ResultPanel result = new ResultPanel(true);
            this.add(result, BorderLayout.CENTER);

            this.revalidate();  // レイアウトの再計算
            this.repaint();
        }
    }

    public static void main(String argv[]) {
        new GameFrame();
    }
}