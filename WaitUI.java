import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WaitUI extends JFrame {
    private String username;
    private JTextArea leftWaitingPlayersArea;
    private JTextArea rightWaitingPlayersArea;
    private Sound backgroundMusic;

    public WaitUI(String username) {
        this.username = username;

        setTitle("Waiting Room - " + username);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel waitingLabel = new JLabel("Waiting for other players to join...", SwingConstants.CENTER);
        add(waitingLabel, BorderLayout.NORTH);

        JPanel waitingPlayersPanel = new JPanel(new GridLayout(1, 2));

        leftWaitingPlayersArea = new JTextArea();
        leftWaitingPlayersArea.setEditable(false);
        leftWaitingPlayersArea.setFocusable(false);
        leftWaitingPlayersArea.setLineWrap(true);
        leftWaitingPlayersArea.setWrapStyleWord(true);
        waitingPlayersPanel.add(new JScrollPane(leftWaitingPlayersArea));

        rightWaitingPlayersArea = new JTextArea();
        rightWaitingPlayersArea.setEditable(false);
        rightWaitingPlayersArea.setFocusable(false);
        rightWaitingPlayersArea.setLineWrap(true);
        rightWaitingPlayersArea.setWrapStyleWord(true);
        waitingPlayersPanel.add(new JScrollPane(rightWaitingPlayersArea));

        add(waitingPlayersPanel, BorderLayout.CENTER);

        setVisible(true);

        // Phát nhạc nền khi mở phòng chờ
        backgroundMusic = new Sound("background_wait.wav");
        backgroundMusic.playLoop();
    }

    public void updateWaitingPlayers(List<String> playerUsernames) {
        leftWaitingPlayersArea.setText("");
        rightWaitingPlayersArea.setText("");
        for (int i = 0; i < playerUsernames.size(); i++) {
            if (i % 2 == 0) {
                leftWaitingPlayersArea.append(playerUsernames.get(i) + "\n");
            } else {
                rightWaitingPlayersArea.append(playerUsernames.get(i) + "\n");
            }
        }
    }

    public void close() {
        setVisible(false);
        dispose();
        // Dừng nhạc nền khi đóng phòng chờ
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
}
