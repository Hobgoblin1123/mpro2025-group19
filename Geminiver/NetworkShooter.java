import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

// ==========================================
// 1. 部品クラス群
// ==========================================

enum PowerType { BIG, POWER, FAST, RAPID }

class ShooterPlayer {
    public int x, y, width = 40, height = 40;
    public int maxHp = 15, hp = maxHp;
    public Color color;
    public int shootDir; 
    
    public boolean isBig, isPower, isFast, isRapid;
    
    private long lastShotTime = 0;
    private int cooldown = 400; 
    
    private long bigEnd = 0, powerEnd = 0, fastEnd = 0, rapidEnd = 0;

    public ShooterPlayer(int x, int y, Color color, int shootDir) {
        this.x = x; this.y = y; this.color = color; this.shootDir = shootDir;
    }

    public void move(int dx, int courtW) {
        x += dx;
        if (x < 0) x = 0;
        if (x > courtW - width) x = courtW - width;
    }

    public void updateStatus() {
        long now = System.currentTimeMillis();
        isBig    = now < bigEnd;
        isPower  = now < powerEnd;
        isFast   = now < fastEnd;
        isRapid  = now < rapidEnd;
    }
    
    public int getStateCode() {
        int code = 0;
        if (isBig)    code += 2;
        if (isPower)  code += 8;
        if (isFast)   code += 16;
        if (isRapid)  code += 32;
        return code;
    }

    public void applyStateCode(int code) {
        isBig    = (code & 2) != 0;
        isPower  = (code & 8) != 0;
        isFast   = (code & 16) != 0;
        isRapid  = (code & 32) != 0;
    }
    
    public void activatePowerUp(PowerType type) {
        long duration = 5000;
        long time = System.currentTimeMillis() + duration;
        switch (type) {
            case BIG:    bigEnd = time; break;
            case POWER:  powerEnd = time; break;
            case FAST:   fastEnd = time; break;
            case RAPID:  rapidEnd = time; break;
        }
    }

    public ArrayList<Bullet> tryShoot() {
        ArrayList<Bullet> newBullets = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        int currentCooldown = isRapid ? cooldown / 2 : cooldown;

        if (currentTime - lastShotTime > currentCooldown) {
            lastShotTime = currentTime;
            int startX = x + width / 2;
            int startY = y + (shootDir == 1 ? height : 0);
            int size = isBig ? 30 : 10;
            int damage = isPower ? 2 : 1;
            int fast = isFast ? 20 : 10;
            
            Color bColor = isPower ? Color.MAGENTA : Color.YELLOW;

            newBullets.add(new Bullet(startX - size/2, startY, 0, shootDir, size, damage, bColor, this, fast));
        }
        return newBullets;
    }

    public void hit(int damage) { hp -= damage; }
    public boolean isDead() { return hp <= 0; }

    public void draw(Graphics g) {
        g.setColor(color);
        int[] xPoints = {x, x + width / 2, x + width};
        int[] yPoints = (shootDir == -1) ? new int[]{y + height, y, y + height} : new int[]{y, y + height, y};
        g.fillPolygon(xPoints, yPoints, 3);
        
        if (isPower) { g.setColor(Color.MAGENTA); g.drawRect(x-4, y-4, width+8, height+8); }
        if (isRapid) { g.setColor(Color.WHITE); g.drawRect(x-6, y-6, width+12, height+12); }
    }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}

class Bullet {
    int x, y, size, damage, dx, dy, fast;
    Color color;
    ShooterPlayer owner;
    
    public Bullet(int x, int y, int dx, int dirY, int size, int damage, Color color, ShooterPlayer owner, int fast) {
        this.x = x; this.y = y; 
        this.dx = dx; this.dy = dirY * fast;
        this.size = size; this.damage = damage; this.color = color; this.owner = owner;
    }
    
    public Bullet(int x, int y, int size, int colorType) {
        this.x = x; this.y = y; this.size = size;
        if (colorType == 1) this.color = Color.MAGENTA; // POWER
        else this.color = Color.YELLOW;
    }

    public void update() {
        y += dy; 
        x += dx; 
    }
    
    public void draw(Graphics g) { g.setColor(color); g.fillOval(x, y, size, size); }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
}


class Star {
    int x, y, speed, size;
    Color color;
    public Star(int width, int height, Random rand) {
        this.x = rand.nextInt(width); this.y = rand.nextInt(height);
        this.speed = rand.nextInt(3) + 1; this.size = rand.nextInt(2) + 1;
        if (speed == 3) color = Color.WHITE; else if (speed == 2) color = Color.LIGHT_GRAY; else color = Color.GRAY;
    }
    public void update(int width, int height, Random rand) {
        y += speed;
        if (y > height) { y = 0; x = rand.nextInt(width); }
    }
    public void draw(Graphics g) { g.setColor(color); g.fillRect(x, y, size, size); }
}

class PowerUpItem {
    int x, y, size = 30;
    PowerType type;
    public PowerUpItem(int x, int y, PowerType type) { this.x = x; this.y = y; this.type = type; }
    public void draw(Graphics g) {
        g.setColor(Color.WHITE); g.drawRect(x, y, size, size);
        switch (type) {
            case BIG:    g.setColor(Color.ORANGE); break;
            case POWER:  g.setColor(new Color(180, 0, 180)); break;
            case FAST:   g.setColor(Color.RED); break;
            case RAPID:  g.setColor(Color.CYAN); break;
            default:     g.setColor(Color.GRAY); break;
        }
        g.fillRect(x + 2, y + 2, size - 4, size - 4);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(type.toString().substring(0, 1), x + 8, y + 22);
    }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
}

// ==========================================
// 2. Model
// ==========================================
class ShooterModel {
    public int width, height;
    public ShooterPlayer player1, player2;
    public ArrayList<Bullet> bullets;
    public ArrayList<PowerUpItem> items;
    public ArrayList<Star> stars;
    private final int STAR_COUNT = 100;

    public String winner = "";
    public boolean isRunning = true;
    
    private boolean isServer;
    private CommServer sv;
    private CommClient cl;
    private Random rand = new Random();

    public ShooterModel(int w, int h, boolean server, String host, int port) {
        this.width = w; this.height = h; this.isServer = server;
        
        player1 = new ShooterPlayer(w / 2 - 20, h - 80, Color.RED, -1);
        player2 = new ShooterPlayer(w / 2 - 20, 30, Color.CYAN, 1);
        bullets = new ArrayList<>();
        items = new ArrayList<>();
        stars = new ArrayList<>();
        for(int i = 0; i < STAR_COUNT; i++) stars.add(new Star(w, h, rand));

        if (isServer) {
            System.out.println("Wait port: " + port);
            sv = new CommServer(port);
            sv.setTimeout(1);
        } else {
            System.out.println("Connect: " + host + ":" + port);
            cl = new CommClient(host, port);
            cl.setTimeout(1);
        }
    }

    public void update() {
        if (!isRunning) return;
        for(Star s : stars) s.update(width, height, rand);

        if (isServer) {
            String msg = sv.recv();
            if (msg != null) {
                try {
                    String[] data = msg.split(" ");
                    player2.x = Integer.parseInt(data[0]);
                    boolean p2Shoot = Boolean.parseBoolean(data[1]);
                    if (p2Shoot) bullets.addAll(player2.tryShoot());
                } catch(Exception e) {}
            }
            serverLogic();
            sendServerData();
        } else {
            String msg = cl.recv();
            if (msg != null) parseServerData(msg);
        }
    }

    private void serverLogic() {
        if (rand.nextInt(100) == 0) {
            PowerType[] types = PowerType.values();
            items.add(new PowerUpItem(rand.nextInt(width - 40) + 20, rand.nextInt(200) + 200, types[rand.nextInt(types.length)]));
        }
        player1.updateStatus();
        player2.updateStatus();

        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update(); 

            if (b.y < -50 || b.y > height + 50 || b.x < -50 || b.x > width + 50) {
                it.remove(); continue;
            }

            boolean hit = false;
            if (b.owner != player1 && player1.getBounds().intersects(b.getBounds())) {
                player1.hit(b.damage); hit = true; // 爆発追加処理を削除
            }
            else if (b.owner != player2 && player2.getBounds().intersects(b.getBounds())) {
                player2.hit(b.damage); hit = true; // 爆発追加処理を削除
            }

            if (!hit) {
                Iterator<PowerUpItem> itemIt = items.iterator();
                while (itemIt.hasNext()) {
                    PowerUpItem item = itemIt.next();
                    if (b.getBounds().intersects(item.getBounds())) {
                        b.owner.activatePowerUp(item.type);
                        itemIt.remove();
                        hit = true; break;
                    }
                }
            }
            if (hit) it.remove();
        }

        if (player1.isDead()) { isRunning = false; winner = "Player 2 (Blue)"; }
        else if (player2.isDead()) { isRunning = false; winner = "Player 1 (Red)"; }
    }

    public void sendClientInput(boolean isShooting) {
        if (!isServer) cl.send(player2.x + " " + isShooting);
    }

    private void sendServerData() {
        StringBuilder sb = new StringBuilder();
        sb.append(player1.x).append(",").append(player1.hp).append(",")
          .append(player2.hp).append(",").append(winner).append(",")
          .append(player1.getStateCode()).append(",").append(player2.getStateCode()).append("#");
        
        for (Bullet b : bullets) {
            int cType = 0;
            if (b.color == Color.MAGENTA) cType = 1;

            sb.append(b.x).append(",").append(b.y).append(",")
              .append(b.size).append(",").append(cType).append(";");
        }
        sb.append("#");

        for (PowerUpItem i : items) {
            sb.append(i.x).append(",").append(i.y).append(",")
              .append(i.type.ordinal()).append(";");
        }
        sb.append("#");
        
        sv.send(sb.toString());
    }

    private void parseServerData(String msg) {
        try {
            String[] sections = msg.split("#", -1);
            if (sections.length < 3) return;

            String[] basic = sections[0].split(",", -1);
            if (basic.length >= 6) { 
                player1.x = Integer.parseInt(basic[0]);
                player1.hp = Integer.parseInt(basic[1]);
                player2.hp = Integer.parseInt(basic[2]);
                winner = basic[3];
                player1.applyStateCode(Integer.parseInt(basic[4]));
                player2.applyStateCode(Integer.parseInt(basic[5]));
                
                if (!winner.isEmpty()) isRunning = false;
            }

            bullets.clear();
            if (!sections[1].isEmpty()) {
                String[] bList = sections[1].split(";");
                for (String bStr : bList) {
                    if (bStr.isEmpty()) continue;
                    String[] val = bStr.split(",");
                    if (val.length >= 4) {
                        bullets.add(new Bullet(
                            Integer.parseInt(val[0]), Integer.parseInt(val[1]),
                            Integer.parseInt(val[2]), Integer.parseInt(val[3])
                        ));
                    }
                }
            }

            items.clear();
            if (!sections[2].isEmpty()) {
                String[] iList = sections[2].split(";");
                for (String iStr : iList) {
                    if (iStr.isEmpty()) continue;
                    String[] val = iStr.split(",");
                    if (val.length >= 3) {
                        items.add(new PowerUpItem(
                            Integer.parseInt(val[0]), Integer.parseInt(val[1]),
                            PowerType.values()[Integer.parseInt(val[2])]
                        ));
                    }
                }
            }
        } catch (Exception e) {}
    }
    
    public void draw(Graphics g) {
        for(Star s : stars) s.draw(g);
        player1.draw(g);
        player2.draw(g);
        for (PowerUpItem item : items) item.draw(g);
        for (Bullet b : bullets) b.draw(g);
    }
    public boolean isServer() { return isServer; }
}

public class NetworkShooter extends JPanel implements ActionListener, KeyListener {
    private ShooterModel model;
    private Timer timer;
    private boolean[] keys = new boolean[256];
    private static final int WIDTH = 500;
    private static final int HEIGHT = 600;

    public NetworkShooter(boolean isServer, String host, int port) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        model = new ShooterModel(WIDTH, HEIGHT, isServer, host, port);
        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (model.isRunning) {
            processInput();
            model.update();
        }
        repaint();
    }

    private void processInput() {
        int speed = 5; 

        if (model.isServer()) {
            if (keys[KeyEvent.VK_LEFT]) model.player1.move(-speed, WIDTH);
            if (keys[KeyEvent.VK_RIGHT]) model.player1.move(speed, WIDTH);
            if (keys[KeyEvent.VK_SPACE]) model.bullets.addAll(model.player1.tryShoot());
        } 
        else {
            if (keys[KeyEvent.VK_LEFT]) model.player2.move(-speed, WIDTH);
            if (keys[KeyEvent.VK_RIGHT]) model.player2.move(speed, WIDTH);
            
            boolean isShooting = keys[KeyEvent.VK_SPACE];
            model.sendClientInput(isShooting);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        model.draw(g);
        drawUI(g);
        if (!model.isRunning) drawGameOver(g);
    }

    private void drawUI(Graphics g) {
        drawPlayerStatus(g, model.player1, 20, HEIGHT - 40);
        drawPlayerStatus(g, model.player2, 20, 20);
    }
    
    private void drawPlayerStatus(Graphics g, ShooterPlayer p, int x, int y) {
        g.setColor(Color.WHITE);
        StringBuilder sb = new StringBuilder("HP ");
        if(p.isRapid) sb.append("[RAPID] ");
        g.drawString(sb.toString(), x, y);

        g.setColor(Color.GRAY); g.fillRect(x + 100, y - 10, 100, 10);
        g.setColor(p.color);
        int hpWidth = (int)((p.hp / (double)p.maxHp) * 100);
        if (hpWidth<0) hpWidth=0;
        g.fillRect(x + 100, y - 10, hpWidth, 10);
    }

    private void drawGameOver(Graphics g) {
        String msg = "WINNER: " + model.winner;
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, HEIGHT / 2);
    }

    public void keyPressed(KeyEvent e) { if (e.getKeyCode() < keys.length) keys[e.getKeyCode()] = true; }
    public void keyReleased(KeyEvent e) { if (e.getKeyCode() < keys.length) keys[e.getKeyCode()] = false; }
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java NetworkShooter [server/client] [port] (host)");
            return;
        }
        boolean isServer = args[0].equalsIgnoreCase("server");
        int port = Integer.parseInt(args[1]);
        String host = (args.length > 2) ? args[2] : "localhost";

        JFrame frame = new JFrame("Network Shooter");
        NetworkShooter game = new NetworkShooter(isServer, host, port);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}