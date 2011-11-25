package andient;

import andient.player.*;
import com.softsynth.jsyn.Synth;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * User: dan
 * Date: 11/22/11
 */
public class Orchestra {
    private static Set<Player> playerSet = new HashSet<Player>();
    private static Orchestra ourInstance = new Orchestra();
    private static int triggerLevel = 50;

    private final static Logger logger = Logger.getLogger(Orchestra.class.getName());
    private static AmplitudeNotificationThread amplitudeThread;

    public static Orchestra getInstance() {
        return ourInstance;
    }

    private Orchestra() {
    }

    public void init() {
        Synth.startEngine(0);
        amplitudeThread = new AmplitudeNotificationThread();
        amplitudeThread.start();
    }

    public Player createAndRegisterPlayer(PlayerTypeEnum playerType) {
        Player newPlayer;
        switch (playerType) {
            case STRUMMER:
                newPlayer = new PlayerStrummer(100, 0.4, 8, 25, 75, triggerLevel);
                break;
            case FLOATER:
                newPlayer = new PlayerFloater(100, 0.4, 8, 25, 75, triggerLevel);
                break;
            case DOODLER:
                newPlayer = new PlayerDoodler(100, 0.4, 8, 25, 75, triggerLevel);
                break;
            case CHUNKER:
                newPlayer = new PlayerChunker(100, 0.4, 8, 25, 75, triggerLevel);
                break;
            case BASS:
                newPlayer = new PlayerBass(100, 0.4, 8, 25, 75, triggerLevel);
                break;
            default:
                newPlayer = new PlayerStrummer(100, 0.4, 8, 25, 75, triggerLevel);
        }
        playerSet.add(newPlayer);
        return newPlayer;
    }

    public void deregisterPlayer(Player player) {
        player.halt();
        playerSet.remove(player);
    }

    public void setTriggerLevel(int newTriggerLevel) {
        this.triggerLevel = newTriggerLevel;

        for (Player p : playerSet) {
            p.onNotifyTriggerLevel(newTriggerLevel);
        }
    }

    protected void fireAmplitudeAdjustNotification() {
        for (Player p : playerSet) {
            p.onNotifyAmplitudeAdjust();
        }
    }

    private class AmplitudeNotificationThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(50);  // todo define constant
                } catch (InterruptedException e) {
                    System.err.println(e);
                } finally {
                    fireAmplitudeAdjustNotification();
                }
            }
        }
    }
}
