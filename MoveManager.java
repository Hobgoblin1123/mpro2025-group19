import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MoveManager {
    private boolean server; // ボールの動きの計算はサーバ側で行う．
    private CommServer sv = null;
    private CommClient cl = null;
    public int court_size_x, court_size_y; // コートの大きさ
    public Player myself, opponent; // Player 自分と対戦相手
    private double ball_x, ball_y; // ballの位置 (斜めに移動するので，doubleにする)
    private double ball_radius; // ボールの半径
    private double ball_moving_dir; // ボールの移動方向 (0-360)
    private double ball_moving_x, ball_moving_y; // ボールの移動方向
    private double ball_speed; // ボールのスピード

    public MoveManager(int x, int y, int offset, boolean server, String host, int port) {
        this.server = server;
        court_size_x = x;
        court_size_y = y;
        if (server) { // server の場合は，自分は左．clientの場合は自分は，右
            myself = new Player(offset, y / 2, 0, y);
            opponent = new Player(x - offset - myself.getWidth(), y / 2, 0, y);
            System.out.println("Waiting for connection with port no: " + port);
            sv = new CommServer(port);
            sv.setTimeout(1); // non-wait で通信
            System.out.println("Connected !");
        } else {
            opponent = new Player(offset, y / 2, 0, y);
            myself = new Player(x - offset - opponent.getWidth(), y / 2, 0, y);
            cl = new CommClient(host, port);
            cl.setTimeout(1); // non-wait で通信
            System.out.println("Connected to " + host + ":" + port + "!");
        }
        ball_x = offset + myself.getWidth() - 1;
        ball_y = y / 2.0 + myself.getHeight() / 2;
        ball_moving_dir = 0;
        calcMovingVector();
        ball_speed = 3;
        ball_radius = 10;
    }

    private void calcMovingVector() {
        while (ball_moving_dir < 0)
            ball_moving_dir += 360;
        while (ball_moving_dir > 360)
            ball_moving_dir -= 360;
        ball_moving_x = Math.cos(Math.toRadians(ball_moving_dir));
        ball_moving_y = Math.sin(Math.toRadians(ball_moving_dir));
    }

    // ボールがコート内かどうかチェック
    // 返り値: 0: コート内，1: 上方向で接触，2: 下で接触，3:左で接触，4:右で接触
    public int checkHit(double x, double y) {
        if (x <= ball_radius && ball_moving_x <= 0)
            return 3;
        if (x >= court_size_x - ball_radius && ball_moving_x >= 0)
            return 4;
        if (y <= ball_radius && ball_moving_y <= 0)
            return 1;
        if (y >= court_size_y - ball_radius && ball_moving_y >= 0)
            return 2;
        return 0;
    }

    // ボールを1ステップ進める．
    public void moveBall() {
        if (!server)
            return;
        double x0 = ball_x + ball_moving_x * ball_speed;
        double y0 = ball_y + ball_moving_y * ball_speed;
        int c = 0, off = 0;
        if (myself.checkHit(x0, y0)) {
            if (server) {
                c = 5;
                off = myself.getX() + myself.getWidth() - 1;
            } else {
                c = 6;
                off = myself.getX();
            }
            // 打ち返す方向にランダム性を導入
            ball_moving_dir += Math.random() * 40 - 20;
        } else if (opponent.checkHit(x0, y0)) {
            if (server) {
                c = 6;
                off = opponent.getX();
            } else {
                c = 5;
                off = opponent.getX() + opponent.getWidth() - 1;
            }
            // 打ち返す方向にランダム性を導入
            ball_moving_dir += Math.random() * 40 - 20;
            off = opponent.getX();
        } else {
            c = checkHit(x0, y0);
        }
        switch (c) {
            case 0:
                ball_x = x0;
                ball_y = y0;
                return;
            case 1: // 上に接触
            case 2: // 下に接触
                ball_moving_dir = 360 - ball_moving_dir;
                calcMovingVector();
                ball_x = x0;
                break;
            case 3: // 左に接触
            case 4: // 右に接触
            case 5: // プレーヤー１の左に接触
            case 6: // プレーヤー２の右に接触
                ball_moving_dir = 180 - ball_moving_dir;
                calcMovingVector();
                ball_y = y0; // 正確には，プレーヤーが打ち返した場合は，打ち返した瞬間に
                             // ランダムで方向が変化するので，それを考慮する必要があるが
                             // ここではそれは考慮せずに簡易的に実装
        }
        switch (c) {
            case 1: // 上に接触
                ball_y = 2 * ball_radius - y0;
                break;
            case 2: // 下に接触
                ball_y = (court_size_y - ball_radius) * 2 - y0;
                break;
            case 3: // 左に接触
                ball_x = 2 * ball_radius - x0;
                break;
            case 4: // 右に接触
                ball_x = (court_size_x - ball_radius) * 2.0 - x0;
                break;
            case 5: // プレーヤー１の左に接触
            case 6: // プレーヤー２の右に接触
                ball_x = off * 2.0 - x0;
                break;
        }

        if (c > 0) // for debug
            System.out.printf("Hit %d (%.2f,%.2f) (%.2f,%.2f) %d %d %f\n",
                    c, x0, y0, ball_x, ball_y, court_size_x, off, ball_moving_dir);
    }

    // ボールの移動情報のセット (クライアントの場合利用)
    public void setBall(double x, double y, double dir, double speed) {
        ball_x = x;
        ball_y = y;
        ball_moving_dir = dir;
        calcMovingVector();
        ball_speed = speed;
    }

    public void send() {
        if (server) {
            String msg = String.format("%.2f %.2f %.2f %.2f %d %d", ball_x, ball_y, ball_moving_dir, ball_speed,
                    myself.getX(), myself.getY());
            sv.send(msg);
        } else {
            String msg = String.format("%d %d", myself.getX(), myself.getY());
            cl.send(msg);
        }
    }

    public void recv() {
        if (server) {
            String msg = sv.recv();
            if (msg == null)
                return;
            String[] xy = msg.split(" ");
            opponent.setXY(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
        } else {
            String msg = cl.recv();
            if (msg == null)
                return;
            String[] xy = msg.split(" ");
            opponent.setXY(Integer.parseInt(xy[4]), Integer.parseInt(xy[5]));
            setBall(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]),
                    Double.parseDouble(xy[2]), Double.parseDouble(xy[3]));
        }
    }

    public boolean isServer() {
        return server;
    }

    public void draw(Graphics g) {
        g.fillOval((int) (ball_x - ball_radius), (int) (ball_y - ball_radius),
                (int) (ball_radius * 2 - 1), (int) (ball_radius * 2 - 1));
        myself.draw(g);
        opponent.draw(g);
        g.drawRect(0, 0, court_size_x, court_size_y);
    }
}
