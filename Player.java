import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Player {
    private String username;
    private int score;
    private boolean finished; // New field to track if the player has completed all questions

    public Player(String username) {
        this.username = username;
        this.score = 0;
        this.finished = false;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void markFinished() {
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public void saveScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write(username + ": " + score + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveScore(String notes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write(username + ": " + score + " (" + notes + ")" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // New method to add separator line after all players finish
    public static void addSeparatorLine() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write("-------------------------------\n");
            writer.write(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n");
            writer.write("-------------***---------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
