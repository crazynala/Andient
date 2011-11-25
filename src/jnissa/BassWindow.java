package jnissa;/*  jnissa.BassWindow -- make a fun little graphics dohickey for
 *	the jnissa.jnissa application
 *
 *	Brad Garton, 12/2001
 *
*/

import java.awt.*;

class BassWindow extends Thread
{
	boolean go = true;
	Color c;
	Window w;
	Image bstore;
	BradCanvas draw;

	int width;

	BassWindow(Frame f, boolean bg)
	{
		Toolkit tk;
		int x, y;

		w = new Window(f);

		draw = new BradCanvas();
		w.add(draw);

		width = (int)BradUtils.crandom(10.0, 500.0);

		w.setSize(width, 1);

		tk = java.awt.Toolkit.getDefaultToolkit();

		x = (int)((double)(tk.getScreenSize().width - width) * Math.random());
		y = (int)((double)(tk.getScreenSize().height - 1) * Math.random());
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
		int x;
		float r,g,b;

		r = (float)Math.random();
		g = (float)Math.random();
		b = (float)Math.random();

		bstore = w.createImage(width, 1);
		bg = bstore.getGraphics();
		bg.setColor(c); // background color from above
		bg.fillRect(0, 0, width, 1);
		draw.setbImage(bstore);

		x = (int)(Math.random() * (double)width);

		while (go == true)
		{
			r = (float)Math.random();
			g = (float)Math.random();
			b = (float)Math.random();
			c = new Color(r, g, b);

			x = (int)(Math.random() * (double)width);
			gc.setColor(c);
			gc.drawLine(x, 0, x+1, 0);

			bg.setColor(c);
			bg.drawLine(x, 0, x+1, 0);

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
