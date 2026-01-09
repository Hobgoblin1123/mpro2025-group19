import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Bullet implements ActionListener{
    private Player enemyPlayer;
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

    public Bullet(Player enemyPlayer,boolean server, int x, int y, int speed, int bounds_x, int radius, int damage, Color color){
        this.enemyPlayer = enemyPlayer;
        this.server = server;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.color = color;
        this.bounds_x = bounds_x;
        this.radius = radius;
        this.damage = damage;
        timer = new Timer(16, this);
        timer.start();
    }

    public void draw(Graphics g){
        g.setColor(this.color);
        g.fillOval(x - radius, y - radius, 2*radius, 2*radius);
    }

    public void move(){
        if(this.server){
            x -= dx * speed;
        }else{
            x += dx * speed;
        }
    }

    public void checkbounds(){
        if( (x + radius) < 0 || (x - radius) > bounds_x ){
            this.color = new Color(0, 0, 0);
            timer.stop();
            isActive = false;
        }
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void actionPerformed(ActionEvent e) {
        if(this.enemyPlayer.checkhit(this.x,this.y,this.radius)){
            enemyPlayer.hit(this.damage);
            this.color = new Color(0, 0, 0);
            timer.stop();
            isActive = false;
        }
        move();
        checkbounds();
    }
}