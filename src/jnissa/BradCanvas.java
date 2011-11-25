package jnissa;/*  jnissa.BradCanvas -- for the various graphics things in jnissa.jnissa
 *
 *  Brad Garton  12/2001
 *
*/
import java.awt.*;

// this is just to implement the backing store
class BradCanvas extends Canvas
{
	Image back;

	public BradCanvas() { }

	public void setbImage(Image img)
	{
		back = img;
	}

        public void paint(Graphics g)
        {
		if (back != null)
		{
			g.drawImage(back, 0, 0, this);
		}
	}
}
