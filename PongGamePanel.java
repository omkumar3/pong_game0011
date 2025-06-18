import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PongGamePanel extends JPanel {
    private static final int WINNING_SCORE = 5;
    private boolean gameEnded = false;
    private String winnerName = null;
    private MainController controller;
    private String playerName1, playerName2;
    private int p1Score = 0, p2Score = 0;
    private int p1Y = 150, p2Y = 150;
    private int ballX = 300, ballY = 200;
    private int ballDX = 3, ballDY = 3;
    private boolean isFirstPlayer;
    private WebSocketClient socketClient; 

    
    public PongGamePanel(MainController controller, String playerName1, String playerName2) {
        this.controller = controller;
        this.playerName1 = playerName1;
        this.playerName2 = playerName2;
        
        String currentPlayer = controller.getPlayerName();
        this.isFirstPlayer = currentPlayer.equals(playerName1);
        
        
        System.out.println("Game started as: " + currentPlayer);
        System.out.println("Player 1: " + playerName1);
        System.out.println("Player 2: " + playerName2);
        System.out.println("Is first player: " + isFirstPlayer);
        
        
        setFocusable(true);
        requestFocusInWindow();
        
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("Game panel gained focus");
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("Game panel lost focus");
            }
        });

        // Add mouse listener to regain focus on click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        // Use key bindings instead of key listener
        bindKeys();

        Timer gameTimer = new Timer(16, e -> gameLoop());
        gameTimer.start();
    }


    private void bindKeys() {
        // Remove any existing key listeners
        for (KeyListener kl : getKeyListeners()) {
            removeKeyListener(kl);
        }

        // Bind keys using InputMap and ActionMap
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Player 1 controls
        inputMap.put(KeyStroke.getKeyStroke("W"), "p1up");
        inputMap.put(KeyStroke.getKeyStroke("S"), "p1down");
        actionMap.put("p1up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFirstPlayer) movePlayer("UP");
            }
        });
        actionMap.put("p1down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFirstPlayer) movePlayer("DOWN");
            }
        });

        // Player 2 controls
        inputMap.put(KeyStroke.getKeyStroke("UP"), "p2up");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "p2down");
        actionMap.put("p2up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isFirstPlayer) movePlayer("UP");
            }
        });
        actionMap.put("p2down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isFirstPlayer) movePlayer("DOWN");
            }
        });
    }

   
    
    private void movePlayer(String direction) {
        System.out.println("Moving player: " + (isFirstPlayer ? "1" : "2") + " " + direction);
        
        if (isFirstPlayer) {
            if (direction.equals("UP")) {
                p1Y = Math.max(0, p1Y - 20);
            } else {
                p1Y = Math.min(getHeight() - 100, p1Y + 20);
            }
        } else {
            if (direction.equals("UP")) {
                p2Y = Math.max(0, p2Y - 20);
            } else {
                p2Y = Math.min(getHeight() - 100, p2Y + 20);
            }
        }
        
        // Use the controller to send moves instead of direct socket access
        controller.sendPlayerMove(direction);
        repaint();
    }

    
    public void handleOpponentMove(String playerName, String direction) {
        SwingUtilities.invokeLater(() -> {
            int movement = direction.equals("UP") ? -20 : 20;
            if (playerName.equals(playerName1)) {
                p1Y = Math.max(0, Math.min(p1Y + movement, getHeight() - 100));
            } else {
                p2Y = Math.max(0, Math.min(p2Y + movement, getHeight() - 100));
            }
            repaint();
        });
    }

    private void gameLoop() {
        if (gameEnded) return;

        
        ballX += ballDX;
        ballY += ballDY;

        
        if (ballX < 0) {
            p2Score++;
            checkWinner();
            resetBall();
        }
        if (ballX > getWidth()) {
            p1Score++;
            checkWinner();
            resetBall();
        }

        repaint();
    }

    private void checkWinner() {
        if (p1Score >= WINNING_SCORE || p2Score >= WINNING_SCORE) {
            gameEnded = true;
            winnerName = (p1Score > p2Score) ? playerName1 : playerName2;
            System.out.println("Game ended! Winner: " + winnerName);
            controller.broadcastGameEnd(winnerName, p1Score, p2Score);
            showGameOverDialog();
        }
    }

    private void showGameOverDialog() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::showGameOverDialog);
            return;
        }

        String message = String.format("Game Over!\n%s wins!\nFinal Score: %d - %d", 
            winnerName, p1Score, p2Score);
            
        Object[] options = {"Return to Lobby", "Exit Game"};
        int choice = JOptionPane.showOptionDialog(this,
            message,
            "Game Over",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == JOptionPane.YES_OPTION) {
            controller.returnToLobby();
        } else {
            System.exit(0);
        }
    }

    public void handleGameEnd(String winner, int score1, int score2) {
        if (!gameEnded) {
            System.out.println("Handling game end. Winner: " + winner);
            gameEnded = true;
            winnerName = winner;
            p1Score = score1;
            p2Score = score2;
            showGameOverDialog();
        }
    }

    
    private void resetBall() {
        
        ballX = getWidth() / 2;
        ballY = getHeight() / 2;
        
        
        double angle = Math.random() * Math.PI/2 - Math.PI/4; 
        int direction = Math.random() < 0.5 ? 1 : -1; 
        
        
        double speed = 5.0;
        ballDX = (int)(Math.cos(angle) * speed) * direction;
        ballDY = (int)(Math.sin(angle) * speed);
        
        System.out.println("Ball reset to center. New direction: " + ballDX + ", " + ballDY);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        
        g.setColor(Color.WHITE);
        g.fillRect(20, p1Y, 10, 100); // Left paddle
        g.fillRect(getWidth() - 30, p2Y, 10, 100); // Right paddle
        g.fillOval(ballX, ballY, 20, 20); // Ball
        
        
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(String.valueOf(p1Score), getWidth()/4, 30);
        g.drawString(String.valueOf(p2Score), 3*getWidth()/4, 30);
        
        
        if (gameEnded) {
            
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String gameOverText = "GAME OVER";
            String winnerText = winnerName + " WINS!";
            String scoreText = p1Score + " - " + p2Score;

            FontMetrics fm = g.getFontMetrics();
            int gameOverX = (getWidth() - fm.stringWidth(gameOverText)) / 2;
            int winnerX = (getWidth() - fm.stringWidth(winnerText)) / 2;
            int scoreX = (getWidth() - fm.stringWidth(scoreText)) / 2;

            g.drawString(gameOverText, gameOverX, getHeight()/2 - 60);
            g.drawString(winnerText, winnerX, getHeight()/2);
            g.drawString(scoreText, scoreX, getHeight()/2 + 60);
        }
    }
}