package jnissa;/*  jnissa.VibDistWindow -- make a fun little graphics dohickey for
 *	the jnissa.jnissa application
 *
 *	Brad Garton, 12/2001
 *
*/

import java.awt.*;

class VibDistWindow extends Thread
{
	boolean go = true;
	Color c;
	Window w;
	Image bstore;
	BradCanvas draw;

	int width, height;

	VibDistWindow(Frame f, boolean bg)
	{
		Toolkit tk;
		int x, y;

		w = new Window(f);
		tk = java.awt.Toolkit.getDefaultToolkit();

		draw = new BradCanvas();
		w.add(draw);

		width = (int)BradUtils.crandom(100.0, 400.0);
		height = (int)BradUtils.crandom(10.0, 50.0);

		w.setSize(width, height);

		x = (int)((double)(tk.getScreenSize().width - width) * Math.random());
		y = (int)((double)(tk.getScreenSize().height - height) * Math.random());

		w.setLocation(x, y);

		c = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());
		draw.setBackground(c);
		w.show();
		if (bg)	w.toBack();
	}


	public void run()
	{
		Graphics gc = draw.getGraphics();
		Graphics bg;
		int center;
		float r,g,b;
		float rincr, gincr, bincr;

		r = (float)Math.random();
		g = (float)Math.random();
		b = (float)Math.random();
		rincr = (float)BradUtils.crandom(-0.01, 0.01);
		gincr = (float)BradUtils.crandom(-0.01, 0.01);
		bincr = (float)BradUtils.crandom(-0.01, 0.01);

		bstore = w.createImage(width, height);
		bg = bstore.getGraphics();
		bg.setColor(c); // background color from above
		bg.fillRect(0, 0, width, height);
		draw.setbImage(bstore);

		center = width/2;

		while (go == true)
		{
			gc.copyArea(1, 0, center,  height, -1, 0); 
			gc.copyArea(center, 0, width-1,  height, 1, 0); 
			c = new Color(r, g, b);
			gc.setColor(c);
			gc.drawLine(center, 0, center, height);

			bg.copyArea(1, 0, center,  height, -1, 0); 
			bg.copyArea(center, 0, width-1,  height, 1, 0); 
			bg.setColor(c);
			bg.drawLine(center, 0, center, height);

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
				this.sleep(40);
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
