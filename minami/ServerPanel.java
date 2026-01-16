import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class ServerPanel extends JPanel implements ActionListener {
    private JLabel statusLbl;
    private JLabel portNum;
    private JButton closeBtn;
    private GameFrame f;
    private boolean isWaiting = false;

    public ServerPanel(GameFrame f) {
        this.f = f;
        // --- 1. レイアウト設定 ---
        this.setLayout(new BorderLayout());

        // --- 2. 構成要素 ---
        portNum = new JLabel("ルームID", JLabel.CENTER);
        statusLbl = new JLabel("起動準備中・・・", JLabel.CENTER);
        closeBtn = new JButton("戻る");

        // --- 3. リスナーの登録 ---
        closeBtn.addActionListener(this);

        // --- 4. 構成要素を載せる ---
        this.add(statusLbl, BorderLayout.CENTER);
        this.add(portNum, BorderLayout.NORTH);
        this.add(closeBtn, BorderLayout.SOUTH);

        serverWait();
    }

    //  サーバー待機ロジック
    public void serverWait() {
        if (isWaiting) return;  //  二重起動防止
        isWaiting = true;

        //  --- 1. ランダムポート番号設定 ---
        int port = (int) (Math.random() * 64511) + 1024;    // 65535 - 1024 = 64511
        portNum.setText(String.valueOf(port));  //  int型をstring型に変換してラベル更新
        statusLbl.setText("接続待機中・・・");

        //  --- 2. 別スレッドで接続待ちを開始 (マルチスレッドを実現 = 裏で処理を行う) ---
        // new Thread(new Runnable() {
        //     @Override
        //     public void run() {  // ← カッコの中（引数）が空っぽ！
        //         // 処理
        //     }
        // });  以下の文はこれと同義
        new Thread(() -> {
            try {
                //  --- 3. Commサーバーをインスタンス化 ---
                CommServer sv = new CommServer(port);
                f.setCommSV(sv);

                // --- 4. 接続成功後、ゲームを実行 ---
                SwingUtilities.invokeLater(() -> {
                    f.startGame(true, port, null);
                    isWaiting = false;
                });
            } catch (Exception e){      // else文(例外=トラブル時の対応)
                e.printStackTrace();    // エラー内容をターミナルに表示
                isWaiting = false;      
                SwingUtilities.invokeLater(() -> statusLbl.setText("エラーが発生しました"));
            }
        }).start();     // Threadの起動
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeBtn) {
            f.showCard("START");
        }
    }
}
