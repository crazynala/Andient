package jnissa;/*  jnissa.BassThread -- long, low note using jnissa.Strum.java
 *
 *		Brad Garton	12/2001
 */

import java.awt.*;

import com.softsynth.jsyn.*;

class BassThread extends Thread
{
	boolean		go = true;
	boolean		paused = true;
	Strum		bass1,bass2;
	LineOut 	noteOut;
	MultiplyUnit	ampoutL, ampoutR;

	EnvelopePlayer	benvplayer1,benvplayer2;
	SynthEnvelope	benv;
	double [] evpdata =
	{
		1.0, 1.0,
		18.0, 1.0,
		6.0, 0.0
	};

	Filter_LowPass	bfilt1,bfilt2;

	BassWindow	basswind;
	Frame           bf; // for the graphics
	boolean		graphon;
	boolean		backflag;
	double		loadval;
	double		pitchbase;


	public BassThread(double ampval, boolean gr, boolean bg, Frame f, double lv, double pb)
	throws SynthException
	{
		bf = f;
		graphon = gr;
		backflag = bg;
		loadval = lv;
		pitchbase = pb - 3.0;

		bass1 = new Strum();
		bass2 = new Strum();
		noteOut = new LineOut();

		benv = new SynthEnvelope(evpdata);
		benvplayer1 = new EnvelopePlayer();
		benvplayer2 = new EnvelopePlayer();

		bfilt1 = new Filter_LowPass();
		bfilt2 = new Filter_LowPass();

		bass1.output.connect(benvplayer1.amplitude);
		bass2.output.connect(benvplayer2.amplitude);

		benvplayer1.output.connect(bfilt1.input);
		benvplayer2.output.connect(bfilt2.input);
		bfilt1.amplitude.set(2.0);
		bfilt2.amplitude.set(2.0);
		bfilt1.frequency.set(2000.0);
		bfilt2.frequency.set(2000.0);
		bfilt1.Q.set(0.3);
		bfilt2.Q.set(0.3);

		ampoutL = new MultiplyUnit();
		ampoutR = new MultiplyUnit();
		bfilt1.output.connect(ampoutL.inputA);
		bfilt2.output.connect(ampoutR.inputA);
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

			bass1.stop();
			bass2.stop();
			benvplayer1.stop();
			benvplayer2.stop();
			bfilt1.stop();
			bfilt2.stop();
			ampoutL.stop();
			ampoutR.stop();
		} catch (SynthException e) {
			System.err.println(e);
		}
	}

	public void halt()
	{
		go = false;
		if (basswind != null)
		{
			basswind.destroy();
			basswind = null;
		}
		stopSound(true);
		interrupt();
	}

	public void run()
	{
		int wait;
		double pch;
		double [] pitches = 
		{
			0.00, 0.05, 0.07, 0.10, 0.12
		};

		while (go == true)
		{
			wait = Synth.getTickCount();
			while (Synth.getUsage() > loadval)
			{
				wait += 1500;
				Synth.sleepUntilTick(wait);
			}

			if (go == true) {
				if (graphon)
				{
					basswind = new BassWindow(bf, backflag);
					basswind.start();
				}
				paused = false;

				try
				{
					bass1.start();
					bass2.start();
					benvplayer1.start();
					benvplayer2.start();
					bfilt1.start();
					bfilt2.start();
					ampoutL.start();
					ampoutR.start();
					noteOut.start();

					pch = pitchbase + BradUtils.chooseItem(pitches);
					pch = BradUtils.cpspch(pch);
					benvplayer1.envelopePort.clear(wait+300);
					benvplayer1.envelopePort.queue(wait+300, benv);
					bass1.go(wait+300, pch + (-0.003*pch), 30.0, 50.1, 20000.0, 2);
					benvplayer2.envelopePort.clear(wait+300);
					benvplayer2.envelopePort.queue(wait+300, benv);
					bass2.go(wait+300, pch + (0.003*pch), 30.0, 50.1, 20000.0, 2);

					wait += (25*689);
					Synth.sleepUntilTick(wait);
				} catch (SynthException e) {
					System.err.println(e);
				}

				stopSound(false);
				if (basswind != null)
				{
					basswind.destroy();
					basswind = null;
				}
				paused = true;

				// pause for next cycle
				wait +=  (int)BradUtils.crandom(30.0, 70.0) * 689;
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
			if (basswind != null) basswind.destroy();
			basswind = null;
		} else {
			if (go == true)
			{
				if ((basswind == null) && (paused == false))
				{
					basswind = new BassWindow(bf, backflag);
					basswind.start();
				}
			}
		}
	}

	public void setBground(boolean bgflag)
	{
		backflag = bgflag;

		if (basswind != null) basswind.bground(bgflag);
	}

	public void setLoad(double lv)
	{
		loadval = lv;
	}

	public void setPitch(double pchbase)
	{
		pitchbase = pchbase - 3.0;
	}
}
