import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Server {
    private static int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static JLabel completedLabel; // Label to show completed clients count
    private static JButton startButton;
    private static JButton finishButton;
    private static JButton leaderButton;
    private static int completedClients = 0; // Counter for completed clients
    private static int totalClients = 0; // Counter for total clients
    private static boolean shuffleQuestions = false;
    private static boolean shuffleAnswers = false;
    private static String ipAddress;
    private static boolean isGameStarted = false;
    private static boolean isShowLeaderboard = false;
    private static boolean dontShowTrueAnswers = false;
    private static boolean noBonusPoint = false;
    private static JDialog kickFrame;
    private static LeaderServer leaderServer;
    private static Set<String> kickedPlayers = new HashSet<>();

    public static void main(String[] args) {

        final Object lock = new Object();
        SwingUtilities.invokeLater(() -> {
            JTextField portField = new JTextField("12345", 7);
            JPanel panel = new JPanel();
            panel.add(new JLabel("Enter PORT:"));
            panel.add(portField);

            while (true) {
                int result = JOptionPane.showConfirmDialog(null, panel, "Server Configuration", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        int port = Integer.parseInt(portField.getText());
                        if (port <= 0 || port > 65535) {
                            throw new NumberFormatException();
                        }
                        PORT = port;
                        break; // Exit the loop if the port is valid
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid port number. Please enter a valid integer between 1 and 65535.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.exit(0);
                }
            }
            synchronized (lock) {
                lock.notify();
            }
        });

        synchronized (lock) {
            try {
            lock.wait();
            } catch (InterruptedException e) {
            e.printStackTrace();
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            ipAddress = java.net.InetAddress.getLocalHost().getHostAddress();
            showServerUI();

            System.out.println("Server IP address: " + ipAddress);
            System.out.println("Server running on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server stopped.");
            broadcast("Server has stopped.");
            }
        }

    private static void showServerUI() {
        JFrame frame = new JFrame("Kahyeet! Server Management");
        frame.setSize(800, 460);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 2));
    
        JLabel ipLabel = new JLabel("Server IP address: " + ipAddress, SwingConstants.CENTER);
        ipLabel.setFont(new Font(ipLabel.getFont().getName(), Font.BOLD, ipLabel.getFont().getSize()));
        JLabel portLabel = new JLabel("Port: " + PORT, SwingConstants.CENTER);
        portLabel.setFont(new Font(portLabel.getFont().getName(), Font.BOLD, portLabel.getFont().getSize()));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Panel to hold start and finish buttons
        startButton = new JButton("START");
        startButton.setBackground(new Color(173, 216, 230)); // Light blue background
        finishButton = new JButton("FINISH FOR ALL");
        finishButton.setBackground(new Color(173, 216, 230));
        buttonPanel.add(startButton);
        buttonPanel.add(finishButton);
    
        JPanel completedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Panel to hold completedLabel
        completedLabel = new JLabel("Completed: 0/" + totalClients);
        completedPanel.add(completedLabel);

        JPanel buttonPanel_2 = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Panel to hold start and finish buttons
        JButton questionButton = new JButton("QUESTION");
        leaderButton = new JButton("LEADERBOARD");
        buttonPanel_2.add(questionButton);
        buttonPanel_2.add(leaderButton);

        JPanel timerPanel = new JPanel(new BorderLayout()); // Panel to hold timer slider
        JLabel timerLabel = new JLabel("Timer: 15 s");
        JSlider timerSlider = new JSlider(0, 90, 15);
        timerSlider.setMajorTickSpacing(10);
        timerSlider.setMinorTickSpacing(1);
        timerSlider.setPaintTicks(true);
        timerSlider.setPaintLabels(true);
        timerSlider.addChangeListener(e -> timerLabel.setText("Timer: " + timerSlider.getValue() + " s"));
        timerPanel.add(timerLabel, BorderLayout.WEST);
        timerPanel.add(timerSlider, BorderLayout.CENTER);
    
        JPanel checkPanel = new JPanel(new GridLayout(2, 2));

        JPanel shuffleQuestionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Panel to hold shuffleQuestionsCheckBox
        JCheckBox shuffleQuestionsCheckBox = new JCheckBox("Shuffle the questions");
        shuffleQuestionsPanel.add(shuffleQuestionsCheckBox);
    
        JPanel shuffleAnswersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Panel to hold shuffleAnswersCheckBox
        JCheckBox shuffleAnswersCheckBox = new JCheckBox("Shuffle the answers");
        shuffleAnswersPanel.add(shuffleAnswersCheckBox);

        JPanel dontShowTrueAnswersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Panel to hold showTrueAnswersCheckBox
        JCheckBox dontShowTrueAnswersCheckBox = new JCheckBox("Don't show the true answers");
        dontShowTrueAnswersPanel.add(dontShowTrueAnswersCheckBox);

        JPanel noBonusPointPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Panel to hold showTrueAnswersCheckBox
        JCheckBox noBonusPointCheckBox = new JCheckBox("Don't give bonus points");
        noBonusPointPanel.add(noBonusPointCheckBox);

        checkPanel.add(shuffleQuestionsPanel);
        checkPanel.add(shuffleAnswersPanel);
        checkPanel.add(dontShowTrueAnswersPanel);
        checkPanel.add(noBonusPointPanel);

        JPanel buttonPanel_3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton kickPlayerButton = new JButton("KICK PLAYER");
        buttonPanel_3.add(kickPlayerButton);
    
        startButton.setEnabled(false);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isGameStarted = true;
                int timerValue = timerSlider.getValue();
                broadcast("TIMER:" + timerValue);
                broadcast("START_GAME");
                if (shuffleQuestions) {
                    broadcast("SHUFFLE_QUESTIONS");
                }
                if (shuffleAnswers) {
                    broadcast("SHUFFLE_ANSWERS");
                }
                if (dontShowTrueAnswers) {
                    broadcast("DONT_SHOW_TRUE_ANSWERS");
                }
                if (noBonusPoint) {
                    broadcast("NO_BONUS_POINT");
                }
                for (ClientHandler client : clients) {
                    client.sendQuestionsToClient();
                }
                Player.addSeparatorLine();
                startButton.setEnabled(false);
                finishButton.setEnabled(true);
                timerSlider.setEnabled(false);
                leaderButton.setEnabled(true);
                shuffleQuestionsCheckBox.setEnabled(false);
                shuffleAnswersCheckBox.setEnabled(false);
                dontShowTrueAnswersCheckBox.setEnabled(false);
                noBonusPointCheckBox.setEnabled(false);
                startButton.setBackground(Color.GRAY);
            }
        });
    
        finishButton.setEnabled(false);
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                broadcast("FINISH");
                finishButton.setEnabled(false);
                finishButton.setBackground(Color.GRAY);
            }
        });

        leaderButton.setEnabled(false);
        leaderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (leaderServer == null || !leaderServer.isWindowOpen()) {
                    leaderServer = new LeaderServer();
                } else {
                    leaderServer.toFront();
                }
            }
        });

        questionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File("questions.txt");
                    if (!file.exists()) {
                        JOptionPane.showMessageDialog(null, "File questions.txt does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        Desktop.getDesktop().open(file); // Mở file với ứng dụng mặc định
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "An error occurred while opening the file.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        shuffleQuestionsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (shuffleQuestionsCheckBox.isSelected()) {
                    shuffleQuestions = true;
                } else {
                    shuffleQuestions = false;
                }
            }
        });

    
        shuffleAnswersCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (shuffleAnswersCheckBox.isSelected()) {
                    shuffleAnswers = true;
                } else {
                    shuffleAnswers = false;
                }
            }
        });

        dontShowTrueAnswersCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (dontShowTrueAnswersCheckBox.isSelected()) {
                    dontShowTrueAnswers = true;
                } else {
                    dontShowTrueAnswers = false;
                }
            }
        });

        noBonusPointCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (noBonusPointCheckBox.isSelected()) {
                    noBonusPoint = true;
                } else {
                    noBonusPoint = false;
                }
            }
        });

        kickPlayerButton.addActionListener(e -> openKickPlayerFrame(frame));

        JTextArea terminalOutput = new JTextArea(10, 30);
        terminalOutput.setEditable(false);
        terminalOutput.setWrapStyleWord(true);
        terminalOutput.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(terminalOutput);
        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            terminalOutput.append(String.valueOf((char) b));
            terminalOutput.setCaretPosition(terminalOutput.getDocument().getLength());
            }
        });
        System.setOut(printStream);
        System.setErr(printStream);
    
        JPanel controlpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlpanel.setLayout(new GridLayout(9, 1));
        controlpanel.add(ipLabel);
        controlpanel.add(portLabel);
        controlpanel.add(buttonPanel);
        controlpanel.add(buttonPanel_2);
        controlpanel.add(completedPanel);
        controlpanel.add(timerPanel);
        controlpanel.add(checkPanel);
        controlpanel.add(Box.createVerticalStrut(20));
        controlpanel.add(buttonPanel_3);
        
        frame.add(controlpanel);
        frame.add(scrollPane);
        frame.setVisible(true);
    }

    public static void updateCompletedClientsCount() {
        if (!isGameStarted) {
            if (totalClients > 0) {
                startButton.setEnabled(true);
            } else {
                startButton.setEnabled(false);
            }
        }
        completedClients = (int) clients.stream().filter(client -> client.getPlayer() != null && client.getPlayer().isFinished()).count();
        completedLabel.setText("Completed: " + completedClients + "/" + totalClients);
    }

    public static boolean isUsernameTaken(String username) {
        for (ClientHandler client : clients) {
            if (client.getPlayer() != null && client.getPlayer().getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static void removeClient(String username) {
        clients.removeIf(client -> client.getPlayer() != null && client.getPlayer().getUsername().equals(username));
    }

    public static void updateWaitingPlayers() {
        StringBuilder usernames = new StringBuilder("UPDATE_WAITING_LIST:");
        for (ClientHandler client : clients) {
            if (client.getPlayer() != null) {
                usernames.append(client.getPlayer().getUsername()).append(",");
            }
        }
        broadcast(usernames.toString());
    }

    public static void checkAllPlayersFinished() {
        updateCompletedClientsCount();
        if (leaderServer != null && leaderServer.isWindowOpen()) {
            leaderServer.updateLeaderboard();
        }
        if (completedClients == totalClients && totalClients !=0 && !isShowLeaderboard) {
            finishButton.setEnabled(false);
            // Gợi Phương thức gửi file score đến client ở class ClientHandler
            broadcast("SHOW_LEADERBOARD");
            isShowLeaderboard = true;
            for (ClientHandler client : clients) {
                client.sendLatestScoreDataToClient();
            }
        }
    }

    public static void addClient(boolean add) {
        if (add) totalClients++;
        else {
            totalClients--;
            checkAllPlayersFinished();
        }
        if (kickFrame != null && kickFrame.isVisible()) {
            SwingUtilities.invokeLater(() -> updatePlayerList(kickFrame));
        }
    }

    public static void broadcast(String message) {
        System.out.println("Message 2 Player: " + message);
        for (ClientHandler client : clients) {
            client.sendMessageToClient(message);
        }
    }

    public static boolean isGameStarted() {
        return isGameStarted;
    }

    // Phương thức hỗ trợ để kick player
    private static void kickPlayer(String username) {
        ClientHandler clientToKick = null;
        for (ClientHandler client : clients) {
            if (client.getPlayer() != null && client.getPlayer().getUsername().equals(username)) {
                clientToKick = client;
                break;
            }
        }
        if (clientToKick != null) {
            kickedPlayers.add(username);
            clientToKick.sendMessageToClient("KICK");
        }
    }

    public static boolean isPlayerKicked(String username) {
        return kickedPlayers.contains(username);
    }

    private static void openKickPlayerFrame(JFrame parentFrame) {
        kickFrame = new JDialog(parentFrame, "Kick Player", true);
        kickFrame.setSize(300, 400);
        kickFrame.setLayout(new BorderLayout());
        kickFrame.setAlwaysOnTop(true);
        updatePlayerList(kickFrame);

        kickFrame.setVisible(true);
    }

    private static void updatePlayerList(JDialog kickFrame) {
        JPanel playerListPanel = new JPanel(new BorderLayout());

        if (totalClients == 0) {
            JLabel noPlayersLabel = new JLabel("No players to kick.", SwingConstants.CENTER);
            playerListPanel.add(noPlayersLabel, BorderLayout.CENTER);
        } else {
            String[] columnNames = {"Player Username"};
            List<String[]> playerData = new ArrayList<>();
            for (ClientHandler client : clients) {
                if (client.getPlayer() != null) {
                    playerData.add(new String[]{client.getPlayer().getUsername()});
                }
            }
            String[][] data = playerData.toArray(new String[0][]);
            JTable playerTable = new JTable(data, columnNames);
            playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            playerTable.getTableHeader().setReorderingAllowed(false);
            JScrollPane scrollPane = new JScrollPane(playerTable);
            playerListPanel.add(scrollPane, BorderLayout.CENTER);

            playerTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = playerTable.getSelectedRow();
                    if (row != -1) {
                        String selectedPlayer = playerTable.getValueAt(row, 0).toString();
                        int confirm = JOptionPane.showConfirmDialog(kickFrame, "Are you sure you want to kick " + selectedPlayer + "?",
                                "Confirm Kick", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            new Thread(() -> kickPlayer(selectedPlayer)).start();
                        }
                    }
                }
            });
        }
        kickFrame.getContentPane().removeAll();
        kickFrame.add(playerListPanel, BorderLayout.CENTER);
        kickFrame.revalidate();
        kickFrame.repaint();
    }
}
