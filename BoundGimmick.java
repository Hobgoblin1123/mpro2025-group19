import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class BoundGimmick {
    private int x;
    private int y;
    private int speed_x;
    private int speed_y;
    private int bounds_up;
    private int bounds_down;
    private int bounds_right;
    private int bounds_left;
    private int radius;
    private int damage;
    private Color color = new Color(255, 0, 0);
    private final static int dx = 5;
    private Timer timer;
    public boolean isActive = true;

    public BoundGimmick(int x, int y, int speed_x, int speed_y, int bounds_up, int bounds_down, int bounds_right,
            int bounds_left, int radius, int damage,
            Color color) {
        this.x = x;
        this.y = y;
        this.speed_x = speed_x;
        this.speed_y = speed_y;
        this.color = color;
        this.bounds_up = bounds_up;
        this.bounds_down = bounds_down;
        this.bounds_right = bounds_right;
        this.bounds_left = bounds_left;
        this.radius = radius;
        this.damage = damage;
    }

    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    public void move() {
        this.x += this.speed_x;
        this.y += this.speed_y;
    }

    public void checkBound() {
        if (this.x + this.speed_x > this.bounds_right) {
            this.speed_x *= -1;
        }
        if (this.x + this.speed_x < this.bounds_left) {
            this.speed_x *= -1;
        }
        if (this.y + this.speed_y > this.bounds_down) {
            this.speed_y *= -1;
        }
        if (this.y + this.speed_y < this.bounds_up) {
            this.speed_y *= -1;
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void update() {
        checkBound();
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