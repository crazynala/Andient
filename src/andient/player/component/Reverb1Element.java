package andient.player.component;

import com.softsynth.jsyn.*;

/**
 * Reverberation
 * This simple reverb uses one pole low pass filters in the feedback
 * loop of several delay lines.
 * <p/>
 * andient.player.component.Reverb1 has a bus style input so multiple sources can be easily mixed.
 *
 * @author (C) 1997 Phil Burk, SoftSynth.com, All Rights Reserved
 */


class Reverb1Element extends SynthCircuit {
    DelayUnit myDelay;
    Filter_1o1p myFilter;
    MultiplyAddUnit myFeedback;
    MultiplyAddUnit myMixer;

    /**
     * Input signal to be reverberated.
     */
    SynthInput input;
    /**
     * Signal to be mixed with this units output. Generally
     * the output of another jnissa.Reverb1Element.
     */
    SynthInput mix;
    /**
     * Scale the output of this units delay line.
     */
    SynthInput amplitude;
    /* Amount of this unit's delayed output that gets fed back into delay line. */
    SynthInput feedback;

    public Reverb1Element(double delayTime)
            throws SynthException {
        super();

        add(myDelay = new DelayUnit(delayTime));
        add(myFilter = new Filter_1o1p());
        add(myFeedback = new MultiplyAddUnit());
        add(myMixer = new MultiplyAddUnit());

        myDelay.output.connect(myMixer.inputA);
        myDelay.output.connect(myFilter.input);
        myFilter.output.connect(myFeedback.inputA);
        myFeedback.output.connect(myDelay.input);

        addPort(feedback = myFeedback.inputB, "Feedback");
        addPort(input = myFeedback.inputC, "Input");
        addPort(amplitude = myMixer.inputB, "Amplitude");
        addPort(mix = myMixer.inputC, "Mix");
        addPort(output = myMixer.output);

        feedback.set(-0.90);
    }
}

