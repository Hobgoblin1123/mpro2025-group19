import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

//  動作確認用
// @SuppressWarnings("deprecation")
// class Stage extends Observable implements KeyListener {
//     private boolean gameOver = false;

//     // gameOverのgetterメソッド
//     public boolean isGameOver() {
//         return this.gameOver;
//     }

//     @Override
//     public void keyPressed(KeyEvent e) {
//         if(e.getKeyCode() == KeyEvent.VK_ENTER) {
//             this.gameOver = true;

//             setChanged();
//             notifyObservers();
//         }
//     }
//     public void keyTyped(KeyEvent e) {

//     }
//     public void keyReleased(KeyEvent e) {
        
//     }
// }

@SuppressWarnings("deprecation")
public class GameFrame extends JFrame implements Observer {
    private JPanel mainPanel;
    private JPanel gamePanel;
    private Object commSV;

    // setterメソッド
    public void setCommSV(Object comm) {
        this.commSV = comm;
    }

    // getterメソッド
    public Object getCommSV() {
        return this.commSV;
    }

    public GameFrame() {
        this.setTitle("GAME");
        this.setSize(1200,800);
        this.setLayout(new BorderLayout());

        // ダミーのベースパネルを作成（ゲーム画面の代わり）
        gamePanel = new JPanel();
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

    public void startGame(boolean isServer,Object comm) {
        System.out.println("ゲーム開始: " + (isServer ? "Server" : "Client"));


        // 1. MoveManager (ゲームロジック) の作成
        // 既存の通信オブジェクト(comm)を渡す
        MoveManager mm = new MoveManager(600, 300, 30, isServer, comm);
        mm.addObserver(this); // ゲーム終了監視用

        // 2. ShootingView (描画パネル) の作成
        ShootingView view = new ShootingView(mm);

        // 3. 画面への追加処理
        gamePanel.removeAll(); // 前のゲーム画面があれば消す
        gamePanel.setLayout(new BorderLayout());
        gamePanel.add(view, BorderLayout.CENTER); // ShootingViewを追加

        
        //  リトライ後を考慮して古いキーリスナーは削除
        for (KeyListener kl : gamePanel.getKeyListeners()) {
            gamePanel.removeKeyListener(kl);
        }

        showCard("GAME");

        SwingUtilities.invokeLater(() -> {      //  gamePanelの描画後に実行(実行予約リストの最後尾に回す)
            gamePanel.requestFocusInWindow();   //  キーボードの入力先をgamePanelに設定
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("ゲームが終了しました");
        MoveManager mm = (MoveManager)o;


        boolean iAmPlayer1 = mm.isServer();
        boolean p1Win = mm.winner.contains("Player 1"); // 勝敗文字列判定
        boolean iWin = (iAmPlayer1 && p1Win) || (!iAmPlayer1 && !p1Win);

        ResultPanel resultPanel = new ResultPanel(iWin);
        mainPanel.add(resultPanel, "RESULT");
        showCard("RESULT");
        }

    public static void main(String argv[]) {
        new GameFrame();
    }
}