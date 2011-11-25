package andient;

/**
 * User: dan
 * Date: 11/22/11
 */
public interface Player {


    int getAmplitudeSetting();

    void setAmplitudeSetting(int amplitude);

    double getLoadValue();

    void setLoadValue(double loadValue);

    double getPitchBase();

    void setPitchBase(double pitchBase);

    int getLowTriggerThreshold();

    void start();

    void halt();

    void onNotifyTriggerLevel(int triggerLevel);

    void setStatusListener(StatusListener listener);

    void onNotifyAmplitudeAdjust();

    void setTriggerThreshold(int lowTriggerThreshold, int highTriggerThreshold);

    void setMuted(boolean muted);

    boolean isMuted();
}
