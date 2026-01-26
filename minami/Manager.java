import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Observable; // GameFrameとの連携に残しておきます

// Observableを継承したまま、中身をShooterModel風に改造

@SuppressWarnings("deprecation")
class MoveManager extends Observable {
    // 通信関係
    private boolean server;
    private CommServer sv = null;
    private CommClient cl = null;

    // ゲームデータ
    public int court_size_x, court_size_y;
    public Player player1, player2;
    public ArrayList<Bullet> bullets;

    // public ArrayList<PowerUpItem> items; // アイテムクラスを作ったら有効化
    // public ArrayList<Star> stars; // 星クラスを作ったら有効化

    public String winner = "";
    public boolean isRunning = true;
    private Random rand = new Random();

    // コンストラクタ
    public MoveManager(int x, int y, int offset, boolean server, Object comm) {
        this.server = server;
        this.court_size_x = x;
        this.court_size_y = y;

        // Playerの初期化 (既存の引数に合わせつつ、調整)
        // ※ Playerクラスのコンストラクタに合わせて調整してください
        player1 = new Player(10, 10, offset, y / 2, 1, 20, 200, y, 5);
        player2 = new Player(10, 10, x - offset - 20, y / 2, 1, 320, 580, y, 5);

        bullets = new ArrayList<>();
        // items = new ArrayList<>();
        // stars = new ArrayList<>();

        // 通信オブジェクトの受け取り
        if (server) {
            this.sv = (CommServer) comm;
            // sv.setTimeout(1); // 必要なら設定
        } else {
            this.cl = (CommClient) comm;
            // cl.setTimeout(1);
        }
    }

    // メインループ (ShooterModelのupdateに相当)
    public void update() {
        if (!isRunning)
            return;

        // 背景エフェクト更新などがあればここ
        // for(Star s : stars) s.update(court_size_x, court_size_y, rand);

        if (server) {
            // --- サーバー側の処理 ---

            // 1. クライアント(Player2)の操作を受信
            String msg = sv.recv();
            if (msg != null) {
                try {
                    // クライアントからは "x y isShooting" が来る想定
                    String[] data = msg.split(" ");
                    int p2x = Integer.parseInt(data[0]);
                    int p2y = Integer.parseInt(data[1]);
                    boolean p2Shoot = Boolean.parseBoolean(data[2]);
                    int bulletType = Integer.parseInt(data[3]);
                    // 位置を同期
                    player2.setXY(p2x, p2y);

                    // 発射処理 (サーバー側で弾を生成してリストに追加)
                    if (p2Shoot) {
                        bullets.addAll(player2.tryShoot(bulletType));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 2. ゲームロジック (弾の移動、当たり判定)
            serverLogic();

            // 3. 全データをクライアントへ送信
            sendServerData();

        } else {
            // --- クライアント側の処理 ---

            // サーバーからの全データを受信して反映
            String msg = cl.recv();
            if (msg != null) {
                parseServerData(msg);
            }
        }
    }

    // サーバーのみが実行する物理演算
    private void serverLogic() {
        // アイテム出現処理などがあればここ

        // 弾の移動と当たり判定
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.move(); // Bulletクラスのmove()を使用

            // 画面外に出たら削除
            if (b.getX() < -50 || b.getX() > court_size_x + 50 ||
                    b.getY() < -50 || b.getY() > court_size_y + 50) {
                it.remove();
                continue;
            }

            boolean hit = false;
            // Player1への当たり判定
            if (b.getOwner() != player1 && isHit(player1, b)) {
                player1.hit(1); // 1ダメージ
                hit = true;
            }
            // Player2への当たり判定
            else if (b.getOwner() != player2 && isHit(player2, b)) {
                player2.hit(1);
                hit = true;
            }

            if (hit)
                it.remove();
        }

        // 勝敗判定
        if (player1.IsDead()) {
            isRunning = false;
            winner = "Player 2 Win!";
            gameEnd();
        } else if (player2.IsDead()) {
            isRunning = false;
            winner = "Player 1 Win!";
            gameEnd();
        }
    }

    // 当たり判定の補助メソッド
    private boolean isHit(Player p, Bullet b) {
        // 距離の2乗で判定 (平方根計算を避けて高速化)
        double dx = p.getX() - b.getX();
        double dy = p.getY() - b.getY();
        double rSum = p.getRadius() + b.getRadius();
        return (dx * dx + dy * dy) <= (rSum * rSum);
    }

    // サーバーからクライアントへデータを送信
    // フォーマット: "P1x,P1y,P1hp,P2x,P2y,P2hp,Winner # Bullet1;Bullet2;... # Items..."
    private void sendServerData() {
        StringBuilder sb = new StringBuilder();

        // 1. プレイヤー情報と勝敗
        sb.append(player1.getX()).append(",").append(player1.getY()).append(",").append(player1.getHp()).append(",")
                .append(player2.getX()).append(",").append(player2.getY()).append(",").append(player2.getHp())
                .append(",")
                .append(winner).append("#");

        // 2. 弾情報 (x, y, radius, colorRGB)
        for (Bullet b : bullets) {
            // Color取得時にnullチェックを入れると安全
            int rgb = (b.getColor() != null) ? b.getColor().getRGB() : Color.RED.getRGB();

            sb.append(b.getX()).append(",").append(b.getY()).append(",")
                    .append(b.getRadius()).append(",").append(rgb).append(";");
        }
        sb.append("#");

        // 3. アイテム情報 (今回は空)
        // for (Item i : items) { ... }
        sb.append("#");

        sv.send(sb.toString());
    }

    // クライアント側でのデータ受信・解析
    private void parseServerData(String msg) {
        try {
            // # で分割（ここも -1 をつけておくのが安全）
            String[] sections = msg.split("#", -1); 
            if (sections.length < 2)
                return;

            // 1. プレイヤー情報
            // ★修正ポイント: split(",", -1) に変更して、末尾の空文字(winner)を消さないようにする
            String[] basic = sections[0].split(",", -1);

            // これで空文字があっても配列の長さは 7 になるので、if文の中に入れる
            if (basic.length >= 7) {
                player1.setXY(Integer.parseInt(basic[0]), Integer.parseInt(basic[1]));
                player1.setHP(Integer.parseInt(basic[2]));
                player2.setXY(Integer.parseInt(basic[3]), Integer.parseInt(basic[4]));
                player2.setHP(Integer.parseInt(basic[5]));
                winner = basic[6]; // 空文字でもエラーにならず代入される

                if (!winner.isEmpty()) {
                    isRunning = false;
                    gameEnd();
                }
            }

            // 2. 弾情報 (リストを毎回作り直す = 完全同期)
            bullets.clear();
            if (!sections[1].isEmpty()) {
                String[] bList = sections[1].split(";");
                for (String bStr : bList) {
                    if (bStr.isEmpty())
                        continue;
                    String[] val = bStr.split(",");
                    if (val.length >= 4) {
                        int bx = Integer.parseInt(val[0]);
                        int by = Integer.parseInt(val[1]);
                        int br = Integer.parseInt(val[2]);
                        // 色情報のパース
                        Color bc = new Color(Integer.parseInt(val[3]));

                        // 受信専用のBulletを作る
                        bullets.add(new Bullet(bx, by, br, bc));
                    }
                }
            }

            // 3. アイテム情報 (実装時はここに追加)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // クライアントからサーバーへ入力情報を送信
    public void sendClientInput(boolean isShooting, int bulletType) {
        if (!server) {
            // "x y isShooting"
            cl.send(player2.getX() + " " + player2.getY() + " " + isShooting + " " + bulletType);
        }
    }

    // 終了通知
    public void gameEnd() {
        setChanged();
        notifyObservers();
    }

    public boolean isServer() {
        return server;
    }

    public void draw(Graphics g) {
        // 背景など
        // for(Star s : stars) s.draw(g);

        g.drawRect(0, 0, court_size_x, court_size_y);
        player1.draw(g);
        player2.draw(g);

        for (Bullet b : bullets) {
            b.draw(g);
        }

        // items.draw(g);
    }
}

// public static void main(String[] args) {
// // String str;
// // boolean server = false;
// // if (args.length < 2) {
// // System.out.println("Usage : java TennisView {server/single/{host name}}
// {port
// // no.} \n");
// // System.exit(1);
// // }
// // MoveManager tm;
// // if (args[0].equals("server")) {
// // server = true;
// // System.out.println("Server mode");
// // str = "server";
// // tm = new MoveManager(600, 300, 30, server, args[0],
// // Integer.parseInt(args[1]));
// // } else {
// // server = false;
// // System.out.println("Client mode");
// // str = "client";
// // tm = new MoveManager(600, 300, 30, server, args[0],
// // Integer.parseInt(args[1]));
// // }

// JFrame frame = new JFrame("TennisView (" + str + " mode)");
// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// ShootingView tv = new ShootingView(tm);
// frame.add(tv, BorderLayout.CENTER);
// frame.pack();
// frame.setVisible(true);

// tv.requestFocusInWindow();
// }
// }