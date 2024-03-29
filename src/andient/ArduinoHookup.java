package andient;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.logging.Logger;

public class ArduinoHookup implements SerialPortEventListener {
    public static final boolean OUTPUT_ARDUINO_DATA = true;

    SerialPort serialPort;
    KnobListener listener;
    byte[] byteBufferArray;

    private final static Logger logger = Logger.getLogger(ArduinoHookup.class.getName());

    /**
     * The port we're normally going to use.
     */
    private static final String PORT_NAMES[] = {
            "/dev/tty.usbmodemfa131"
    };
    /**
     * Buffered input stream from the port
     */
    private InputStream input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 9600;

    public void initialize(KnobListener listener) {
        this.listener = listener;
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // iterate through, looking for the port
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }

        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    // has carriage return & new line
    boolean hasCRNL(byte[] bytearray) {
        return (ArrayUtils.contains(bytearray, (byte) 0xd) && ArrayUtils.contains(bytearray, (byte) 0xa));
    }

    int indexOfCR(byte[] bytearray) {
        return ArrayUtils.indexOf(bytearray, (byte) 0xd);
    }

    String readTrimmedFirstLine(byte[] bytearray) {
        if (hasCRNL(bytearray)) {
            int crIndex = indexOfCR(bytearray);
            return new String(ArrayUtils.subarray(bytearray, 0, crIndex)); // endIndex is exclusive, so chop off the CR & NL
        } else return null;
    }

    byte[] removeFirstLine(byte[] bytearray) {
        if (hasCRNL(bytearray)) {
            return (ArrayUtils.subarray(bytearray, indexOfCR(bytearray) + 2, bytearray.length));
        }
        return null;
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int available = input.available();
                byte[] chunk = new byte[available];
                input.read(chunk, 0, available);

                byteBufferArray = ArrayUtils.addAll(byteBufferArray, chunk);

                if (hasCRNL(byteBufferArray)) {
                    String data = readTrimmedFirstLine(byteBufferArray);
                    byteBufferArray = removeFirstLine(byteBufferArray);
                    if (OUTPUT_ARDUINO_DATA) {
                        System.out.println(ArrayUtils.toString(byteBufferArray) + " -> '" + ArrayUtils.toString(data) + " -> " + new String(data).trim());
                    }
                    if (data.length() > 0) {
                        try {
                            if (listener != null) {
                                listener.onKnobNotify(Integer.valueOf(data));
                            }
                        } catch (NumberFormatException e) {
                            // swallow it
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public static void main(String[] args) throws Exception {
        ArduinoHookup main = new ArduinoHookup();
        main.initialize(null);
        System.out.println("Started");
    }
}

