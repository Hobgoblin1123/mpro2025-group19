import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.ImageIcon;

public class Gimmick {
    private int x;
    private int y;
    protected int radius;
    private Color color = new Color(255, 0, 0);
    protected Image img;
    private int time;
    private int type;

    public Gimmick(int x, int y, int radius, Color color, int time, int type) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
        this.time = time;
        this.type = type;
        if(type == 0){
            this.img = new ImageIcon(getClass().getResource("./images/BigRadius.png")).getImage();
        }else if(type == 1){
            this.img = new ImageIcon(getClass().getResource("./images/Heal.png")).getImage();
        }
    }

    public void draw(Graphics g) {
        //g.setColor(this.color);
        //g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        g.drawImage(img, x - radius, y - radius, 2 * radius, 2 * radius, null);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
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

    public Color getColor() {
        return this.color;
    }
}