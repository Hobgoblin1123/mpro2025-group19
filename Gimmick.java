import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.ImageIcon;

//ギミッククラス
public class Gimmick {
    private int x; //x座標
    private int y; //y座標
    protected int radius; //半径
    private Color color = new Color(255, 0, 0); //色(円で描画する場合)
    protected Image img; //ギミックの画像
    private int time; //表示されている時間経過を表すフレーム
    private int type; //ギミックの種類(0:弾の半径拡大, 1:回復)

    //コンストラクタ
    public Gimmick(int x, int y, int radius, Color color, int time, int type) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
        this.time = time;
        this.type = type;
        if(type == 0){ //弾の半径拡大ギミックの画像読み込み
            this.img = new ImageIcon(getClass().getResource("/images/BigRadius.png")).getImage();
        }else if(type == 1){ //回復ギミックの画像読み込み
            this.img = new ImageIcon(getClass().getResource("/images/Heal.png")).getImage();
        }
    }

    //ギミックの描画
    public void draw(Graphics g) {
        //g.setColor(this.color); //画像でなく円で描画する場合
        //g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        //imgに応じて画像を描画
        g.drawImage(img, x - radius, y - radius, 2 * radius, 2 * radius, null);
    }

    //getter, setterメソッド
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