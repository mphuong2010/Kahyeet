import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Player player;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Nhan tin nhan lay username tu client
            String initialMessage = in.readLine();
            if (initialMessage != null && initialMessage.startsWith("USERNAME:")) {
                String username = initialMessage.substring(9);
                if (Server.isPlayerKicked(username)) {
                    sendMessageToClient("ERROR: You have been kicked from the server and cannot reconnect.");
                    closeConnection();
                    return;
                }
                if (Server.isUsernameTaken(username)) {
                    sendMessageToClient("ERROR: Username already taken.");
                    closeConnection();
                    return;
                } else if (Server.isGameStarted()) {
                    sendMessageToClient("ERROR: Game already started.");
                    closeConnection();
                    return;
                }
                player = new Player(username);
                Server.broadcast(username + " has joined.");
                Server.addClient(true);
                Server.updateCompletedClientsCount();
                try {
                    Thread.sleep(1000);
                    Server.updateWaitingPlayers();
                } catch (InterruptedException e) {
                    System.err.format("IOException: %s%n", e);
                }
            }

            // Nhan cac tin nhan con lai tu client
            String message;
            while ((message = in.readLine()) != null) {  // Đọc tin nhắn từ client
                System.out.println(player.getUsername() + ": " + message);
                // Tin nhan tra loi cau hoi
                if (message.startsWith("ANSWER:")) {
                    handleAnswer(message);
                } else if (message.startsWith("SCORE:")) {
                    int score = Integer.parseInt(message.split(":")[1]);
                    player.setScore(score);
                } else if (message.equals("END")) {
                    player.markFinished();
                    player.saveScore();
                    Server.checkAllPlayersFinished();
                }
                // them cac tin nhan tu client o day
            }
        } catch (IOException e) {
            disconnectPlayer();
        } finally {
            closeConnection();
        }
    }

    // Phương thức gửi tin nhắn đến client
    public void sendMessageToClient(String message) { // done
        out.println(message);  // Gửi chuỗi tới client
    }

    private void handleAnswer(String messageResult) {
        System.out.println("PLAYER " + player.getUsername() + " " + messageResult);
    }

    public void disconnectPlayer() {
        if (player != null) {
            Server.broadcast(player.getUsername() + " disconnected.");
            if (!player.isFinished() && !Server.isPlayerKicked(player.getUsername())) {
                player.saveScore("disconnected");
            }
            Server.removeClient(player.getUsername());
            Server.addClient(false);
            Server.updateCompletedClientsCount();
            Server.updateWaitingPlayers();
        }
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void sendLatestScoreDataToClient() {
    try (BufferedReader reader = new BufferedReader(new FileReader("scores.txt"))) {
        List<String> latestSection = new ArrayList<>();
        String line;
        
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("-------------***---------------")) {
                latestSection.clear();  // Clear previous lines when a new section starts
            }
            latestSection.add(line);  // Add line to latest section
        }
        
        // Send the latest section to the client
        for (String sectionLine : latestSection) {
            if (!sectionLine.startsWith("-------------------------------") && !sectionLine.startsWith("-------------***---------------")) {
                sendMessageToClient("SCORE_DATA:" + sectionLine);  // Send only relevant score lines
            }
        }
        sendMessageToClient("SCORE_DATA_END");  // End marker for score data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendQuestionsToClient() {
        try (BufferedReader reader = new BufferedReader(new FileReader("questions.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sendMessageToClient("QUESTION:" + line);
            }
            sendMessageToClient("QUESTION_END");
        } catch (IOException e) {
            System.out.println("Failed to load questions.");
            e.printStackTrace();
        }
    }
}