package andient;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MainFrame extends JDialog implements KnobListener {
    private JPanel contentPane;
    private JButton buttonQuit;
    private PlayerPanel instrumentPanel1;
    private PlayerPanel instrumentPanel2;
    private JSlider triggerSlider;
    ArduinoHookup arduinoHookup = new ArduinoHookup();

    public MainFrame() {
        Orchestra.getInstance().init();
        arduinoHookup.initialize(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonQuit);

        buttonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        triggerSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                Orchestra.getInstance().setTriggerLevel(triggerSlider.getValue());
            }
        });
        triggerSlider.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                triggerSlider.setValue(triggerSlider.getValue() + mouseWheelEvent.getUnitsToScroll());
            }
        });
    }

    private void onOK() {
        dispose();
    }

    public static void main(String[] args) {
        MainFrame dialog = new MainFrame();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void onKnobNotify(int value) {
        if (value != triggerSlider.getValue()) {
            triggerSlider.setValue(value);
        }
    }
}
