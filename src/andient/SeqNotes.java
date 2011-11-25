package andient;/* andient.SeqNotes -- sequencer-like thing, shades of the olden days!
 * part of the andient.jlooch app
 * 
 *		Brad Garton	10/2001
 */

import andient.player.component.BradUtils;
import com.softsynth.jsyn.*;

import java.util.logging.Logger;

class DoPulse extends SynthCircuit {
    public PulseOscillator pOsc;
    public SineOscillator pMod;
    public Filter_BandPass pFilt;
    public LinearLag pLag;
    EnvelopePlayer pEnvPlayer;
    SynthEnvelope pEnv;

    double[] peData =
            {
                    0.0, 0.0,
                    0.1, 1.0,
                    0.8, 1.0,
                    0.1, 0.0
            };

    public DoPulse() {
        pOsc = new PulseOscillator();
        add(pOsc);
        pMod = new SineOscillator();
        add(pMod);
        pMod.amplitude.set(0.7);
        pMod.output.connect(pOsc.width);

        pLag = new LinearLag();
        pLag.output.connect(pOsc.frequency);
        add(pLag);

        pEnvPlayer = new EnvelopePlayer();
        add(pEnvPlayer);
        pEnv = new SynthEnvelope(peData);

        pFilt = new Filter_BandPass();
        add(pFilt);
        pFilt.amplitude.set(1.0);

        pOsc.output.connect(pFilt.input);
        pFilt.output.connect(pEnvPlayer.amplitude);

        output = pEnvPlayer.output;
    }


    public void go(int start, double dur, double freq, double amp, double filtfreq, double filtband)
    // start in ticks, dur in seconds, freq in Hz
    {
        pLag.time.set(0.03);
        pLag.input.set(start, freq);
        pOsc.amplitude.set(start, amp);
        pFilt.frequency.set(start, filtfreq);
        pFilt.Q.set(start, filtband);
        peData[2] = 0.1 * dur;
        peData[4] = 0.8 * dur;
        peData[6] = 0.1 * dur;
        pEnv.write(peData);
        pEnvPlayer.envelopePort.clear(start);
        pEnvPlayer.envelopePort.queue(start, pEnv);
    }

}

class SeqThread extends Thread {
    private final static Logger logger = Logger.getLogger(SeqThread.class.getName());

    int threadNum;
    public double prob = 1.0;
    boolean go = true;
    DoPulse pulsey;
    LineOut noteOut;
    DelayUnit noteDelay;
    BradVerb1 bverb;
    MultiplyUnit bverbsend, directmult;
    AddUnit addoutA, addoutB;
    AddUnit addverbA, addverbB;
    PanUnit panverbA, panverbB;

    public SeqThread()
            throws SynthException {
        int i;

        noteOut = new LineOut();
        noteDelay = new DelayUnit(0.121);
        bverb = new BradVerb1();
        bverbsend = new MultiplyUnit();
        pulsey = new DoPulse();
        addoutA = new AddUnit();
        addoutB = new AddUnit();
        directmult = new MultiplyUnit();
        addverbA = new AddUnit();
        addverbB = new AddUnit();
        panverbA = new PanUnit();
        panverbB = new PanUnit();

        bverb.bv1FeedBack.set(0.9);

        pulsey.output.connect(bverbsend.inputA);
        bverbsend.inputB.set(2.0);
        bverbsend.output.connect(bverb.bv1In);

        pulsey.output.connect(directmult.inputA);
        directmult.inputB.set(0.25);
        directmult.output.connect(addoutA.inputA);

        bverb.bv1OutA.connect(panverbA.input);
        panverbA.output.connect(0, addverbA.inputA, 0);
        panverbA.output.connect(1, addverbB.inputA, 0);
        addverbA.output.connect(addoutA.inputB);


        directmult.output.connect(noteDelay.input);
        noteDelay.output.connect(addoutB.inputA);

        bverb.bv1OutB.connect(panverbB.input);
        panverbB.output.connect(0, addverbA.inputB, 0);
        panverbB.output.connect(1, addverbB.inputB, 0);
        addverbB.output.connect(addoutB.inputB);

        addoutA.output.connect(0, noteOut.input, 0);
        addoutB.output.connect(0, noteOut.input, 1);
    }

    void stopSound() {
        try {
            noteOut.stop();
            noteDelay.stop();
            bverb.stop();
            bverbsend.stop();
            addoutA.stop();
            addoutB.stop();
            pulsey.stop();
            directmult.stop();
            panverbA.stop();
            panverbB.stop();
            addverbA.stop();
            addverbB.stop();
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
        double[] freqs = new double[7];
        double dur = 0.12;
        double amp, ampadder;
        double ffreq, fband, ffreqrange, ffreqadder;
        int i, j;
        int NNOTES, NSEQS, halfNSEQS;
        double oldnote;
        double[] pitches =
                {
                        9.00, 9.03, 9.05, 9.07, 9.10, 10.00, 10.03, 10.05,
                        10.07, 10.10, 11.00, 11.03, 11.05
                };

        try {
            pulsey.start();
            noteOut.start();
            noteDelay.start();
            bverb.start();
            bverbsend.start();
            addoutA.start();
            addoutB.start();
            directmult.start();
            panverbA.start();
            panverbB.start();
            addverbA.start();
            addverbB.start();

            pulsey.pMod.frequency.set(0.5);

            int flipper = 0;
            while (go == true) {
                while (Math.random() > prob) {
                    wait += (15 * 689);
                    logger.fine("[" + threadNum + "] waiting " + wait);
                    Synth.sleepUntilTick(wait);
                }

                oldnote = 0.0;
                NNOTES = (int) BradUtils.crandom(4.0, 7.0);
                for (i = 0; i < NNOTES; i++) {
                    do {
                        freqs[i] = BradUtils.chooseItem(pitches);
                    } while (freqs[i] == oldnote);

                    oldnote = freqs[i];
                }

                if (flipper++ == 0) {
                    panverbA.pan.set(1.0);
                    panverbB.pan.set(-1.0);
                } else {
                    panverbA.pan.set(-1.0);
                    panverbB.pan.set(1.0);
                    flipper = 0;
                }

                NSEQS = 20;
                halfNSEQS = NSEQS / 2;
                amp = 0.0;
                ampadder = 0.4 / (double) halfNSEQS;
                ffreq = BradUtils.crandom(100.0, 700.0);
                ffreqrange = BradUtils.crandom(2000.0, 7000.0) - ffreq;
                ffreqadder = ffreqrange / (double) halfNSEQS;
                fband = BradUtils.crandom(1.0, 10.0);
                for (i = 0; i < NSEQS; i++) {
                    for (j = 0; j < NNOTES; j++) {
                        pulsey.go(wait, dur, BradUtils.cpspch(freqs[j]), amp, ffreq, fband);
                        wait += (int) (dur * 689.0);
                    }

                    if (Math.random() < 0.5) {
                        for (j = NNOTES - 1; j >= 0; j--) {
                            pulsey.go(wait, dur, BradUtils.cpspch(freqs[j]), amp, ffreq, fband);
                            wait += (int) (dur * 689.0);
                        }
                    }

                    if (i < halfNSEQS) {
                        amp += ampadder;
                        ffreq += ffreqadder;
                    } else {
                        amp -= ampadder;
                        ffreq -= ffreqadder;
                    }
                }
                wait += (int) BradUtils.crandom(5000.0, 9000.0);
                Synth.sleepUntilTick(wait);
            }
        } catch (SynthException e) {
            System.err.println(e);
        }
        stopSound();
    }
}


public class SeqNotes extends Thread {
    public final static int NUM_SEQ_THREADS = 5;
    private SeqThread[] notes = new SeqThread[NUM_SEQ_THREADS];

    public void start() {
        try {
            for (int i = 0; i < notes.length; i++) {
                notes[i] = new SeqThread();
                notes[i].threadNum = i;
                notes[i].start();

            }
        } catch (SynthException e) {
            SynthAlert.showError(e);
        }
    }


    public void stopSound() {
        try {
            for (int i = 0; i < notes.length; i++) {
                notes[i].stopSound();
            }
        } catch (SynthException e) {
            SynthAlert.showError(e);
        }
    }

    public void setProb(double p) {
        for (int i = 0; i < notes.length; i++) {
            notes[i].prob = p;
        }
    }
}
