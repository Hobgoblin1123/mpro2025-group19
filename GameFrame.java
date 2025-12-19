import java.awt.*;
import java.awt.event.*;
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
    public GameFrame() {
        this.setTitle("GAME");
        this.setSize(1200,800);

        // ダミーのベースパネルを作成（ゲーム画面の代わり）
        JPanel gameScreen = new JPanel();
        gameScreen.setBackground(Color.BLACK);
        gameScreen.setPreferredSize(new Dimension(800, 600));
        this.add(gameScreen, BorderLayout.CENTER);
        
        // 動作確認用ステージクラス
        Stage stage = new Stage();
        stage.addObserver(this);
        gameScreen.addKeyListener(stage);
        gameScreen.setFocusable(true);

        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    // ×を押したら閉じる
        this.setVisible(true);

        gameScreen.requestFocusInWindow();  // 実行後すぐにキー入力を受け付けるようにフォーカスを要求
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