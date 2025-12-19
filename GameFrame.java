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
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
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
    public GameFrame(String ser_cli, String port) {
        this.setTitle("GAME");
        this.setSize(1200, 800);

        String str;
        boolean server = false;
        MoveManager tm;
        if (ser_cli.equals("server")) {
            server = true;
            System.out.println("Server mode");
            str = "server";
            tm = new MoveManager(600, 300, 30, server, ser_cli, Integer.parseInt(port));
        } else {
            server = false;
            System.out.println("Client mode");
            str = "client";
            tm = new MoveManager(600, 300, 30, server, ser_cli, Integer.parseInt(port));
        }
        ShootingView tv = new ShootingView(tm);
        this.add(tv, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);

        tv.requestFocusInWindow();

        // 動作確認用ステージクラス
        Stage stage = new Stage();
        stage.addObserver(this);
        tv.addKeyListener(stage);
        this.setFocusable(true);

        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ×を押したら閉じる
        this.setVisible(true);

        // gameScreen.requestFocusInWindow(); // 実行後すぐにキー入力を受け付けるようにフォーカスを要求
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("ゲームが終了しました");
        // stage変数を通じてStageクラスのメソッドやフィールドにアクセスできるようにする
        Stage stage = (Stage) o;

        if (stage.isGameOver()) {
            this.getContentPane().removeAll();
            ResultPanel result = new ResultPanel(true);
            this.add(result, BorderLayout.CENTER);

            this.revalidate(); // レイアウトの再計算
            this.repaint();
        }
    }

    public static void main(String argv[]) {

        new GameFrame(argv[0], argv[1]);
    }
}