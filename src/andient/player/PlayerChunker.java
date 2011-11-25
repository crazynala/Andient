package andient.player;

import andient.player.component.BradFlange;
import andient.player.component.BradUtils;
import andient.player.component.Dist;
import com.softsynth.jsyn.*;

public class PlayerChunker extends BaseThreadedPlayer {
    boolean go = true;
    boolean paused = true;
    Dist[] chunkers = new Dist[3];
    SynthMixer chmix;
    Filter_BandPass chfilt;
    DelayUnit chdelay;
    BradFlange chflanger;

    public PlayerChunker(int amplitudeSetting, double loadValue, double pitchBase, int lowTriggerThreshold, int highTriggerThreshold, int triggerLevel) throws SynthException {
        super(amplitudeSetting, loadValue, pitchBase, lowTriggerThreshold, highTriggerThreshold, triggerLevel);
        {
            int i;
            chmix = new SynthMixer(3, 1);
            for (i = 0; i < 3; i++) {
                chunkers[i] = new Dist();
                chmix.connectInput(i, chunkers[i].output, 0);
                chmix.setGain(i, 0, 0.5);
            }

            chdelay = new DelayUnit(0.009);
            chfilt = new Filter_BandPass();
            chfilt.Q.set(2.0);
            chfilt.frequency.set(2500.0);
            chfilt.amplitude.set(0.02);

            chflanger = new BradFlange();
            chflanger.feedback.set(0.7);
            chflanger.sweepfreq.set(0.5);
            chflanger.depth.set(0.01);

            chmix.connectOutput(0, chfilt.input, 0);
            chfilt.output.connect(chflanger.flinput);
            chflanger.output.connect(chdelay.input);

            chflanger.output.connect(ampoutL.inputA);
            chdelay.output.connect(ampoutR.inputA);
        }
    }


    void stopSound(boolean noteOutStop) {
        try {
            int i;

            // don't stop noteOut while app is running, clicks!
            if (noteOutStop == true) noteOut.stop();

            for (i = 0; i < 3; i++) {
                chunkers[i].stop();
            }
            chmix.stop();
            chfilt.stop();
            chdelay.stop();
            chflanger.stop();
            ampoutL.stop();
            ampoutR.stop();
        } catch (SynthException e) {
            System.err.println(e);
        }
    }

    public void halt() {
        go = false;
        stopSound(true);
        interrupt();
    }

    public void run() {
        int i, j, k;
        int wait;
        int nscheds;
        int beatrest;
        double beat = 0.56;
        double mygain = 0.2;
        double schedamp, schedincr;
        int nbeat;

        nbeat = (int) (beat * Synth.getTickRate());
        while (go == true) {
            wait = Synth.getTickCount();

            if (go == true) {
                paused = false;

                try {
                    for (i = 0; i < 3; i++) {
                        chunkers[i].start();
                    }
                    chmix.start();
                    chfilt.start();
                    chdelay.start();
                    chflanger.start();
                    ampoutL.start();
                    ampoutR.start();
                    noteOut.start();

                    // choose a new filter center freq
                    chfilt.frequency.set(BradUtils.crandom(1500.0, 3500.0));

                    nscheds = (int) BradUtils.crandom(3.0, 6.0);
                    beatrest = (int) BradUtils.crandom(1.0, 4.0);

                    schedamp = 0.0;
                    schedincr = mygain / (double) (nscheds + 1);

                    for (k = 0; k < nscheds; k++) {
                        schedamp += schedincr;
                        for (j = 0; j < 3; j++) // set amp
                            chmix.setGain(wait, j, 0, schedamp);

                        // schedule 4 at a time
                        for (i = 0; i < 4; i++) {
                            // 3 in each chunk
                            for (j = 0; j < 3; j++) {
                                chunkers[j].go(wait + 300, BradUtils.crandom(50.0, 150.0), 2.0, 1.0, 20000.0, 1, 51.0, 100.0, 0.0007);
                            }
                            wait += nbeat;
                            for (j = 0; j < 3; j++) {
                                chunkers[j].go(wait + 300, BradUtils.crandom(50.0, 200.0), 0.01, 0.001, 20000.0, 1, 11.0, 100.0, 0.0);
                            }
                            wait += (nbeat * beatrest);
                        }
                    }

                    wait += 1000; // wait to stop
                    Synth.sleepUntilTick(wait);
                    stopSound(false);
                    paused = true;

                    // pause for next cycle
                    wait += (int) BradUtils.crandom(40.0, 70.0) * 689;
                    Synth.sleepUntilTick(wait);
                } catch (SynthException e) {
                    System.err.println(e);
                }
            }
        }
    }

}

