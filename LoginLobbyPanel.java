import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LoginLobbyPanel extends JPanel {
    private MainController controller;
    private JTextField nameField;
    private JButton joinButton;
    private JButton startButton;
    private JList<String> playerList;
    
    
    private final Color BACKGROUND_COLOR = new Color(25, 25, 25);
    private final Color ACCENT_COLOR = new Color(0, 255, 127);
    private final Color TEXT_COLOR = Color.WHITE;

    public LoginLobbyPanel(MainController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        
        JLabel titleLabel = new JLabel("PONG MULTIPLAYER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        
        nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        nameField.setBackground(new Color(45, 45, 45));
        nameField.setForeground(TEXT_COLOR);
        nameField.setCaretColor(TEXT_COLOR);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        
        joinButton = createStyledButton("Join Lobby");
        startButton = createStyledButton("Start Game");
        startButton.setEnabled(false);

        
        playerList = new JList<>();
        playerList.setBackground(new Color(45, 45, 45));
        playerList.setForeground(TEXT_COLOR);
        playerList.setFont(new Font("Arial", Font.BOLD, 16));
        playerList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        
        JScrollPane scrollPane = new JScrollPane(playerList);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));

    
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.add(nameField, BorderLayout.CENTER);
        topPanel.add(joinButton, BorderLayout.EAST);

        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(topPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        
        joinButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                controller.joinLobby(name);
                joinButton.setEnabled(false);
                nameField.setEnabled(false);
            }
        });

        startButton.addActionListener(e -> controller.startGame());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
        });
        
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(30, 30, 30),
            0, getHeight(), new Color(20, 20, 20)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    public void updateLobby(String[] players) {
        System.out.println("LoginLobbyPanel updating with " + players.length + " players");
        
        
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String player : players) {
            model.addElement("Player: " + player);
        }
        playerList.setModel(model);
        
        // Update start button
        boolean canStart = players.length == 2;
        System.out.println("Setting start button enabled: " + canStart);
        startButton.setEnabled(canStart);
        
        if (canStart) {
            startButton.setText("Start Game (2/2 Players)");
            startButton.setBackground(ACCENT_COLOR);
        } else {
            startButton.setText("Waiting for players... (" + players.length + "/2)");
            startButton.setBackground(Color.GRAY);
        }
        
        revalidate();
        repaint();
    }
    
    public void enableStartButton(boolean enable) {
        startButton.setEnabled(enable);
        if (enable) {
            startButton.setText("Start Game (2/2 Players)");
            startButton.setBackground(ACCENT_COLOR);
        } else {
            startButton.setText("Waiting for players...");
            startButton.setBackground(Color.GRAY);
        }
        revalidate();
        repaint();
    }
}
