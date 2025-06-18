import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;

class WebSocketClient implements WebSocket.Listener {
    private WebSocket webSocket;
    private MainController mainController;

    public WebSocketClient(String url, MainController mainController) {
        System.out.println("WebSocketClient constructor called");
        System.out.println("Attempting to connect to WebSocket: " + url);
        this.mainController = mainController;
        
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .buildAsync(URI.create(url), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    System.out.println("Connected to WebSocket!");
                    System.out.println("WebSocket opened");
                })
                .exceptionally(e -> {
                    System.err.println("Failed to connect: " + e.getMessage());
                    return null;
                });
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        System.out.println("Raw message received: " + message);
        
        try {
            
            mainController.handleServerMessage(message);
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Listener.super.onText(webSocket, data, last);
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            System.out.println("Sending message: " + message);
            webSocket.sendText(message, true);
        } else {
            System.err.println("WebSocket is null! Message not sent: " + message);
        }
    }
}
