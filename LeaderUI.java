import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LeaderUI extends JFrame {
    public LeaderUI(String currentUsername, String scoreData) {
        setTitle("Leaderboard");
        setSize(new Dimension(400, 300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"Rank", "Username", "Score"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);

        // Read scores from file
        List<ScoreEntry> scores = parseScoreData(scoreData);  // Parse score data into a list of entries
        
        // Sort scores in descending order
        scores.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));

        // Populate table
        for (int i = 0; i < scores.size(); i++) {
            ScoreEntry entry = scores.get(i);
            String rank = switch (i) {
                case 0 -> "GOLD";
                case 1 -> "SILVER";
                case 2 -> "BRONZE";
                default -> String.valueOf(i + 1);
            };
            model.addRow(new Object[]{rank, entry.getUsername(), entry.getScore()});
        }

        // Custom cell renderer
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                cell.setHorizontalAlignment(JLabel.CENTER);

                String rank = (String) table.getValueAt(row, 0);
                ScoreEntry entry = scores.get(row);  // Get the corresponding ScoreEntry

                if (entry.isDisconnected()) cell.setBackground(Color.RED);
                else if (rank.equals("GOLD")) cell.setBackground(Color.YELLOW);
                else if (rank.equals("SILVER")) cell.setBackground(Color.LIGHT_GRAY);
                else if (rank.equals("BRONZE")) cell.setBackground(new Color(205, 127, 50));
                else cell.setBackground(Color.WHITE); // Default color for other ranks

                // Highlight the current user's row
                String username = (String) table.getValueAt(row, 1);
                if (username.equals(currentUsername)) {
                    cell.setForeground(Color.BLUE);
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD));
                } else if (entry.isDisconnected()) cell.setForeground(Color.WHITE);
                else cell.setForeground(Color.BLACK);
                return cell;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        setVisible(true);
    }

    private List<ScoreEntry> parseScoreData(String scoreData) {
        List<ScoreEntry> scores = new ArrayList<>();
        String[] lines = scoreData.split("\n");
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":");
                String username = parts[0].trim();
                String scoreText = parts[1].trim().split(" ")[0];
                boolean isDisconnected = line.contains("(disconnected)");

                try {
                    int score = Integer.parseInt(scoreText);
                    scores.add(new ScoreEntry(username, score, isDisconnected));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid score format for user " + username + ": " + parts[1].trim());
                }
            }
        }
        return scores;
    }

    private static class ScoreEntry {
        private final String username;
        private final int score;
        private final boolean isDisconnected;

        public ScoreEntry(String username, int score, boolean isDisconnected) {
            this.username = username;
            this.score = score;
            this.isDisconnected = isDisconnected;
        }

        public String getUsername() {
            return username;
        }

        public int getScore() {
            return score;
        }

        public boolean isDisconnected() {
            return isDisconnected;
        }
    }
}
