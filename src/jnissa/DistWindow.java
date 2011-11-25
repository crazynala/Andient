package jnissa;/*  jnissa.DistWindow -- make a fun little graphics dohickey for
 *	the jnissa.jnissa application
 *
 *	Brad Garton, 12/2001
 *
*/

import java.awt.*;

class DistWindow extends Thread
{
	boolean go = true;
	Color c;
	Window w;
	Image bstore;
	BradCanvas draw;

	int width, height;

	DistWindow(Frame f, boolean bg)
	{
		Toolkit tk;
		int x, y;

		w = new Window(f);
		tk = java.awt.Toolkit.getDefaultToolkit();

		draw = new BradCanvas();
		w.add(draw);

		width = (int)BradUtils.crandom(20.0, 70.0);
		height = (int)BradUtils.crandom(20.0, 70.0);

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
		int x, y, x1, y1;
		int xjump, yjump;
		float r,g,b;
		double cvar;

		x = (int)(Math.random() * (double)width);
		y = (int)(Math.random() * (double)height);
		r = (float)Math.random();
		g = (float)Math.random();
		b = (float)Math.random();
		xjump = (int)BradUtils.crandom(-4.0, 4.0);
		yjump = (int)BradUtils.crandom(-4.0, 4.0);
		x1 = x;
		y1 = y;
		cvar = Math.random() * 3.0;

		bstore = w.createImage(width, height);
		bg = bstore.getGraphics();
		bg.setColor(c); // background color from above
		bg.fillRect(0, 0, width, height);
		draw.setbImage(bstore);

		while (go == true)
		{
			// a torus in this case
			if (x1 > width)
			{
				x = 0;
				x1 = x + xjump;
			}
			if (x1 < 0)
			{
				x = width;
				x1 = x + xjump;
			}
			if (y1 > height)
			{
				y = 0;
				y1 = y + yjump;
			}
			if (y1 < 0)
			{
				y = height;
				y1 = y + yjump;
			}

			c = new Color(r, g, b);
			gc.setColor(c);
			gc.drawLine(x, y, x1, y1);
			bg.setColor(c);
			bg.drawLine(x, y, x1, y1);
			x = x1;
			y = y1;
			x1 = x + xjump;
			y1 = y + yjump;
			xjump = (int)BradUtils.crandom(-4.0, 4.0);
			yjump = (int)BradUtils.crandom(-4.0, 4.0);

			if (cvar < 1.0)
			{
				r += (float)BradUtils.crandom(-0.1, 0.1);
				if (r > 1.0) r = (float)1.0;
				if (r < 0.0) r = (float)0.0;
			} else if (cvar < 2.0) {
				g += (float)BradUtils.crandom(-0.1, 0.1);
				if (g > 1.0) g = (float)1.0;
				if (g < 0.0) g = (float)0.0;
			} else {
				b += (float)BradUtils.crandom(-0.1, 0.1);
				if (b > 1.0) b = (float)1.0;
				if (b < 0.0) b = (float)0.0;
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
