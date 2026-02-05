import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.ImageIcon;

//通常の弾クラス
public class Bullet {
    private Player ownerPlayer; // 弾の所有者
    private int x; // 弾のx座標
    private int y; // 弾のy座標
    private float speed; // 弾の速度
    protected int radius; // 弾の半径
    private int damage; // 弾が持つダメージ
    private int Shootdir; // 弾の向き(1:サーバー側)
    private Color color = new Color(255, 0, 0); // 弾の色(円で描画する場合)
    private final static int dx = 5; // 弾の移動量
    public boolean isActive = true; // 弾のアクティブ状態
    protected Image img; // 弾の画像
    protected int state_explosion = 0; // 弾の爆発状態
    protected int AnimationFrames; // 弾の爆発アニメーションのフレーム状態

    // サーバー用のコンストラクタ
    public Bullet(Player owner, int x, int y, float speed, int radius, int damage, int Shootdir,
            Color color) {
        this.ownerPlayer = owner;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.color = color;
        this.radius = radius;
        this.damage = damage;
        this.Shootdir = Shootdir;
        if (Shootdir == 1) // サーバー側の弾の画像
            this.img = new ImageIcon(getClass().getResource("./images/Bullet1.png")).getImage();
        else { // クライアント側の弾の画像
            this.img = new ImageIcon(getClass().getResource("./images/Bullet2.png")).getImage();
        }
    }

    // クライアント用のコンストラクタ
    public Bullet(int x, int y, int radius, Color color, int Shootdir, int state_explosion, int AnimationFrames) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
        this.Shootdir = Shootdir;
        this.AnimationFrames = AnimationFrames;
        this.state_explosion = state_explosion;

        // 爆発状態に応じて画像を変更
        if (state_explosion == 1) { // 状態1
            this.img = new ImageIcon(getClass().getResource("./images/BulletExplosion1.png")).getImage();
            this.radius = 20; // 爆発時の半径は大きくする
        } else if (state_explosion == 2) { // 状態2
            this.img = new ImageIcon(getClass().getResource("./images/BulletExplosion2.png")).getImage();
            this.radius += 3;
        } else if (state_explosion == 3) { // 状態3
            this.img = new ImageIcon(getClass().getResource("./images/BulletExplosion3.png")).getImage();
            this.radius += 3;
        } else if (state_explosion == 4) { // 状態4
            this.img = new ImageIcon(getClass().getResource("./images/BulletExplosion4.png")).getImage();
            this.radius = 10;
        } else if (state_explosion != 5 && Shootdir == 1) // 通常状態(サーバー側)
            this.img = new ImageIcon(getClass().getResource("./images/Bullet1.png")).getImage();
        else if (state_explosion != 5) { // 通常状態(クライアント側)
            this.img = new ImageIcon(getClass().getResource("./images/Bullet2.png")).getImage();
        }
    }

    // 弾の描画
    public void draw(Graphics g) {
        // g.setColor(this.color);, 画像でなく円で描画する場合
        // g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        // imgに応じて画像を描画
        g.drawImage(img, x - radius, y - radius, 2 * radius, 2 * radius, null);
    }

    // 弾の移動
    public void move() {
        if (this.state_explosion == 0) { // 爆発状態でなければ移動
            x += dx * speed * Shootdir;
        }
    }

    // 爆発処理, 爆発が進むとtrueを返す
    public boolean explosion() {
        if (this.state_explosion == 1) { // 状態1
            this.img = new ImageIcon(getClass().getResource("./images/BulletExplosion1.png")).getImage();
            this.radius = 20; // 爆発時の半径は大きくする
            return true;
        } else if (this.state_explosion == 2 && this.AnimationFrames == 5) { // 状態2, 5フレームごとに更新
            this.img = new ImageIcon(getClass().getResource("./images/BulletExplosion2.png")).getImage();
            this.radius += 3;
            return true;
        } else if (this.state_explosion == 3 && this.AnimationFrames == 5) { // 状態3
            this.img = new ImageIcon(getClass().getResource("./images/BulletExplosion3.png")).getImage();
            this.radius += 3;
            return true;
        } else if (this.state_explosion == 4 && this.AnimationFrames == 5) { // 状態4
            this.img = new ImageIcon(getClass().getResource("./images/BulletExplosion4.png")).getImage();
            this.radius = 10;
            return true;
        } else if (this.state_explosion == 5 && this.AnimationFrames == 5) { // 状態5(爆発終了), 非アクティブ化
            this.isActive = false;
            return true;
        }
        return false; // 爆発が進んでいない場合はfalseを返す
    }

    // 弾の座標更新
    public void update() {
        move();
    }

    // getter, setterメソッド
    public Player getOwner() {
        return this.ownerPlayer;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean getIsActive() {
        return this.isActive;
    }

    public int getStateExplosion() {
        return this.state_explosion;
    }

    public void setStateExplosion(int state_explosion) {
        this.state_explosion = state_explosion;
    }

    public int getAnimationFrames() {
        return this.AnimationFrames;
    }

    public void setAnimationFrames(int AnimationFrames) {
        this.AnimationFrames = AnimationFrames;
    }

    public int getShootdir() {
        return this.Shootdir;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Shape getBounds() {
        return new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
    }

    public int getRadius() {
        return this.radius;
    }

    public int getDamage() {
        return this.damage;
    }

    public Color getColor() {
        return this.color;
    }

    public float getSpeed() {
        return speed;
    }
}