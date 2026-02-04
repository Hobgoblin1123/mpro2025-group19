import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import java.util.Observable;
import java.util.Observer;

// // ---  MP3再生用  -----------------
// import javazoom.jl.player.Player;
// import java.io.BufferedInputStream;
// import java.io.FileInputStream;
// //  ----------------------------------

// --- 標準ライブラリのみを使用 ---
import javax.sound.sampled.*; 
// ----------------------------------

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
    private JPanel mainPanel;
    private JPanel gamePanel;
    private Object commSV;
    private MoveManager mm;
    private ShootingView view;

    // ---- BGM制御用 ----
    // private Player player;
    // private Thread bgmThread;
    // private boolean isLoop = true;
    private Clip clip;
    // ------------------

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
        this.setSize(1200, 800);
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
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ×を押したら閉じる
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "START");
        playBGM("music/main.wav", 0.3f);
        this.setVisible(true);
    }

    // 画面切り替え用メソッド
    public void showCard(String key) {
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, key);
    }

    public void startGame(boolean isServer, Object comm) {
        this.commSV = comm;
        System.out.println("ゲーム開始: " + (isServer ? "Server" : "Client"));

        // 1. MoveManager (ゲームロジック) の作成
        // 既存の通信オブジェクト(comm)を渡す
        mm = new MoveManager(1200, 800, 30, isServer, comm);
        mm.addObserver(this); // ゲーム終了監視用

        // 2. ShootingView (描画パネル) の作成
        view = new ShootingView(mm);

        // 3. 画面への追加処理
        gamePanel.removeAll(); // 前のゲーム画面があれば消す
        gamePanel.setLayout(new BorderLayout());
        gamePanel.add(view, BorderLayout.CENTER); // ShootingViewを追加

        // リトライ後を考慮して古いキーリスナーは削除
        for (KeyListener kl : gamePanel.getKeyListeners()) {
            gamePanel.removeKeyListener(kl);
        }

        gamePanel.addKeyListener(view);

        showCard("GAME");
        stopBGM();
        playBGM("music/battle.wav", 0.5f);

        SwingUtilities.invokeLater(() -> { // gamePanelの描画後に実行(実行予約リストの最後尾に回す)
            view.requestFocusInWindow(); // キーボードの入力先をgamePanelに設定
        });
    }

    // ------ リトライと終了機能 -------------------------------

    // 共通送信メソッド(これを作らないと以下メソッドの送受信のやり取りがめんどくさい(主語が設定がだるい))
    public boolean sendMessage(String msg) {
        if (commSV instanceof CommServer)
            return ((CommServer) commSV).send(msg);
        else if (commSV instanceof CommClient)
            return ((CommClient) commSV).send(msg);
        return false;
    }

    // 共通受信メソッド
    public String receiveMessage() {
        if (commSV instanceof CommServer)
            return ((CommServer) commSV).recv();
        else if (commSV instanceof CommClient)
            return ((CommClient) commSV).recv();
        return null;
    }

    public void tryRetry() {
        // 通信待ち時のフリーズ回避のためにマルチスレッド
        new Thread(() -> {
            boolean isSend = sendMessage("RETRY");
            System.out.println("リトライ要求を送信中・・・");

            // 送信バッファに "RETRY" が置けてない
            if (!isSend) {
                SwingUtilities.invokeLater(() -> {
                    // ポップアップを表示
                    JOptionPane.showMessageDialog(this, "通信が切断されています。"); // 自身側で通信が切断
                    backToStart();
                });
                return;
            }

            // 相手の応答待機
            String response = receiveMessage();
            System.out.println("相手の応答: " + response);

            SwingUtilities.invokeLater(() -> {
                if (response.equals("RETRY")) {
                    retryGame();
                } else {
                    JOptionPane.showMessageDialog(this, "相手がゲームを終了しました。");
                    backToStart(); // 自分もスタートに戻る
                }
            });
        }).start();
    }

    public void retryGame() {
        // 1度消して再生成
        this.getContentPane().removeAll();
        this.add(mainPanel, BorderLayout.CENTER);

        boolean isServer = (commSV instanceof CommServer);
        startGame(isServer, commSV);

        this.revalidate(); // レイアウトの再計算
        this.repaint();
    }

    public void backToStart() {
        // 通信の切断
        if (commSV instanceof CommServer) {
            ((CommServer) commSV).close(); // 念のためキャスト(下も同じ)
        } else if (commSV instanceof CommClient) {
            ((CommClient) commSV).close();
        }
        commSV = null;

        this.getContentPane().removeAll();
        this.add(mainPanel, BorderLayout.CENTER);
        showCard("START");

        this.revalidate();
        this.repaint();
    }
    // ---------------------------------------------------

    // --- BGM再生メソッド --------------------------------
    // public void playBGM(String filePath) {
    //     isLoop = true;
    //     bgmThread = new Thread(() -> {
    //         while (isLoop) { // ループ再生
    //             try {
    //                 FileInputStream fis = new FileInputStream(filePath);
    //                 BufferedInputStream bis = new BufferedInputStream(fis);
    //                 player = new Player(bis);
    //                 player.play(); // 再生終了までブロックされる
    //             } catch (Exception e) {
    //                 System.out.println("BGM再生エラー: " + e.getMessage());
    //                 isLoop = false; // エラー時はループを抜ける
    //             }
    //         }
    //     });
    //     bgmThread.start();
    // }

    // // --- BGM停止メソッド ---
    // public void stopBGM() {
    //     isLoop = false; // ループフラグを下ろす
    //     if (player != null) {
    //         player.close(); // 現在再生中のプレイヤーを閉じる
    //     }
    // }
    // --- BGM再生メソッド (WAV版) ---
    public void playBGM(String filePath, float volumeLevel) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            
            // 音声データをロード
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // --- 音量調整処理 ---
            // MASTER_GAIN というコントロールを取得
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            // 0.0(消音) ～ 1.0(最大) の値を デシベル(dB) に変換する計算式
            // dB = 20 * log10(volume)
            float db = (float) (Math.log10(volumeLevel) * 20.0);
            
            // 設定可能な最小値より小さくならないように制限
            float min = gainControl.getMinimum(); // 通常 -80.0dBくらい
            if (db < min) db = min;
            
            gainControl.setValue(db);
            // ------------------

            // ループ設定して再生開始
            clip.loop(Clip.LOOP_CONTINUOUSLY); 

        } catch (Exception e) {
            System.out.println("BGM再生エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- 変更: BGM停止メソッド ---
    public void stopBGM() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close(); // メモリ解放
        }
    }

    // --- : SE再生用メソッド -------
    public void playSE(String filePath, float volumeLevel) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // 音量調整 (BGMと同じロジック)
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = (float) (Math.log10(volumeLevel) * 20.0);
            float min = gainControl.getMinimum();
            if (db < min) db = min;
            gainControl.setValue(db);

            // 再生終了時に自動で閉じるためのリスナーを追加 (メモリ節約)
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start(); // 再生開始
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ---------------------------------------------------------------------------

    @Override
    public void update(Observable o, Object arg) {
        // stage変数を通じてStageクラスのメソッドやフィールドにアクセスできるようにする
        Timer timer = new Timer(2000, e -> {
            SwingUtilities.invokeLater(() -> {
                this.getContentPane().removeAll();
                ResultPanel result = new ResultPanel(this, mm.getPlayer().getIsWin());
                this.add(result, BorderLayout.CENTER);
                stopBGM();
                playBGM("music/main.wav", 0.3f);

                this.revalidate();// レイアウトの再計算
                this.repaint();
            });
        });
        timer.setRepeats(false); // 1回だけ
        timer.start();

    }

    public static void main(String argv[]) {
        new GameFrame();
    }
}