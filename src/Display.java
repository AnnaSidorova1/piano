import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.File;
import javax.sound.sampled.*;
import javax.swing.*;


public class Display extends Canvas implements Runnable {
    private static final long serialVersionUID = 1L;
    private boolean running;
    public static int WIDTH = 300;
    public static int HEIGHT = 600;
    public static String NAME = "Piano";
    //private boolean leftPressed_Mouse = false;
    int cnt_repaint = 0;
    int repaint_px = 4;
    int x_cell = -10;
    int y_cell = -10;
    public int time_start = 0;

    Rectangle[][] rec = new Rectangle[1000][4];
    boolean [][] cellState = new boolean[1000][4];
    boolean [][] cellTouch = new boolean[1000][4];
    Clip clip;

    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void run() {
        init();
        BufferStrategy bs = getBufferStrategy();
        createBufferStrategy(2);
        requestFocus();
        bs = getBufferStrategy();

        Graphics g = bs.getDrawGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        create_cells(g, bs);

        try{
            File soundFile = new File("data/song6.wav");
            AudioInputStream inAudio = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(inAudio);
        }
        catch (Exception e1) {
            System.out.println("Song was wrong");
            System.exit(1);
        }


        while(running) {
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            move(g, bs);
            cnt_repaint++;
        }
        g.dispose();
        bs.show();
    }


    public void init() {
        addMouseListener(new MousePress());
    }

    public void create_cells(Graphics g, BufferStrategy bs) {
        for (int i = 0; i < 1000; i++) {
            int t = 2*i%7;
            int m_x = (t*i+1)%4;

            //добавляем сначала сами черные клавиши в массив
            //cellState - массив состояния черная клавиша или белая (True - черная)
            rec[i][m_x] = new Rectangle(75 * m_x,400-100*(i+1),75, 100);
            cellState[i][m_x] = true;

        }
        bs.show();
    }

    public void move(Graphics g, BufferStrategy bs) {
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 4; j++) {
                g.setColor(Color.GRAY);
                g.drawRect(75*j, 400-(i+1)*100+time_start, 75, 100);
                if(rec[i][j] != null) {
                    g.setColor(Color.BLACK);
                    g.fillRect(rec[i][j].x, rec[i][j].y+time_start, 75, 100);
                }
                if(rec[i][j] != null && cellTouch[i][j]) {
                    if (i != 0 && !cellTouch[i-1][0] && !cellTouch[i-1][1] && !cellTouch[i-1][2] && !cellTouch[i-1][3]) {
                        try {
                            File soundFile = new File("data/end.wav");
                            AudioInputStream inAudio = AudioSystem.getAudioInputStream(soundFile);
                            Clip clip_not_win = AudioSystem.getClip();
                            clip_not_win.open(inAudio);
                            clip_not_win.start();
                            g.setColor(Color.white);
                            g.fillRect(0, 0, getWidth(), getHeight());
                            g.setColor(Color.red);
                            g.setFont(new Font("Arial", Font.BOLD, 48));
                            g.drawString("Game over", 20, 300);
                            bs.show();
                            Thread.sleep(5000);
                            System.exit(1);
                        } catch (Exception e1) {
                            System.out.println("Exit wrong");
                            System.exit(1);
                        }
                    }
                    //leftPressed_Mouse = false;
                    cellTouch[i][j] = true;
                    g.setColor(Color.green);
                    g.fillRect(rec[i][j].x, rec[i][j].y+time_start, 75, 100);
                }
            }
        }
        time_start+=repaint_px;
        bs.show();
    }

    public void playMusic (Clip clip) {
        try{
        clip.start();
        }
        catch (Exception e1) {
            System.out.println("Song was wrong on start");
            System.exit(1);
        }
    }
    public void stopMusic (Clip clip) {
        try{
            Thread.sleep(400);
            clip.stop();
        }
        catch (Exception e1) {
            System.out.println("Song was wrong on stop");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Display game = new Display();
        game.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        JFrame frame = new JFrame(Display.NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(700,100);
        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        game.start();
    }

    private class MousePress implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            playMusic(clip);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            x_cell = e.getX() / 75;
            y_cell = e.getY() / 100;
            int y_position = Math.abs(y_cell - (repaint_px*cnt_repaint)/100 - 3);
            if(e.getY()% 100 <= (repaint_px*cnt_repaint)%100)
                y_position = Math.abs(y_cell - (repaint_px*cnt_repaint)/100 - 4);
            cellTouch[y_position][x_cell] = true;
            stopMusic(clip);

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

    }
}