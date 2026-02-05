import java.awt.*;
import javax.swing.ImageIcon;

//斜め下方向に進む弾クラス
public class DownDiagonalBullet extends Bullet {
    private final static int dy = 5; //y方向の移動量

    //サーバー用のコンストラクタ
    public DownDiagonalBullet(Player owner, int x, int y, int speed, int radius, int damage, int Shootdir,
            Color color) {
        super(owner, x, y, speed, radius, damage, Shootdir, color);
        if (Shootdir == 1) //サーバー側の弾の画像
            this.img = new ImageIcon(getClass().getResource("./images/DownDiagonalBullet1.png")).getImage();
        else { //クライアント側の弾の画像
            this.img = new ImageIcon(getClass().getResource("./images/DownDiagonalBullet2.png")).getImage();
        }
    }

    //クライアント用のコンストラクタ
    public DownDiagonalBullet(int x, int y, int radius, Color color, int Shootdir, int state_explosion, int AnimationFrames) {
        super(x, y, radius, color, Shootdir, state_explosion, AnimationFrames);
        //爆発状態に応じて画像を変更
        if (state_explosion == 1) { //状態1
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion1.png")).getImage();
            this.radius = 20; //爆発時の半径は大きくする
        } else if (state_explosion == 2) { //状態2
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion2.png")).getImage();
            this.radius += 3;
        } else if (state_explosion == 3) { //状態3
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion3.png")).getImage();
            this.radius += 3;
        } else if (state_explosion == 4) { //状態4
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion4.png")).getImage();
            this.radius = 10;
        } else if (state_explosion != 5 && Shootdir == 1) //サーバー側の画像
            this.img = new ImageIcon(getClass().getResource("./images/DownDiagonalBullet1.png")).getImage();
        else if(state_explosion != 5){ //クライアント側の画像
            this.img = new ImageIcon(getClass().getResource("./images/DownDiagonalBullet2.png")).getImage();
        }
    }

    //爆発処理, 爆発が進むとtrueを返す
    public boolean explosion() {
        if(this.state_explosion == 1){ //状態1
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion1.png")).getImage();
            this.radius = 20; //爆発時の半径は大きくする
            return true;
        }else if(this.state_explosion == 2 && this.AnimationFrames == 5){ //状態2, 5フレームごとに更新
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion2.png")).getImage();
            this.radius += 3;
            return true;
        }else if(this.state_explosion == 3 && this.AnimationFrames == 5){ //状態3
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion3.png")).getImage();
            this.radius += 3;
            return true;
        }else if(this.state_explosion == 4 && this.AnimationFrames == 5){ //状態4
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion4.png")).getImage();
            this.radius = 10;
            return true;
        }else if(this.state_explosion == 5 && this.AnimationFrames == 5){ //状態5(爆発終了), 非アクティブ化
            this.isActive = false;
            return true;
        }
        return false; //爆発が進んでいない場合falseを返す
    }

    //弾の描画
    public void draw(Graphics g) {
        //g.setColor(this.color); //画像でなく円で描画する場合
        //g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        //imgに応じて画像を描画
        g.drawImage(this.img, getX() - getRadius(), getY() - getRadius(), 2 * getRadius(), 2 * getRadius(), null);
    }

    //弾の移動
    public void move() {
        super.move();

        //y座標更新(爆発状態でなければ)
        if(super.getStateExplosion() == 0){
            int newY = (int)(super.getY() + 0.2 * dy * super.getSpeed());
            super.setY(newY); //y座標セット
        }
    }
}