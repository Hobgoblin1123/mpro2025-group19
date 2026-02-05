import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Observable;

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

    // コンストラクタ
    public MoveManager(int x, int y, int offset, boolean server, Object comm) {
        this.server = server;
        this.court_size_x = x;
        this.court_size_y = y;

        // Playerの初期化 (既存の引数に合わせつつ、調整)
        // ※ Playerクラスのコンストラクタに合わせて調整してください
        player1 = new Player(10, 10, offset + 20, y / 2, 1, 20, x / 2 - 20, y, 20, 1, 0, 0);
        player2 = new Player(10, 10, x - offset - 20, y / 2, 1, x / 2 + 20, x - 20, y, 20, -1, 0, 0);

        bullets = new ArrayList<>();
        gimmicks = new ArrayList<>();
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
        if (rand.nextInt(500) < 1) {
            int gx = rand.nextInt(court_size_x - 100) + 50;
            int gy = rand.nextInt(court_size_y - 100) + 50;
            gimmicks.add(new Gimmick(gx, gy, 20, new Color(0, 255, 0), 0));
        }

        // 弾の移動と当たり判定
        Iterator<Bullet> it = bullets.iterator();
        Iterator<Gimmick> it2 = gimmicks.iterator();
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
            if (b.getStateExplosion() != 0) {
                if (b.explosion()) {
                    b.setStateExplosion(b.getStateExplosion() + 1);
                    b.setAnimationFrames(0);
                } else {
                    b.setAnimationFrames(b.getAnimationFrames() + 1);
                }
            }

            if (!b.getIsActive()) {
                it.remove();
                continue;
            }

            boolean hit = false;
            // Player1への当たり判定
            if (b.getOwner() != player1 && isHit(player1, b) && b.getStateExplosion() == 0) {
                GameFrame.playSE("music/damaged.wav", 0.5f);
                player1.hit(1); // 1ダメージ
                hit = true;
            }
            // Player2への当たり判定
            else if (b.getOwner() != player2 && isHit(player2, b) && b.getStateExplosion() == 0) {
                GameFrame.playSE("music/damaged.wav", 0.5f);
                player2.hit(1);
                hit = true;
            }

            if (hit) {
                b.setStateExplosion(1);
                b.setAnimationFrames(0);
            }
        }

        while (it2.hasNext()) {
            Gimmick g = it2.next();

            // 画面外に出たら削除
            if (g.getX() < -50 || g.getX() > court_size_x + 50 ||
                    g.getY() < -50 || g.getY() > court_size_y + 50) {
                it2.remove();
                continue;
            }

            boolean hit = false;
            // Player1への当たり判定
            if (isHit_Gimmick(player1, g) && player1.getStatePowerup() == 0) {
                player1.setStatePowerup(1);
                player1.setImg();
                player1.setBiggerbullet(20);
                hit = true;
            }
            // Player2への当たり判定
            else if (isHit_Gimmick(player2, g) && player2.getStatePowerup() == 0) {
                player2.setStatePowerup(1);
                player2.setImg();
                player2.setBiggerbullet(20);
                hit = true;
            }

            g.setTime(g.getTime() + 1);

            if (g.getTime() > 750 || hit) {
                it2.remove();
            }
        }

        if (player1.getStatePowerup() > 0 && player1.getStatePowerup() < 400) {
                player1.setStatePowerup(player1.getStatePowerup() + 1);
                player1.setImg();
            }else if (player1.getStatePowerup() >= 400) {
                player1.setStatePowerup(0);
                player1.setBiggerbullet(0);
                player1.setImg();
            }

            if (player2.getStatePowerup() > 0 && player2.getStatePowerup() < 400) {
                player2.setStatePowerup(player2.getStatePowerup() + 1);
                player2.setImg();
            }else if (player2.getStatePowerup() >= 400) {
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

    // 当たり判定の補助メソッド
    private boolean isHit(Player p, Bullet b) {
        // 距離の2乗で判定 (平方根計算を避けて高速化)
        double dx = p.getX() - b.getX();
        double dy = p.getY() - b.getY();
        double rSum = p.getRadius() + b.getRadius();
        return (dx * dx + dy * dy) <= (rSum * rSum);
    }

    private boolean isHit_Gimmick(Player p, Gimmick g) {
        // 距離の2乗で判定 (平方根計算を避けて高速化)
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
                .append(winner).append("#");

        // 2. 弾情報 (x, y, radius, colorRGB)
        for (Bullet b : bullets) {
            // Color取得時にnullチェックを入れると安全
            int rgb = (b.getColor() != null) ? b.getColor().getRGB() : Color.RED.getRGB();

            // 弾の型判定
            int type = 0;
            if (b instanceof CurveBullet) {
                type = 1;
            } else if (b instanceof UpDiagonalBullet) {
                type = 2;
            } else if (b instanceof DownDiagonalBullet) {
                type = 3;
            }
            sb.append(b.getX()).append(",").append(b.getY()).append(",")
                    .append(b.getRadius()).append(",").append(rgb).append(",")
                    .append(b.getShootdir()).append(",")
                    .append(type).append(",")
                    .append(b.getStateExplosion()).append(",")
                    .append(b.getAnimationFrames()).append(";");
        }
        sb.append("#");

        //3 Gimmick情報
        for (Gimmick g : gimmicks) {
            int rgb = (g.getColor() != null) ? g.getColor().getRGB() : Color.RED.getRGB();

            sb.append(g.getX()).append(",").append(g.getY()).append(",")
                    .append(g.getRadius()).append(",").append(rgb).append(",")
                    .append(g.getTime()).append(";");
        }
        sb.append("#");

        sv.send(sb.toString());
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

            // これで空文字があっても配列の長さは 11 になるので、if文の中に入れる
            if (basic.length >= 11) {
                player1.setXY(Integer.parseInt(basic[0]), Integer.parseInt(basic[1]));
                player1.setHP(Integer.parseInt(basic[2]));
                player2.setXY(Integer.parseInt(basic[3]), Integer.parseInt(basic[4]));
                player2.setHP(Integer.parseInt(basic[5]));
                player1.setStatePowerup(Integer.parseInt(basic[6]));
                player2.setStatePowerup(Integer.parseInt(basic[7]));
                player1.setBiggerbullet(Integer.parseInt(basic[8]));
                player2.setBiggerbullet(Integer.parseInt(basic[9]));
                if (Integer.parseInt(basic[5]) < old_hp) {
                    player1.hit(0);
                }
                winner = basic[10]; // 空文字でもエラーにならず代入される

                player1.setImg();
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

            // 2. 弾情報 (リストを毎回作り直す = 完全同期)
            bullets.clear();
            if (!sections[1].isEmpty()) {
                String[] bList = sections[1].split(";");
                for (String bStr : bList) {
                    if (bStr.isEmpty())
                        continue;
                    String[] val = bStr.split(",");
                    if (val.length >= 8) {
                        int bx = Integer.parseInt(val[0]);
                        int by = Integer.parseInt(val[1]);
                        int br = Integer.parseInt(val[2]);
                        // 色情報のパース
                        Color bc = new Color(Integer.parseInt(val[3]));
                        // どちらの弾か
                        int bs = Integer.parseInt(val[4]);
                        // なんの弾か
                        int type = Integer.parseInt(val[5]);
                        // 爆発の段階
                        int se = Integer.parseInt(val[6]);
                        // 爆発アニメーションのフレーム
                        int af = Integer.parseInt(val[7]);

                        // 受信専用のBulletを作る

                        if (type == 0) {
                            bullets.add(new Bullet(bx, by, br, bc, bs, se, af));
                        } else if (type == 1) {
                            bullets.add(new CurveBullet(bx, by, br, bc, bs, se, af));
                        } else if (type == 2) {
                            bullets.add(new UpDiagonalBullet(bx, by, br, bc, bs, se, af));
                        } else if (type == 3) {
                            bullets.add(new DownDiagonalBullet(bx, by, br, bc, bs, se, af));
                        }
                    }
                }
            }

            // 3. アイテム情報 (実装時はここに追加)
            gimmicks.clear();
            if (!sections[2].isEmpty()) {
                String[] gList = sections[2].split(";");
                for (String gStr : gList) {
                    if (gStr.isEmpty())
                        continue;
                    String[] val = gStr.split(",");
                    if (val.length >= 5) {
                        int gx = Integer.parseInt(val[0]);
                        int gy = Integer.parseInt(val[1]);
                        int gr = Integer.parseInt(val[2]);
                        // 色情報のパース
                        Color gc = new Color(Integer.parseInt(val[3]));
                        int gt = Integer.parseInt(val[4]);

                        // 受信専用のGimmickを作る
                        gimmicks.add(new Gimmick(gx, gy, gr, gc, gt));
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

    public boolean getIsRunning() {
        return this.isRunning;
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

        for (Gimmick gm : gimmicks) {
            gm.draw(g);
        }
    }
}
