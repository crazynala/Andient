package com.jsyn.examples;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JApplet;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.swing.DoubleBoundedRangeModel;
import com.jsyn.swing.PortModelFactory;
import com.jsyn.swing.RotaryTextController;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.swing.JAppletFrame;

/**
 * Play a tone using a JSyn oscillator. Modulate the amplitudeSetting using a DAHDSR
 * envelope.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * 
 */
public class HearDAHDSR extends JApplet
{
	private static final long serialVersionUID = -2704222221111608377L;
	private Synthesizer synth;
	private UnitOscillator osc;
	// Use a square wave to trigger the envelope.
	private UnitOscillator gatingOsc;
	private EnvelopeDAHDSR dahdsr;
	private LineOut lineOut;

	public void init()
	{
		synth = JSyn.createSynthesizer();
		
		// Add a tone generator.
		synth.add( osc = new SineOscillator() );
		// Add a trigger.
		synth.add( gatingOsc = new SquareOscillator() );
		// Use an envelope to control the amplitudeSetting.
		synth.add( dahdsr = new EnvelopeDAHDSR() );
		// Add an output mixer.
		synth.add( lineOut = new LineOut() );

		gatingOsc.output.connect( dahdsr.input );
		dahdsr.output.connect( osc.amplitude );
		osc.output.connect( 0, lineOut.input, 0 );

		gatingOsc.frequency.set( 0.5 );
		
		// Arrange the knob in a row.
		setLayout( new GridLayout( 1, 0 ) );

		setupPortKnob( osc.frequency, 500.0, "Freq" );
		setupPortKnob( gatingOsc.frequency, 5.0, "Rate" );
		setupPortKnob( dahdsr.attack, 10.0 );
		setupPortKnob( dahdsr.hold, 10.0 );
		setupPortKnob( dahdsr.decay, 20.0 );
		setupPortKnob( dahdsr.sustain, 1.0 );
		setupPortKnob( dahdsr.release, 20.0 );

		validate();
	}

	private void setupPortKnob( UnitInputPort port, double max )
	{
		setupPortKnob( port, max, port.getName() );
	}

	private void setupPortKnob( UnitInputPort port, double max, String label )
	{
		port.setMinimum( 0.0 );
		port.setMaximum( max );

		DoubleBoundedRangeModel model = PortModelFactory
				.createExponentialModel( port );
		System.out.println("Make knob for " + label + ", model.getDV = " + model.getDoubleValue()
				+ ", model.getV = " + model.getValue()
				+ ", port.getV = " + port.getValue()
				);
		RotaryTextController knob = new RotaryTextController( model, 10 );
		knob.setBorder( BorderFactory.createTitledBorder( label ) );
		knob.setTitle( label );
		add( knob );
	}

	public void start()
	{
		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();
		// We only need to start the LineOut. It will pull data from the
		// oscillator.
		synth.startUnit( lineOut );
	}

	public void stop()
	{
		synth.stop();
	}

	/* Can be run as either an application or as an applet. */
	public static void main( String args[] )
	{
		HearDAHDSR applet = new HearDAHDSR();
		JAppletFrame frame = new JAppletFrame( "Hear DAHDSR Envelope", applet );
		frame.setSize( 640, 200 );
		frame.setVisible( true );
		frame.test();
	}

}
