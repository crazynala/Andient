package andient.player.component;

import com.softsynth.jsyn.*;

/**
 * User: dan
 * Date: 11/25/11
 */
public class Reverb1 extends SynthCircuit {
    final static int NUM_ELEMENTS = 6; // 6 recommended by Moore
    Reverb1Element delays[] = new Reverb1Element[NUM_ELEMENTS];
    MultiplyAddUnit myMixer;
    BusReader busIn;
    double times[] = {0.050, 0.056, 0.061, 0.068, 0.072, 0.078};

    public SynthBusInput busInput;
    public SynthInput dryGain;

    public Reverb1()
            throws SynthException {

        add(busIn = new BusReader());
        add(myMixer = new MultiplyAddUnit());

        for (int i = 0; i < delays.length; i++) {
            Reverb1Element delay = new Reverb1Element(times[i]);
            delays[i] = delay;
            add(delay);
            busIn.output.connect(delay.input);
            delay.amplitude.set(0.9 / NUM_ELEMENTS);
            if (i > 0) delays[i - 1].output.connect(delays[i].mix);
        }

        busIn.output.connect(myMixer.inputA);
        delays[NUM_ELEMENTS - 1].output.connect(myMixer.inputC);

        addPort(busInput = busIn.busInput);
        addPort(dryGain = myMixer.inputB, "dryGain");
        addPort(output = myMixer.output);
        dryGain.set(0.7);
        compile();
    }
}
