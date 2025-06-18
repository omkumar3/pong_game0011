import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Arrays;

public class MainController {

    private JFrame frame;
    private LoginLobbyPanel lobbyPanel;
    private PongGamePanel gamePanel;
    private WebSocketClient socketClient;
    private String playerName;
    private String opponentName;

    public MainController(JFrame frame) {
        System.out.println("MainController constructor called");
        this.frame = frame;
        
        
        String websocketUrl = "wss://0yg6rgepmk.execute-api.us-east-1.amazonaws.com/prod/";
        this.socketClient = new WebSocketClient(websocketUrl, this);

        lobbyPanel = new LoginLobbyPanel(this);
        frame.setContentPane(lobbyPanel);
        frame.revalidate();
    }

    public void joinLobby(String name) {
        this.playerName = name;
        JSONObject message = new JSONObject();
        message.put("action", "joinLobby");
        message.put("name", name);
        System.out.println("Sending join message: " + message.toString()); 
        socketClient.sendMessage(message.toString());
    }

    public void startGame() {
        if (opponentName != null) {
            JSONObject message = new JSONObject();
            message.put("action", "startGame");
            message.put("player1", playerName);
            message.put("player2", opponentName);
            message.put("roomId", playerName + "_" + opponentName); 
            System.out.println("\n=== Requesting Game Start ===");
            System.out.println("Sending start game request for room: " + playerName + "_" + opponentName);
            socketClient.sendMessage(message.toString());
        } else {
            System.err.println("Cannot start game - waiting for opponent to join");
            JOptionPane.showMessageDialog(frame, 
                "Waiting for opponent to join", 
                "Cannot Start Game", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    public void handleServerMessage(String message) {
        System.out.println("\n=== Processing Message ===\n" + message);
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");

            if ("lobbyUpdate".equals(type)) {
                JSONArray playersArray = json.getJSONArray("players");
                String[] players = new String[playersArray.length()];
                for (int i = 0; i < playersArray.length(); i++) {
                    players[i] = playersArray.getString(i);
                }
                
                System.out.println("Lobby players: " + String.join(", ", players));
                if (players.length == 2) {
                    opponentName = players[0].equals(playerName) ? players[1] : players[0];
                    System.out.println("Found opponent: " + opponentName);
                }
                updateLobbyPlayers(players);
            } 
            else if ("gameStarted".equals(type)) {
                String roomId = json.getString("roomId");
                String player1 = json.getString("player1");
                String player2 = json.getString("player2");
                
                System.out.println("Game starting in room: " + roomId);
                System.out.println("Player 1: " + player1);
                System.out.println("Player 2: " + player2);
                
                if (gamePanel == null) {
                    switchToGamePanel(player1, player2);
                }
            }
            else if ("gameEnd".equals(type)) {
                if (gamePanel != null) {
                    String winner = json.getString("winner");
                    int score1 = json.getInt("score1");
                    int score2 = json.getInt("score2");
                    gamePanel.handleGameEnd(winner, score1, score2);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchToGamePanel(String player1, String player2) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Creating game panel. Player1: " + player1 + 
                             ", Player2: " + player2 + 
                             ", Current player: " + playerName);
            gamePanel = new PongGamePanel(this, player1, player2);
            frame.setContentPane(gamePanel);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void updateLobbyPlayers(String[] players) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Updating lobby UI with " + players.length + " players");
            if (lobbyPanel != null) {
                lobbyPanel.updateLobby(players);
            }
        });
    }

    public void sendPlayerMove(String direction) {
        if (opponentName != null && gamePanel != null) {
            JSONObject message = new JSONObject();
            message.put("action", "sendMove");
            message.put("direction", direction);
            message.put("player", playerName);
            System.out.println("Sending move: " + direction + " from " + playerName);
            socketClient.sendMessage(message.toString());
        }
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void broadcastGameEnd(String winner, int score1, int score2) {
        JSONObject message = new JSONObject();
        message.put("action", "gameEnd");
        message.put("winner", winner);
        message.put("score1", score1);
        message.put("score2", score2);
        System.out.println("Broadcasting game end: " + message);
        socketClient.sendMessage(message.toString());
    }

    public void returnToLobby() {
        gamePanel = null;
        lobbyPanel = new LoginLobbyPanel(this);
        frame.setContentPane(lobbyPanel);
        frame.revalidate();
        frame.repaint();
    }
}