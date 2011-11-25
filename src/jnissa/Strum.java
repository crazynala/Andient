package jnissa;/*  jnissa.Strum -- basic plucked-string object (for use with JSyn)
 *
 *  based on Charlie Sullivan's version of the plucked-string algorithm
 *	(from the RTcmix version, START() )
 *
 *  uses jnissa.StrumSet.java to calculate params
 *
 *		Brad Garton	11/2001
 */

import com.softsynth.jsyn.*;

public class Strum extends SynthCircuit
{
	SynthSample		strBurst;
	short []		strBdata;
	SampleReader_16F1	strSamp;
	InterpolatingDelayUnit	strDelay;
	AddUnit			strDelayin;

	StrumSet		strSet;

	InterpolatingDelayUnit	strDA0;
	InterpolatingDelayUnit	strDA1;
	InterpolatingDelayUnit	strDA2;
	InterpolatingDelayUnit	strDA3;
	SynthMixer		strDAmix;

	Filter_HighShelf	strDCfilt;

	public Strum()
	{
		strSet = new StrumSet();

		strSamp = new SampleReader_16F1();
		add(strSamp);

		strDelay = new InterpolatingDelayUnit(0.1);
		add(strDelay);
		strDelayin = new AddUnit();
		add(strDelayin);

		// pluck filter
		strDA0 = new InterpolatingDelayUnit(1.0/44100.0);
		add(strDA0);
		strDA1 = new InterpolatingDelayUnit(2.0/44100.0);
		add(strDA1);
		strDA2 = new InterpolatingDelayUnit(3.0/44100.0);
		add(strDA2);
		strDA3 = new InterpolatingDelayUnit(4.0/44100.0);
		add(strDA3);
		strDAmix = new SynthMixer(4, 1);
		add(strDAmix);

		// filter out DC
		strDCfilt = new Filter_HighShelf();
		add(strDCfilt);

		strSamp.output.connect(strDelayin.inputA);
		strDelayin.output.connect(strDelay.input);

		// pluck filter
		strDelay.output.connect(strDA0.input);
		strDelay.output.connect(strDA1.input);
		strDelay.output.connect(strDA2.input);
		strDelay.output.connect(strDA3.input);
		strDAmix.connectInput(0, strDA0.output, 0);
		strDAmix.connectInput(1, strDA1.output, 0);
		strDAmix.connectInput(2, strDA2.output, 0);
		strDAmix.connectInput(3, strDA3.output, 0);
		strDAmix.connectOutput(0, strDCfilt.input, 0);

		strDA0.delay.set(1.0/44100.0);
		strDA1.delay.set(2.0/44100.0);
		strDA2.delay.set(3.0/44100.0);
		strDA3.delay.set(4.0/44100.0);


		// filter out DC -- this isn't really being done correctly!
		// but it seems to work; tends to shorten very high notes
		strDCfilt.frequency.set(20.0);
		strDCfilt.gain.set(1.0);
		strDCfilt.slope.set(1.0);
		strDCfilt.output.connect(strDelayin.inputB);

		output = strDelay.output;
	}

	public void go(int start, double freq, double tf0, double tfNy, double amp, int squish)
/*
 *  start == starting time in JSyn ticks
 *  freq == frequency in Hz
 *  tf0 == decay time of fundamental
 *  tfNy == decay time at Nyquist
 *  amp == amp (32768 max)
 *  squish == lowpass filter of original noise burst 0-sharp, 10-flabby
 *
*/
	{
		int i, dlength;

		strSet.sset(freq, tf0, tfNy);
		strSet.randfill(amp, squish);
		dlength = (int)strSet.delsamps;

		// not sure why "17" is the magic number for tuning here --
		// I suspect the 8-sample delay + 1 unit delay I added
		strDelay.delay.set(start, (double)(dlength-17)/44100.0);

		strBurst = new SynthSample(dlength);
		strBdata = new short[dlength];
		for (i = 0; i < dlength; i++)
		{
			strBdata[i] = (short)strSet.ninit[i];
		}
		strBurst.write(strBdata);
		strBdata = null;

		strDAmix.setGain(start, 0, 0, strSet.a0);
		strDAmix.setGain(start, 1, 0, strSet.a1);
		strDAmix.setGain(start, 2, 0, strSet.a2);
		strDAmix.setGain(start, 3, 0, strSet.a3);
		strDCfilt.amplitude.set(start, strSet.dca0);

		strSamp.samplePort.queue(start, strBurst, 0, (int)strSet.delsamps);
	}
}


