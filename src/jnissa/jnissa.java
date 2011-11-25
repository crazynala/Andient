package jnissa;/*  jnissa.jnissa -- another fun JSyn jobby
 *
 *  demonstrates the various uses of jnissa.Strum/jnissa.Dist/jnissa.VibDist --
 *  all based on Charlie Sullivan's original plucked-sting extensions
 *  from the implementations in RTcmix STRUM
 *
 *		Brad Garton	12/2001
 */

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.applet.Applet;
import com.softsynth.jsyn.*;

public class jnissa extends Applet implements ActionListener, AdjustmentListener, ItemListener
{
	boolean go = true;
	boolean isApplet = true;

	StrumThread strnotes;
	VibDistThread vdstnotes;
	DistThread dstnotes;
	ChunkThread chknotes;
	BassThread bassnotes;
	Frame baseframe;
	GridBagLayout gridbag;
	GridBagConstraints c;
	BradButton strumbutton;
	Label strumlabel;
	BradButton vibdistbutton;
	Label vibdistlabel;
	BradButton distbutton;
	Label distlabel;
	BradButton chunkbutton;
	Label chunklabel;
	BradButton bassbutton;
	Label basslabel;
	Checkbox graphicson;
	Checkbox graphicsbg;
	BradButton onoffbutton;
	Scrollbar ampscroll;
	Label toplabel;
	Label amplabel;
	Label loadlightlabel, loadheavylabel;
	Scrollbar loadscroll;
	Label cpulabel;
	Label namelabel;

	myCanvas nissacanvas;

	Color col;
	Color gocolor, stopcolor;
	Font lfont;

	double amp = 1.0;
	boolean grstate = true;
	boolean bgstate = false;
	boolean allgoing = false;
	double loadval = 0.4;
	PitchThread pitcher;

	public static void main(String args[])
	{
		jnissa applet = new jnissa();
		AppletFrame frame = new AppletFrame("like, meow!", applet);
		applet.isApplet = false;
		frame.resize(350,220);
		frame.show();
		frame.test();
		frame.setResizable(false);
	}

	public void init()
	{
		int wait = 0;

		baseframe = new Frame(); // invisible frame for goofy graphics

		col = new Color((float)0.0, (float)0.1, (float)0.1);
		setBackground(col);
		col = new Color((float)0.2, (float)0.7, (float)0.6);
		setForeground(col);
		gocolor = new Color((float)0.0, (float)0.9, (float)0.0);
		stopcolor = new Color((float)0.9, (float)0.0, (float)0.0);

		gridbag = new GridBagLayout();
		setLayout(gridbag);
		c = new GridBagConstraints();

		// top label
		c.weightx = 1.0;
		c.fill = GridBagConstraints.REMAINDER;
		c.gridwidth = 3;
		lfont = new Font("Times", Font.BOLD | Font.ITALIC, 14);
		toplabel = new Label("Go Cat Go");
		toplabel.setFont(lfont);
		add(toplabel);
		c.gridx = 1;
		c.gridy = 0;
		gridbag.setConstraints(toplabel, c);
		
		col = new Color((float)0.0, (float)0.5, (float)0.4);

		// thread on/off buttons
		c.anchor = GridBagConstraints.SOUTH;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.insets.top = 10;
		strumbutton = new BradButton(0, "+", this);
		strumbutton.setBackground(gocolor);
		add(strumbutton);
		c.gridx = 0;
		c.gridy = 1;
		gridbag.setConstraints(strumbutton, c);

		vibdistbutton = new BradButton(1, "+", this);
		vibdistbutton.setBackground(gocolor);
		add(vibdistbutton);
		c.gridx = 1;
		c.gridy = 1;
		gridbag.setConstraints(vibdistbutton, c);

		distbutton = new BradButton(2, "+", this);
		distbutton.setBackground(gocolor);
		add(distbutton);
		c.gridx = 2;
		c.gridy = 1;
		gridbag.setConstraints(distbutton, c);

		chunkbutton = new BradButton(3, "+", this);
		chunkbutton.setBackground(gocolor);
		add(chunkbutton);
		c.gridx = 3;
		c.gridy = 1;
		gridbag.setConstraints(chunkbutton, c);

		bassbutton = new BradButton(4, "+", this);
		bassbutton.setBackground(gocolor);
		add(bassbutton);
		c.gridx = 4;
		c.gridy = 1;
		gridbag.setConstraints(bassbutton, c);

		// labels for the buttons
		c.anchor = GridBagConstraints.NORTH;
		c.insets.top = 0;
		lfont = new Font("Helvetica", Font.PLAIN, 10);
		strumlabel = new Label("strums");
		strumlabel.setFont(lfont);
		add(strumlabel);
		c.gridx = 0;
		c.gridy = 2;
		gridbag.setConstraints(strumlabel, c);

		vibdistlabel = new Label("soars");
		vibdistlabel.setFont(lfont);
		add(vibdistlabel);
		c.gridx = 1;
		c.gridy = 2;
		gridbag.setConstraints(vibdistlabel, c);

		distlabel = new Label("doodles");
		distlabel.setFont(lfont);
		add(distlabel);
		c.gridx = 2;
		c.gridy = 2;
		gridbag.setConstraints(distlabel, c);

		chunklabel = new Label("chunks");
		chunklabel.setFont(lfont);
		add(chunklabel);
		c.gridx = 3;
		c.gridy = 2;
		gridbag.setConstraints(chunklabel, c);

		basslabel = new Label("lows");
		basslabel.setFont(lfont);
		add(basslabel);
		c.gridx = 4;
		c.gridy = 2;
		gridbag.setConstraints(basslabel, c);

		// graphics checkboxes
		lfont = new Font("Helvetica", Font.PLAIN, 8);
		graphicson = new Checkbox("graphics on", true);
		graphicson.setFont(lfont);
		graphicson.addItemListener(this); 
		add(graphicson);
		c.gridx = 1;
		c.gridy = 3;
		gridbag.setConstraints(graphicson, c);

		graphicsbg = new Checkbox("graphics bg", false);
		graphicsbg.setFont(lfont);
		graphicsbg.addItemListener(this); 
		add(graphicsbg);
		c.gridx = 3;
		c.gridy = 3;
		gridbag.setConstraints(graphicsbg, c);

		// ampslider, on-off button
		onoffbutton = new BradButton(5, " go! ", this);
		onoffbutton.setBackground(col);
		add(onoffbutton);
		c.gridx = 0;
		c.gridy = 4;
		gridbag.setConstraints(onoffbutton, c);

		c.insets.right = 10;
		c.insets.left = 5;
		ampscroll = new Scrollbar(Scrollbar.HORIZONTAL);
		ampscroll.setBackground(col);
		ampscroll.setValues(100, 10, 0, 310);
		add(ampscroll);
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(ampscroll, c);
		ampscroll.addAdjustmentListener(this);

		// amp slider label
		c.insets.right = 0;
		c.insets.left = 0;
		amplabel = new Label("volume");
		lfont = new Font("Helvetica", Font.PLAIN, 10);
		amplabel.setFont(lfont);
		add(amplabel);
		c.gridx = 2;
		c.gridy = 5;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(amplabel, c);

		// top of jnissaicon image
		try {
			if (isApplet == true)
			{
				nissacanvas = new myCanvas(new URL("http://music.columbia.edu/~brad/jnissa.jnissa/jnissaicon.gif"));
			} else {
				nissacanvas = new myCanvas(new URL("file:./jnissaicon.gif"));
			}
		} catch(MalformedURLException e) {
			System.err.println("no image");
			return;
		}
		nissacanvas.setSize(30,40);
		add(nissacanvas);
		c.gridx = 0;
		c.gridy = 6;
		c.gridheight = 2;
		gridbag.setConstraints(nissacanvas, c);

		// CPU load slider row
		c.insets.left = 40;
		c.insets.top = 5;
		c.gridheight = 1;
		loadlightlabel = new Label("light");
		lfont = new Font("Helvetica", Font.PLAIN, 8);
		loadlightlabel.setFont(lfont);
		add(loadlightlabel);
		c.gridx = 2;
		c.gridy = 6;
		gridbag.setConstraints(loadlightlabel, c);

		loadscroll = new Scrollbar(Scrollbar.HORIZONTAL);
		loadscroll.setBackground(col);
		loadscroll.setValues(40, 2, 0, 102);
		add(loadscroll);
		c.insets.left = 0;
		c.insets.right = 30;
		c.gridx = 3;
		c.gridy = 6;
		gridbag.setConstraints(loadscroll, c);
		loadscroll.addAdjustmentListener(this);

		c.insets.right = 0;
		loadheavylabel = new Label("heavy");
		loadheavylabel.setFont(lfont);
		add(loadheavylabel);
		c.gridx = 4;
		c.gridy = 6;
		gridbag.setConstraints(loadheavylabel, c);

		// my name label!
		c.insets.right = 0;
		c.insets.left = 0;
		namelabel = new Label("by Brad Garton");
		lfont = new Font("Times", Font.ITALIC, 8);
		namelabel.setFont(lfont);
		add(namelabel);
		c.gridx = 1;
		c.gridy = 7;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(namelabel, c);

		// CPU load slider label
		cpulabel = new Label("CPU load");
		lfont = new Font("Helvetica", Font.PLAIN, 10);
		cpulabel.setFont(lfont);
		add(cpulabel);
		c.gridx = 3;
		c.gridy = 7;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(cpulabel, c);

		doLayout();
		validate();

		strumbutton.resize(10, 10);
		vibdistbutton.resize(10, 10);
		distbutton.resize(10, 10);
		chunkbutton.resize(10, 10);
		bassbutton.resize(10, 10);
		loadscroll.resize(70, 10);
	}


	public void start()
	{
		try
		{
			Synth.startEngine(0);
		} catch(SynthException e) {
			SynthAlert.showError(this,e);
		}

		pitcher = new PitchThread(this);
		pitcher.start();
	}

	public void stop()
	{
		go = false;

		try
		{
			if (strnotes != null) strnotes.stop();
			if (vdstnotes != null) vdstnotes.stop();
			if (dstnotes != null) dstnotes.stop();
			if (chknotes != null) chknotes.stop();
			if (bassnotes != null) bassnotes.stop();
			Synth.stopEngine();
		} catch(SynthException e) {
			SynthAlert.showError(this,e);
		}
	}

	public void paint(Graphics g)
	{
		if (strumbutton != null) strumbutton.resize(10, 10);
		if (vibdistbutton != null) vibdistbutton.resize(10, 10);
		if (distbutton != null) distbutton.resize(10, 10);
		if (chunkbutton != null) chunkbutton.resize(10, 10);
		if (bassbutton != null) bassbutton.resize(10, 10);
		if (loadscroll != null) loadscroll.resize(70, 10);
	}

	public void actionPerformed(ActionEvent e)
	{
		BradButton theButton = (BradButton)e.getSource();

		if (theButton.index == 0) // strums
		{
			if (theButton.going)
			{
				if (strnotes != null)
				{
					strnotes.halt();
					strnotes = null;
				}
				strumbutton.setBackground(stopcolor);
				strumbutton.setForeground(Color.cyan);
				strumbutton.setLabel("-");
				theButton.going = false;
			} else {
				if (allgoing)
				{
					strnotes = new StrumThread(amp, grstate, bgstate,  baseframe, loadval, pitcher.pitchbase);
					strnotes.start();
				}
				strumbutton.setBackground(gocolor);
				strumbutton.setForeground(Color.black);
				strumbutton.setLabel("+");
				theButton.going = true;
			}
		}

		if (theButton.index == 1) // vibdists
		{
			if (theButton.going)
			{
				if (vdstnotes != null)
				{
					vdstnotes.halt();
					vdstnotes = null;
				}
				vibdistbutton.setBackground(stopcolor);
				vibdistbutton.setForeground(Color.cyan);
				vibdistbutton.setLabel("-");
				theButton.going = false;
			} else {
				if (allgoing)
				{
					vdstnotes = new VibDistThread(amp, grstate, bgstate, baseframe, loadval, pitcher.pitchbase);
					vdstnotes.start();
				}
				vibdistbutton.setBackground(gocolor);
				vibdistbutton.setForeground(Color.black);
				vibdistbutton.setLabel("+");
				theButton.going = true;
			}
		}

		if (theButton.index == 2) // dists
		{
			if (theButton.going)
			{
				if (dstnotes != null)
				{
					dstnotes.halt();
					dstnotes = null;
				}
				distbutton.setBackground(stopcolor);
				distbutton.setForeground(Color.cyan);
				distbutton.setLabel("-");
				theButton.going = false;
			} else {
				if (allgoing)
				{
					dstnotes = new DistThread(amp, grstate, bgstate, baseframe, loadval, pitcher.pitchbase);
					dstnotes.start();
				}
				distbutton.setBackground(gocolor);
				distbutton.setForeground(Color.black);
				distbutton.setLabel("+");
				theButton.going = true;
			}
		}

		if (theButton.index == 3) // chunks
		{
			if (theButton.going)
			{
				if (chknotes != null)
				{
					chknotes.halt();
					chknotes = null;
				}
				chunkbutton.setBackground(stopcolor);
				chunkbutton.setForeground(Color.cyan);
				chunkbutton.setLabel("-");
				theButton.going = false;
			} else {
				if (allgoing)
				{
					chknotes = new ChunkThread(amp, grstate, bgstate,  baseframe, loadval);
					chknotes.start();
				}
				chunkbutton.setBackground(gocolor);
				chunkbutton.setForeground(Color.black);
				chunkbutton.setLabel("+");
				theButton.going = true;
			}
		}

		if (theButton.index == 4) // bass notes
		{
			if (theButton.going)
			{
				if (bassnotes != null)
				{
					bassnotes.halt();
					bassnotes = null;
				}
				bassbutton.setBackground(stopcolor);
				bassbutton.setForeground(Color.cyan);
				bassbutton.setLabel("-");
				theButton.going = false;
			} else {
				if (allgoing)
				{
					bassnotes = new BassThread(amp, grstate, bgstate, baseframe, loadval, pitcher.pitchbase);
					bassnotes.start();
				}
				bassbutton.setBackground(gocolor);
				bassbutton.setForeground(Color.black);
				bassbutton.setLabel("+");
				theButton.going = true;
			}
		}



		if (theButton.index == 5)
		{
			if (allgoing)
			{
				if (strnotes != null)
				{
					strnotes.halt();
					strnotes = null;
				}
				if (vdstnotes != null)
				{
					vdstnotes.halt();
					vdstnotes = null;
				}
				if (dstnotes != null)
				{
					dstnotes.halt();
					dstnotes = null;
				}
				if (chknotes != null)
				{
					chknotes.halt();
					chknotes = null;
				}
				if (bassnotes != null)
				{
					bassnotes.halt();
					bassnotes = null;
				}
				onoffbutton.setLabel(" go! ");
				allgoing = false;
			} else {
				// all the sleeps() below are to stagger
				// entrances to allow getUsage() in the
				// individual threads to get better CPU use
				if (strumbutton.going)
				{
					strnotes = new StrumThread(amp, grstate, bgstate, baseframe, loadval, pitcher.pitchbase);
					strnotes.start();
				}
				if (vibdistbutton.going)
				{
					try
					{
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						System.err.println(ex);
					}

					vdstnotes = new VibDistThread(amp, grstate, bgstate, baseframe, loadval, pitcher.pitchbase);
					vdstnotes.start();
				}
				if (distbutton.going)
				{
					try
					{
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						System.err.println(ex);
					}

					dstnotes = new DistThread(amp, grstate, bgstate, baseframe, loadval, pitcher.pitchbase);
					dstnotes.start();
				}
				if (chunkbutton.going)
				{
					try
					{
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						System.err.println(ex);
					}

					chknotes = new ChunkThread(amp, grstate, bgstate, baseframe, loadval);
					chknotes.start();
				}
				if (bassbutton.going)
				{
					try
					{
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						System.err.println(ex);
					}

					bassnotes = new BassThread(amp, grstate, bgstate, baseframe, loadval, pitcher.pitchbase);
					bassnotes.start();
				}
				onoffbutton.setLabel("stop!");
				allgoing = true;
			}
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		Scrollbar theScroll = (Scrollbar)e.getAdjustable();
		int value;

		if (theScroll == ampscroll)
		{
			value =  theScroll.getValue();
			amp = (double)value/100.0;

			if (strnotes != null) strnotes.setAmp(amp);
			if (vdstnotes != null) vdstnotes.setAmp(amp);
			if (dstnotes != null) dstnotes.setAmp(amp);
			if (chknotes != null) chknotes.setAmp(amp);
			if (bassnotes != null) bassnotes.setAmp(amp);
		} else {
			value =  theScroll.getValue();
			loadval = (double)value/100.0;

			if (strnotes != null) strnotes.setLoad(loadval);
			if (vdstnotes != null) vdstnotes.setLoad(loadval);
			if (dstnotes != null) dstnotes.setLoad(loadval);
			if (chknotes != null) chknotes.setLoad(loadval);
			if (bassnotes != null) bassnotes.setLoad(loadval);
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
		Checkbox theCheck = (Checkbox)e.getItemSelectable();

		if (theCheck.getLabel() == "graphics on")
		{
			grstate = theCheck.getState();
			if (strnotes != null) strnotes.setGraphics(grstate);
			if (vdstnotes != null) vdstnotes.setGraphics(grstate);
			if (dstnotes != null) dstnotes.setGraphics(grstate);
			if (chknotes != null) chknotes.setGraphics(grstate);
			if (bassnotes != null) bassnotes.setGraphics(grstate);
		} else {
			bgstate = theCheck.getState();
			if (strnotes != null) strnotes.setBground(bgstate);
			if (vdstnotes != null) vdstnotes.setBground(bgstate);
			if (dstnotes != null) dstnotes.setBground(bgstate);
			if (chknotes != null) chknotes.setBground(bgstate);
			if (bassnotes != null) bassnotes.setBground(bgstate);
		}
	}
}

class BradButton extends Button
{
	public int index; // used in the ActionListener method
			// numbered by order on interface
	public boolean going;

	public BradButton(int buttondex, String lbl, jnissa action)
	{
		index = buttondex;
		this.addActionListener(action);
		this.setLabel(lbl);
		going = true;
	}
}

class myCanvas extends Canvas
{
	Image nissaimage;
	MediaTracker tracker = new MediaTracker(this);

	public myCanvas(URL db)
	{
		super();
		nissaimage = Toolkit.getDefaultToolkit().getImage(db);
		tracker.addImage(nissaimage, 0);
		try {
			tracker.waitForID(0);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}

	public void paint(Graphics g)
	{
		try {
			tracker.waitForID(0);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
		g.drawImage(nissaimage, 0, 0, 30, 40, this);
	}
}
	

