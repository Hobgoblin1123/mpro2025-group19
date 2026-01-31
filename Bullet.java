import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Bullet {
    private Player ownerPlayer;
    private boolean server;
    private int x;
    private int y;
    private int speed;
    private int bounds_x;
    private int radius;
    private int damage;
    private int Shootdir;
    private Color color = new Color(255, 0, 0);
    private final static int dx = 5;
    private Timer timer;
    public boolean isActive = true;

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
    }

    public Bullet(int x, int y, int radius, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
    }

    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    public void move() {
        x += dx * speed * Shootdir;
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