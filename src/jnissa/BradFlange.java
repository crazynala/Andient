package jnissa;/*  basic flanger -- nothing fancy here
 *
 *  Brad Garton 12/2001
 *
*/

import java.util.*;
import java.awt.*;
import com.softsynth.jsyn.*;
import com.softsynth.jsyn.circuits.*;

public class BradFlange extends SynthCircuit
{
/*
 *  flinput = input
 *  feedback = feedback multiplier (< 1!)
 *  sweepfreq = flanger sweep rate
 *  depth = maximum delay length (< 0.1!)
 *
*/

	public SynthInput	flinput;
	public SynthInput	feedback;
	public SynthInput	sweepfreq;
	public SynthInput	depth;
	InterpolatingDelayUnit	fldelay;
	SineOscillator		flcontrol;
	AddUnit			flctrladder;
	MultiplyUnit		fldepthmult;
	MultiplyUnit		flfeedback;
	AddUnit			flfbadder;

	public BradFlange()
	{
		fldelay = new InterpolatingDelayUnit(0.1);
		add(fldelay);
		flcontrol = new SineOscillator();
		add(flcontrol);
		flctrladder = new AddUnit();
		add(flctrladder);
		fldepthmult = new MultiplyUnit();
		add(fldepthmult);
		flfeedback = new MultiplyUnit();
		add(flfeedback);
		flfbadder = new AddUnit();
		add(flfbadder);

		flinput = flfbadder.inputA;
		flfbadder.output.connect(fldelay.input);
		fldelay.output.connect(flfeedback.inputA);
		flfeedback.output.connect(flfbadder.inputB);

		flcontrol.amplitude.set(0.5);
		flcontrol.output.connect(flctrladder.inputA);
		flctrladder.inputB.set(0.5);
		flctrladder.output.connect(fldepthmult.inputA);
		fldepthmult.output.connect(fldelay.delay);

		feedback = flfeedback.inputB;
		sweepfreq = flcontrol.frequency;
		depth = fldepthmult.inputB;
		output = fldelay.output;
	}
}
