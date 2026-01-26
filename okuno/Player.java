import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.geom.Ellipse2D;

class Player {
    private int hp;
    private int max_hp;
    private int x;
    private int y;
    private int speed;
    private int bounds_x_max;
    private int bounds_x_min;
    private int bounds_y;
    private int radius;
    private boolean isWin;
    private final static int dx = 5;
    private final static int dy = 5;
    private Image img;

    public Player(int hp, int max_hp, int x, int y, int speed, int bounds_x_min, int bounds_x_max, int bounds_y,
            int radius) {
        this.hp = hp;
        this.max_hp = max_hp;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.bounds_x_max = bounds_x_max;
        this.bounds_x_min = bounds_x_min;
        this.bounds_y = bounds_y;
        this.radius = radius;

        try {
            if (bounds_x_min == 0)
                this.img = new ImageIcon(getClass().getResource("player1.jpg")).getImage();
            else
                this.img = new ImageIcon(getClass().getResource("player2.jpg")).getImage();
        } catch (Exception e) {
            // 画像がないときは何もしない（drawでエラーにならないよう注意）
            this.img = null; 
        }
    }

    public void draw(Graphics g) {
        // g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        g.drawImage(img, x - radius, y - radius, 2 * radius, 2 * radius, null);
    }

    public void moveUp() {
        if ((y - radius) - dy * speed < 0) {
            y = radius;
            return;
        }
        y -= dy * speed;
    }

    public void moveDown() {
        if ((y + radius) + dy * speed > bounds_y) {
            y = bounds_y - radius;
            return;
        }
        y += dy * speed;
    }

    public void moveLeft() {
        if ((x - radius) - dx * speed < bounds_x_min) {
            x = bounds_x_min + radius;
            return;
        }
        x -= dx * speed;
    }

    public void moveRight() {
        if ((x + radius) + dx * speed > bounds_x_max) {
            x = bounds_x_max - radius;
            return;
        }
        x += dx * speed;
    }

    public int hit(int damage) {
        hp -= damage;
        if (hp < 0) {
            hp = 0;
        }
        return hp;
    }

    public int heal(int amount) {
        if (hp + amount > max_hp) {
            hp = max_hp;
        } else {
            hp += amount;
        }
        return hp;
    }

    public Shape getBounds() {
        return new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
    }

    public int getHp() {
        return hp;
    }

    public boolean IsDead() {
        return (hp <= 0);
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

    public void setWin(boolean isWin) {
        this.isWin = isWin;
    }

    public boolean getWin() {
        return isWin;
    }

    // 修正後の tryShoot メソッド
    public ArrayList<Bullet> tryShoot() {
        ArrayList<Bullet> newBullets = new ArrayList<>();
        
        // 1. 自分がPlayer1(左側スタート)かPlayer2(右側スタート)かを判定
        // bounds_x_min が 0 なら Player1 (Server側と仮定) とみなす
        boolean isPlayer1 = (bounds_x_min == 0);

        // 2. 弾の色を決める (Player1は赤、Player2は青など)
        Color bulletColor = isPlayer1 ? Color.RED : Color.BLUE;

        // 3. 弾のサイズとダメージ
        int bulletSize = 10;
        int bulletDamage = 1;
        int bulletSpeed = 10; // プレイヤーより速くする

        // 4. Bulletを生成 (正しい引数の順番で！)
        // (Owner, 方向フラグ, 現在X, 現在Y, 速度, 画面幅, サイズ, ダメージ, 色)
        newBullets.add(new Bullet( this, isPlayer1, this.x, this.y, bulletSpeed, bounds_x_max, bulletSize, bulletDamage, bulletColor));
        return newBullets;
    }
}