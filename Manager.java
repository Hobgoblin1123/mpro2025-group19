import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Observable;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("deprecation")

class MoveManager extends Observable {
    private boolean server; // ボールの動きの計算はサーバ側で行う．
    private CommServer sv = null;
    private CommClient cl = null;
    public int court_size_x, court_size_y; // コートの大きさ
    public Player player1, player2; // Player 自分と対戦相手
    public ArrayList<Bullet> bullets;

    // public Shot myShot[];

    public MoveManager(int x, int y, int offset, boolean server, String host, int port) {
        this.server = server;
        court_size_x = x;
        court_size_y = y;

        player1 = new Player(10, 10, offset, y / 2, 1, 20, 200, y, 5);
        player2 = new Player(10, 10, x - offset - player1.getRadius(), y / 2, 1, 320, 580, y, 5);

        if (server) { // server の場合は，自分は左．clientの場合は自分は，右

            System.out.println("Waiting for connection with port no: " + port);
            sv = new CommServer(port);
            sv.setTimeout(1); // non-wait で通信
            System.out.println("Connected !");
        } else {
            cl = new CommClient(host, port);
            cl.setTimeout(1); // non-wait で通信
            System.out.println("Connected to " + host + ":" + port + "!");
        }
    }

    public void update() {
        if (server) {
            String msg = sv.recv();
            if (msg != null) {
                String data[] = msg.split(" ");
                player2.setXY(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                boolean IsP2Shot = Boolean.parseBoolean(data[2]);
                if (IsP2Shot) {
                    bullets.addAll(player2.tryShoot());
                }

            }
            serverLogic();// サーバー側で現状の計算
            sendServerData();// クライアントへの送信
        } else {
            String msg = cl.recv();
            if (msg != null) {
                parseServerData(msg);// サーバーから送られた情報の解読
            }
        }
    }

    public void serverLogic() {
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            if ((b.getX() < 0 || b.getX() > court_size_x) || (b.getY() < 0 || b.getY() > court_size_y)) {
                it.remove();
                continue;
            }

            boolean hit = false;
            if (b.getOwner() == player1 && (player1.getX() - b.getX()) * (player1.getX() - b.getX())
                    + (player1.getY() - b.getY()) * (player1.getY() - b.getY()) <= (b.getRadius() + player1.getRadius())
                            * (b.getRadius() + player1.getRadius())) {
                player1.hit(b.getDamage());
                hit = true;
            } else if (b.getOwner() == player2 && (player2.getX() - b.getX()) * (player2.getX() - b.getX())
                    + (player2.getY() - b.getY()) * (player2.getY() - b.getY()) <= (b.getRadius() + player2.getRadius())
                            * (b.getRadius() + player2.getRadius())) {
                player2.hit(b.getDamage());
                hit = true;
            }

            if (hit)
                it.remove();
            // 球がプレイヤーに当たったら削除
        }
        if (player1.IsDead()) {
            System.out.println("Winner is player1");
            GameEnd();
        }
        if (player2.IsDead()) {
            System.out.println("Winner is player2");
            GameEnd();
        }

    }

    public void sendServerData() {
        StringBuilder sb = new StringBuilder();
        sb.append(player1.getX()).append(",").append(player1.getY()).append(",").append(player1.getHp())
                .append(player2.getX()).append(",").append(player2.getY()).append(",").append(player2.getHp())
                .append("#");

        for (Bullet b : bullets) {
            sb.append(b.getX()).append(",").append(b.getY()).append(",")
                    .append(b.getRadius()).append(",").append(b.getColor().getRGB()).append(";");
            // 各弾丸は";"で区切られ、またその玉の情報は","で区切られる
        }
        sb.append("#");

        sv.send(sb.toString());
    }

    public void parseServerData(String msg) {
        String section[] = msg.split("#");
        String chara_info[] = section[0].split(",");
        player1.setXY(Integer.parseInt(chara_info[0]), Integer.parseInt(chara_info[1]));
        player1.setHP(Integer.parseInt(chara_info[2]));
        player2.setXY(Integer.parseInt(chara_info[3]), Integer.parseInt(chara_info[4]));
        player2.setHP(Integer.parseInt(chara_info[5]));

        bullets.clear();
        String bullet_kind[] = section[1].split(";");
        for (String blt : bullet_kind) {
            String bullt_info[] = blt.split(",");
            int rgb = Integer.parseInt(bullt_info[3]);
            Color color = new Color(rgb);
            bullets.add(new Bullet(Integer.parseInt(bullt_info[0]), Integer.parseInt(bullt_info[1]),
                    Integer.parseInt(bullt_info[2]), color));
        }

    }

    public void sendClientInput(boolean isShooting) {
        if (!isServer())
            cl.send(player2.getX() + " " + player2.getY() + " " + isShooting);// player2の位置と射撃情報を空白文字で区切って送信
    }

    public void GameEnd() {
        setChanged();
        notifyObservers();
    }

    public boolean isServer() {
        return server;
    }

    public void draw(Graphics g) {
        player1.draw(g);
        player2.draw(g);
        g.drawRect(0, 0, court_size_x, court_size_y);
    }
}

class ShootingView extends JPanel implements KeyListener, ActionListener {
    private Timer timer;
    private MoveManager tm;
    final static int court_size_x = 600;
    final static int court_size_y = 300;

    public ShootingView(MoveManager tm) {
        this.tm = tm;
        setBackground(Color.white);
        setPreferredSize(new Dimension(tm.court_size_x + 1, tm.court_size_y + 1));
        setFocusable(true);

        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);

        timer = new Timer(10, this); // 10ミリ秒ごとにボールが移動
        if (!tm.isServer())
            timer.start();

        requestFocusInWindow();

    }

    public void keyPressed(KeyEvent e) {

        if (!timer.isRunning())
            timer.start(); // キーを押すとゲーム開始

        int k = e.getKeyCode();
        if (k == KeyEvent.VK_DOWN) {
            tm.myself.moveDown();
            if (!tm.isServer())
                tm.send();
            repaint();
        } else if (k == KeyEvent.VK_UP) {
            tm.myself.moveUp();
            if (!tm.isServer())
                tm.send();
            repaint();
        } else if (k == KeyEvent.VK_RIGHT) {
            tm.myself.moveRight();
            if (!tm.isServer())
                tm.send();
            repaint();
        } else if (k == KeyEvent.VK_LEFT) {
            tm.myself.moveLeft();
            if (!tm.isServer())
                tm.send();
            repaint();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        tm.draw(g);
    }

    public void actionPerformed(ActionEvent e) {
        if (tm.isServer()) {
            tm.send();
            tm.recv();
        } else {
            tm.recv();
        }
        repaint();
    }
}

// public static void main(String[] args) {
// // String str;
// // boolean server = false;
// // if (args.length < 2) {
// // System.out.println("Usage : java TennisView {server/single/{host name}}
// {port
// // no.} \n");
// // System.exit(1);
// // }
// // MoveManager tm;
// // if (args[0].equals("server")) {
// // server = true;
// // System.out.println("Server mode");
// // str = "server";
// // tm = new MoveManager(600, 300, 30, server, args[0],
// // Integer.parseInt(args[1]));
// // } else {
// // server = false;
// // System.out.println("Client mode");
// // str = "client";
// // tm = new MoveManager(600, 300, 30, server, args[0],
// // Integer.parseInt(args[1]));
// // }

// JFrame frame = new JFrame("TennisView (" + str + " mode)");
// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// ShootingView tv = new ShootingView(tm);
// frame.add(tv, BorderLayout.CENTER);
// frame.pack();
// frame.setVisible(true);

// tv.requestFocusInWindow();
// }
// }