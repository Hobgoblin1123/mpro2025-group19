import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.ImageIcon;

public class Bullet {
    private Player ownerPlayer;
    private boolean server;
    private int x;
    private int y;
    private int speed;
    private int bounds_x;
    protected int radius;
    private int damage;
    private int Shootdir;
    private Color color = new Color(255, 0, 0);
    private final static int dx = 5;
    private Timer timer;
    public boolean isActive = true;
    protected Image img;
    private int state_explosion = 0;
    private int AnimationFrames;

    public Bullet(Player owner, int x, int y, int speed, int radius, int damage, int Shootdir,
            Color color) {
        this.ownerPlayer = owner;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.color = color;
        this.radius = radius;
        this.damage = damage;
        this.Shootdir = Shootdir;
        if (Shootdir == 1)
            this.img = new ImageIcon(getClass().getResource("./images/Bullet1.png")).getImage();
        else {
            this.img = new ImageIcon(getClass().getResource("./images/Bullet2.png")).getImage();
        }
    }

    public Bullet(int x, int y, int radius, Color color, int Shootdir, int state_explosion, int AnimationFrames) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
        this.Shootdir = Shootdir;
        this.AnimationFrames = AnimationFrames;
        this.state_explosion = state_explosion;

        if (state_explosion == 1) {
            this.img = new ImageIcon(getClass().getResource("./images/Explosion1.png")).getImage();
            this.radius = 20;
        } else if (state_explosion == 2) {
            this.img = new ImageIcon(getClass().getResource("./images/Explosion2.png")).getImage();
            this.radius += 3;
        } else if (state_explosion == 3) {
            this.img = new ImageIcon(getClass().getResource("./images/Explosion3.png")).getImage();
            this.radius += 3;
        } else if (state_explosion == 4) {
            this.img = new ImageIcon(getClass().getResource("./images/Explosion4.png")).getImage();
            this.radius = 10;
        } else if (state_explosion != 5 && Shootdir == 1)
            this.img = new ImageIcon(getClass().getResource("./images/Bullet1.png")).getImage();
        else if(state_explosion != 5){
            this.img = new ImageIcon(getClass().getResource("./images/Bullet2.png")).getImage();
        }
    }

    public void draw(Graphics g) {
        // g.setColor(this.color);
        // g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        g.drawImage(img, x - radius, y - radius, 2 * radius, 2 * radius, null);
    }

    public void move() {
        if(this.state_explosion == 0){
            x += dx * speed * Shootdir;
        }
    }

    public boolean explosion() {
        if(this.state_explosion == 1){
            this.img = new ImageIcon(getClass().getResource("./images/Explosion1.png")).getImage();
            this.radius = 20;
            return true;
        }else if(this.state_explosion == 2 && this.AnimationFrames == 5){
            this.img = new ImageIcon(getClass().getResource("./images/Explosion2.png")).getImage();
            this.radius += 3;
            return true;
        }else if(this.state_explosion == 3 && this.AnimationFrames == 5){
            this.img = new ImageIcon(getClass().getResource("./images/Explosion3.png")).getImage();
            this.radius += 3;
            return true;
        }else if(this.state_explosion == 4 && this.AnimationFrames == 5){
            this.img = new ImageIcon(getClass().getResource("./images/Explosion4.png")).getImage();
            this.radius = 10;
            return true;
        }else if(this.state_explosion == 5 && this.AnimationFrames == 5){
            this.isActive = false;
            return true;
        }
        return false;
    }

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

    public void update() {
        move();
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

    public int getSpeed() {
        return speed;
    }
}