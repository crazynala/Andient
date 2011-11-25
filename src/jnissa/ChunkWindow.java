package jnissa;/*  jnissa.ChunkWindow -- make a fun little graphics dohickey for
 *	the jnissa.jnissa application
 *
 *	Brad Garton, 12/2001
 *
*/

import java.awt.*;

class ChunkWindow extends Thread
{
	boolean go = true;
	Color c;
	Window w;
	Image bstore;
	BradCanvas draw;

	int width, height;

	ChunkWindow(Frame f, boolean bg)
	{
		Toolkit tk;
		int x, y;

		w = new Window(f);
		tk = java.awt.Toolkit.getDefaultToolkit();

		draw = new BradCanvas();
		w.add(draw);

		// rectangles will be 5x5, so multiply each of these by 5
		width = (int)BradUtils.crandom(3.0, 15.0);
		height = (int)BradUtils.crandom(3.0, 15.0);
		width *= 5;
		height *= 5;

		w.setSize(width, height);

		x = (int)((double)(tk.getScreenSize().width - width) * Math.random());
		y = (int)((double)(tk.getScreenSize().height - height) * Math.random());
		w.setLocation(x, y);

		c = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());
		draw.setBackground(c);
		w.show();
		if (bg) w.toBack();
	}


	public void run()
	{
		Graphics gc = draw.getGraphics();
		Graphics bg;
		int x, y;
		float r,g,b;
		float rincr, gincr, bincr;

		x = 0;
		y = 0;
		r = (float)Math.random();
		g = (float)Math.random();
		b = (float)Math.random();
		rincr = (float)BradUtils.crandom(-0.1, 0.1);
		gincr = (float)BradUtils.crandom(-0.1, 0.1);
		bincr = (float)BradUtils.crandom(-0.1, 0.1);

		bstore = w.createImage(width, height);
		bg = bstore.getGraphics();
		bg.setColor(c); // background color from above
		bg.fillRect(0, 0, width, height);
		draw.setbImage(bstore);

		while (go == true)
		{
			c = new Color(r, g, b);
			gc.setColor(c);
			gc.fillRect(x, y, 5, 5);
			bg.setColor(c);
			bg.fillRect(x, y, 5, 5);
			x += 5;
			if (x > width) 
			{
				x = 0;
				y += 5;
			}
			if (y > (height-5)) y = 0;

			r += rincr;
			if ( (r > 1.0) || (r < 0.0) )
			{
				rincr = -rincr;
				r += rincr;
			}
			g += gincr;
			if ( (g > 1.0) || (g < 0.0) )
			{
				gincr = -gincr;
				g += gincr;
			}
			b += bincr;
			if ( (b > 1.0) || (b < 0.0) )
			{
				bincr = -bincr;
				b += bincr;
			}

			try
			{
				this.sleep(50);
			} catch (InterruptedException ex) {
				System.err.println(ex);
			}
		}
	}

	public void halt()
	{
		go = false;
	}

	public void destroy()
	{
		go = false;
		w.dispose();
	}

	public void bground(boolean bgflag)
	{
		if (bgflag) w.toBack();
		else w.toFront();
	}
}

