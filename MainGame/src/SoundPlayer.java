import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Helper class to play the sounds
 */
public class SoundPlayer {

    /**
     * Handles the play behavior.
     *
     * @param path the path value
     */
    public static void play(String path) {
        try {
            URL url = SoundPlayer.class.getClassLoader().getResource(path);
            if (url == null) return;

            AudioInputStream audio = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception ignored) {
        }
    }
}
