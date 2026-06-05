import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BrickBreakerGame extends JFrame {
    private GamePanel gamePanel;
    private ControlPanel controlPanel;

    public BrickBreakerGame() {
        setTitle("Brick Breaker Game");
        setSize(900,600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Top panel for title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.BLACK);
        titlePanel.setPreferredSize(new Dimension(900, 50));
        JLabel titleLabel = new JLabel("Brick Breaker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Left: Game panel
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(700, 550)); // Adjusted for top panel
        add(gamePanel, BorderLayout.CENTER);

        // Right: Control panel
        controlPanel = new ControlPanel(gamePanel);
        controlPanel.setPreferredSize(new Dimension(200, 550));
        add(controlPanel, BorderLayout.EAST);

        setVisible(true);
    }

    // Game panel for gameplay
    class GamePanel extends JPanel implements ActionListener {
        private boolean play = false;
        private boolean paused = false;
        private int score = 0;
        private int totalBricks = 21;
        private int level = 1;
        private Timer timer;
        private int delay = 8;

        private int playerX = 310;
        private int playerY = 500; // Moved up from 550
        private int ballX = 350; // Center of game area (700/2)
        private int ballY = 350;
        private double ballDirX = -1.5;
        private double ballDirY = -2.0;
        private int paddleWidth = 100;
        private int paddleSpeed = 0; // Paddle movement speed (-5 left, +5 right, 0 stop)

        private int[][] bricks;
        private int rows = 3;
        private int cols = 7;
        private boolean showStartScreen = true;
        private Timer paddleTimer;

        public GamePanel() {
            bricks = new int[rows][cols];
            initBricks();
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (showStartScreen && e.getKeyCode() == KeyEvent.VK_SPACE) {
                        showStartScreen = false;
                        play = true;
                        repaint();
                    }
                }
            });
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            timer = new Timer(delay, this);
            timer.start();
            paddleTimer = new Timer(20, e -> {
                if (paddleSpeed != 0) {
                    playerX = Math.max(10, Math.min(690 - paddleWidth, playerX + paddleSpeed));
                    repaint();
                }
            });
            paddleTimer.start();
        }

        private void initBricks() {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    bricks[i][j] = 1;
                }
            }
            totalBricks = rows * cols;
        }

        public void resetGame() {
            if (totalBricks == 0) {
                level++;
                rows = Math.min(5, rows + 1);
                cols = Math.min(9, cols + 1);
                bricks = new int[rows][cols];
                initBricks();
            }
            play = true;
            paused = false;
            showStartScreen = false;
            ballX = 350;
            ballY = 350;
            ballDirX = -1.5;
            ballDirY = -2.0;
            score = 0;
            paddleWidth = 100;
            paddleSpeed = 0;
            playerX = 310;
            playerY = 500; // Reset to visible position
            repaint();
        }

        public void togglePause() {
            if (play && !showStartScreen) {
                paused = !paused;
                repaint();
            }
        }

        public void setPaddleSpeed(int speed) {
            paddleSpeed = speed;
        }

        public void setScore(int score) {
            this.score = score;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // White background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 700, 550); // Adjusted to panel size

            // Black borders
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 3, 550); // Left
            g2d.fillRect(0, 0, 700, 3); // Top
            g2d.fillRect(697, 0, 3, 550); // Right

            if (showStartScreen) {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 30));
                g2d.drawString("Brick Breaker", 250, 250);
                g2d.setFont(new Font("Arial", Font.PLAIN, 20));
                g2d.drawString("Press Space to Start", 260, 300);
                return;
            }

            // Draw bricks with gradient
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (bricks[i][j] > 0) {
                        int brickX = 80 + j * 80;
                        int brickY = 50 + i * 40;
                        GradientPaint gradient = new GradientPaint(
                                brickX, brickY, Color.WHITE,
                                brickX, brickY + 30, new Color(200, 200, 200)
                        );
                        g2d.setPaint(gradient);
                        g2d.fillRect(brickX, brickY, 60, 30);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(brickX, brickY, 60, 30);
                    }
                }
            }

            // Pause message
            if (paused) {
                g2d.setColor(Color.BLUE);
                g2d.setFont(new Font("Arial", Font.BOLD, 30));
                g2d.drawString("Paused", 300, 250);
            } else {
                // Paddle with shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRect(playerX + 5, playerY + 5, paddleWidth, 8);
                g2d.setColor(Color.BLACK);
                g2d.fillRect(playerX, playerY, paddleWidth, 8);

                // Ball with shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillOval(ballX + 3, ballY + 3, 20, 20);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(ballX, ballY, 20, 20);
            }

            // Game Over
            if (ballY > 530) { // Adjusted for new paddle Y
                play = false;
                ballDirX = 0;
                ballDirY = 0;
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 30));
                g2d.drawString("Game Over, Score: " + score, 250, 250);
                g2d.drawString("Click Restart to Play Again", 230, 300);
            }

            // Win condition
            if (totalBricks == 0) {
                play = false;
                ballDirX = 0;
                ballDirY = 0;
                g2d.setColor(Color.GREEN);
                g2d.setFont(new Font("Arial", Font.BOLD, 30));
                g2d.drawString("Level " + level + " Cleared!", 250, 250);
                g2d.drawString("Click Restart for Next Level", 230, 300);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!play || paused) return;

            double speedMultiplier = Math.min(2.0, 1.0 + (score / 200.0));
            double currentBallDirX = ballDirX * speedMultiplier;
            double currentBallDirY = ballDirY * speedMultiplier;

            // Ball-paddle collision
            if (new Rectangle(ballX, ballY, 20, 20).intersects(new Rectangle(playerX, playerY, paddleWidth, 8))) {
                double hitPos = (ballX + 10 - playerX) / (double) paddleWidth; // 0 to 1
                ballDirX = -1.5 + 3.0 * hitPos; // -1.5 to 1.5
                ballDirY = -Math.abs(ballDirY);
            }

            // Ball-brick collision
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (bricks[i][j] > 0) {
                        int brickX = 80 + j * 80;
                        int brickY = 50 + i * 40;
                        Rectangle brickRect = new Rectangle(brickX, brickY, 60, 30);
                        Rectangle ballRect = new Rectangle(ballX, ballY, 20, 20);

                        if (ballRect.intersects(brickRect)) {
                            bricks[i][j] = 0;
                            totalBricks--;
                            score += 5;
                            controlPanel.updateScore(score);
                            ballDirY = -ballDirY;
                            if (Math.random() < 0.1 && paddleWidth < 150) {
                                paddleWidth += 20;
                            }
                        }
                    }
                }
            }

            // Ball movement
            ballX += currentBallDirX;
            ballY += currentBallDirY;

            // Border collision
            if (ballX < 0 || ballX > 670) ballDirX = -ballDirX;
            if (ballY < 0) ballDirY = -ballDirY;

            repaint();
        }
    }

    // Control panel for buttons and score
    class ControlPanel extends JPanel {
        private JLabel scoreLabel;
        private JLabel levelLabel;
        private JButton pauseButton;
        private JButton restartButton;
        private JButton leftButton;
        private JButton rightButton;
        private GamePanel gamePanel;

        public ControlPanel(GamePanel gamePanel) {
            this.gamePanel = gamePanel;
            setBackground(Color.WHITE);
            setLayout(null);

            // Score label
            scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
            scoreLabel.setBounds(20, 20, 150, 30);
            add(scoreLabel);

            // Level label
            levelLabel = new JLabel("Level: 1");
            levelLabel.setFont(new Font("Arial", Font.BOLD, 20));
            levelLabel.setBounds(20, 50, 150, 30);
            add(levelLabel);

            // Pause button
            pauseButton = new JButton("Pause");
            pauseButton.setBounds(20, 100, 100, 30);
            pauseButton.setBackground(Color.BLACK);
            pauseButton.setForeground(Color.WHITE);
            pauseButton.setFont(new Font("Arial", Font.PLAIN, 12));
            pauseButton.addActionListener(e -> {
                gamePanel.togglePause();
                pauseButton.setText(gamePanel.paused ? "Resume" : "Pause");
            });
            add(pauseButton);

            // Restart button
            restartButton = new JButton("Restart");
            restartButton.setBounds(20, 140, 100, 30);
            restartButton.setBackground(Color.BLACK);
            restartButton.setForeground(Color.WHITE);
            restartButton.setFont(new Font("Arial", Font.PLAIN, 12));
            restartButton.addActionListener(e -> gamePanel.resetGame());
            add(restartButton);

            // Left paddle button
            leftButton = new JButton("<--");
            leftButton.setBounds(20, 180, 100, 30);
            leftButton.setBackground(Color.BLACK);
            leftButton.setForeground(Color.WHITE);
            leftButton.setFont(new Font("Arial", Font.PLAIN, 12));
            leftButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    gamePanel.setPaddleSpeed(-5);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    gamePanel.setPaddleSpeed(0);
                }
            });
            add(leftButton);

            // Right paddle button
            rightButton = new JButton("-->");
            rightButton.setBounds(20, 220, 100, 30);
            rightButton.setBackground(Color.BLACK);
            rightButton.setForeground(Color.WHITE);
            rightButton.setFont(new Font("Arial", Font.PLAIN, 12));
            rightButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    gamePanel.setPaddleSpeed(5);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    gamePanel.setPaddleSpeed(0);
                }
            });
            add(rightButton);
        }

        public void updateScore(int score) {
            scoreLabel.setText("Score: " + score);
            levelLabel.setText("Level: " + gamePanel.level);
        }
    }

    public static void main(String[] args) {
        new BrickBreakerGame();
    }
}