package andient;/*  really cheesy reverb done by cycling through some multitap
 *  delays, but what the heck -- it sounds ok in context
 *  used by the andient.SeqNotes in the andient.jlooch app
 *
 *  Brad Garton  10/2001
 */

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.circuits.MultiTapDelay;

class BradVerb1 extends SynthCircuit {
    public SynthInput bv1In;
    public SynthInput bv1FeedBack;
    public SynthOutput bv1OutA;
    public SynthOutput bv1OutB;
    MultiplyUnit bv1FeedMult;
    MultiTapDelay mDel1, mDel2;
    AddUnit loopAdder;
    Filter_LowPass bv1Filt1;
    InterpolatingDelayUnit bv1Flange;
    SineOscillator flCtrl;
    AddUnit flangeAdder;
    AddUnit ctrlAdder, flFBAdder;
    MultiplyUnit flFB;
    double[] mdels1 =
            {0.05, 0.191, 0.135, 0.49, 0.145, 0.289, 0.095, 0.7458};
    double[] mdels2 =
            {0.08, 0.29241, 0.17, 0.53, 0.24, 0.1943, 0.8289, 0.4321};
    double[] mgains1 =
            {0.05, -0.27, 0.02, 0.15, -0.01, -0.05, 0.14, -0.04};
    double[] mgains2 =
            {0.06, -0.18, 0.15, -0.29, 0.021, -0.12, -0.17, 0.05};


    public BradVerb1() {
        mDel1 = new MultiTapDelay(mdels1, mgains1);
        add(mDel1);
        mDel2 = new MultiTapDelay(mdels2, mgains2);
        add(mDel2);
        loopAdder = new AddUnit();
        add(loopAdder);
        bv1FeedMult = new MultiplyUnit();
        add(bv1FeedMult);
        bv1Filt1 = new Filter_LowPass();
        add(bv1Filt1);
        bv1Flange = new InterpolatingDelayUnit(0.05);
        add(bv1Flange);
        flCtrl = new SineOscillator();
        add(flCtrl);
        flangeAdder = new AddUnit();
        add(flangeAdder);
        ctrlAdder = new AddUnit();
        add(ctrlAdder);
        flFBAdder = new AddUnit();
        add(flFBAdder);
        flFB = new MultiplyUnit();
        add(flFB);

        bv1In = loopAdder.inputA;
        loopAdder.output.connect(mDel1.input);
        mDel1.output.connect(mDel2.input);
        mDel2.output.connect(bv1FeedMult.inputA);
        bv1FeedBack = bv1FeedMult.inputB;
        bv1FeedMult.output.connect(flFBAdder.inputA);
        bv1FeedMult.output.connect(flangeAdder.inputA);
        flFB.output.connect(flFBAdder.inputB);
        flFBAdder.output.connect(bv1Flange.input);

        bv1Flange.output.connect(flFB.inputA);
        bv1Flange.output.connect(flangeAdder.inputB);
        flFB.inputB.set(0.7);

        flangeAdder.output.connect(bv1Filt1.input);
        bv1Filt1.output.connect(loopAdder.inputB);

        flCtrl.output.connect(ctrlAdder.inputA);
        ctrlAdder.inputB.set(0.004);
        ctrlAdder.output.connect(bv1Flange.delay);
        flCtrl.amplitude.set(0.003);
        flCtrl.frequency.set(0.3);

        bv1Filt1.frequency.set(8000.0);
        bv1Filt1.amplitude.set(1.0);
        bv1Filt1.Q.set(1.0);

        bv1OutA = mDel1.output;
        bv1OutB = mDel2.output;
    }
}

