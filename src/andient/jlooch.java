package andient;/* andient.jlooch -- make them droney sounds once again, using JSyn
 *
 * 		Brad Garton, fall 2001
 *
*/

import com.softsynth.jsyn.AppletFrame;
import com.softsynth.jsyn.Synth;
import com.softsynth.jsyn.SynthAlert;
import com.softsynth.jsyn.SynthException;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class jlooch extends Applet implements AdjustmentListener, ActionListener {
    private final static Logger logger = Logger.getLogger(jlooch.class.getName());
    Scrollbar DroneScroll;
    Scrollbar SeqScroll;
    Scrollbar WarbleScroll;
    Scrollbar BurstScroll;
    Button goButton;
    Button[] onoffs = new Button[4];
    int[] onoffstates = {1, 1, 1, 1};
    myCanvas drawArea;
    URL netimage;

    // synthesis stuff
    DroneNotes droneyThread;
    SeqNotes seqThread;
    WarbleNote warbleThread;
    BurstNote burstThread;

    public static void main(String args[]) {
        jlooch andientArduino = new jlooch();
        AppletFrame f = new AppletFrame("Andient Arduino", andientArduino);
        f.resize(210, 350);
        f.show();
        f.test();
        f.setResizable(false);
    }

    public void init() {
        Font bfont;
        int i;

        Color fc = new Color((float) 0.1, (float) 0.7, (float) 0.7);
        DroneScroll = new Scrollbar(Scrollbar.VERTICAL, 0, 10, 0, 110);
        DroneScroll.setLocation(20, 45);
        DroneScroll.setSize(25, 200);
        DroneScroll.setName("drones");
        DroneScroll.setBackground(fc);
        DroneScroll.addAdjustmentListener(this);
        add(DroneScroll);

        fc = new Color((float) 0.2, (float) 0.6, (float) 0.7);
        SeqScroll = new Scrollbar(Scrollbar.VERTICAL, 79, 10, 0, 110);
        SeqScroll.setLocation(65, 45);
        SeqScroll.setSize(25, 200);
        SeqScroll.setName("seqs");
        SeqScroll.setBackground(fc);
        SeqScroll.addAdjustmentListener(this);
        add(SeqScroll);

        fc = new Color((float) 0.3, (float) 0.5, (float) 0.7);
        WarbleScroll = new Scrollbar(Scrollbar.VERTICAL, 86, 10, 0, 110);
        WarbleScroll.setLocation(110, 45);
        WarbleScroll.setSize(25, 200);
        WarbleScroll.setName("warbles");
        WarbleScroll.setBackground(fc);
        WarbleScroll.addAdjustmentListener(this);
        add(WarbleScroll);

        fc = new Color((float) 0.4, (float) 0.4, (float) 0.7);
        BurstScroll = new Scrollbar(Scrollbar.VERTICAL, 88, 10, 0, 110);
        BurstScroll.setLocation(155, 45);
        BurstScroll.setSize(25, 200);
        BurstScroll.setName("bursts");
        BurstScroll.setBackground(fc);
        BurstScroll.addAdjustmentListener(this);
        add(BurstScroll);

        fc = new Color((float) 0.9, (float) 0.1, (float) 0.2);
        for (i = 0; i < 4; i++) {
            onoffs[i] = new Button();
            onoffs[i].setLocation(i * 45 + 20, 20);
            onoffs[i].setSize(20, 20);
            onoffs[i].setBackground(fc);
            onoffs[i].setForeground(Color.yellow);
            onoffs[i].setLabel("+");
            onoffs[i].addActionListener(this);
            add(onoffs[i]);
        }
        onoffs[0].setName("drones");
        onoffs[1].setName("seqs");
        onoffs[2].setName("warbles");
        onoffs[3].setName("bursts");

        fc = new Color((float) 0.2, (float) 0.7, (float) 0.8);
        goButton = new Button();
        goButton.setLocation(65, 270);
        goButton.setSize(70, 20);
        goButton.setBackground(fc);
        bfont = new Font("Times", Font.ITALIC, 10);
        goButton.setFont(bfont);
        fc = new Color((float) 0.9, (float) 0.1, (float) 0.1);
        goButton.setForeground(fc);
        goButton.setLabel("Play");
        goButton.setName("main");
        goButton.addActionListener(this);
        add(goButton);

        try {
            netimage = new URL("file:./loochicon.gif");
//			netimage = new URL("http://music.columbia.edu/~brad/andient.jlooch/loochicon.gif");
        } catch (MalformedURLException e) {
            System.err.println("no image");
            return;
        }
        drawArea = new myCanvas(netimage);
        drawArea.setBackground(Color.white);
        drawArea.setSize(210, 350);
        drawArea.setLocation(0, 0);
        add(drawArea);
    }

    public void start() {
        drawArea.start();

        try {
            Synth.startEngine(0);

            droneyThread = new DroneNotes();
            seqThread = new SeqNotes();
            warbleThread = new WarbleNote();
            burstThread = new BurstNote();
        } catch (SynthException e) {
            SynthAlert.showError(this, e);
        }
    }

    public void stop() {
        try {
            if (going == 1) {
                if (onoffstates[0] == 1) {
                    droneyThread.stopSound();
                }
                if (onoffstates[1] == 1) {
                    seqThread.stopSound();
                }
                if (onoffstates[2] == 1) {
                    warbleThread.stopSound();
                }
                if (onoffstates[3] == 1) {
                    burstThread.stopSound();
                }
            }
            droneyThread.stop();
            seqThread.stop();
            warbleThread.stop();
            burstThread.stop();
            if (started == 1) {
                Synth.stopEngine();
            }
        } catch (SynthException e) {
            SynthAlert.showError(this, e);
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        Scrollbar theScroll = (Scrollbar) e.getAdjustable();
        int value;
        double prob;

        value = theScroll.getValue();
        prob = (double) (100 - value) / 100.0;

        if (started == 1) {
            if (theScroll.getName() == "drones") {
                droneyThread.setProb(prob);
            }
            if (theScroll.getName() == "seqs") {
                seqThread.setProb(prob);
            }
            if (theScroll.getName() == "warbles") {
                warbleThread.setProb(prob);
            }
            if (theScroll.getName() == "bursts") {
                burstThread.setProb(prob);
            }
        }
    }

    int going = 0;
    int started = 0;

    public void actionPerformed(ActionEvent e) {
        int switcher = 4;
        Color c, goc, stopc;
        Button theButton = (Button) e.getSource();

        goc = new Color((float) 0.9, (float) 0.1, (float) 0.2);
        stopc = new Color((float) 0.1, (float) 0.8, (float) 0.7);
        if (theButton.getName() == "drones") {
            switcher = 0;
        }
        if (theButton.getName() == "seqs") {
            switcher = 1;
        }
        if (theButton.getName() == "warbles") {
            switcher = 2;
        }
        if (theButton.getName() == "bursts") {
            switcher = 3;
        }
        if (theButton.getName() == "main") {
            switcher = 4;
        }
        try {
            switch (switcher) {
                case 0:
                    if (going == 1) {
                        if (onoffstates[0] == 0) {
                            droneyThread.start();
                            droneyThread.setProb((100.0 - (double) DroneScroll.getValue()) / 100.0);
                            onoffs[0].setBackground(goc);
                            onoffs[0].setForeground(Color.yellow);
                            onoffs[0].setLabel("+");
                            onoffstates[0] = 1;
                        } else {
                            droneyThread.stopSound();
                            onoffs[0].setBackground(stopc);
                            onoffs[0].setForeground(Color.white);
                            onoffs[0].setLabel("-");
                            onoffstates[0] = 0;
                        }
                    } else {
                        if (onoffstates[0] == 0) {
                            onoffs[0].setBackground(goc);
                            onoffs[0].setForeground(Color.yellow);
                            onoffs[0].setLabel("+");
                            onoffstates[0] = 1;
                        } else {
                            onoffs[0].setBackground(stopc);
                            onoffs[0].setForeground(Color.white);
                            onoffs[0].setLabel("-");
                            onoffstates[0] = 0;
                        }
                    }
                    break;
                case 1:
                    if (going == 1) {
                        if (onoffstates[1] == 0) {
                            seqThread.start();
                            seqThread.setProb((100.0 - (double) SeqScroll.getValue()) / 100.0);
                            onoffs[1].setBackground(goc);
                            onoffs[1].setForeground(Color.yellow);
                            onoffs[1].setLabel("+");
                            onoffstates[1] = 1;
                        } else {
                            seqThread.stopSound();
                            onoffs[1].setBackground(stopc);
                            onoffs[1].setForeground(Color.white);
                            onoffs[1].setLabel("-");
                            onoffstates[1] = 0;
                        }
                    } else {
                        if (onoffstates[1] == 0) {
                            onoffs[1].setBackground(goc);
                            onoffs[1].setForeground(Color.yellow);
                            onoffs[1].setLabel("+");
                            onoffstates[1] = 1;
                        } else {
                            onoffs[1].setBackground(stopc);
                            onoffs[1].setForeground(Color.white);
                            onoffs[1].setLabel("-");
                            onoffstates[1] = 0;
                        }
                    }
                    break;
                case 2:
                    if (going == 1) {
                        if (onoffstates[2] == 0) {
                            warbleThread.start();
                            warbleThread.setProb((100.0 - (double) WarbleScroll.getValue()) / 100.0);
                            onoffs[2].setBackground(goc);
                            onoffs[2].setForeground(Color.yellow);
                            onoffs[2].setLabel("+");
                            onoffstates[2] = 1;
                        } else {
                            warbleThread.stopSound();
                            onoffs[2].setBackground(stopc);
                            onoffs[2].setForeground(Color.white);
                            onoffs[2].setLabel("-");
                            onoffstates[2] = 0;
                        }
                    } else {
                        if (onoffstates[2] == 0) {
                            onoffs[2].setBackground(goc);
                            onoffs[2].setForeground(Color.yellow);
                            onoffs[2].setLabel("+");
                            onoffstates[2] = 1;
                        } else {
                            onoffs[2].setBackground(stopc);
                            onoffs[2].setForeground(Color.white);
                            onoffs[2].setLabel("-");
                            onoffstates[2] = 0;
                        }
                    }
                    break;
                case 3:
                    if (going == 1) {
                        if (onoffstates[3] == 0) {
                            burstThread.start();
                            burstThread.setProb((100.0 - (double) BurstScroll.getValue()) / 100.0);
                            onoffs[3].setBackground(goc);
                            onoffs[3].setForeground(Color.yellow);
                            onoffs[3].setLabel("+");
                            onoffstates[3] = 1;
                        } else {
                            burstThread.stopSound();
                            onoffs[3].setBackground(stopc);
                            onoffs[3].setForeground(Color.white);
                            onoffs[3].setLabel("-");
                            onoffstates[3] = 0;
                        }
                    } else {
                        if (onoffstates[3] == 0) {
                            onoffs[3].setBackground(goc);
                            onoffs[3].setForeground(Color.yellow);
                            onoffs[3].setLabel("+");
                            onoffstates[3] = 1;
                        } else {
                            onoffs[3].setBackground(stopc);
                            onoffs[3].setForeground(Color.white);
                            onoffs[3].setLabel("-");
                            onoffstates[3] = 0;
                        }
                    }
                    break;
                case 4:
                    started = 1;
                    if (going == 1) {
                        c = new Color((float) 0.2, (float) 0.7, (float) 0.8);
                        goButton.setBackground(c);
                        c = new Color((float) 0.9, (float) 0.1, (float) 0.1);
                        goButton.setForeground(c);
                        goButton.setLabel("drono...");

                        if (onoffstates[0] == 1) {
                            droneyThread.stopSound();
                        }
                        if (onoffstates[1] == 1) {
                            seqThread.stopSound();
                        }
                        if (onoffstates[2] == 1) {
                            warbleThread.stopSound();
                        }
                        if (onoffstates[3] == 1) {
                            burstThread.stopSound();
                        }
                        going = 0;
                    } else {
                        c = new Color((float) 0.3, (float) 0.7, (float) 0.7);
                        goButton.setBackground(c);
                        c = new Color((float) 0.9, (float) 0.1, (float) 0.1);
                        goButton.setForeground(c);
                        goButton.setLabel("Stop");

                        if (onoffstates[0] == 1) {
                            droneyThread.start();
                            droneyThread.setProb((100.0 - (double) DroneScroll.getValue()) / 100.0);
                        }
                        if (onoffstates[1] == 1) {
                            seqThread.start();
                            seqThread.setProb((100.0 - (double) SeqScroll.getValue()) / 100.0);
                        }
                        if (onoffstates[2] == 1) {
                            warbleThread.start();
                            warbleThread.setProb((100.0 - (double) WarbleScroll.getValue()) / 100.0);
                        }
                        if (onoffstates[3] == 1) {
                            burstThread.start();
                            burstThread.setProb((100.0 - (double) BurstScroll.getValue()) / 100.0);
                        }
                        going = 1;
                    }
                    break;
            }
        } catch (SynthException se) {
            SynthAlert.showError(this, se);
        }
    }


}

class myCanvas extends Canvas {
    Graphics cg, bg;
    Speckle goDots;
    Image bstore;
    Image loochimage;
    MediaTracker tracker = new MediaTracker(this);

    public myCanvas(URL db) {
        super();
        loochimage = Toolkit.getDefaultToolkit().getImage(db);
        goDots = new Speckle(210, 350);
    }

    public void start() {
        bstore = createImage(210, 350);
        tracker.addImage(bstore, 0);
        tracker.addImage(loochimage, 1);
        try {
            tracker.waitForID(0);
            tracker.waitForID(1);
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        cg = this.getGraphics();
        bg = bstore.getGraphics();
        goDots.setGraphics(cg, bg);
        goDots.start();
    }

    public void paint(Graphics g) {
        Font lfont;

        // no idea why I have to do this... the MediaTracker
        // should catch these errors, I thought
        while ((bstore == null) || (loochimage == null)) {
            try {
                java.lang.Thread.sleep(10);
            } catch (InterruptedException e) {
                return;
            }
        }
        g.drawImage(bstore, 0, 0, Color.white, this);
        g.drawImage(loochimage, 3, 297, Color.white, this);

        lfont = new Font("Times", Font.PLAIN, 10);
        g.setFont(lfont);
        g.setColor(Color.black);
        g.drawString("drones", 18, 260);
        g.drawString("sequences", 55, 260);
        g.drawString("warbles", 105, 260);
        g.drawString("noises", 155, 260);
        lfont = new Font("Times", Font.BOLD, 14);
        g.setFont(lfont);
        g.drawString("Spirit of the Looch", 40, 12);
        lfont = new Font("Helvetica", Font.PLAIN, 8);
        g.setFont(lfont);
        g.drawString("Brad Garton", 145, 317);
    }
}

