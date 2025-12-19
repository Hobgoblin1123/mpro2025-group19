import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Observable;

class MoveManager extends Observable {
    private boolean server; // ボールの動きの計算はサーバ側で行う．
    private CommServer sv = null;
    private CommClient cl = null;
    public int court_size_x, court_size_y; // コートの大きさ
    public Player myself, opponent; // Player 自分と対戦相手

    public MoveManager(int x, int y, int offset, boolean server, String host, int port) {
        this.server = server;
        court_size_x = x;
        court_size_y = y;
        if (server) { // server の場合は，自分は左．clientの場合は自分は，右
            myself = new Player(10, 10, offset, y / 2, 1, 20, 200, y, 5);
            opponent = new Player(10, 10, x - offset - myself.getRadius(), y / 2, 1, 320, 580, y, 5);
            System.out.println("Waiting for connection with port no: " + port);
            sv = new CommServer(port);
            sv.setTimeout(1); // non-wait で通信
            System.out.println("Connected !");
        } else {
            opponent = new Player(10, 10, offset, y / 2, 1, 20, 200, y, 5);
            myself = new Player(10, 10, x - offset - opponent.getRadius(), y / 2, 1, 320, 580, y, 5);
            cl = new CommClient(host, port);
            cl.setTimeout(1); // non-wait で通信
            System.out.println("Connected to " + host + ":" + port + "!");
        }
    }

    public void send() {
        if (server) {
            String msg = String.format("%d %d", myself.getX(), myself.getY());
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
            opponent.setXY(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
        }
    }

    public void GameEnd() {
        setChanged();
        notifyObservers();
    }

    public boolean isServer() {
        return server;
    }

    public void draw(Graphics g) {
        myself.draw(g);
        opponent.draw(g);
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

    public static void main(String[] args) {
        String str;
        boolean server = false;
        if (args.length < 2) {
            System.out.println("Usage : java TennisView {server/single/{host name}} {port no.} \n");
            System.exit(1);
        }
        MoveManager tm;
        if (args[0].equals("server")) {
            server = true;
            System.out.println("Server mode");
            str = "server";
            tm = new MoveManager(600, 300, 30, server, args[0], Integer.parseInt(args[1]));
        } else {
            server = false;
            System.out.println("Client mode");
            str = "client";
            tm = new MoveManager(600, 300, 30, server, args[0], Integer.parseInt(args[1]));
        }
        JFrame frame = new JFrame("TennisView (" + str + " mode)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ShootingView tv = new ShootingView(tm);
        frame.add(tv, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        tv.requestFocusInWindow();
    }
}