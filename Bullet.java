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
    private Color color = new Color(255, 0, 0);
    private final static int dx = 5;
    private Timer timer;
    public boolean isActive = true;

    public Bullet(Player owner, boolean server, int x, int y, int speed, int bounds_x, int radius, int damage,
            Color color) {
        this.ownerPlayer = owner;
        this.server = server;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.color = color;
        this.bounds_x = bounds_x;
        this.radius = radius;
        this.damage = damage;
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
        if (this.server) {
            x -= dx * speed;
        } else {
            x += dx * speed;
        }
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
}