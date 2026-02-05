import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.geom.Ellipse2D;

//プレイヤークラス
class Player {
    private int hp; // 現在の体力
    private int max_hp; // 最大体力
    private int x; // x座標
    private int y; // y座標
    private float speed; // 移動速度
    private int bounds_x_max; // x座標の最大値(壁)
    private int bounds_x_min; // x座標の最小値(壁)
    private int bounds_y; // y座標の最大値(床)
    private int radius; // 半径
    private int Shootdir; // プレイヤーの向き(1:サーバー側)
    private final static int dx = 5; // x方向の移動量
    private final static int dy = 5; // y方向の移動量
    private Image img; // プレイヤーの画像
    private boolean isWin = false; // 勝利フラグ
    private float shakeTime = 0; // 画面揺れ時間
    private int biggerbullet = 0; // ギミック効果時に弾の大きさをどれだけ大きくするか
    private int statePowerup = 0; // パワーアップ状態(0:通常, 1以上:パワーアップ中)

    // コンストラクタ
    public Player(int hp, int max_hp, int x, int y, float speed, int bounds_x_min, int bounds_x_max, int bounds_y,
            int radius, int Shootdir, int biggerbullet, int statePowerup) {
        this.hp = hp;
        this.max_hp = max_hp;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.bounds_x_max = bounds_x_max;
        this.bounds_x_min = bounds_x_min;
        this.bounds_y = bounds_y;
        this.radius = radius;
        this.Shootdir = Shootdir;
        this.biggerbullet = biggerbullet;
        this.statePowerup = statePowerup;

        setImg(); // 画像読み込み
    }

    // 画像読み込み
    public void setImg() {
        if (Shootdir == 1 && statePowerup == 0) { // サーバー側, 通常時
            this.img = new ImageIcon(getClass().getResource("player1T.png")).getImage();
        } else if (Shootdir == 1 && statePowerup > 0) { // サーバー側, 弾半径増加時
            if ((statePowerup / 10) % 2 == 0) { // 効果中2つの画像を切り替える
                this.img = new ImageIcon(getClass().getResource("./images/player1PowerupT1.png")).getImage();
            } else {
                this.img = new ImageIcon(getClass().getResource("./images/player1PowerupT2.png")).getImage();
            }
        } else if (Shootdir != 1 && statePowerup == 0) { // クライアント側, 通常時
            this.img = new ImageIcon(getClass().getResource("player2T.png")).getImage();
        } else if (Shootdir != 1 && statePowerup > 0) { // クライアント側, 弾半径増加時
            if ((statePowerup / 10) % 2 == 0) { // 効果中2つの画像を切り替える
                this.img = new ImageIcon(getClass().getResource("./images/player2PowerupT1.png")).getImage();
            } else {
                this.img = new ImageIcon(getClass().getResource("./images/player2PowerupT2.png")).getImage();
            }
        }
    }

    // 描画処理
    public void draw(Graphics g) {
        // g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius); //円で描画する場合
        // imgに応じて描画
        g.drawImage(img, x - radius, y - radius, 2 * radius, 2 * radius, null);
    }

    // 上移動
    public void moveUp() {
        if ((y - radius) - dy * speed < 0) {
            y = radius;
            return;
        }
        y -= dy * speed;
    }

    // 下移動
    public void moveDown() {
        if ((y + radius) + dy * speed > bounds_y) {
            y = bounds_y - radius;
            return;
        }
        y += dy * speed;
    }

    // 左移動
    public void moveLeft() {
        if ((x - radius) - dx * speed < bounds_x_min) {
            x = bounds_x_min + radius;
            return;
        }
        x -= dx * speed;
    }

    // 右移動
    public void moveRight() {
        if ((x + radius) + dx * speed > bounds_x_max) {
            x = bounds_x_max - radius;
            return;
        }
        x += dx * speed;
    }

    // ダメージ
    public int hit(int damage) {
        hp -= damage;
        shakeTime = 15;
        if (hp < 0) {
            hp = 0;
        }
        return hp;
    }

    // 回復
    public int heal(int amount) {
        if (hp + amount > max_hp) {
            hp = max_hp;
        } else {
            hp += amount;
        }
        return hp;
    }

    // 死亡判定
    public boolean IsDead() {
        return (hp <= 0);
    }

    // 勝利判定を返す
    public boolean getIsWin() {
        return isWin;
    }

    // 画面揺れ時間を経過させる
    public void passShakeTime() {
        this.shakeTime -= 1;
    }

    // 弾を発射する
    public ArrayList<Bullet> tryShoot(int type) {

        ArrayList<Bullet> newBullets = new ArrayList<>(); // 発射する弾のリスト(呼び出し側で, 元からある配列と結合する)
        if (type == 0) {
            // 直進弾
            GameFrame.playSE("music/Gun_Shot.wav", 0.5f);
            newBullets
                    .add(new Bullet(this, this.getX(), this.getY(), 4, 15 + this.getBiggerbullet(), 1, Shootdir, null));
        } else if (type == 1) {
            // 曲線弾
            GameFrame.playSE("music/Thunder_Shot.wav", 0.5f);
            newBullets.add(
                    new CurveBullet(this, this.getX(), this.getY(), 1, 10 + this.getBiggerbullet(), 1, Shootdir, null));
        } else if (type == 2) {
            // 斜め2方向弾
            GameFrame.playSE("music/Gun_Shot.wav", 0.5f);
            newBullets.add(new UpDiagonalBullet(this, this.getX(), this.getY(), 2, 15 + this.getBiggerbullet(), 1,
                    Shootdir, null));
            newBullets.add(new DownDiagonalBullet(this, this.getX(), this.getY(), 2, 15 + this.getBiggerbullet(), 1,
                    Shootdir, null));
        }
        return newBullets;
    }

    // getter, setterメソッド
    public Shape getBounds() {
        return new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
    }

    public int getHp() {
        return hp;
    }

    public float getShakeTime() {
        return this.shakeTime;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setHP(int hp) {
        this.hp = hp;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public int getBiggerbullet() {
        return biggerbullet;
    }

    public void setBiggerbullet(int biggerbullet) {
        this.biggerbullet = biggerbullet;
    }

    public int getStatePowerup() {
        return statePowerup;
    }

    public void setStatePowerup(int statePowerup) {
        this.statePowerup = statePowerup;
    }

    public void setIsWin(boolean isWin) {
        this.isWin = isWin;
    }
}