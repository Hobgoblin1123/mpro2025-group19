import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CurveBullet extends Bullet implements ActionListener {
    double time = 0;
    double frequency = 0.3;
    double amplitude = 20;
    int y = super.getY();

    public CurveBullet(Player owner, int x, int y, int speed, int radius, int damage, int Shootdir,
            Color color) {
        super(owner, x, y, speed, radius, damage, Shootdir, color);
    }

    public void move() {
        super.move();

        time += 1;

        int newY = (int) (y + Math.sin(time * frequency) * amplitude);
        super.setY(newY);

    }

    public void actionPerformed(ActionEvent e) {
        move();
    }
}