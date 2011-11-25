package andient;

import andient.player.Player;
import andient.player.PlayerTypeEnum;
import slider.RangeSlider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Logger;

/**
 * User: dan
 * Date: 11/21/11
 */
public class PlayerPanel implements StatusListener {
    public static String[] INTRUMENT_NAMES = {"--", "Strummer", "Floater", "Doodler", "Chunker", "Bass"};

    private Player player;

    private JCheckBox enableCheckBox;
    private JComboBox playerTypeComboBox;
    private JSlider amplitudeSlider;
    private JPanel rootPanel;
    private RangeSlider thresholdRangeSlider;
    private JLabel statusDisplay;

    private final static Logger logger = Logger.getLogger(PlayerPanel.class.getName());

    public PlayerPanel() {
        amplitudeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                if (!amplitudeSlider.getValueIsAdjusting() && player != null) {
                    player.setAmplitudeSetting(amplitudeSlider.getValue());
                }
            }
        });
        thresholdRangeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                if (!thresholdRangeSlider.getValueIsAdjusting() && player != null) {
                    player.setTriggerThreshold(thresholdRangeSlider.getValue(), thresholdRangeSlider.getUpperValue());
                }
            }
        });
        playerTypeComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    logger.finest("Handling SELECTED event: " + itemEvent);
                    createPlayerType((PlayerTypeEnum) itemEvent.getItem());
                } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                    logger.finest("Handling DESELECTED event: " + itemEvent);
                    destroyActivePlayer();
                }
            }
        });
        enableCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                if (player != null) {
                    player.setMuted(enableCheckBox.isSelected());
                }
            }
        });
    }

    private void createPlayerType(PlayerTypeEnum playerType) {
        if (player != null) {
            destroyActivePlayer();   // clean up just in case
        }
        logger.fine("Creating player of type: " + playerType);
        if (playerType != PlayerTypeEnum.NULL) {
            player = Orchestra.getInstance().createAndRegisterPlayer(playerType);
        }
        if (player != null) {
            player.setStatusListener(this);
            player.start();
        }
    }

    private void destroyActivePlayer() {
        logger.fine("Destroying active player: " + player);
        statusDisplay.setText("");
        if (player == null) return; // nothing to do

        Orchestra.getInstance().deregisterPlayer(player);
        player = null;
    }

    public void setData(SampleBean data) {
        enableCheckBox.setSelected(data.isItsEnabled());
    }

    public void getData(SampleBean data) {
        data.setItsEnabled(enableCheckBox.isSelected());
    }

    public boolean isModified(SampleBean data) {
        if (enableCheckBox.isSelected() != data.isItsEnabled()) return true;
        return false;
    }

    private void createUIComponents() {
        playerTypeComboBox = new JComboBox(PlayerTypeEnum.values());
    }

    public void notifyStatus(String status) {
        statusDisplay.setText(status);
    }
}
