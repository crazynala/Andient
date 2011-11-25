package jnissa;/*  jnissa.StrumSet -- object to set up coeffs + other data for the jnissa.Strum
 *	synthesis object
 *
 *  based on Charlie Sullivan's version of the plucked-string algorithm
 *	(from the RTcmix version, START() )
 *
 *		Brad Garton	11/2001
 */

import java.lang.*;

public class StrumSet extends Object
{
	public double a0, a1, a2, a3; // plucked-string filter coeffs
	public double dcz1, dcb1, dca1, dca0; // dc-blocking filter coeffs
	public double delsamps; // length of delay line
	public double[] ninit; // initial noise array, feed into the delay

	public StrumSet() {}

	public StrumSet(double f, double tF, double tN)
	{
		sset(f, tF, tN);
	}

	public void sset(double freq, double tF0, double tNy)
	{

// original note from Charlie follows -- the "strumq" struct is
// now public vars -- BGG
/* Sets up strumq structure for strum to use as plucked string.
   Uses a two point averaging filter to adjust the phase for exact
   correct pitch, and then uses a linear phase three point averaging
   filter to adjust the fundamental frequency to decay in time tf0,
   and in time tNy at the Nyquist frequency.  In some cases, the
   decay time at the Nyquist frequency (which always must be less than
   tf0) will not be exactly as requested, but a wide range of variation
   is possible.  The two point and three point filters are combined into
   a single four point filter.  A single pole dc-blocking filter is added
   because the four point filter may have some gain a dc, and non-zero dc
   response can cause problems with clicks and what not.
   Randfill must be called after (and not before) this routine is called the
   first time, as it initializes some things in the strumq structure.
   This routine does not initialize them so it may be used to change parameters
   during the course of a note.

                    Charlie Sullivan
                    1/87                                      */

		double SR;
		double xlen, xerr;
		double dH0, dHNy, H01, H02, HNy1, HNy2, H;
		double w0, tgent, c, c1, c2, s, g, aa0, aa1;
		double ncycles0, ncyclesNy, temp;

		SR = 44100.0; // change this later...
		xlen = 1.0/freq*SR;
		w0 = freq/SR*2.0*Math.PI;

		// ncycles is not an integer,and is number of cycles to decay
		ncycles0 = freq * tF0;
		ncyclesNy = freq * tNy;

		// level will be down to -20db after t
		dH0 = Math.pow(0.1, (1.0/ncycles0));
		dHNy = Math.pow(0.1, (1.0/ncyclesNy));

		delsamps = Math.floor(xlen - 1.0); // public var for del time
		xerr = delsamps - xlen + 1.0; // xerr will be negative

		/* Calculate the phase shift needed from two-point averaging
			filter, calculate the filter coefficient c1:
			y = c1*xn + (1-c1)*x(n-1)  */
		tgent = Math.tan(xerr*w0);  // tan of theta
		c = Math.cos(w0);
		s = Math.sin(w0);
		c1 = (-s - c*tgent)/(tgent*(1.0 - c) - s);
		c2 = 1.0 - c1;

		// effect of this filter on amplitudeSetting response
		H01 = Math.sqrt(c2*c2*s*s + (c1*(1.0-c)+c) * (c1*(1.0-c)+c) );
		HNy1 = Math.abs(2.0*c1 - 1.0);

		/* Now add three point linear phase averaging filter
			with delay of 1, y = xn*a0 + xn-1*a1 + xn-2*a0
			and a gain or loss factor, g, so that the filter*g
			has response H02 and HNy2 to make the total
			response of all the filters dH0 and dHNy */
		H02 = dH0/H01;
		if (HNy1 > 0.0) {
			HNy2 = dHNy/HNy1;
		} else {
			HNy2 = 1.e10;
		}

		g = (2.0*H02 - (1.0-c)*HNy2)/(1.0 + c);
		aa1 = (HNy2/g + 1.0)/2.0;

		/* For this filter to be monotonic low pass, a1 must be
			between 1/2 and 1, if it isn't response at Nyquist
			won't be as specified, but it will be set as
			 close as is feasible */
		if(aa1 < 0.5)
		{
			aa1 = 0.5;
			H = (1.0 - aa1)*c + aa1;
			g = H02/H;
		}

		if(aa1 > 1.0)
		{
			aa1 = 1.0;
			g = H02;
		}

		aa0 = (1.0 - aa1)/2.0;
		aa0 *= g;
		aa1 *= g;

		/* Now combine the two and three point averaging filters
			into one four point filter with
			coefficients a0-a3  (public vars) */
		a0 = aa0*c1;
		a1 = aa0*c2 + aa1*c1;
		a2 = aa0*c1 + aa1*c2;
		a3 = aa0*c2;

		// set up dc blocking filter
		temp = Math.PI*(freq/18.0/SR);
		dca0 = 1.0/(1.0 + temp); // public var
		dca1 = -dca0; // public var
		dcb1 = dca0*(1.0 - temp); // public var
	}

	public void randfill(double amp, int squish)
	{
// charlie's original note:
/* Fills plucked string structure q with random values, and intitialize things.
	Call only after a call to sset.
	Can be used with zero amplitudeSetting to just initialize things.
	Squish models the softness of a plucking implement by filtering
	the values put in the string with an averaging filter.
	The filter makes squish passes.  The loss of amplitudeSetting at the
	fundamental frequency is compensated for, but the overall amplitudeSetting
	of the squished string is lowered, as the energy at other frequencies
	is decreased.  */

		double total, average;
		int i;

		dcz1 = 0.0; // public var for dc filter;

		// ninit is set up here to allow sset to reinitialize
		// NOTE:  this is probably a memory leak...
		ninit = new double[(int)delsamps]; // public var, initial noise

		// fill with white noise and subtract any dc component
		total = 0.0;
		for (i = 0; i < delsamps; i++)
		{
			ninit[i] = (Math.random()*2.0 - 1.0) * amp;
			total += ninit[i];
		}

		average = total/delsamps;
		for (i = 0; i < delsamps; i++) {
			ninit[i] -= average;
		}

		squisher(squish);
	}


	void squisher(int squish)
	{

// charlie's original note:
/* Routine for use with 'strum' plucked string.  Called by randfill
	Low- pass filters vales of string, squish times.  Compensates
	for loss of level at fundamental, but not for overall loss. */

		int i, j, p1, p2;
		double mult;

		p1 = (int)delsamps - 1;
		p2 = (int)delsamps - 2;

		mult = Math.abs(1.0/(2.0*Math.cos(2.0*Math.PI/delsamps) + 1.0));

		for(j = 0; j < squish; j++)
		{
			for(i = 0; i < delsamps; i++)
			{
				ninit[i] = mult*(ninit[p2]+ninit[i]+ninit[p1]);
				p2 = p1;
				p1 = i;
			}
		}
	}
}
