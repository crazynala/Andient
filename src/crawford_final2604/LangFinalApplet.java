package crawford_final2604;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.softsynth.jmsl.JMSL;
import com.softsynth.jmsl.JMSLMixerContainer;
import com.softsynth.jmsl.JMSLRandom;
import com.softsynth.jmsl.MusicDevice;
import com.softsynth.jmsl.MusicShape;
import com.softsynth.jmsl.jsyn.JSynMusicDevice;
import com.softsynth.jmsl.jsyn.SynthNoteAllPortsInstrument;
import com.softsynth.jmsl.jsyn.SynthNoteAllPortsInstrumentSP;

/**
 * @author Langdon C.
 *  
 */
public class LangFinalApplet extends java.applet.Applet implements ActionListener {

    Panel p;
    JMSLMixerContainer mixer;

    Button startButton;
    Button stopButton;

    SynthNoteAllPortsInstrument ins;
    SynthNoteAllPortsInstrument ins2;
    SynthNoteAllPortsInstrument ins3;
    SynthNoteAllPortsInstrument ins4;
    SynthNoteAllPortsInstrumentSP dsP1;
    SynthNoteAllPortsInstrumentSP dsP2;
    MusicShape s;
    MusicShape s2;
    MusicShape s3;
    MusicShape s4;
    MusicDevice dev;

    public static void main(String[] args) {
        LangFinalApplet applet = new LangFinalApplet();
        Frame f = new Frame("Mmm scrollbars");
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
        f.add(applet, BorderLayout.CENTER);
        applet.init();
        applet.start();
        f.setSize(600, 200);
        f.setVisible(true);
        applet.init();
        f.resize(500, 550);
        f.show();

    }

    public void start() {

        // open jsyn music device
        JMSL.clock.setAdvance(0.1);
        dev = JSynMusicDevice.instance();
        dev.open();

        // build instrument and mixer
        ins = new SynthNoteAllPortsInstrument(8, LangDroneBass.class.getName());
        ins2 = new SynthNoteAllPortsInstrument(8, LangFilteredPulse.class.getName());
        ins3 = new SynthNoteAllPortsInstrument(8, LangPoocher.class.getName());
        ins4 = new SynthNoteAllPortsInstrument(8, LangWindString.class.getName());

        dsP1 = new SynthNoteAllPortsInstrumentSP(2, LangDelayLeft.class.getName());
        dsP2 = new SynthNoteAllPortsInstrumentSP(2, LangDelayRight.class.getName());

        dsP1.addSignalSource(ins2.getOutput());
        dsP2.addSignalSource(ins2.getOutput());

        ins.setName("bass Drone");
        dsP1.setName("filt Pulse.L");
        dsP2.setName("filt Pulse.R");
        ins3.setName("bowed metal");
        ins4.setName("wind String");

        mixer = new JMSLMixerContainer();
        mixer.start();
        mixer.addInstrument(ins);
        mixer.addInstrument(dsP1, 0.26, 0.5);
        //mixer.addInstrument(ins2);
        mixer.addInstrument(dsP2, 0.74, 0.5);

        mixer.addInstrument(ins3);
        mixer.addInstrument(ins4);

        double[] vals = { 0.62, 0.62, 0.62 };
        double[] vals2 = { 0.82, 0.82, 0.82 };
        dsP1.on(0, 1, vals);
        dsP2.on(0, 1, vals2);

        buildGUI();
    }

    public void buildShape1() {

        s = new MusicShape(ins.getDimensionNameSpace());
        s.setRepeats(100);
        s.setRepeatPause(1);
        s.launch(JMSL.now());
        s.setInstrument(ins); // !!!

        for (int i = 0; i < 573; i++) {

            double muting[] = { 0, 0.7, 0, 0, 0.5, 0, 1, 0, };
            double muter = muting[JMSLRandom.choose(0, muting.length - 1)];

            // DroneBass
            double duration = 9; JMSLRandom.choose(4, 9);
            double pitch = 8; JMSLRandom.choose(1, 8);
            double amplitude = muter;
            double hold = 9; JMSLRandom.choose(5, 9);
            double realFreq = 80; JMSLRandom.choose(20, 80);
            double cut = realFreq * 1.5;
            double rez = JMSLRandom.choose(5, 15);
            double rate = 1;
            s.add(duration, pitch, amplitude, hold, realFreq, cut, rez, rate);
            //			System.out.println( "dbDur " + duration +" dbHz "+ realFreq + "
            // "+
            //					" amp = " + amplitudeSetting +
            //					" cutHz = " + cut +" rez " + rez );
        }// end music shape s

    }

    public void buildShape2() {
        s2 = new MusicShape(ins2.getDimensionNameSpace());
        s2.setRepeats(6);
        s2.setRepeatPause(1000);
        s2.setInstrument(ins2);
        s2.launch(JMSL.now() + 10);

        for (int i = 0; i < 720; i++) {

            double muting[] = { 0, 0, 0, 0, 0, 1, 0, 0, };
            double muter = muting[JMSLRandom.choose(0, muting.length - 1)];
            // filtered pulse
            double duration = JMSLRandom.choose(1., 4.5);
            double pitch = JMSLRandom.choose(1, 8);
            double amplitude = muter * 0.125;
            double hold = JMSLRandom.choose(4., 8.1838);
            double realFreq = JMSLRandom.choose(0.1, 25) * (1 - JMSLRandom.choose(0.1, 1));
            double cut = Math.abs(JMSLRandom.choose(25, 1200) * 2 * Math.random());
            double rez = JMSLRandom.choose(25, 50);
            double rate = 1;
            s2.add(duration, pitch, amplitude, hold, realFreq, cut, rez, rate);
            //			System.out.println(" fpDur " + duration +" fpHz "+ realFreq + "
            // "+
            //					" amp = " + amplitudeSetting +
            //					" cutoff hz = " + cut );
        } //end music shape s2

    }

    public void buildShape3() {

        s3 = new MusicShape(ins3.getDimensionNameSpace());
        s3.setRepeats(7);
        s3.setRepeatPause(1000);
        s3.setInstrument(ins3);
        s3.launch(JMSL.now() + 15);

        for (int i = 0; i < 500; i++) {

            double muting[] = { 0, 0, 0, 1, 0, 0, 0.2, 0, };
            double muter = muting[JMSLRandom.choose(0, muting.length - 1)];

            // poocher
            double duration = JMSLRandom.choose(1.234, 5.33) - JMSLRandom.choose();
            double pitch = JMSLRandom.choose(1, 8);
            double amplitude = muter * 0.3;
            double hold = JMSLRandom.choose(5.4, 8.3) + JMSLRandom.choose();
            double realFreq = JMSLRandom.choose(110, 440);
            double cut = JMSLRandom.choose(500, 900);
            double rez = JMSLRandom.choose(25, 40);
            double rate = 1;
            s3.add(duration, pitch, amplitude, hold, realFreq, cut, rez, rate);
            //			System.out.println("poDur " + duration +" amp = " + amplitudeSetting +
            //					" poHz "+ realFreq +
            //					" poCut hz = " + cut+
            //					" poRez " + rez);
        } //end music shape s3

    }

    public void buildShape4() {
        s4 = new MusicShape(ins4.getDimensionNameSpace());
        s4.setRepeats(5);
        s4.setRepeatPause(10);
        s4.setInstrument(ins4);
        s4.launch(JMSL.now() + 25);

        for (int i = 0; i < 633; i++) {

            double muting[] = { 0, 0, 0, 0.2, 0, 0, 0, 0, 1, 0, 0, };
            double muter = muting[JMSLRandom.choose(0, muting.length - 1)];

            // windString
            double duration = JMSLRandom.choose(1.3, 4.5) + JMSLRandom.choose();
            double pitch = JMSLRandom.choose(1, 8);
            double amplitude = 0.7 * muter;
            double hold = JMSLRandom.choose(5.1, 8.);
            double realFreq = 7000;
            double cut = JMSLRandom.choose(900, 9000);
            double rez = JMSLRandom.choose(25, 45);
            double rate = 1;
            s4.add(duration, pitch, amplitude, hold, realFreq, cut, rez, rate);
            //			System.out.println("wsDur " + duration +" wsAmp = " + amplitudeSetting +
            //					" wsHz "+ realFreq +
            //					" wsCut hz = " + cut+
            //					" wsRez " + rez);
        } //end music shape s4

    }

    public void handelStart() {
        buildShape1();
        buildShape2();
        buildShape3();
        buildShape4();
    }

    public void handelStop() {
        System.out.println("this doesn't do anything yet");
    }

    void buildGUI() {
        Panel p = new Panel();
        setLayout(new BorderLayout());
        p.setLayout(new GridLayout(0, 1));
        p.add(mixer.getPanAmpControlPanel());
        p.add(startButton = new Button("START"));
        p.add(stopButton = new Button("STOP"));

        add(BorderLayout.NORTH, p);
        add(BorderLayout.SOUTH, mixer.getPanAmpControlPanel());
        startButton.addActionListener(this);
        stopButton.addActionListener(this);

    }

    public void actionPerformed(ActionEvent ev) {
        Object source = ev.getSource();
        if (source == startButton) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            handelStart();

        }
        if (source == stopButton) {
            //stopButton.setEnabled(false);
            //startButton.setEnabled(true);
            handelStop();
        }
    }

    public void init() {
        JMSL.setIsApplet(true);
    }

    public void stop() {
        s.finishAll();
        s2.finishAll();
        s3.finishAll();
        s4.finishAll();
        removeAll();
        JMSL.closeMusicDevices();
    }

}