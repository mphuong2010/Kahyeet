import javax.sound.sampled.*;
import java.io.File;

public class Sound {
    private Clip clip;

    // Constructor để load file âm thanh
    public Sound(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("sound/" + filePath).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Phát âm thanh một lần
    public void playOnce() {
        if (clip != null) {
            clip.setFramePosition(0); // Reset về đầu
            clip.start();
        }
    }

    // Phát âm thanh lặp lại
    public void playLoop() {
        if (clip != null) {
            clip.setFramePosition(0); // Reset về đầu
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Lặp lại liên tục
            clip.start();
        }
    }

    // Dừng phát âm thanh
    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
}
