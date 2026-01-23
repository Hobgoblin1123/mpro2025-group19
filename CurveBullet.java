import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CurveBullet extends Bullet implements ActionListener{
    double time = 0;
    double frequency = 0.3;
    double amplitude = 20;
    int y = super.getY();

    public CurveBullet(Player owner, boolean server, int x, int y, int speed, int bounds_x, int radius, int damage, Color color){
        super(owner, server, x, y, speed, bounds_x, radius, damage, color);
    }

    public void move(){
        super.move();

        time += 1;

        int newY = (int) (y + Math.sin(time * frequency) * amplitude);
        super.setY(newY);

    }

    public void actionPerformed(ActionEvent e) {
        move();
    }
}