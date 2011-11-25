package andient.player;

import andient.StatusListener;
import com.softsynth.jsyn.LineOut;
import com.softsynth.jsyn.MultiplyUnit;
import com.softsynth.jsyn.SynthException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: dan
 * Date: 11/22/11
 */
public abstract class BaseThreadedPlayer extends Thread implements Player {
    public static final double AMP_ADJUST_GRANULARITY = .1; // 10% per notification

    protected double loadValue;
    protected double pitchBase;
    protected int amplitudeSetting;
    protected int lowTriggerThreshold;
    protected int highTriggerThreshold;
    protected int triggerLevel;

    protected boolean muted = false;

    protected int currentAmplitude = 0;
    protected int targetAmplitude;

    protected StatusListener statusListener;

    protected LineOut noteOut;
    protected MultiplyUnit ampoutL = new MultiplyUnit();
    protected MultiplyUnit ampoutR = new MultiplyUnit();

    private final static Logger logger = Logger.getLogger(BaseThreadedPlayer.class.getName());

    protected BaseThreadedPlayer(int amplitudeSetting, double loadValue, double pitchBase, int lowTriggerThreshold, int highTriggerThreshold, int triggerLevel) throws SynthException {
        this.amplitudeSetting = amplitudeSetting;
        this.loadValue = loadValue;
        setPitchBase(pitchBase);
        this.lowTriggerThreshold = lowTriggerThreshold;
        this.highTriggerThreshold = highTriggerThreshold;
        this.triggerLevel = triggerLevel;
        calculateTargetAmplitude();

        ampoutL.inputB.set(currentAmplitude);
        ampoutR.inputB.set(currentAmplitude);

        noteOut = new LineOut();
        ampoutL.output.connect(0, noteOut.input, 0);
        ampoutR.output.connect(0, noteOut.input, 1);
    }

    public int getAmplitudeSetting() {
        return amplitudeSetting;
    }

    public double getLoadValue() {
        return loadValue;
    }

    public void setLoadValue(double loadValue) {
        this.loadValue = loadValue;
    }

    public double getPitchBase() {
        return pitchBase;
    }

    public void setPitchBase(double pitchBase) {
        this.pitchBase = pitchBase;
    }

    public int getLowTriggerThreshold() {
        return lowTriggerThreshold;
    }

    public void setTriggerThreshold(int lowTriggerThreshold, int highTriggerThreshold) {
        this.lowTriggerThreshold = lowTriggerThreshold;
        this.highTriggerThreshold = highTriggerThreshold;
        calculateTargetAmplitude();
        updateStatus();
    }

    public void setAmplitudeSetting(int amplitudeSetting) {
        this.amplitudeSetting = Math.min(100, Math.max(0, amplitudeSetting));
        calculateTargetAmplitude();
        updateStatus();
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        calculateTargetAmplitude();
        updateStatus();
    }

    public void onNotifyTriggerLevel(int triggerLevel) {
        this.triggerLevel = triggerLevel;
        calculateTargetAmplitude();
        updateStatus();
    }

    public void onNotifyAmplitudeAdjust() {
        if (currentAmplitude != targetAmplitude) {
            long ampDelta;
//            if (targetAmplitude > currentAmplitude)
//                ampDelta = Math.max(1, Math.round((targetAmplitude - currentAmplitude) * AMP_ADJUST_GRANULARITY));
//            else
//                ampDelta = Math.min(-1, Math.round((targetAmplitude - currentAmplitude) * AMP_ADJUST_GRANULARITY));
            if (targetAmplitude > currentAmplitude)
                ampDelta = Math.min(4, targetAmplitude - currentAmplitude);     // todo fix constants
            else
                ampDelta = Math.max(-4, targetAmplitude - currentAmplitude);

            if (logger.isLoggable(Level.FINE)) {
                logger.finer("Amplitude adjust " + currentAmplitude + "+" + ampDelta + "=" + currentAmplitude + ampDelta + "->" + targetAmplitude);
            }
            currentAmplitude += ampDelta;
            double ampDouble = currentAmplitude;
            ampoutL.inputB.set(ampDouble / 100);
            ampoutR.inputB.set(ampDouble / 100);
        }
        updateStatus();
    }

    protected boolean isInTriggerRange() {
        return (lowTriggerThreshold <= triggerLevel && triggerLevel <= highTriggerThreshold);
    }

    public boolean isMuted() {
        return muted;
    }

    protected void calculateTargetAmplitude() {
        if (isMuted()) {
            targetAmplitude = 0;
        } else if (!isInTriggerRange()) {
            targetAmplitude = 0;
        } else {   // in trigger range and not muted
            targetAmplitude = amplitudeSetting;
        }
    }

    public void setStatusListener(StatusListener listener) {
        this.statusListener = listener;
        updateStatus();
    }

    protected void updateStatus() {
        if (statusListener != null) {
            String status;
            if (isMuted()) {
                status = "mute " + currentAmplitude + " -> 0";
            } else if (!isInTriggerRange()) {
                status = "sqlc " + currentAmplitude + " -> 0";
            } else {   // in trigger range and not muted
                status = "LIVE " + currentAmplitude + " -> " + targetAmplitude;
            }
            statusListener.notifyStatus(status);
        }
    }
}
