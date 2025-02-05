import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeaderServer extends JFrame {
    private static boolean windowOpen = false;
    private DefaultTableModel model; // Lưu model của bảng để có thể cập nhật lại khi cần
    private List<ScoreEntry> scores = new ArrayList<>(); // Lưu trữ danh sách ScoreEntry để dùng trong cellRenderer
    //scoreEntry luu du lieu username: ten, diem, rank

    public LeaderServer() {
        windowOpen = true;
        setTitle("Leaderboard - SERVER");      
        setSize(new Dimension(400, 300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"Rank", "Username", "Score"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false); //tat ko cho kéo qua lại

        // Đọc và hiển thị bảng xếp hạng lần đầu
        updateLeaderboard();

        // Thiết lập bộ render cho bảng
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                cell.setHorizontalAlignment(JLabel.CENTER);

                // Sử dụng đối tượng ScoreEntry từ danh sách scores thay vì từ model của bảng
                ScoreEntry entry = scores.get(row);
                String rank = (String) table.getValueAt(row, 0);

                // Tô màu theo rank hoặc trạng thái
                if (entry.isDisconnected()) cell.setBackground(Color.RED);
                else if (rank.equals("GOLD")) cell.setBackground(Color.YELLOW);
                else if (rank.equals("SILVER")) cell.setBackground(Color.LIGHT_GRAY);
                else if (rank.equals("BRONZE")) cell.setBackground(new Color(205, 127, 50));
                else cell.setBackground(Color.WHITE);

                cell.setForeground(entry.isDisconnected() ? Color.WHITE : Color.BLACK);
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

    // Phương thức cập nhật lại bảng xếp hạng
    public void updateLeaderboard() {
        model.setRowCount(0); // Xóa các hàng hiện tại
        scores = readScoresFromFile(); // Đọc điểm mới từ file

        // Sắp xếp và thêm dữ liệu vào bảng
        scores.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));
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
    }

    private List<ScoreEntry> readScoresFromFile() {
        List<ScoreEntry> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("scores.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("-------------***---------------")) {
                    scores.clear();
                } else if (line.contains(":")) {
                    String[] parts = line.split(":");
                    String username = parts[0].trim();
                    boolean isDisconnected = line.contains("(disconnected)");
                    String scoreText = parts[1].trim().split(" ")[0];
                    
                    try {
                        int score = Integer.parseInt(scoreText);
                        scores.add(new ScoreEntry(username, score, isDisconnected));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid score format for user " + username + ": " + parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scores;
    }    

    private class ScoreEntry {
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

    public boolean isWindowOpen() {
        return windowOpen;
    }

    @Override
    public void dispose() {
        windowOpen = false;
        super.dispose();
    }
}