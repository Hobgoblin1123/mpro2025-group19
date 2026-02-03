import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;

public class DownDiagonalBullet extends Bullet implements ActionListener {
    private final static int dy = 5;
    private Image img;

    public DownDiagonalBullet(Player owner, int x, int y, int speed, int radius, int damage, int Shootdir,
            Color color) {
        super(owner, x, y, speed, radius, damage, Shootdir, color);
        if (Shootdir == 1)
            this.img = new ImageIcon(getClass().getResource("./images/DownDiagonalBullet1.png")).getImage();
        else {
            this.img = new ImageIcon(getClass().getResource("./images/DownDiagonalBullet2.png")).getImage();
        }
    }

    public DownDiagonalBullet(int x, int y, int radius, Color color, int Shootdir) {
        super(x, y, radius, color, Shootdir);
        if (Shootdir == 1)
            this.img = new ImageIcon(getClass().getResource("./images/DownDiagonalBullet1.png")).getImage();
        else {
            this.img = new ImageIcon(getClass().getResource("./images/DownDiagonalBullet2.png")).getImage();
        }
    }

    public void draw(Graphics g) {
        //g.setColor(this.color);
        //g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        g.drawImage(this.img, getX() - getRadius(), getY() - getRadius(), 2 * getRadius(), 2 * getRadius(), null);
    }

    public void move() {
        super.move();

        int newY = (int)(super.getY() + 0.4 * dy * super.getSpeed());
        super.setY(newY);

    }

    public void actionPerformed(ActionEvent e) {
        move();
    }
}