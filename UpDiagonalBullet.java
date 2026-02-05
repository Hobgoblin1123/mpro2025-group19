import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;

public class UpDiagonalBullet extends Bullet implements ActionListener {
    private final static int dy = 5;

    public UpDiagonalBullet(Player owner, int x, int y, float speed, int radius, int damage, int Shootdir,
            Color color) {
        super(owner, x, y, speed, radius, damage, Shootdir, color);
        if (Shootdir == 1)
            this.img = new ImageIcon(getClass().getResource("./images/UpDiagonalBullet1.png")).getImage();
        else {
            this.img = new ImageIcon(getClass().getResource("./images/UpDiagonalBullet2.png")).getImage();
        }
    }

    public UpDiagonalBullet(int x, int y, int radius, Color color, int Shootdir, int state_explosion,
            int AnimationFrames) {
        super(x, y, radius, color, Shootdir, state_explosion, AnimationFrames);
        if (state_explosion == 1) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion1.png")).getImage();
            this.radius = 20;
        } else if (state_explosion == 2) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion2.png")).getImage();
            this.radius += 3;
        } else if (state_explosion == 3) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion3.png")).getImage();
            this.radius += 3;
        } else if (state_explosion == 4) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion4.png")).getImage();
            this.radius = 10;
        } else if (state_explosion != 5 && Shootdir == 1)
            this.img = new ImageIcon(getClass().getResource("./images/UpDiagonalBullet1.png")).getImage();
        else if (state_explosion != 5) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDiagonalBullet2.png")).getImage();
        }
    }

    public boolean explosion() {
        if (this.state_explosion == 1) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion1.png")).getImage();
            this.radius = 20;
            return true;
        } else if (this.state_explosion == 2 && this.AnimationFrames == 5) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion2.png")).getImage();
            this.radius += 3;
            return true;
        } else if (this.state_explosion == 3 && this.AnimationFrames == 5) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion3.png")).getImage();
            this.radius += 3;
            return true;
        } else if (this.state_explosion == 4 && this.AnimationFrames == 5) {
            this.img = new ImageIcon(getClass().getResource("./images/UpDownBulletExplosion4.png")).getImage();
            this.radius = 10;
            return true;
        } else if (this.state_explosion == 5 && this.AnimationFrames == 5) {
            this.isActive = false;
            return true;
        }
        return false;
    }

    public void draw(Graphics g) {
        // g.setColor(this.color);
        // g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        g.drawImage(this.img, getX() - getRadius(), getY() - getRadius(), 2 * getRadius(), 2 * getRadius(), null);
    }

    public void move() {
        super.move();

        if (super.getStateExplosion() == 0) {
            int newY = (int) (super.getY() - 0.2 * dy * super.getSpeed());
            super.setY(newY);
        }

    }

    public void actionPerformed(ActionEvent e) {
        move();
    }
}