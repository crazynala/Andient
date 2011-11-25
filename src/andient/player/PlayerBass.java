package andient.player;

import andient.player.component.BradUtils;
import andient.player.component.Strum;
import com.softsynth.jsyn.*;

public class PlayerBass extends BaseThreadedPlayer {
    boolean go = true;
    boolean paused = true;
    Strum bass1, bass2;

    EnvelopePlayer benvplayer1, benvplayer2;
    SynthEnvelope benv;
    double[] evpdata =
            {
                    1.0, 1.0,
                    18.0, 1.0,
                    6.0, 0.0
            };

    Filter_LowPass bfilt1, bfilt2;

    public PlayerBass(int amplitudeSetting, double loadValue, double pitchBase, int lowTriggerThreshold, int highTriggerThreshold, int triggerLevel) throws SynthException {
        super(amplitudeSetting, loadValue, pitchBase, lowTriggerThreshold, highTriggerThreshold, triggerLevel);

        bass1 = new Strum();
        bass2 = new Strum();
        noteOut = new LineOut();

        benv = new SynthEnvelope(evpdata);
        benvplayer1 = new EnvelopePlayer();
        benvplayer2 = new EnvelopePlayer();

        bfilt1 = new Filter_LowPass();
        bfilt2 = new Filter_LowPass();

        bass1.output.connect(benvplayer1.amplitude);
        bass2.output.connect(benvplayer2.amplitude);

        benvplayer1.output.connect(bfilt1.input);
        benvplayer2.output.connect(bfilt2.input);
        bfilt1.amplitude.set(2.0);
        bfilt2.amplitude.set(2.0);
        bfilt1.frequency.set(2000.0);
        bfilt2.frequency.set(2000.0);
        bfilt1.Q.set(0.3);
        bfilt2.Q.set(0.3);

        bfilt1.output.connect(ampoutL.inputA);
        bfilt2.output.connect(ampoutR.inputA);
    }

    void stopSound(boolean noteOutStop) {
        int i;

        try {
            // don't stop noteOut while app is running, clicks!
            if (noteOutStop == true) noteOut.stop();

            bass1.stop();
            bass2.stop();
            benvplayer1.stop();
            benvplayer2.stop();
            bfilt1.stop();
            bfilt2.stop();
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
        int wait;
        double pch;
        double[] pitches =
                {
                        0.00, 0.05, 0.07, 0.10, 0.12
                };

        while (go == true) {
            wait = Synth.getTickCount();

            if (go == true) {
                paused = false;

                try {
                    bass1.start();
                    bass2.start();
                    benvplayer1.start();
                    benvplayer2.start();
                    bfilt1.start();
                    bfilt2.start();
                    ampoutL.start();
                    ampoutR.start();
                    noteOut.start();

                    pch = getPitchBase() + BradUtils.chooseItem(pitches);
                    pch = BradUtils.cpspch(pch);
                    benvplayer1.envelopePort.clear(wait + 300);
                    benvplayer1.envelopePort.queue(wait + 300, benv);
                    bass1.go(wait + 300, pch + (-0.003 * pch), 30.0, 50.1, 20000.0, 2);
                    benvplayer2.envelopePort.clear(wait + 300);
                    benvplayer2.envelopePort.queue(wait + 300, benv);
                    bass2.go(wait + 300, pch + (0.003 * pch), 30.0, 50.1, 20000.0, 2);

                    wait += (25 * 689);
                    Synth.sleepUntilTick(wait);
                } catch (SynthException e) {
                    System.err.println(e);
                }

                stopSound(false);
                paused = true;

                // pause for next cycle
                wait += (int) BradUtils.crandom(30.0, 70.0) * 689;
                Synth.sleepUntilTick(wait);
            }
        }
    }

    @Override
    public void setPitchBase(double pitchBase) {
        this.pitchBase = pitchBase - 3.0;
    }
}
