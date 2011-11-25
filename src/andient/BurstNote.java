package andient;/* andient.BurstNote -- blips of noise + swoopy windish sounds (randomly selected)
 * used in the andient.jlooch app
 *
 *		Brad Garton	10/2001
 */

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.circuits.MultiTapDelay;

class Burst extends SynthCircuit {
    WhiteNoise bNoise;
    Filter_BandPass bFilt;
    EnvelopePlayer bEnvPlayer;
    SynthEnvelope bEnv;
    MultiplyUnit bAmp;
    LinearLag bLag;
    double[] bData =
            {
                    0.0, 0.0,
                    0.1, 1.0,
                    0.9, 0.0
            };

    public Burst() {
        bNoise = new WhiteNoise();
        add(bNoise);

        bFilt = new Filter_BandPass();
        add(bFilt);

        bLag = new LinearLag();
        add(bLag);
        bLag.output.connect(bFilt.frequency);

        bEnvPlayer = new EnvelopePlayer();
        add(bEnvPlayer);
        bEnv = new SynthEnvelope(bData);

        bAmp = new MultiplyUnit();
        add(bAmp);

        bNoise.output.connect(bFilt.input);
        bFilt.output.connect(bEnvPlayer.amplitude);
        bEnvPlayer.output.connect(bAmp.inputA);

        output = bAmp.output;
    }

    public void go(int start, double dur, double filtfreq, double amp, double filtQ, double ltime)
    // start in ticks, dur in seconds, freq in Hz
    {
        bAmp.inputB.set(amp);
        bNoise.amplitude.set(start, 500.0);
        bFilt.amplitude.set(start, 1.0);
        bLag.time.set(ltime);
        bLag.input.set(start, filtfreq);
        bFilt.Q.set(start, filtQ);

        bData[2] = 0.2 * dur;
        bData[4] = 0.8 * dur;
        bEnv.write(bData);
        bEnvPlayer.envelopePort.clear(start);
        bEnvPlayer.envelopePort.queue(start, bEnv);
    }

}

class BurstThread extends Thread {
    public double prob = 1.0;
    boolean go = true;
    Burst burstey;
    MultiTapDelay bDel1, bDel2;
    double[] b1dels = {1.1, 1.17, 1.09};
    double[] b2dels = {1.04, 1.21, 1.115};
    double[] b1gains = {0.3, 0.2, 0.4};
    double[] b2gains = {0.4, 0.4, 0.1};
    AddUnit bDel1in, bDel2in;
    LineOut noteOut;

    public BurstThread()
            throws SynthException {
        noteOut = new LineOut();
        burstey = new Burst();

        bDel1 = new MultiTapDelay(b1dels, b1gains);
        bDel2 = new MultiTapDelay(b2dels, b2gains);
        bDel1in = new AddUnit();
        bDel2in = new AddUnit();

        burstey.output.connect(bDel1in.inputA);
        burstey.output.connect(bDel2in.inputA);
        bDel1.output.connect(bDel2in.inputB);
        bDel2.output.connect(bDel1in.inputB);

        bDel1in.output.connect(bDel1.input);
        bDel2in.output.connect(bDel2.input);

        bDel1.output.connect(0, noteOut.input, 0);
        bDel2.output.connect(0, noteOut.input, 1);
    }

    void stopSound() {
        try {
            noteOut.stop();
            bDel1.stop();
            bDel2.stop();
            bDel1in.stop();
            bDel2in.stop();
            burstey.stop();
        } catch (SynthException e) {
            System.err.println(e);
        }
    }

    public void halt() {
        go = false;
        interrupt();
    }

    public void run() {
        int wait = 0;
        int i, nmore, longflag;
        double freq, oldfreq;
        double dur, lag;
        double amp = 0.025;
        double Q = 100.0;
        double[] pitches =
                {
                        7.00, 7.05, 7.07, 7.10, 8.00, 8.05, 8.07, 8.10, 9.00, 9.05, 9.07
                };

        try {
            burstey.start();
            noteOut.start();
            bDel1.start();
            bDel2.start();
            bDel1in.start();
            bDel2in.start();

            while (go == true) {
                while (Math.random() > prob) {
                    wait += (20 * 689);
                    Synth.sleepUntilTick(wait);
                }

                freq = BradUtils.chooseItem(pitches);
                freq = BradUtils.cpspch(freq);
                if (Math.random() < 0.5) {
                    dur = BradUtils.crandom(1.5, 5.0);
                    lag = BradUtils.crandom(0.1, 1.5);
                    nmore = (int) BradUtils.crandom(0.0, 7.0);
                    longflag = 1;
                } else {
                    dur = 0.03;
                    lag = 0.01;
                    nmore = (int) BradUtils.crandom(0.0, 78.0);
                    longflag = 0;
                }


                burstey.go(wait, dur, freq, amp, Q, lag);

                for (i = 0; i < nmore; i++) {
                    oldfreq = freq;
                    do {
                        freq = BradUtils.chooseItem(pitches);
                    } while (oldfreq == freq);
                    if (longflag == 1) {
                        dur = BradUtils.crandom(1.5, 5.0);
                        lag = BradUtils.crandom(0.1, 1.5);
                    }
                    freq = BradUtils.cpspch(freq);
                    burstey.go(wait, dur, freq, amp, Q, lag);
                    wait += (int) (dur * 689.0);
                }


                wait += (int) BradUtils.crandom(4000.0, 10000.0);
                Synth.sleepUntilTick(wait);
            }
        } catch (SynthException e) {
            System.err.println(e);
        }
        stopSound();
    }
}


public class BurstNote extends Thread {
    BurstThread bnotes1;

    public void start() {
        try {
            bnotes1 = new BurstThread();
            bnotes1.start();
        } catch (SynthException e) {
            SynthAlert.showError(e);
        }
    }

    public void stopSound() {
        try {
            bnotes1.stopSound();
        } catch (SynthException e) {
            SynthAlert.showError(e);
        }
    }

    public void setProb(double p) {
        bnotes1.prob = p;
    }
}
