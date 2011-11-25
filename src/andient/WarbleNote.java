package andient;/*  andient.WarbleNote -- makes a warbley note, of course
 *  used in the andient.jlooch app
 *
 *		Brad Garton	10/2001
 */

import com.softsynth.jsyn.*;

class Warble extends SynthCircuit {
    public SynthOutput outA, outB;
    PulseOscillator wOsc;
    SineOscillator wFlOsc;
    InterpolatingDelayUnit wFlange;
    DelayUnit wDelay;
    Filter_LowPass wFilt;
    EnvelopePlayer wEnvPlayer;
    SynthEnvelope wEnv;
    EnvelopePlayer wModPlayer;
    SynthEnvelope wMod;
    EnvelopePlayer wPchPlayer;
    SynthEnvelope wPch;
    MultiplyUnit wPchmult;
    AddUnit wFlangeadder;
    AddUnit wFlangeoffset;
    double[] wData =
            {
                    0.0, 0.0,
                    3.0, 1.0,
                    3.0, 1.0,
                    4.0, 0.0
            };
    double[] wModData =
            {
                    0.0, 1.0,
                    10.0, 0.7
            };
    double[] wPchData =
            {
                    0.0, 0.95,
                    0.1, 1.0,
                    0.7, 1.0,
                    0.2, 0.95
            };

    public Warble() {
        wOsc = new PulseOscillator();
        add(wOsc);

        wPchPlayer = new EnvelopePlayer();
        add(wPchPlayer);
        wPch = new SynthEnvelope(wPchData);
        wPchmult = new MultiplyUnit();
        add(wPchmult);

        wModPlayer = new EnvelopePlayer();
        add(wModPlayer);
        wMod = new SynthEnvelope(wModData);

        wFlOsc = new SineOscillator();
        add(wFlOsc);
        wFlangeoffset = new AddUnit();
        add(wFlangeoffset);
        wFlangeadder = new AddUnit();
        add(wFlangeadder);
        wFlange = new InterpolatingDelayUnit(0.01);
        add(wFlange);

        wFilt = new Filter_LowPass();
        add(wFilt);

        wEnvPlayer = new EnvelopePlayer();
        add(wEnvPlayer);
        wEnv = new SynthEnvelope(wData);

        wDelay = new DelayUnit(0.1);
        add(wDelay);

        wModPlayer.output.connect(wOsc.width);
        wPchPlayer.output.connect(wPchmult.inputB);
        wPchmult.output.connect(wOsc.frequency);

        wFlOsc.frequency.set(0.1);
        wFlOsc.amplitude.set(0.005);
        wFlOsc.output.connect(wFlangeoffset.inputA);
        wFlangeoffset.inputB.set(0.005);
        wFlangeoffset.output.connect(wFlange.delay);

        wOsc.output.connect(wFlange.input);
        wOsc.output.connect(wFlangeadder.inputA);
        wFlange.output.connect(wFlangeadder.inputB);

        wFlangeadder.output.connect(wFilt.input);
        wFilt.output.connect(wEnvPlayer.amplitude);

        wEnvPlayer.output.connect(wDelay.input);

        outA = wEnvPlayer.output;
        outB = wDelay.output;
    }

    public void go(int start, double dur, double freq, double amp, double cyctime)
    // start in ticks, dur in seconds, freq in Hz
    {
        wPchmult.inputA.set(start, freq);
        wOsc.amplitude.set(start, amp);

        wFilt.frequency.set(start, BradUtils.crandom(700.0, 4000.0));
        wFilt.Q.set(start, BradUtils.crandom(1.0, 4.0));
        wFilt.amplitude.set(start, 1.0);

        wModData[2] = dur;
        wMod.write(wModData);
        wModPlayer.envelopePort.clear(start);
        wModPlayer.envelopePort.queue(start, wMod);

        wPchData[2] = 0.1 * cyctime;
        wPchData[4] = 0.7 * cyctime;
        wPchData[6] = 0.2 * cyctime;
        wPch.write(wPchData);
        wPchPlayer.envelopePort.clear(start);
        wPchPlayer.envelopePort.queueLoop(start, wPch);

        wData[2] = 0.3 * dur;
        wData[4] = 0.3 * dur;
        wData[6] = 0.4 * dur;
        wEnv.write(wData);
        wEnvPlayer.envelopePort.clear(start);
        wEnvPlayer.envelopePort.queue(start, wEnv);
    }

}

class WarbleThread extends Thread {
    public double prob = 1.0;
    boolean go = true;
    Warble[] warbley = new Warble[3];
    LineOut noteOut;
    BusWriter[] outBusWriterA = new BusWriter[3];
    BusWriter[] outBusWriterB = new BusWriter[3];
    BusReader outBusReaderA, outBusReaderB;


    public WarbleThread()
            throws SynthException {
        int i;

        noteOut = new LineOut();
        for (i = 0; i < 3; i++) {
            warbley[i] = new Warble();
            outBusWriterA[i] = new BusWriter();
            outBusWriterB[i] = new BusWriter();
        }
        outBusReaderA = new BusReader();
        outBusReaderB = new BusReader();

        warbley[0].outA.connect(outBusWriterA[0].input);
        warbley[0].outB.connect(outBusWriterB[0].input);
        warbley[1].outB.connect(outBusWriterA[1].input);
        warbley[1].outA.connect(outBusWriterB[1].input);
        warbley[2].outA.connect(outBusWriterA[2].input);
        warbley[2].outB.connect(outBusWriterB[2].input);

        for (i = 0; i < 3; i++) {
            outBusWriterA[i].busOutput.connect(outBusReaderA.busInput);
            outBusWriterB[i].busOutput.connect(outBusReaderB.busInput);
        }

        outBusReaderA.output.connect(0, noteOut.input, 0);
        outBusReaderB.output.connect(0, noteOut.input, 1);
    }

    void stopSound() {
        try {
            int i;

            noteOut.stop();
            for (i = 0; i < 3; i++) {
                warbley[i].stop();
                outBusWriterA[i].stop();
                outBusWriterB[i].stop();
            }
            outBusReaderA.stop();
            outBusReaderB.stop();
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
        double freq;
        double dur = 10.0;
        double amp = 0.03;
        double cyc = 0.5;
        double[] pitches =
                {
                        6.05, 6.07, 6.10, 7.00, 7.05, 7.07, 7.10, 8.00, 8.05, 8.07, 8.10
                };

        try {
            int i;

            noteOut.start();
            for (i = 0; i < 3; i++) {
                warbley[i].start();
                outBusWriterA[i].start();
                outBusWriterB[i].start();
            }
            outBusReaderA.start();
            outBusReaderB.start();

            while (go == true) {
                while (Math.random() > prob) {
                    wait += (20 * 689);
                    Synth.sleepUntilTick(wait);
                }

                dur = BradUtils.crandom(10.0, 20.0);
                freq = BradUtils.chooseItem(pitches);
                freq = BradUtils.cpspch(freq);
                cyc = BradUtils.crandom(0.5, 0.7);
                warbley[0].go(wait, dur, freq, amp, cyc);


                if (Math.random() < 0.4) {
                    dur = BradUtils.crandom(0.0, 4.0);
                    wait += (int) (dur * 689.0);
                    Synth.sleepUntilTick(wait);

                    dur = BradUtils.crandom(10.0, 20.0);
                    freq = BradUtils.chooseItem(pitches);
                    freq = BradUtils.cpspch(freq);
                    cyc = BradUtils.crandom(0.5, 0.7);
                    warbley[1].go(wait, dur, freq, amp, cyc);
                }


                if (Math.random() < 0.4) {
                    dur = BradUtils.crandom(0.0, 4.0);
                    wait += (int) (dur * 689.0);
                    Synth.sleepUntilTick(wait);

                    dur = BradUtils.crandom(10.0, 20.0);
                    freq = BradUtils.chooseItem(pitches);
                    freq = BradUtils.cpspch(freq);
                    cyc = BradUtils.crandom(0.5, 0.7);
                    warbley[2].go(wait, dur, freq, amp, cyc);
                }

                wait += (int) (dur * 689.0);
                wait += BradUtils.crandom(6000.0, 10000.0);
                Synth.sleepUntilTick(wait);
            }
        } catch (SynthException e) {
            System.err.println(e);
        }
        stopSound();
    }
}


public class WarbleNote extends Thread {
    WarbleThread wnotes1;

    public void start() {
        try {
            wnotes1 = new WarbleThread();
            wnotes1.start();
        } catch (SynthException e) {
            SynthAlert.showError(e);
        }
    }

    public void stopSound() {
        try {
            wnotes1.stopSound();
        } catch (SynthException e) {
            SynthAlert.showError(e);
        }
    }

    public void setProb(double p) {
        wnotes1.prob = p;
    }
}
