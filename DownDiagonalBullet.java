import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DownDiagonalBullet extends Bullet implements ActionListener {
    private final static int dy = 5;

    public DownDiagonalBullet(Player owner, int x, int y, int speed, int radius, int damage, int Shootdir,
            Color color) {
        super(owner, x, y, speed, radius, damage, Shootdir, color);
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