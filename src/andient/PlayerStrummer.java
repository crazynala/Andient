package andient;

import com.softsynth.jsyn.*;
import jnissa.BradFlange;
import jnissa.Strum;

import java.util.logging.Logger;

class PlayerStrummer extends BaseThreadedPlayer {
    boolean go = true;
    boolean paused = true;
    Strum[] strummers = new Strum[2];
    BradFlange flanger;
    DelayUnit delayL, delayR;
    SynthMixer mixout;
    BusReader delbusinconnect;
    BusWriter[] delbusinput = new BusWriter[3];
    MultiplyUnit regen;

    private final static Logger logger = Logger.getLogger(PlayerStrummer.class.getName());

    protected PlayerStrummer(int amplitude, double loadValue, double pitchBase, int lowTriggerThreshold, int highTriggerThreshold, int triggerLevel) throws SynthException {
        super(amplitude, loadValue, pitchBase, lowTriggerThreshold, highTriggerThreshold, triggerLevel);

        int i;

        setLoadValue(loadValue);
        setPitchBase(pitchBase);

        mixout = new SynthMixer(6, 2);
        for (i = 0; i < 2; i++) {
            strummers[i] = new Strum();

            mixout.connectInput(2 * i, strummers[i].output, 0);
            mixout.connectInput(2 * i + 1, strummers[i].output, 0);

            delbusinput[i] = new BusWriter();
            strummers[i].output.connect(delbusinput[i].input);
        }
        delbusinput[2] = new BusWriter();

        delbusinconnect = new BusReader();
        delayL = new DelayUnit(0.17);
        delayR = new DelayUnit(0.131);
        regen = new MultiplyUnit();

        flanger = new BradFlange();
        flanger.feedback.set(0.6);
        flanger.sweepfreq.set(0.2);
        flanger.depth.set(0.008);

        for (i = 0; i < 3; i++) {
            delbusinput[i].busOutput.connect(delbusinconnect.busInput);
        }

        delbusinconnect.output.connect(delayL.input);
        delayL.output.connect(delayR.input);
        delayR.output.connect(flanger.flinput);
        flanger.output.connect(regen.inputA);
        regen.inputB.set(0.4);
        regen.output.connect(delbusinput[2].input);

        mixout.connectInput(4, delayL.output, 0);
        mixout.connectInput(5, delayR.output, 0);
        mixout.setGain(4, 0, 0.6);
        mixout.setGain(5, 1, 0.6);

        mixout.connectOutput(0, ampoutL.inputA, 0);
        mixout.connectOutput(1, ampoutR.inputA, 0);
    }


    public void setPitchBase(double pitchBase) {
        this.pitchBase = pitchBase - 1.0;
    }

    void stopSound(boolean noteOutStop) {
        int i;

        try {
            // don't stop noteOut while app is running, clicks!
            if (noteOutStop == true) noteOut.stop();

            for (i = 0; i < 2; i++) {
                strummers[i].stop();
            }
            flanger.stop();
            delayL.stop();
            delayR.stop();
            mixout.stop();
            delbusinconnect.stop();
            for (i = 0; i < 3; i++) {
                delbusinput[i].stop();
            }
            regen.stop();
            ampoutL.stop();
            ampoutR.stop();
        } catch (SynthException e) {
            System.err.println(e);
        }
    }

    public void halt() {
        go = false;   // tody what is this for?
        stopSound(true);
        interrupt();
    }

    public void run() {
        int i, j;
        int wait;
        double beat = 0.14;
        int ibeat;
        double squish, squincr;
        int nscheds;
        double pch;
        double panval;
        double[] pitches =
                {
                        0.00, 0.02, 0.03, 0.04, 0.05, 0.07, 0.10,
                        0.12, 0.14, 0.15, 0.16, 0.17, 0.19, 0.22, 0.24
                };
        double[] enterpitches =
                {
                        0.00, 0.05, 0.07, 0.10, 0.12, 0.14
                };


        ibeat = (int) ((double) Synth.getTickRate() * beat);
        while (go == true) {
            wait = Synth.getTickCount();
            while (Synth.getUsage() > loadValue) {
                wait += 900;
                Synth.sleepUntilTick(wait);
            }

            if (go == true) {
                paused = false;

                try {
                    // get the units started
                    for (i = 0; i < 2; i++) {
                        strummers[i].start();
                    }
                    flanger.start();
                    delayL.start();
                    delayR.start();
                    mixout.start();
                    delbusinconnect.start();
                    for (i = 0; i < 3; i++) {
                        delbusinput[i].start();
                    }
                    regen.start();
                    ampoutL.start();
                    ampoutR.start();
                    noteOut.start();


                    nscheds = (int) BradUtils.crandom(20.0, 40.0);
                    squish = 10.0;
                    squincr = 8.0 / (double) nscheds;
                    pch = pitchBase + BradUtils.chooseItem(enterpitches);
                    pch = BradUtils.cpspch(pch);
                    panval = BradUtils.crandom(0.0, 1.0);
                    for (j = 0; j < nscheds; j++) {
                        strummers[0].go(wait + 300, pch, 0.05, 0.01, 10000.0, (int) squish);
                        mixout.setGain(wait + 300, 0, 0, panval);
                        mixout.setGain(wait + 300, 1, 1, 1.0 - panval);
                        wait += ibeat;
                        strummers[1].go(wait + 300, pch, 1.0, 0.5, 10000.0, (int) squish);
                        mixout.setGain(wait + 300, 2, 0, panval);
                        mixout.setGain(wait + 300, 3, 1, 1.0 - panval);
                        wait += ibeat;
                        squish -= squincr;
                    }


                    nscheds = (int) BradUtils.crandom(40.0, 70.0);
                    for (j = 0; j < nscheds; j++) {
                        for (i = 0; i < 2; i++) {
                            pch = pitchBase + BradUtils.chooseItem(pitches);
                            strummers[i].go(wait + (ibeat * i) + 300, BradUtils.cpspch(pch), 1.0, 1.0, 10000.0, 2);
                            panval = BradUtils.crandom(0.0, 1.0);
                            mixout.setGain(wait + (ibeat * i) + 300, i * 2, 0, panval);
                            mixout.setGain(wait + (ibeat * i) + 300, i * 2 + 1, 1, 1.0 - panval);
                        }
                        wait += (2 * ibeat);
                    }

                    nscheds = (int) BradUtils.crandom(20.0, 40.0);
                    squish = 2.0;
                    squincr = 8.0 / (double) nscheds;
                    pch = pitchBase + BradUtils.chooseItem(enterpitches);
                    pch = BradUtils.cpspch(pch);
                    panval = BradUtils.crandom(0.0, 1.0);
                    for (j = 0; j < nscheds; j++) {
                        strummers[0].go(wait + 300, pch, 0.05, 0.01, 10000.0, (int) squish);
                        mixout.setGain(wait + 300, 0, 0, panval);
                        mixout.setGain(wait + 300, 1, 1, 1.0 - panval);
                        wait += ibeat;
                        strummers[1].go(wait + 300, pch, 1.0, 0.5, 10000.0, (int) squish);
                        mixout.setGain(wait + 300, 2, 0, panval);
                        mixout.setGain(wait + 300, 3, 1, 1.0 - panval);
                        wait += ibeat;
                        squish += squincr;
                    }
                } catch (SynthException e) {
                    System.err.println(e);
                }

                wait += 2500; // ring down the echoes
                Synth.sleepUntilTick(wait);
                stopSound(false);
                paused = true;

                // now pause for next cycle
                wait += (int) BradUtils.crandom(0.0, 7.0) * 689;
                Synth.sleepUntilTick(wait);
            }
        }
    }

}
