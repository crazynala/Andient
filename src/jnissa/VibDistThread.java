package jnissa;/*  jnissa.VibDistThread -- generate some fun feedback clusters using jnissa.VibDist.java
 *
 *              Brad Garton     12/2001
 *
 */

import java.awt.*;
import com.softsynth.jsyn.*;

class VibDistThread extends Thread
{
	boolean		go = true;
	boolean		paused = true;
	VibDist []	vibdisters = new VibDist[3];
	SynthMixer	mixout;
	LineOut		noteOut;

	Reverb1		rvb;
	BusWriter	rvbbus;
	DelayUnit	delayL,delayR;
	MultiplyAddUnit feedback;
	MultiplyUnit	ampoutL, ampoutR;

	VibDistWindow vdstwind;
	Frame           bf; // for the graphics
	boolean		graphon;
	boolean		backflag;
	double		loadval;
	double		pitchbase;

	public VibDistThread(double ampval, boolean gr, boolean bg, Frame f, double lv, double pb)
	throws SynthException
	{
		int i;

		bf = f;
		graphon = gr;
		backflag = bg;
		loadval = lv;
		pitchbase = pb;

		mixout = new SynthMixer(3, 1);
		for (i = 0; i < 3; i++)
		{
			vibdisters[i] = new VibDist();
			mixout.connectInput(i, vibdisters[i].output, 0);
		}

		noteOut = new LineOut();

		rvb = new Reverb1();
		rvb.dryGain.set(0.0);
		rvbbus = new BusWriter();
		rvbbus.busOutput.connect(rvb.busInput);
		mixout.connectOutput(0, rvbbus.input, 0);

		delayL = new DelayUnit(0.34);
		delayR = new DelayUnit(0.25);
		feedback = new MultiplyAddUnit();

		rvb.output.connect(feedback.inputC);
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

		ampoutL.output.connect(0, noteOut.input, 0);
		ampoutR.output.connect(0, noteOut.input, 1);
	}

	void stopSound(boolean noteOutStop)
	{
		int i;

		try
		{
			// don't stop noteOut while app is running, clicks!
			if (noteOutStop == true) noteOut.stop();

			for (i = 0; i < 3; i++)
			{
				vibdisters[i].stop();
			}
			mixout.stop();
			rvb.stop();
			rvbbus.stop();
			delayL.stop();
			delayR.stop();
			feedback.stop();
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
		if (vdstwind != null)
		{
			vdstwind.destroy();
			vdstwind = null;
		}
		interrupt();
	}

	public void run()
	{
		int i,j;
		int wait;
		int numtimes;
		double fbfreq;
		double fbpchbase;
		double mygain = 0.000007;
		double [] fbpitches = { 0.00, 0.05, 0.07, 0.10, 1.00, 1.02 };
		double [] pchadds = { 0.00, 0.02, 0.07 };



		while (go == true)
		{
			wait = Synth.getTickCount();
			while (Synth.getUsage() > loadval)
			{
				wait += 1000;
				Synth.sleepUntilTick(wait);
			}

			if (go == true)
			{
				if (graphon)
				{
					vdstwind = new VibDistWindow(bf, backflag);
					vdstwind.start();
				}
				paused = false;

				try
				{
					for (i = 0; i < 3; i++)
					{
						vibdisters[i].start();
					}
					mixout.start();
					rvb.start();
					rvbbus.start();
					delayL.start();
					delayR.start();
					feedback.start();
					ampoutL.start();
					ampoutR.start();
					noteOut.start();

					for (i = 0; i < 3; i++) // set the amp
						mixout.setGain(i, 0, mygain);

					fbpchbase = pitchbase;
					// start each cluster
					for (i = 0; i < 3; i++)
					{
						fbfreq = fbpchbase + BradUtils.chooseItem(fbpitches);
						fbfreq = BradUtils.cpspch(fbfreq);
						vibdisters[i].go(wait+300, BradUtils.cpspch(pitchbase+pchadds[i]), 2.0, 1.0, 10000.0, 1, 1.0, fbfreq, 0.05, 0.007, 2.0, 4.0);
					}

					wait += (5*689);
					Synth.sleepUntilTick(wait);
					// switch the feedback around
					numtimes = (int)BradUtils.crandom(4.0, 7.0);
					for (i = 0; i < numtimes; i++)
					{
						for (j = 0; j < 3; j++)
						{
							fbfreq = fbpchbase + BradUtils.chooseItem(fbpitches);
							fbfreq = BradUtils.cpspch(fbfreq);
							vibdisters[j].alter(wait+300, BradUtils.cpspch(pitchbase+pchadds[j]), 2.0, 1.0, 10000.0, 1.0, fbfreq, 0.05, 0.007, 2.0, 4.0);
						}
						wait += 5*689;
						Synth.sleepUntilTick(wait);
					}

					// ring 'em down
					for (i = 0; i < 3; i++)
					{
						fbfreq = fbpchbase + BradUtils.chooseItem(fbpitches);
						fbfreq = BradUtils.cpspch(fbfreq);
						vibdisters[i].alter(wait+300, BradUtils.cpspch(pitchbase+pchadds[i]), 2.0, 1.0, 10000.0, 1.0, fbfreq, 0.0, 0.007, 2.0, 4.0);
					}
				} catch (SynthException e) {
					System.err.println(e);
				}

				wait += 6*689;
				Synth.sleepUntilTick(wait);
				stopSound(false);
				if (vdstwind != null)
				{
					vdstwind.destroy();
					vdstwind = null;
				}
				paused = true;

				// now pause for the next time around
				wait += (int)BradUtils.crandom(15.0, 30.0) * 689;
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
			if (vdstwind != null) vdstwind.destroy();
			vdstwind = null;
		} else {
			if (go == true)
			{
				if ((vdstwind == null) && (paused == false))
				{
					vdstwind = new VibDistWindow(bf, backflag);
					vdstwind.start();
				}
			}
		}
	}

	public void setBground(boolean bgflag)
	{
		backflag = bgflag;

		if (vdstwind != null) vdstwind.bground(bgflag);
	}

	public void setLoad(double lv)
	{
		loadval = lv;
	}

	public void setPitch(double pchbase)
	{
		pitchbase = pchbase;
	}
}
