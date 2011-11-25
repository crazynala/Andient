package jnissa;/*  DistNote -- generate some wailin' riffs (ha ha!) using jnissa.Dist.java
 *
 *		Brad Garton	12/2001
 */

import java.awt.*;

import com.softsynth.jsyn.*;

class DistThread extends Thread
{
	boolean		go = true;
	boolean		paused = true;
	Dist		dister;
	MultiplyUnit	amp;
	LineOut		noteOut;

	DelayUnit	delayL, delayR;
	MultiplyAddUnit feedback;
	MultiplyUnit	ampoutL, ampoutR;

	DistWindow	dstwind;
	Frame           bf; // for the graphics
	boolean		graphon;
	boolean		backflag;
	double		loadval;
	double		pitchbase;

	public DistThread(double ampval, boolean gr, boolean bg, Frame f, double lv, double pb)
	throws SynthException
	{
		bf = f;
		graphon = gr;
		backflag = bg;
		loadval = lv;
		pitchbase = pb + 1.0;

		dister = new Dist();

		delayL = new DelayUnit(0.34);
		delayR = new DelayUnit(0.49);
		feedback = new MultiplyAddUnit();
		amp = new MultiplyUnit();

		dister.output.connect(amp.inputA);
		amp.output.connect(feedback.inputC);
		feedback.output.connect(delayL.input);
		delayL.output.connect(delayR.input);
		delayR.output.connect(feedback.inputA);
		feedback.inputB.set(0.5);

		ampoutL = new MultiplyUnit();
		ampoutR = new MultiplyUnit();
		delayL.output.connect(ampoutL.inputA);
		delayR.output.connect(ampoutR.inputA);
		ampoutL.inputB.set(ampval);
		ampoutR.inputB.set(ampval);

		noteOut = new LineOut();
		ampoutL.output.connect(0, noteOut.input, 0);
		ampoutR.output.connect(0, noteOut.input, 1);
	}

	void stopSound(boolean noteOutStop)
	{
		try
		{
			// don't stop noteOut while app is running, clicks!
			if (noteOutStop == true) noteOut.stop();
			dister.stop();
			delayL.stop();
			delayR.stop();
			feedback.stop();
			amp.stop();
			ampoutL.stop();
			ampoutR.stop();
		} catch (SynthException e) {
			System.err.println(e);
		}
	}

	public void halt()
	{
		go = false;
		stopSound(true);
		if (dstwind != null)
		{
			dstwind.destroy();
			dstwind = null;
		}
		interrupt();
	}

	public void run()
	{
		int i, j;
		int wait;
		double beat = 0.07;
		int nbeat;
		int nnotes;
		int nriffs, nriffshalf;
		double [] pitches =
		{ 0.00, 0.02, 0.03, 0.04, 0.05, 0.07, 0.08, 0.09, 0.10, 0.12 };
		double [] riffnotes = new double[4];
		double schedamp, ampincr, mygain;

		mygain = 0.00005;
		nbeat = (int)(beat * Synth.getTickRate());
		while (go == true)
		{
			wait = Synth.getTickCount();
			while (Synth.getUsage() > loadval)
			{
				wait += 1100;
				Synth.sleepUntilTick(wait);
			}

			if (go == true)
			{
				if (graphon)
				{
					dstwind = new DistWindow(bf, backflag);
					dstwind.start();
				}
				paused = false;

				try
				{
					dister.start();
					noteOut.start();
					delayL.start();
					delayR.start();
					feedback.start();
					amp.start();
					ampoutL.start();
					ampoutR.start();

					if (Math.random() > 0.5)
					{
						nnotes = 3;
					} else {
						nnotes = 4;
					}

					// choose the riff notes
					for (i = 0; i < nnotes; i++)
					{
						riffnotes[i] = pitchbase + BradUtils.chooseItem(pitches);
					}

					schedamp = 0.0;
					nriffs = (int)BradUtils.crandom(45.0, 80.0);
					nriffshalf = nriffs/2;
					ampincr = mygain/(double)nriffshalf;
					// schedule some riffs
					dister.go(wait+300, BradUtils.cpspch(riffnotes[0]), 2.0, 1.0, 2000.0, 1, 11.0, BradUtils.cpspch(riffnotes[0]), 0.05);
					for (i = 0; i < nriffs; i++)
					{
						for (j = 0; j < nnotes; j++)
						{
							amp.inputB.set(wait+300, schedamp);
							dister.alter(wait+300, BradUtils.cpspch(riffnotes[j]), 2.0, 1.0, 2000.0, 11.0, BradUtils.cpspch(riffnotes[j]), 0.05);
							wait += nbeat;
						}

						if (i < nriffshalf)
						{
							schedamp += ampincr;
						} else {
							schedamp -= ampincr;
						}
					}
					dister.alter(wait+300, BradUtils.cpspch(riffnotes[0]), 0.01, 0.01, 2000.0, 11.0, BradUtils.cpspch(riffnotes[0]), 0.0);

				} catch (SynthException e) {
					System.err.println(e);
				}


				wait += 3500;
				Synth.sleepUntilTick(wait);
				stopSound(false);
				if (dstwind != null)
				{
					dstwind.destroy();
					dstwind = null;
				}
				paused = true;

				// now pause for the next time around
				wait +=  (int)BradUtils.crandom(15.0, 30.0) * 689;
				Synth.sleepUntilTick(wait);
			}
		}
	}

	public void setAmp(double aaa)
	{
		ampoutL.inputB.set(aaa);
		ampoutR.inputB.set(aaa);
	}

	public void setGraphics(boolean grstate)
	{
		graphon = grstate;

		if (!graphon)
		{
			if (dstwind != null) dstwind.destroy();
			dstwind = null;
		} else {
			if (go == true)
			{
				if ((dstwind == null) && (paused == false))
				{
					dstwind = new DistWindow(bf, backflag);
					dstwind.start();
				}
			}
		}
	}

	public void setBground(boolean bgflag)
	{
		backflag = bgflag;

		if (dstwind != null) dstwind.bground(bgflag);
	}

	public void setLoad(double lv)
	{
		loadval = lv;
	}
	public void setPitch(double pchbase)
	{
		pitchbase = pchbase + 1.0;
	}
}

