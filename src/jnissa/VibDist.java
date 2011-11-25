package jnissa;/* jnissa.VibDist -- the distortion/feedback jnissa.Strum instrument
 *	with randomly-varying vibrato!  yay!  (for use with JSyn)
 *
 *  based on Charlie Sullivan's version of the plucked-string algorithm
 *      (from the RTcmix version, VSTART1() )
 *
 *  uses jnissa.StrumSet.java to set the params
 *
 *              Brad Garton     12/2001
 */

import com.softsynth.jsyn.*;

public class VibDist extends SynthCircuit
{
	SynthSample		vdstBurst;
	short []		vdstBdata;
	SampleReader_16F1	vdstSamp;
	InterpolatingDelayUnit	vdstDelay;
	AddUnit			vdstDelayin;

	StrumSet		vdstSet;

	InterpolatingDelayUnit	vdstDA0;
	InterpolatingDelayUnit	vdstDA1;
	InterpolatingDelayUnit	vdstDA2;
	InterpolatingDelayUnit	vdstDA3;
	SynthMixer		vdstDAmix;

	Filter_HighShelf	vdstDCfilt;

	SynthTable		distTable;
	MultiplyUnit		vdstDistin;
	WaveShaper		vdstDist;
	double []		distTdata = new double[11];

	InterpolatingDelayUnit	vdstFBdelay;
	MultiplyUnit		vdstFBgain;
	AddUnit			vdstFBadd;

	SineOscillator		vibOsc;
	AddUnit			vibAdd;
	RedNoise		vibChange;
	AddUnit			vibChadd;

	public VibDist()
	{
		int i;

		vdstSet = new StrumSet();

		vdstSamp = new SampleReader_16F1();
		add(vdstSamp);

		vdstDelay = new InterpolatingDelayUnit(0.1);
		add(vdstDelay);
		vdstDelayin = new AddUnit();
		add(vdstDelayin);

		// pluck filter
		vdstDA0 = new InterpolatingDelayUnit(1.0/44100.0);
		add(vdstDA0);
		vdstDA1 = new InterpolatingDelayUnit(2.0/44100.0);
		add(vdstDA1);
		vdstDA2 = new InterpolatingDelayUnit(3.0/44100.0);
		add(vdstDA2);
		vdstDA3 = new InterpolatingDelayUnit(4.0/44100.0);
		add(vdstDA3);
		vdstDAmix = new SynthMixer(4, 1);
		add(vdstDAmix);

		// filter out DC
		vdstDCfilt = new Filter_HighShelf();
		add(vdstDCfilt);

		// feedback system
		vdstFBdelay = new InterpolatingDelayUnit(0.1);
		add(vdstFBdelay);
		vdstFBgain = new MultiplyUnit();
		add(vdstFBgain);
		vdstFBadd = new AddUnit();
		add(vdstFBadd);

		vdstSamp.output.connect(vdstFBadd.inputA);
		vdstDelayin.output.connect(vdstDelay.input);

		// pluck filter
		vdstDelay.output.connect(vdstDA0.input);
		vdstDelay.output.connect(vdstDA1.input);
		vdstDelay.output.connect(vdstDA2.input);
		vdstDelay.output.connect(vdstDA3.input);
		vdstDAmix.connectInput(0, vdstDA0.output, 0);
		vdstDAmix.connectInput(1, vdstDA1.output, 0);
		vdstDAmix.connectInput(2, vdstDA2.output, 0);
		vdstDAmix.connectInput(3, vdstDA3.output, 0);
		vdstDAmix.connectOutput(0, vdstDCfilt.input, 0);

		vdstDA0.delay.set(1.0/44100.0);
		vdstDA1.delay.set(2.0/44100.0);
		vdstDA2.delay.set(3.0/44100.0);
		vdstDA3.delay.set(4.0/44100.0);


		// filter out DC -- this isn't really being done correctly!
		vdstDCfilt.frequency.set(20.0);
		vdstDCfilt.gain.set(1.0);
		vdstDCfilt.slope.set(1.0);
		vdstDCfilt.output.connect(vdstDelayin.inputB);


		// distortion -- see note in jnissa.Dist.java
		for (i = 0; i < 3; i++)
		{
			distTdata[i] = 0.8;
		}
		distTdata[3] = 0.6;
		distTdata[4] = 0.2;
		distTdata[5] = 0.0;
		distTdata[6] = -0.2;
		distTdata[7] = -0.6;
		for (i = 8; i < 11; i++)
		{
			distTdata[i] =- 0.8;
		}

		distTable = new SynthTable(distTdata);
		vdstDist = new WaveShaper();
		add(vdstDist);
		vdstDist.tablePort.setTable(distTable);
		vdstDistin = new MultiplyUnit();
		add(vdstDistin);

		vdstDelay.output.connect(vdstDistin.inputA);
		vdstDistin.output.connect(vdstDist.input);

		// feedback system
		vdstDist.output.connect(vdstFBgain.inputA);
		vdstFBgain.output.connect(vdstFBdelay.input);
		vdstFBdelay.output.connect(vdstFBadd.inputB);
		vdstFBadd.output.connect(vdstDelayin.inputA);

		// vibrato
		vibOsc = new SineOscillator();
		add(vibOsc);
		vibAdd = new AddUnit();
		add(vibAdd);
		vibChange = new RedNoise();
		add(vibChange);
		vibChadd = new AddUnit();
		add(vibChadd);

		vibChange.output.connect(vibChadd.inputA);
		vibChadd.output.connect(vibOsc.frequency);
		vibOsc.output.connect(vibAdd.inputA);
		vibAdd.output.connect(vdstDelay.delay);
	
		output = vdstDist.output;
	}

	public void go(int start, double freq, double tf0, double tfNy, double amp, int squish, double dgain, double fbfreq, double fbgain, double vibamt, double vfreqlo, double vfreqhi)
	{
/*
 *  start == starting time in JSyn ticks
 *  freq == frequency in Hz
 *  tf0 == decay time of fundamental
 *  tfNy == decay time at Nyquist
 *  amp == amp (32768 max)
 *  squish == lowpass filter of original noise burst 0-sharp, 10-flabby
 *  dgain == distortion gain (1.0 gives almost no dist, 11.0 is Spinal Tap)
 *  fbfreq == sets the delay length of the feedback loop (calculated from Hz)
 *  fbgain == feedback multiplier -- low values work well (0.005),
 *      high values force quick distortion sound
 *  vibamt == mulitplier of basic freq to give you the vibrato "spread" --
 *	e.g. 0.01 will give you 1 1% variation around the center freq
 *  vfreqlo == low vibrato rate
 *  vfreqhi == hi vibrato rate
 *
*/

		int i, dlength;

		vdstSet.sset(freq, tf0, tfNy);
		vdstSet.randfill(amp, squish);
		dlength = (int)vdstSet.delsamps;

		vibChadd.inputB.set(start, (vfreqlo+vfreqhi)/2.0);
		vibChange.frequency.set(start, vfreqlo);
		vibChange.amplitude.set(start, (vfreqhi-vfreqlo)/2.0);
		vibOsc.amplitude.set(start, 1.0/freq - 1.0/(freq + freq*vibamt));
		vibAdd.inputB.set(start, (double)(dlength-17)/44100.0);

		vdstBurst = new SynthSample(dlength);
		vdstBdata = new short[dlength];
		for (i = 0; i < dlength; i++)
		{
			vdstBdata[i] = (short)vdstSet.ninit[i];
		}
		vdstBurst.write(vdstBdata);
		vdstBdata = null;

		vdstDAmix.setGain(start, 0, 0, vdstSet.a0);
		vdstDAmix.setGain(start, 1, 0, vdstSet.a1);
		vdstDAmix.setGain(start, 2, 0, vdstSet.a2);
		vdstDAmix.setGain(start, 3, 0, vdstSet.a3);
		vdstDCfilt.amplitude.set(start, vdstSet.dca0);

		vdstDistin.inputB.set(start, dgain/amp);
		vdstDist.amplitude.set(start, amp);

		vdstFBdelay.delay.set(start,fbfreq/44100.0);
		vdstFBgain.inputB.set(start,fbgain);

		vdstSamp.samplePort.queue(start, vdstBurst, 0, (int)vdstSet.delsamps);
	}

	public void alter(int start, double freq, double tf0, double tfNy, double amp, double dgain, double fbfreq, double fbgain, double vibamt, double vfreqlo, double vfreqhi)
	{
// alter params without replucking
// see listing of params under the go() method

		int i, dlength;

		vdstSet.sset(freq, tf0, tfNy);
		dlength = (int)vdstSet.delsamps;

		vibChadd.inputB.set(start, (vfreqlo+vfreqhi)/2.0);
		vibChange.frequency.set(start, vfreqlo);
		vibChange.amplitude.set(start, (vfreqhi-vfreqlo)/2.0);
		vibOsc.amplitude.set(start, 1.0/freq - 1.0/(freq + freq*vibamt));
		vibAdd.inputB.set(start, (double)(dlength-17)/44100.0);

		vdstDAmix.setGain(start, 0, 0, vdstSet.a0);
		vdstDAmix.setGain(start, 1, 0, vdstSet.a1);
		vdstDAmix.setGain(start, 2, 0, vdstSet.a2);
		vdstDAmix.setGain(start, 3, 0, vdstSet.a3);
		vdstDCfilt.amplitude.set(start, vdstSet.dca0);

		vdstDistin.inputB.set(start, dgain/amp);
		vdstDist.amplitude.set(start, amp);

		vdstFBdelay.delay.set(start, 1.0/fbfreq);
		vdstFBgain.inputB.set(start, fbgain);
	}
}
