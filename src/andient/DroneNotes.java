package andient;/* andient.DroneNotes -- notes that drone
 * part of the andient.jlooch app
 *
 *		Brad Garton	10/2001
 */

import andient.player.component.BradUtils;
import com.softsynth.jsyn.*;

import java.util.logging.Logger;

class DroneThread extends Thread {
    private final static Logger logger = Logger.getLogger(DroneThread.class.getName());
    public double prob = 1.0;
    boolean go = true;
    boolean running = false;
    int threadNum;
    TableOscillator[] droneOsc = new TableOscillator[2];
    AddUnit oscMixer;
    SynthTable oscTable;
    EnvelopePlayer droneEnvPlayer;
    SynthEnvelope droneEnv;
    LineOut droneOut;
    double[] tData = new double[257];
    Filter_LowPass droneFilt;
    DelayUnit del1;
    DelayUnit del2;
    AddUnit del1inMixer;
    AddUnit outAMixer;
    MultiplyUnit feedBack;
    EnvelopePlayer filtEnvPlayer;
    SynthEnvelope filtEnv;
    double[] fData = new double[4];
    double[] notes =
            {
                    5.05, 5.07, 5.10, 6.00, 6.05, 6.10,
                    7.00, 7.03, 7.05, 7.07, 7.10, 8.00,
                    8.02, 8.03, 8.05, 8.07, 8.10, 9.00
            };

    public DroneThread() throws SynthException {
        int i;

        droneOsc[0] = new TableOscillator();
        droneOsc[1] = new TableOscillator();
        oscMixer = new AddUnit();
        droneFilt = new Filter_LowPass();
        del1 = new DelayUnit(0.35);
        del2 = new DelayUnit(0.49);
        droneEnvPlayer = new EnvelopePlayer();
        filtEnvPlayer = new EnvelopePlayer();
        droneOut = new LineOut();
        del1inMixer = new AddUnit();
        outAMixer = new AddUnit();
        feedBack = new MultiplyUnit();


        droneOsc[0].output.connect(oscMixer.inputA);
        droneOsc[1].output.connect(oscMixer.inputB);
        oscMixer.output.connect(droneFilt.input);
        filtEnvPlayer.output.connect(droneFilt.frequency);
        droneFilt.output.connect(droneEnvPlayer.amplitude);
        droneEnvPlayer.output.connect(outAMixer.inputA);
        del2.output.connect(outAMixer.inputB);
        del2.output.connect(feedBack.inputA);
        outAMixer.output.connect(0, droneOut.input, 0);
        droneEnvPlayer.output.connect(del1inMixer.inputA);
        feedBack.output.connect(del1inMixer.inputB);
        del1inMixer.output.connect(del1.input);
        del1.output.connect(0, droneOut.input, 1);
        del1.output.connect(del2.input);

        double[] data =
                {
                        5.0, 1.0,
                        5.0, 0.0
                };
        droneEnv = new SynthEnvelope(data);

        fData[0] = 5.0;
        fData[1] = 500.0;
        fData[2] = 5.0;
        fData[3] = 100.0;
        filtEnv = new SynthEnvelope(fData);

        for (i = 0; i < 256; i++) {
            tData[i] = Math.random() * 2.0 - 1.0;
        }
        tData[256] = tData[0];
        oscTable = new SynthTable(tData);

        droneOsc[0].tablePort.setTable(oscTable);
        droneOsc[1].tablePort.setTable(oscTable);

    }

    void stopSound() {
        try {
            droneOut.stop();
            droneOsc[0].stop();
            droneOsc[1].stop();
            oscMixer.stop();
            del1inMixer.stop();
            outAMixer.stop();
            feedBack.stop();
            droneFilt.stop();
            del1.stop();
            del2.stop();
            droneEnvPlayer.stop();
            filtEnvPlayer.stop();
        } catch (SynthException e) {
            System.err.println(e);
        }
    }

    public void halt() {
        go = false;
        interrupt();
    }

    private void setRunning(boolean runState) {
        if (running != runState) {
            running = runState;
            DroneNotes.logThreadStatus();
        }
    }

    public void run() {
        double freq;
        double wait;
        int basewait = 0;
        int i;

        try {
            droneEnvPlayer.start();
            filtEnvPlayer.start();
            droneOsc[0].start();
            droneOsc[1].start();
            oscMixer.start();
            del1inMixer.start();
            outAMixer.start();
            feedBack.start();
            droneFilt.start();
            del1.start();
            del2.start();
            droneOut.start();

            while (go == true) {
                droneOsc[0].amplitude.set(0.1);
                droneOsc[1].amplitude.set(0.1);
                freq = BradUtils.cpspch(BradUtils.chooseItem(notes));
                droneOsc[0].frequency.set(BradUtils.windowfreq(freq, 0.017));
                droneOsc[1].frequency.set(BradUtils.windowfreq(freq, 0.017));

                droneFilt.frequency.set(100.0);
                droneFilt.amplitude.set(1.0);
                droneFilt.Q.set(BradUtils.crandom(1.0, 15.0));
                feedBack.inputB.set(0.92);
                fData[0] = 5.0;
                fData[1] = BradUtils.crandom(100.0, 900.0);
                fData[2] = 5.0;
                fData[3] = BradUtils.crandom(100.0, 500.0);
                filtEnv.write(fData);
                filtEnvPlayer.envelopePort.clear();
                filtEnvPlayer.envelopePort.queue(filtEnv);
                droneEnvPlayer.envelopePort.clear();
                droneEnvPlayer.envelopePort.queue(droneEnv);

                wait = Math.random() * 10.0 + 7.0;
                wait = wait * 689.0;
                basewait = basewait + (int) wait;
                Synth.sleepUntilTick(basewait);

                setRunning(false);
                while (Math.random() > prob) {
                    basewait = basewait + (15 * 689);
                    Synth.sleepUntilTick(basewait);
                }
                setRunning(true);

                for (i = 0; i < 256; i++) {
                    tData[i] = Math.random() * 2.0 - 1.0;
                }
                tData[256] = tData[0];
                oscTable.write(tData);
            }
        } catch (SynthException e) {
            System.err.println(e);
        }
        stopSound();
    }

}

public class DroneNotes extends Thread {
    public final static int NUM_DRONE_THREADS = 10;
    private final static Logger logger = Logger.getLogger(DroneNotes.class.getName());

    static DroneThread[] drones = new DroneThread[NUM_DRONE_THREADS];

    public void start() {
        try {
            for (int i = 0; i < drones.length; i++) {
                drones[i] = new DroneThread();
                drones[i].threadNum = i;
                drones[i].start();

            }
        } catch (SynthException e) {
            SynthAlert.showError(e);
        }
    }

    public void stopSound() {
        try {
            for (int i = 0; i < drones.length; i++) {
                drones[i].stopSound();
            }
        } catch (SynthException e) {
            SynthAlert.showError(e);
        }
    }

    public void setProb(double p) {
        for (int i = 0; i < drones.length; i++) {
            drones[i].prob = p;
        }
    }

    static public void logThreadStatus() {
        StringBuffer status = new StringBuffer();
        for (int i = 0; i < drones.length; i++) {
            if (drones[i].go == false)
                status.append("X");
            else if (drones[i].running)
                status.append("+");
            else
                status.append("-");
        }
        logger.fine("Drone thread status: " + status);
    }
}
