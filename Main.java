import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Main started");
        JFrame frame = new JFrame("Multiplayer Pong");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        new MainController(frame);
        frame.setVisible(true);
    }
}
