package jnissa;/* jnissa.Dist -- the distortion/feedback jnissa.Strum instrument (for use with JSyn)
 *
 *  based on Charlie Sullivan's version of the plucked-string algorithm
 *	(from the RTcmix version, START1() )
 *
 *  uses jnissa.StrumSet.java to set the params
 *
 *		Brad Garton	12/2001
 */

import com.softsynth.jsyn.*;

class Dist extends SynthCircuit
{
	SynthSample		dstBurst;
	short []		dstBdata;
	SampleReader_16F1	dstSamp;
	InterpolatingDelayUnit	dstDelay;
	AddUnit			dstDelayin;

	StrumSet		dstSet;

	InterpolatingDelayUnit	dstDA0;
	InterpolatingDelayUnit	dstDA1;
	InterpolatingDelayUnit	dstDA2;
	InterpolatingDelayUnit	dstDA3;
	SynthMixer		dstDAmix;

	Filter_HighShelf	dstDCfilt;

	SynthTable		distTable;
	MultiplyUnit		dstDistin;
	WaveShaper		dstDist;
	double []		distTdata = new double[11];

	InterpolatingDelayUnit	dstFBdelay;
	MultiplyUnit		dstFBgain;
	AddUnit			dstFBadd;

	public Dist()
	{
		int i;

		dstSet = new StrumSet();

		dstSamp = new SampleReader_16F1();
		add(dstSamp);

		dstDelay = new InterpolatingDelayUnit(0.1);
		add(dstDelay);
		dstDelayin = new AddUnit();
		add(dstDelayin);

		// pluck filter
		dstDA0 = new InterpolatingDelayUnit(1.0/44100.0);
		add(dstDA0);
		dstDA1 = new InterpolatingDelayUnit(2.0/44100.0);
		add(dstDA1);
		dstDA2 = new InterpolatingDelayUnit(3.0/44100.0);
		add(dstDA2);
		dstDA3 = new InterpolatingDelayUnit(4.0/44100.0);
		add(dstDA3);
		dstDAmix = new SynthMixer(4, 1);
		add(dstDAmix);

		// filter out DC
		dstDCfilt = new Filter_HighShelf();
		add(dstDCfilt);

		// feedback system
		dstFBdelay = new InterpolatingDelayUnit(0.1);
		add(dstFBdelay);
		dstFBgain = new MultiplyUnit();
		add(dstFBgain);
		dstFBadd = new AddUnit();
		add(dstFBadd);

		dstSamp.output.connect(dstFBadd.inputA);
		dstDelayin.output.connect(dstDelay.input);

		// pluck filter
		dstDelay.output.connect(dstDA0.input);
		dstDelay.output.connect(dstDA1.input);
		dstDelay.output.connect(dstDA2.input);
		dstDelay.output.connect(dstDA3.input);
		dstDAmix.connectInput(0, dstDA0.output, 0);
		dstDAmix.connectInput(1, dstDA1.output, 0);
		dstDAmix.connectInput(2, dstDA2.output, 0);
		dstDAmix.connectInput(3, dstDA3.output, 0);
		dstDAmix.connectOutput(0, dstDCfilt.input, 0);

		dstDA0.delay.set(1.0/44100.0);
		dstDA1.delay.set(2.0/44100.0);
		dstDA2.delay.set(3.0/44100.0);
		dstDA3.delay.set(4.0/44100.0);


		// filter out DC -- this isn't really being done correctly!
		dstDCfilt.frequency.set(20.0);
		dstDCfilt.gain.set(1.0);
		dstDCfilt.slope.set(1.0);
		dstDCfilt.output.connect(dstDelayin.inputB);


		// distortion
		// not a very good waveshape, but it works
		// the table seems "backwards" to me (pos v. neg)
		// but without the flip the signal goes to +/- 32768
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
		dstDist = new WaveShaper();
		add(dstDist);
		dstDist.tablePort.setTable(distTable);
		dstDistin = new MultiplyUnit();
		add(dstDistin);

		dstDelay.output.connect(dstDistin.inputA);
		dstDistin.output.connect(dstDist.input);

		// feedback system
		dstDist.output.connect(dstFBgain.inputA);
		dstFBgain.output.connect(dstFBdelay.input);
		dstFBdelay.output.connect(dstFBadd.inputB);
		dstFBadd.output.connect(dstDelayin.inputA);
	
		output = dstDist.output;
	}

	public void go(int start, double freq, double tf0, double tfNy, double amp, int squish, double dgain, double fbfreq, double fbgain)
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
 *	high values force quick distortion sound
 *
*/
		int i, dlength;

		dstSet.sset(freq, tf0, tfNy);
		dstSet.randfill(amp, squish);
		dlength = (int)dstSet.delsamps;

		// not sure why "17" is the magic number for tuning here --
		// I suspect the 8-sample delay + 1 unit delay I added
		dstDelay.delay.set(start, (double)(dlength-17)/44100.0);

		dstBurst = new SynthSample(dlength);
		dstBdata = new short[dlength];
		for (i = 0; i < dlength; i++)
		{
			dstBdata[i] = (short)dstSet.ninit[i];
		}
		dstBurst.write(dstBdata);
		dstBdata = null;

		dstDAmix.setGain(start, 0, 0, dstSet.a0);
		dstDAmix.setGain(start, 1, 0, dstSet.a1);
		dstDAmix.setGain(start, 2, 0, dstSet.a2);
		dstDAmix.setGain(start, 3, 0, dstSet.a3);
		dstDCfilt.amplitude.set(start, dstSet.dca0);

		dstDistin.inputB.set(start, dgain/amp);
		dstDist.amplitude.set(start, amp);

		dstFBdelay.delay.set(start,fbfreq/44100.0);
		dstFBgain.inputB.set(start,fbgain);

		dstSamp.samplePort.queue(start, dstBurst, 0, (int)dstSet.delsamps);
	}

	public void alter(int start, double freq, double tf0, double tfNy, double amp, double dgain, double fbfreq, double fbgain)
	{
// alter params without replucking
// see listing of params under the go() method

		int i, dlength;

		dstSet.sset(freq, tf0, tfNy);
		dlength = (int)dstSet.delsamps;

		dstDelay.delay.set(start, (double)(dlength-17)/44100.0);

		dstDAmix.setGain(start, 0, 0, dstSet.a0);
		dstDAmix.setGain(start, 1, 0, dstSet.a1);
		dstDAmix.setGain(start, 2, 0, dstSet.a2);
		dstDAmix.setGain(start, 3, 0, dstSet.a3);
		dstDCfilt.amplitude.set(start, dstSet.dca0);

		dstDistin.inputB.set(start, dgain/amp);
		dstDist.amplitude.set(start, amp);

		dstFBdelay.delay.set(start, 1.0/fbfreq);
		dstFBgain.inputB.set(start, fbgain);
	}
}
