package com.jsyn.examples;

import java.awt.BorderLayout;

import javax.swing.JApplet;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.DoubleTable;
import com.jsyn.instruments.WaveShapingVoice;
import com.jsyn.scope.AudioScope;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.PseudoRandom;
import com.jsyn.util.VoiceAllocator;
import com.jsyn.util.VoiceFactory;
import com.softsynth.jsyn.EqualTemperedTuning;
import com.softsynth.math.ChebyshevPolynomial;
import com.softsynth.math.PolynomialTableData;
import com.softsynth.shared.time.TimeStamp;
import com.jsyn.swing.JAppletFrame;

/***************************************************************
 * Play notes using a WaveShapingVoice. Allocate the notes using a
 * VoiceAllocator.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class ChebyshevSong extends JApplet implements Runnable
{
	private static final long serialVersionUID = -7459137388629333223L;
	private DoubleTable table;
	private Synthesizer synth;
	private Add mixer;
	private LineOut lineOut;
	private AudioScope scope;
	private boolean go = false;
	private PseudoRandom pseudo = new PseudoRandom();
	private final static int MAX_VOICES = 8;
	private final static int MAX_NOTES = 5;
	private com.jsyn.examples.ChebyshevSong.MyVoiceFactory factory;
	private VoiceAllocator allocator;
	private final static int scale[] = { 0, 2, 4, 7, 9 }; // pentatonic scale
	private final static int CHEBYSHEV_ORDER = 11;

	/* Can be run as either an application or as an applet. */
	public static void main( String args[] )
	{
		ChebyshevSong applet = new ChebyshevSong();
		JAppletFrame frame = new JAppletFrame( "ChebyshevSong", applet );
		frame.setSize( 640, 300 );
		frame.setVisible( true );
		frame.test();
	}

	class MyVoiceFactory implements VoiceFactory
	{
		public UnitVoice createVoice( int tag )
		{
			WaveShapingVoice voice = new WaveShapingVoice();
			voice.function.set( table );
			synth.add( voice );
			// TODO Pass in time so we do not start pulling data too soon.
			voice.getOutput().connect( mixer.inputA );
			return voice;
		}
	}

	/*
	 * Setup synthesis.
	 */
	public void start()
	{
		setLayout( new BorderLayout() );

		synth = JSyn.createSynthesizer();

		// Make table with Chebyshev polynomial to share among voices
		PolynomialTableData chebData = new PolynomialTableData(
				ChebyshevPolynomial.T( CHEBYSHEV_ORDER ), 1024 );
		table = new DoubleTable( chebData.getData() );

		// Create a voice allocator and connect it to a LineOut.
		allocator = new VoiceAllocator( MAX_VOICES );
		factory = new MyVoiceFactory();
		allocator.setVoiceFactory( factory );

		// Use a submix so we can show it on the scope.
		synth.add( mixer = new Add() );
		synth.add( lineOut = new LineOut() );

		mixer.output.connect( 0, lineOut.input, 0 );
		mixer.output.connect( 0, lineOut.input, 1 );

		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();
		lineOut.start();

		// Use a scope to show the mixed output.
		scope = new AudioScope( synth );
		scope.addProbe( mixer.output );
		scope.setTriggerMode( AudioScope.TriggerMode.NORMAL );
		scope.getView().setShowControls( true );
		add( BorderLayout.CENTER, scope.getView() );
		scope.start();

		/* Synchronize Java display. */
		getParent().validate();
		getToolkit().sync();

		// start thread that plays notes
		Thread thread = new Thread( this );
		go = true;
		thread.start();

	}

	public void stop()
	{
		// tell song thread to finish
		go = false;
		removeAll();
		synth.stop();
	}

	double indexToFrequency( int index )
	{
		int octave = index / scale.length;
		int temp = index % scale.length;
		int pitch = scale[temp] + (12 * octave);
		return EqualTemperedTuning.getMIDIFrequency( (int) (pitch + 16) );
	}

	private void noteOff( double time, int noteNumber )
	{
		UnitVoice voice = allocator.off( noteNumber );
		if( voice != null )
		{
			voice.noteOff( new TimeStamp( time ) );
		}
	}

	private void noteOn( double time, int noteNumber )
	{
		UnitVoice voice = allocator.allocate( noteNumber );
		double frequency = indexToFrequency( noteNumber );
		double amplitude = 0.1;
		TimeStamp timeStamp = new TimeStamp( time );
		voice.noteOn( timeStamp, frequency, amplitude );
	}

	public void run()
	{
		// always choose a new song based on time&date
		int savedSeed = (int) System.currentTimeMillis();
		// calculate tempo
		double duration = 0.2;
		// set time ahead of any system latency
		double advanceTime = 0.5;
		// time for next note to start
		double nextTime = synth.getCurrentTime() + advanceTime;
		// note is ON for half the duration
		double onTime = duration / 2;
		int beatIndex = 0;
		try
		{
			do
			{
				// on every measure, maybe repeat previous pattern
				if( (beatIndex & 7) == 0 )
				{
					if( (Math.random() < (1.0 / 2.0)) )
						pseudo.setSeed( savedSeed );
					else if( (Math.random() < (1.0 / 2.0)) )
						savedSeed = pseudo.getSeed();
				}

				// Play a bunch of random notes in the scale.
				int numNotes = pseudo.choose( MAX_NOTES );
				for( int i = 0; i < numNotes; i++ )
				{
					int noteNumber = pseudo.choose( 30 );
					noteOn( nextTime, noteNumber );
					noteOff( nextTime + onTime, noteNumber );
				}
		
				nextTime += duration;
				beatIndex += 1;

				// wake up before we need to play note to cover system latency
				synth.sleepUntil( nextTime - advanceTime );
			} while( go );
		} catch( InterruptedException e )
		{
			System.err.println( "Song exiting. " + e );
		}
	}
}
