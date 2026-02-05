import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("deprecation") // 非推奨Componentを使用しても警告が出ないようにする
class MoveManager extends Observable {
    private boolean server;
    private CommServer sv = null;
    private CommClient cl = null;

    // ゲームデータ
    public int court_size_x, court_size_y;
    public Player player1, player2;
    public ArrayList<Bullet> bullets;
    public ArrayList<Gimmick> gimmicks;

    public String winner = "";
    public boolean isRunning = true;
    private Random rand = new Random();
    public ConcurrentLinkedQueue<String> msgQueue;

    // コンストラクタ
    public MoveManager(int x, int y, int offset, boolean server, Object comm) {

        this.server = server;
        this.court_size_x = x;
        this.court_size_y = y;
        msgQueue = new ConcurrentLinkedQueue<>();

        // Playerの初期化 (既存の引数に合わせつつ、調整)
        // ※ Playerクラスのコンストラクタに合わせて調整してください
        player1 = new Player(10, 10, offset + 20, y / 2, 0.5f, 20, x / 2 - 20, y, 20, 1, 0, 0);
        player2 = new Player(10, 10, x - offset - 20, y / 2, 0.5f, x / 2 + 20, x - 20, y, 20, -1, 0, 0);

        bullets = new ArrayList<>(); // 弾リストの初期化
        gimmicks = new ArrayList<>(); // ギミックリストの初期化

        // 通信オブジェクトの受け取り
        if (server) {
            this.sv = (CommServer) comm;
            // sv.setTimeout(1); // 必要なら設定
        } else {
            this.cl = (CommClient) comm;
            // cl.setTimeout(1);
        }

        Thread recvThread = new Thread(() -> {
            while (isRunning) {
                String msg = null;
                // サーバーかクライアントかで使い分け
                if (server && sv != null) {
                    msg = sv.recv();
                } else if (!server && cl != null) {
                    msg = cl.recv();
                }

                if (msg != null) {
                    // 受信したらメッセージをキューに保存
                    msgQueue.add(msg);
                } else {
                    // エラーや切断時の処理（必要ならisRunningをfalseにする等）
                }
            }
        });
        recvThread.start();
    }

    // メインループ
    public void update() {
        if (!isRunning)
            return;

        if (server) {
            // --- サーバー側の処理 ---

            // 1. クライアント(Player2)の操作を受信
            String msg;
            while ((msg = msgQueue.poll()) != null) {
                try {

                    if (msg.startsWith("Data:")) {
                        String actualData = msg.substring(5);
                        // クライアントからは "x y isShooting" が来る想定
                        String[] data = actualData.split(" ");
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
            String msg;
            while ((msg = msgQueue.poll()) != null) {
                if (msg.startsWith("Data:")) {
                    String actualData = msg.substring(5);
                    parseServerData(actualData);
                }
            }
        }
    }

    // サーバーのみが実行する物理演算
    private void serverLogic() {
        // ギミックを確率1/450で生成
        if (rand.nextInt(450) < 1) {
            int gx = rand.nextInt(court_size_x - 100) + 50; // ギミックのx座標をランダムに決定
            int gy = rand.nextInt(court_size_y - 100) + 50; // ギミックのy座標をランダムに決定
            // ギミックの種類0, 1をランダムに決定して配列に追加
            gimmicks.add(new Gimmick(gx, gy, 20, new Color(0, 255, 0), 0, rand.nextInt(2)));
        }

        // 弾の移動と当たり判定, ギミックの判定
        Iterator<Bullet> it = bullets.iterator();
        Iterator<Gimmick> it2 = gimmicks.iterator();
        // 弾の移動と当たり判定
        while (it.hasNext()) {
            Bullet b = it.next();
            b.move(); // Bulletクラスのmove()を使用

            // 画面外に出たら削除
            if (b.getX() < -50 || b.getX() > court_size_x + 50 ||
                    b.getY() < -50 || b.getY() > court_size_y + 50) {
                it.remove();
                continue;
            }

            // 爆発
            if (b.getStateExplosion() != 0) { // 爆発状態なら
                if (b.explosion()) { // 爆発が進んだら
                    b.setStateExplosion(b.getStateExplosion() + 1); // 状態を進める
                    b.setAnimationFrames(0); // アニメーションフレームをリセット
                } else { // 爆発が進んでいなければ
                    // アニメーションフレームを進める(これらの数字の判定はBulletクラス内で行う)
                    b.setAnimationFrames(b.getAnimationFrames() + 1);
                }
            }

            // 非アクティブなら削除
            if (!b.getIsActive()) {
                it.remove();
                continue;
            }

            boolean hit = false; // 当たったか(初期値はfalse)
            // Player1への当たり判定
            if (b.getOwner() != player1 && isHit(player1, b) && b.getStateExplosion() == 0) { // 自分の弾でなく, 当たっていて,
                                                                                              // 爆発状態でなければ
                GameFrame.playSE("music/damaged.wav", 0.5f); // SE再生
                player1.hit(1); // 1ダメージ
                hit = true; // 当たったことを記録
            }
            // Player2への当たり判定
            else if (b.getOwner() != player2 && isHit(player2, b) && b.getStateExplosion() == 0) { // 自分の弾でなく, 当たっていて,
                                                                                                   // 爆発状態でなければ
                GameFrame.playSE("music/damaged.wav", 0.5f); // SE再生
                player2.hit(1); // 1ダメージ
                hit = true; // 当たったことを記録
            }

            if (hit) { // 当たっていたら
                b.setStateExplosion(1); // 爆発状態を1(爆発初期)にする
                b.setAnimationFrames(0); // アニメーションフレームをリセット
            }
        }

        // ギミックの移動と当たり判定
        while (it2.hasNext()) {
            Gimmick g = it2.next();

            // 画面外に出たら削除
            if (g.getX() < -50 || g.getX() > court_size_x + 50 ||
                    g.getY() < -50 || g.getY() > court_size_y + 50) {
                it2.remove();
                continue;
            }

            boolean hit = false; // 当たったか(初期値はfalse)
            // Player1への当たり判定
            if (isHit_Gimmick(player1, g) && player1.getStatePowerup() == 0 && g.getType() == 0) { // ギミックに当たっていて,
                                                                                                   // 半径拡大の効果中でなく,
                                                                                                   // 当たったのが弾の半径拡大ギミックなら
                player1.setStatePowerup(1); // 半径拡大中(初期)にする
                player1.setImg(); // 画像更新
                player1.setBiggerbullet(10); // 弾の半径を10大きくする
                hit = true; // 当たったことを記録
            } else if (isHit_Gimmick(player1, g) && g.getType() == 1) { // ギミックに当たっていて, 当たったのが回復ギミックなら
                player1.heal(1); // 1回復
                hit = true; // 当たったことを記録
            }
            // Player2への当たり判定
            else if (isHit_Gimmick(player2, g) && player2.getStatePowerup() == 0 && g.getType() == 0) { // ギミックに当たっていて,
                                                                                                        // 半径拡大の効果中でなく,
                                                                                                        // 当たったのが弾の半径拡大ギミックなら
                player2.setStatePowerup(1); // 半径拡大中(初期)にする
                player2.setImg(); // 画像更新
                player2.setBiggerbullet(10); // 弾の半径を10大きくする
                hit = true; // 当たったことを記録
            } else if (isHit_Gimmick(player2, g) && g.getType() == 1) { // ギミックに当たっていて, 当たったのが回復ギミックなら
                player2.heal(1); // 1回復
                hit = true; // 当たったことを記録
            }

            // ギミックが表示されている経過時間を進める
            g.setTime(g.getTime() + 1);

            // ギミック表示時間が750より長いか, 当たっていたら削除
            if (g.getTime() > 750 || hit) {
                it2.remove();
            }
        }

        // プレイヤーのパワーアップ状態の管理
        if (player1.getStatePowerup() > 0 && player1.getStatePowerup() < 600) { // 弾の半径拡大効果中
            player1.setStatePowerup(player1.getStatePowerup() + 1); // 効果時間を進める
            player1.setImg(); // プレイヤーの画像更新
        } else if (player1.getStatePowerup() >= 600) { // 効果時間終了
            player1.setStatePowerup(0); // 効果終了
            player1.setBiggerbullet(0); // 弾の半径を元に戻す
            player1.setImg(); // プレイヤーの画像更新
        }

        // プレイヤー2のパワーアップ状態の管理
        if (player2.getStatePowerup() > 0 && player2.getStatePowerup() < 600) {
            player2.setStatePowerup(player2.getStatePowerup() + 1);
            player2.setImg();
        } else if (player2.getStatePowerup() >= 600) {
            player2.setStatePowerup(0);
            player2.setBiggerbullet(0);
            player2.setImg();
        }

        // 勝敗判定
        if (player1.IsDead()) {
            isRunning = false;
            player1.setIsWin(false);
            winner = "player2";
            gameEnd();
        } else if (player2.IsDead()) {
            isRunning = false;
            player1.setIsWin(true);
            winner = "player1";
            gameEnd();
        }
    }

    public Player getPlayer() {
        return this.player1;
    }

    // 弾との当たり判定の補助メソッド
    private boolean isHit(Player p, Bullet b) {
        // 距離の2乗で判定 (平方根計算を避けて高速化)
        double dx = p.getX() - b.getX();
        double dy = p.getY() - b.getY();
        double rSum = p.getRadius() + b.getRadius();
        return (dx * dx + dy * dy) <= (rSum * rSum);
    }

    // ギミックとの当たり判定の補助メソッド
    private boolean isHit_Gimmick(Player p, Gimmick g) {
        double dx = p.getX() - g.getX();
        double dy = p.getY() - g.getY();
        double rSum = p.getRadius() + g.getRadius();
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
                .append(player1.getStatePowerup()).append(",")
                .append(player2.getStatePowerup()).append(",")
                .append(player1.getBiggerbullet()).append(",")
                .append(player2.getBiggerbullet()).append(",")
                .append(winner).append("#"); // #でプレイヤーに関する情報, 弾に関する情報, ギミックに関する情報を区切る

        // 2. 弾情報
        for (Bullet b : bullets) {
            // Color取得(円で描画する場合)
            int rgb = (b.getColor() != null) ? b.getColor().getRGB() : Color.RED.getRGB();

            // 弾の型判定
            int type = 0; // 通常の弾
            if (b instanceof CurveBullet) { // 曲線弾
                type = 1;
            } else if (b instanceof UpDiagonalBullet) { // 斜め上弾
                type = 2;
            } else if (b instanceof DownDiagonalBullet) { // 斜め下弾
                type = 3;
            }
            sb.append(b.getX()).append(",").append(b.getY()).append(",")
                    .append(b.getRadius()).append(",").append(rgb).append(",")
                    .append(b.getShootdir()).append(",")
                    .append(type).append(",")
                    .append(b.getStateExplosion()).append(",")
                    .append(b.getAnimationFrames()).append(";"); // ;で弾同士を区切る
        }
        sb.append("#");

        // 3 Gimmick情報
        for (Gimmick g : gimmicks) {
            // Color取得(円で描画する場合)
            int rgb = (g.getColor() != null) ? g.getColor().getRGB() : Color.RED.getRGB();

            sb.append(g.getX()).append(",").append(g.getY()).append(",")
                    .append(g.getRadius()).append(",").append(rgb).append(",")
                    .append(g.getTime()).append(",")
                    .append(g.getType()).append(";"); // ;でギミック同士を区切る

        }
        sb.append("#");

        sv.send("Data:" + sb.toString());
    }

    // クライアント側でのデータ受信・解析
    private void parseServerData(String msg) {
        try {
            // # で分割
            String[] sections = msg.split("#", -1);
            if (sections.length < 3)
                return;
            int old_hp = player2.getHp();
            // 1. プレイヤー情報
            // split(",", -1) にして、末尾の空文字(winner)を消さないようにする
            String[] basic = sections[0].split(",", -1);

            // 空文字があっても配列の長さは 11 になるので、if文の中に入れる
            if (basic.length >= 11) {// クライアント側での情報の入力
                player1.setXY(Integer.parseInt(basic[0]), Integer.parseInt(basic[1]));
                player1.setHP(Integer.parseInt(basic[2]));
                // player2.setXY(Integer.parseInt(basic[3]), Integer.parseInt(basic[4]));
                player2.setHP(Integer.parseInt(basic[5]));
                player1.setStatePowerup(Integer.parseInt(basic[6]));
                player2.setStatePowerup(Integer.parseInt(basic[7]));
                player1.setBiggerbullet(Integer.parseInt(basic[8]));
                player2.setBiggerbullet(Integer.parseInt(basic[9]));
                if (Integer.parseInt(basic[5]) < old_hp) {
                    player1.hit(0);
                }
                winner = basic[10]; // 空文字でもエラーにならず代入される

                player1.setImg(); // プレイヤーの画像更新
                player2.setImg();

                if (!winner.isEmpty()) {

                    if ("player1".equals(winner)) {
                        player1.setIsWin(false);
                    }
                    if ("player2".equals(winner)) {
                        player1.setIsWin(true);
                    }
                    isRunning = false;
                    gameEnd();
                }
            }

            // 2. 弾情報 (リストを毎回作り直す)
            bullets.clear();
            if (!sections[1].isEmpty()) {
                String[] bList = sections[1].split(";");
                for (String bStr : bList) {
                    if (bStr.isEmpty())
                        continue;
                    String[] val = bStr.split(",");
                    if (val.length >= 8) {
                        int bx = Integer.parseInt(val[0]); // 弾のx座標
                        int by = Integer.parseInt(val[1]); // 弾のy座標
                        int br = Integer.parseInt(val[2]); // 弾の半径
                        // 色情報のパース
                        Color bc = new Color(Integer.parseInt(val[3])); // 弾の色(円で描画する場合)
                        // どちらの弾か
                        int bs = Integer.parseInt(val[4]);
                        // なんの弾か
                        int type = Integer.parseInt(val[5]);
                        // 爆発の段階
                        int se = Integer.parseInt(val[6]);
                        // 爆発アニメーションのフレーム
                        int af = Integer.parseInt(val[7]);

                        // 受信専用のBulletを作る

                        if (type == 0) { // 通常の弾
                            bullets.add(new Bullet(bx, by, br, bc, bs, se, af));
                        } else if (type == 1) { // 曲線弾
                            bullets.add(new CurveBullet(bx, by, br, bc, bs, se, af));
                        } else if (type == 2) { // 斜め上弾
                            bullets.add(new UpDiagonalBullet(bx, by, br, bc, bs, se, af));
                        } else if (type == 3) { // 斜め下弾
                            bullets.add(new DownDiagonalBullet(bx, by, br, bc, bs, se, af));
                        }
                    }
                }
            }

            // 3. Gimmick情報
            gimmicks.clear();
            if (!sections[2].isEmpty()) {
                String[] gList = sections[2].split(";");
                for (String gStr : gList) {
                    if (gStr.isEmpty())
                        continue;
                    String[] val = gStr.split(",");
                    if (val.length >= 6) {
                        int gx = Integer.parseInt(val[0]); // ギミックのx座標
                        int gy = Integer.parseInt(val[1]); // ギミックのy座標
                        int gr = Integer.parseInt(val[2]); // ギミックの半径
                        // 色情報のパース(円で描画する場合)
                        Color gc = new Color(Integer.parseInt(val[3]));
                        // 表示されてからの経過時間
                        int gt = Integer.parseInt(val[4]);
                        // ギミックの種類
                        int gty = Integer.parseInt(val[5]);

                        // 受信専用のGimmickを作る
                        gimmicks.add(new Gimmick(gx, gy, gr, gc, gt, gty));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // クライアントからサーバーへ入力情報を送信
    public void sendClientInput(boolean isShooting, int bulletType) {
        if (!server) {
            // "x y isShooting"
            cl.send("Data:" + player2.getX() + " " + player2.getY() + " " + isShooting + " " + bulletType);
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

    public boolean getIsRunning() {
        return this.isRunning;
    }

    public void draw(Graphics g) {
        // 背景など
        // for(Star s : stars) s.draw(g);

        g.drawRect(0, 0, court_size_x, court_size_y);
        player1.draw(g);
        player2.draw(g);

        for (Bullet b : bullets) { // すべての弾の描画
            b.draw(g);
        }

        for (Gimmick gm : gimmicks) { // すべてのギミックの描画
            gm.draw(g);
        }
    }
}