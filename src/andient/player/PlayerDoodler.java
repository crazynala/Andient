package andient.player;

import andient.player.component.BradUtils;
import andient.player.component.Dist;
import com.softsynth.jsyn.*;

public class PlayerDoodler extends BaseThreadedPlayer {
    boolean go = true;
    boolean paused = true;
    Dist dister;
    MultiplyUnit amp;

    DelayUnit delayL, delayR;
    MultiplyAddUnit feedback;

    public PlayerDoodler(int amplitudeSetting, double loadValue, double pitchBase, int lowTriggerThreshold, int highTriggerThreshold, int triggerLevel) throws SynthException {
        super(amplitudeSetting, loadValue, pitchBase, lowTriggerThreshold, highTriggerThreshold, triggerLevel);

        dister = new Dist();

        delayL = new DelayUnit(0.34);
        delayR = new DelayUnit(0.49);
        feedback = new MultiplyAddUnit();
        amp = new MultiplyUnit();

        dister.output.connect(amp.inputA);
        amp.output.connect(feedback.inputC);
        feedback.output.connect(delayL.input);
        delayL.output.connect(delayR.input);
        delayR.output.connect(feedback.inputA);
        feedback.inputB.set(0.5);

        ampoutL = new MultiplyUnit();
        ampoutR = new MultiplyUnit();
        delayL.output.connect(ampoutL.inputA);
        delayR.output.connect(ampoutR.inputA);
    }

    void stopSound(boolean noteOutStop) {
        try {
            // don't stop noteOut while app is running, clicks!
            if (noteOutStop == true) noteOut.stop();
            dister.stop();
            delayL.stop();
            delayR.stop();
            feedback.stop();
            amp.stop();
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
        int i, j;
        int wait;
        double beat = 0.07;
        int nbeat;
        int nnotes;
        int nriffs, nriffshalf;
        double[] pitches =
                {0.00, 0.02, 0.03, 0.04, 0.05, 0.07, 0.08, 0.09, 0.10, 0.12};
        double[] riffnotes = new double[4];
        double schedamp, ampincr, mygain;

        mygain = 0.00005;
        nbeat = (int) (beat * Synth.getTickRate());
        while (go == true) {
            wait = Synth.getTickCount();

            if (go == true) {
                paused = false;

                try {
                    dister.start();
                    noteOut.start();
                    delayL.start();
                    delayR.start();
                    feedback.start();
                    amp.start();
                    ampoutL.start();
                    ampoutR.start();

                    if (Math.random() > 0.5) {
                        nnotes = 3;
                    } else {
                        nnotes = 4;
                    }

                    // choose the riff notes
                    for (i = 0; i < nnotes; i++) {
                        riffnotes[i] = getPitchBase() + BradUtils.chooseItem(pitches);
                    }

                    schedamp = 0.0;
                    nriffs = (int) BradUtils.crandom(45.0, 80.0);
                    nriffshalf = nriffs / 2;
                    ampincr = mygain / (double) nriffshalf;
                    // schedule some riffs
                    dister.go(wait + 300, BradUtils.cpspch(riffnotes[0]), 2.0, 1.0, 2000.0, 1, 11.0, BradUtils.cpspch(riffnotes[0]), 0.05);
                    for (i = 0; i < nriffs; i++) {
                        for (j = 0; j < nnotes; j++) {
                            amp.inputB.set(wait + 300, schedamp);
                            dister.alter(wait + 300, BradUtils.cpspch(riffnotes[j]), 2.0, 1.0, 2000.0, 11.0, BradUtils.cpspch(riffnotes[j]), 0.05);
                            wait += nbeat;
                        }

                        if (i < nriffshalf) {
                            schedamp += ampincr;
                        } else {
                            schedamp -= ampincr;
                        }
                    }
                    dister.alter(wait + 300, BradUtils.cpspch(riffnotes[0]), 0.01, 0.01, 2000.0, 11.0, BradUtils.cpspch(riffnotes[0]), 0.0);

                } catch (SynthException e) {
                    System.err.println(e);
                }


                wait += 3500;
                Synth.sleepUntilTick(wait);
                stopSound(false);
                paused = true;

                // now pause for the next time around
                wait += (int) BradUtils.crandom(15.0, 30.0) * 689;
                Synth.sleepUntilTick(wait);
            }
        }
    }

    public void setPitchBase(double pchbase) {
        this.pitchBase = pchbase + 1.0;
    }
}

