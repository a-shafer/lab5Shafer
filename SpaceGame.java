
/** Project: Solo Lab 7 Assignment
* Purpose Details: a space shooter Game
* Course:IST 242
* Author:Andrew Shafer & Prof. Joe Oakes
* Date Developed: 6/20/2024
* Last Date Changed:6/21/2024
* Rev:n/a

*/

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SpaceGame extends JFrame implements KeyListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 40;
    private static final int PLAYER_HEIGHT = 40;
    private static final int OBSTACLE_WIDTH = 40;
    private static final int OBSTACLE_HEIGHT = 40;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 15;
    private static final int OBSTACLE_SPEED = 3;
    private static final int POWERUP_SPEED = 6;
    private static final int PROJECTILE_SPEED = 10;
    private int score = 0;
    private JPanel gamePanel;
    private JLabel scoreLabel;
    private Timer timer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private java.util.List<Point> obstacles;
    private java.util.List<Point> powerUp;
    private java.util.List<Point> stars;
    private BufferedImage shipImage;
    private BufferedImage blaster;
    private BufferedImage powerUPPng;
    private BufferedImage alienShip;
    private int blasterWidth = 32;
    private int blasterHight = 32;
    private int powerUPPngWidth = 32;
    private int powerUPPngHight = 32;
    private int alienShipWidth = 32;
    private int alienShipHight = 32;
    private Clip blasterSound;
    private Clip shipBoom;
    private Clip gameOverSound;
    private boolean sheildActive = false;
    private int sheildDuration = 5000;
    private long sheildStartTime;
    private int playerHealth;
    private JLabel healthLable;
    private JLabel timeLable;
    private int time = 60000;
    private long gameStartTime;
    private int challengeLVL;

    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        stars = generateStars(420);//generates 200 stars
        playerHealth = 50;//sets initial health
        gameStartTime = System.currentTimeMillis();//recorsd game start time for the timer
        challengeLVL = 1;//sets inital challenge level to 1
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };
        // Loaads the ships and the blaster animation image as well as all sounds and handles the sounds/errors
        try {
            shipImage = ImageIO.read(new File("Ship.png"));
            blaster = ImageIO.read(new File("blaster.png"));
            alienShip = ImageIO.read(new File("alien.png"));
            powerUPPng = ImageIO.read(new File("HelthPowerUp.png"));
            AudioInputStream audioInputStreamBlaster = AudioSystem.getAudioInputStream((new File("blaster.wav").getAbsoluteFile()));
            AudioInputStream audioInputStreamBoom = AudioSystem.getAudioInputStream((new File("deltarune-explosion.wav").getAbsoluteFile()));
            AudioInputStream audioInputStreamGameOver = AudioSystem.getAudioInputStream((new File("emotional-damage-meme.wav").getAbsoluteFile()));
            blasterSound = AudioSystem.getClip();
            shipBoom = AudioSystem.getClip();
            gameOverSound = AudioSystem.getClip();
            blasterSound.open(audioInputStreamBlaster);
            shipBoom.open(audioInputStreamBoom);
            gameOverSound.open(audioInputStreamGameOver);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        catch (UnsupportedAudioFileException ex){
            ex.printStackTrace();
        }
        catch (LineUnavailableException ex){
            ex.printStackTrace();
        }



        // all the labels at top of screen
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLUE);
        scoreLabel.setBounds(100, 10, 100, 20);
        gamePanel.add(scoreLabel);
        healthLable = new JLabel("Health: 50");
        healthLable.setForeground(Color.RED);
        healthLable.setBounds(20, 10, 100, 20);
        gamePanel.add(healthLable);
        timeLable = new JLabel("Time: 60");
        timeLable.setForeground(Color.YELLOW);
        timeLable.setBounds(30, 10, 100, 20);
        gamePanel.add(timeLable);
        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new java.util.ArrayList<>();
        powerUp = new java.util.ArrayList<>();

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();
    }

    //Plays the Game Over Sound
    public void playGameOver(){
        if(gameOverSound != null){
            gameOverSound.setFramePosition(0);//sets to begining
            gameOverSound.start();//starts the sound
        }
    }
    //Plays the blaster Sound
        public void playBlaster(){
        if(blasterSound != null){
            blasterSound.setFramePosition(0);//sets to begining
            blasterSound.start();//starts the sound
        }
    }
    //Plays the ship death Sound
        public void playshipBoom(){
        if(shipBoom != null){
            shipBoom.setFramePosition(0);//sets to begining
            shipBoom.start();//starts the sound
        }
    }
    // activates sheild
    private void activateSheild(){
        sheildActive = true;
        sheildStartTime = System.currentTimeMillis();
    }
    //Deactivates Sheild
    private void deactivateShield(){
        sheildActive = false;
    }
    //Returns status of sheild
    private boolean isSheildActive( ){
        return sheildActive && (System.currentTimeMillis()-sheildStartTime) < sheildDuration;
    }

    private void draw(Graphics g) {
        //BackGround
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        //Player
        //it's not small..... it's average size
        g.drawImage(shipImage,playerX,playerY,null);
        //sheilds draw action
        if (isSheildActive()){

            g.setColor(new Color(0,255,255,100));
            g.fillOval(playerX,playerY,60,60);
        }
        //Projectile w/animation
        if (isProjectileVisible) {
            if (blaster != null){
                Random random = new Random();
                int blasterIndex = random.nextInt(4);

                int blasterx = blasterIndex * blasterWidth;
                int blastery = 0;
                g.drawImage(blaster.getSubimage(blasterx, blastery,blasterWidth,blasterHight),projectileX,projectileY,null);
            }

        }
        //Obsicles w/ animation
        for (Point obstacle : obstacles) {
            if (alienShip != null){
                Random random = new Random();
                int alienShipIndex = random.nextInt(4);

                int alienx = alienShipIndex * alienShipWidth;
                int alieny = 0;
                g.drawImage(alienShip.getSubimage(alienx,alieny,alienShipWidth,alienShipHight),obstacle.x,obstacle.y,null);
            }

        }
        //Power UP w/ animation
        for (Point powerup : powerUp) {
            if (powerUPPng != null){
                Random random = new Random();
                int powerUPIndex = random.nextInt(4);

                int powerUpx = powerUPIndex * powerUPPngWidth;
                int powerupy = 0;
                g.drawImage(powerUPPng.getSubimage(powerUpx, powerupy,powerUPPngWidth,powerUPPngHight), powerup.x, powerup.y,null);
            }

        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
        //draws and colors stars randomly
        g.setColor(generateRandomColor());
        for (Point star : stars){
            g.fillOval(star.x, star.y, 2,2);
        }
    }
    //generates star locations randomly
    private java.util.List<Point> generateStars(int Numstars){
        java.util.List<Point> starsList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < Numstars; i++){
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            starsList.add(new Point(x,y));
        }
        return starsList;
    }
    //random color generator
    public static Color generateRandomColor(){
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return new Color(r,g,b);
    }
    private void update() {
        if (!isGameOver) {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += OBSTACLE_SPEED;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }
            //Adds challenge leveling to the player based on score, to scale with skill over time
            if (score > 49 && score < 100){
                challengeLVL = 2;

            }
            if (score > 199 && score < 250){
                challengeLVL = 3;

            }
            if (score > 249 && score < 300){
                challengeLVL = 4;

            }
            // Generate new obstacles
            if (Math.random() < (0.02 * challengeLVL)) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
            }

            // Moves power Ups
            for (int i = 0; i < powerUp.size(); i++) {
                powerUp.get(i).y += POWERUP_SPEED;
                if (powerUp.get(i).y > HEIGHT) {
                    powerUp.remove(i);
                    i--;
                }
            }

            // Generates new power UPS
            if (Math.random() < 0.01) {
                int pwerUPx = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                powerUp.add(new Point(pwerUPx, 0));
            }


            // Moves projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (Point obstacle : obstacles) {
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);

                //removes health if player collides with enemy ships
                if (playerRect.intersects(obstacleRect) && !isSheildActive()) {
                    playerHealth -= 1;
                }
                if (playerRect.intersects(obstacleRect) && !isSheildActive() && playerHealth < 1) {
                    isGameOver = true;
                    playGameOver();
                    break;

                }
                //CountDownTimer
                if (System.currentTimeMillis()-gameStartTime > time){
                    isGameOver = true;
                    playGameOver();
                    break;
                }
            }
            //PowerUp Intersect with player
            for (Point powerUP : powerUp) {
                Rectangle powerUpRect = new Rectangle(powerUP.x, powerUP.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(powerUpRect)) {
                    playerHealth += 1;
                }
                // Check collision with obstacle
                Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
                for (int i = 0; i < obstacles.size(); i++) {
                    Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                    if (projectileRect.intersects(obstacleRect)) {
                        obstacles.remove(i);
                        score += 10;
                        playshipBoom();
                        isProjectileVisible = false;
                        break;
                    }
                }
                //updates lables
                scoreLabel.setText("Score: " + score);
                healthLable.setText("Health: " + playerHealth);
                timeLable.setText("Time:" + (60+((gameStartTime/1000) - (System.currentTimeMillis()/1000))));
            }
        }
    }
    @Override
    // event handler for keystrokes. allows movement and shooting at players will.
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_CONTROL) {
            activateSheild();
        }else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            playBlaster();
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // Limit firing rate
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}