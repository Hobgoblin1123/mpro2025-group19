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
    private int Shootdir;
    private final static int dx = 5;
    private final static int dy = 5;
    private Image img;
    private boolean isWin = false;
    private float shakeTime = 0;
    private int biggerbullet = 0;
    private int statePowerup = 0;

    public Player(int hp, int max_hp, int x, int y, int speed, int bounds_x_min, int bounds_x_max, int bounds_y,
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

        setImg();
    }

    public void setImg(){
        if (Shootdir == 1 && statePowerup == 0){
            this.img = new ImageIcon(getClass().getResource("player1T.png")).getImage();
        }else if(Shootdir == 1 && statePowerup > 0){
            if((statePowerup / 10) % 2 == 0){
                this.img = new ImageIcon(getClass().getResource("./images/player1PowerupT1.png")).getImage();
            }else{
                this.img = new ImageIcon(getClass().getResource("./images/player1PowerupT2.png")).getImage();
            }
        }else if(Shootdir != 1 && statePowerup == 0){
            this.img = new ImageIcon(getClass().getResource("player2T.png")).getImage();
        }else{
            if((statePowerup / 10) % 2 == 0){
                this.img = new ImageIcon(getClass().getResource("./images/player2PowerupT1.png")).getImage();
            }else{
                this.img = new ImageIcon(getClass().getResource("./images/player2PowerupT2.png")).getImage();
            }
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
        shakeTime = 15;
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

    public void passShakeTime() {
        this.shakeTime -= 1;
    }

    public void setIsWin(boolean isWin) {
        this.isWin = isWin;
    }

    public boolean getIsWin() {
        return isWin;
    }

    public ArrayList<Bullet> tryShoot(int type) {

        ArrayList<Bullet> newBullets = new ArrayList<>();
        if (type == 0) {
            // 直進弾
            GameFrame.playSE("music/Gun_Shot.wav", 0.5f);
            newBullets.add(new Bullet(this, this.getX(), this.getY(), 4, 15 + this.getBiggerbullet(), 1, Shootdir, null));
        } else if (type == 1) {
            // 曲線弾
            GameFrame.playSE("music/Thunder_Shot.wav", 0.5f);
            newBullets.add(new CurveBullet(this, this.getX(), this.getY(), 1, 10 + this.getBiggerbullet(), 1, Shootdir, null));
        } else if (type == 2) {
            // 斜め2方向弾
            GameFrame.playSE("music/Gun_Shot.wav", 0.5f);
            newBullets.add(new UpDiagonalBullet(this, this.getX(), this.getY(), 2, 15 + this.getBiggerbullet(), 1, Shootdir, null));
            newBullets.add(new DownDiagonalBullet(this, this.getX(), this.getY(), 2, 15 + this.getBiggerbullet(), 1, Shootdir, null));
        }
        return newBullets;
    }
}