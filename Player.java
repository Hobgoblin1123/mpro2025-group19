import java.awt.*;

public class Player{
    private int hp;
    private int max_hp;
    private int x;
    private int y;
    private int speed;
    private int bounds_x;
    private int bounds_y;
    private int radius;
    private final static int dx = 5;
    private final static int dy = 5;

    public Player(int hp, int max_hp, int x, int y, int speed, int bounds_x, int bounds_y, int radius){
        this.hp = hp;
        this.max_hp = max_hp;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.bounds_x = bounds_x;
        this.bounds_y = bounds_y;
        this.radius = radius;
    }

    public void draw(Graphics g){
        g.fillOval(x - radius, y - radius, 2*radius, 2*radius);
    }

    public void moveUp(){
        if( (y - radius) - dy * speed < 0 ){
            y = radius;
            return;
        }
        y -= dy * speed;
    }

    public void moveDown(){
        if( (y + radius) + dy * speed > bounds_y ){
            y = bounds_y - radius;
            return;
        }
        y += dy * speed;
    }

    public void moveLeft(){
        if( (x - radius) - dx * speed < 0 ){
            x = radius;
            return;
        }
        x -= dx * speed;
    }

    public void moveRight(){
        if( (x + radius) + dx * speed > bounds_x ){
            x = bounds_x - radius;
            return;
        }
        x += dx * speed;
    }

    public int hit(int damage){
        hp -= damage;
        if(hp < 0){
            hp = 0;
        }
        return hp;
    }

    public int heal(int amount){
        if(hp + amount > max_hp){
            hp = max_hp;
        }else{
          hp += amount;
        }
        return hp;
    }

    public int checkhit(int x_bullet, int y_bullet, int radius_bullet){
        int dist_x = x - x_bullet;
        int dist_y = y - y_bullet;
        int dist2 = dist_x * dist_x + dist_y * dist_y;
        if( dist2 <= radius * radius ){
            return 1;
        }else{
            return 0;
        }
    }

    public int getHp(){
        return hp;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
}