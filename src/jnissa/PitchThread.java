package jnissa;/*  jnissa.PitchThread -- periodically resets the pitch of the various jnissa.jnissa threads
 *
 *              Brad Garton     12/2001
 */

class PitchThread extends Thread
{
	boolean mygo;
	double pitchbase = 8.00;
	jnissa parent;

	public PitchThread(jnissa p)
	{
		parent = p;
	}

	public void halt()
	{
		mygo = false;
		interrupt();
	}

	public void run()
	{
		double [] pchmove = { 0.05, 0.07, -0.07, -0.05 };

		mygo = parent.go;
		while (mygo == true)
		{
			mygo = parent.go;
			try
			{
				// change key every once in awhile
				Thread.sleep((int)BradUtils.crandom(20000.0, 60000.0));
			} catch (InterruptedException ex) {
				System.err.println(ex);
			}

			pitchbase = BradUtils.pchadd(pitchbase, BradUtils.chooseItem(pchmove));
			if (pitchbase > 9.00) pitchbase -= 1.00;
			if (pitchbase < 7.00) pitchbase += 1.00;

			if (parent.strnotes != null) parent.strnotes.setPitch(pitchbase);
			if (parent.vdstnotes != null) parent.vdstnotes.setPitch(pitchbase);
			if (parent.dstnotes != null) parent.dstnotes.setPitch(pitchbase);
			if (parent.bassnotes != null) parent.bassnotes.setPitch(pitchbase);
		}
	}
}
