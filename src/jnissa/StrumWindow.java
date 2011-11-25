package jnissa;/*  jnissa.StrumWindow -- make a fun little graphics dohickey for
 *	the jnissa.jnissa application
 *
 *	Brad Garton, 12/2001
 *
*/

import java.awt.*;

public class StrumWindow extends Thread
{
	boolean go = true;
	Color c;
	Window w;
	Image bstore;
	BradCanvas draw;

	int width, height;

	StrumWindow(Frame f, boolean bg)
	{
		Toolkit tk;
		int x, y;

		w = new Window(f);
		tk = java.awt.Toolkit.getDefaultToolkit();

		draw = new BradCanvas();
		w.add(draw);

		if (Math.random() > 0.5)
		{
			width = (int)BradUtils.crandom(10.0, 50.0);
			height = (int)BradUtils.crandom(300.0, 500.0);
		} else {
			width = (int)BradUtils.crandom(300.0, 500.0);
			height = (int)BradUtils.crandom(10.0, 50.0);
		}

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
		int xory;
		int npoints;
		float r,g,b;
		int incr;
		double cvar;

		x = (int)(Math.random() * (double)width);
		y = (int)(Math.random() * (double)height);
		r = (float)Math.random();
		g = (float)Math.random();
		b = (float)Math.random();
		xjump = (int)BradUtils.crandom(1.0, (double)(width/5));
		yjump = (int)BradUtils.crandom(1.0, (double)(height/5));
		npoints = (int)BradUtils.crandom(1.0, 7.0);
		xory = 0;
		incr = 0;
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
			if (x1 > width || x1 < 0)
			{
				xjump = -xjump;
				x1 += xjump;
			}
			if (y1 > height || y1 < 0)
			{
				yjump = -yjump;
				y1 += yjump;
			}
			if (xory == 0)
			{
				x1 = x + xjump;
			} else {
				y1 = y + yjump;
			}

			c = new Color(r, g, b);
			gc.setColor(c);
			gc.drawLine(x, y, x1, y1);
			bg.setColor(c);
			bg.drawLine(x, y, x1, y1);
			x = x1;
			y = y1;

			if (incr++ > npoints)
			{
				npoints = (int)BradUtils.crandom(1.0, 5.0);
				if (++xory > 1)
				{
					xory = 0;
					do
					{
						xjump = (int)BradUtils.crandom((double)(-width/5), (double)(width/5));
					} while (xjump == 0);
				} else {
					do
					{
						yjump = (int)BradUtils.crandom((double)(-height/5), (double)(height/5));
					} while (yjump == 0);
				}
				incr = 0;
			}

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
				this.sleep(100);
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
