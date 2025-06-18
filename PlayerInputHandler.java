import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PlayerInputHandler extends KeyAdapter {

    private final WebSocketClient socketClient;

    public PlayerInputHandler(WebSocketClient client) {
        this.socketClient = client;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        String move = "";
        if (e.getKeyCode() == KeyEvent.VK_W) move = "UP";
        if (e.getKeyCode() == KeyEvent.VK_S) move = "DOWN";

        if (!move.isEmpty()) {
            socketClient.sendMessage("{\"action\":\"sendMove\", \"direction\":\"" + move + "\"}");
        }
    }
}
