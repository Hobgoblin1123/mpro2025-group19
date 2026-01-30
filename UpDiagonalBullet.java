import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UpDiagonalBullet extends Bullet implements ActionListener{
    private final static int dy = 5;


    public UpDiagonalBullet(Player owner, boolean server, int x, int y, int speed, int bounds_x, int radius, int damage, Color color){
        super(owner, server, x, y, speed, bounds_x, radius, damage, color);
    }

    public void move(){
        super.move();

        int  newY = super.getY() - dy * super.getSpeed();
        super.setY(newY);

    }

    public void actionPerformed(ActionEvent e) {
        move();
    }
}