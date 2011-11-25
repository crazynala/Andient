package jnissa;

import com.softsynth.jsyn.*;
/**
 * Reverberation
 * This simple reverb uses one pole low pass filters in the feedback
 * loop of several delay lines.
 *
 * jnissa.Reverb1 has a bus style input so multiple sources can be easily mixed.
 *
 * @author (C) 1997 Phil Burk, SoftSynth.com, All Rights Reserved
 */


class Reverb1Element extends SynthCircuit
{
	DelayUnit         myDelay;
	Filter_1o1p       myFilter;
	MultiplyAddUnit   myFeedback;
	MultiplyAddUnit   myMixer;
	
/** Input signal to be reverberated. */
	SynthInput        input;
/**
 * Signal to be mixed with this units output. Generally
 * the output of another jnissa.Reverb1Element.
 */
	SynthInput        mix;
/** Scale the output of this units delay line. */
	SynthInput        amplitude;
/* Amount of this unit's delayed output that gets fed back into delay line. */
	SynthInput        feedback;

	public Reverb1Element( double delayTime )
	throws SynthException
	{
		super();

		add( myDelay       = new DelayUnit(delayTime));
		add( myFilter      = new Filter_1o1p());
		add( myFeedback    = new MultiplyAddUnit());
		add( myMixer       = new MultiplyAddUnit());

		myDelay.output.connect( myMixer.inputA );
		myDelay.output.connect( myFilter.input );
		myFilter.output.connect( myFeedback.inputA );
		myFeedback.output.connect( myDelay.input );

		addPort( feedback = myFeedback.inputB, "Feedback" );
		addPort( input = myFeedback.inputC, "Input" );
		addPort( amplitude = myMixer.inputB, "Amplitude" );
		addPort( mix = myMixer.inputC, "Mix" );
		addPort( output = myMixer.output );

		feedback.set( -0.90 );
	}
}

public class Reverb1 extends SynthCircuit
{
	final static int NUM_ELEMENTS = 6; // 6 recommended by Moore
	Reverb1Element    delays[] = new Reverb1Element[NUM_ELEMENTS];  
	MultiplyAddUnit   myMixer;
	BusReader         busIn;
	double times[] = { 0.050, 0.056, 0.061, 0.068, 0.072, 0.078 };

	public SynthBusInput   busInput;
	public SynthInput      dryGain;

	public Reverb1() 
	throws SynthException
	{

		add( busIn   = new BusReader());
		add( myMixer = new MultiplyAddUnit());

		for( int i=0; i<delays.length; i++ )
		{
			Reverb1Element delay = new Reverb1Element( times[i] );
			delays[i] = delay;
			add( delay );
			busIn.output.connect( delay.input );
			delay.amplitude.set( 0.9/NUM_ELEMENTS );
			if( i>0 ) delays[i-1].output.connect( delays[i].mix );
		}

		busIn.output.connect( myMixer.inputA );
		delays[ NUM_ELEMENTS-1 ].output.connect( myMixer.inputC );
			
		addPort( busInput = busIn.busInput );
		addPort( dryGain = myMixer.inputB, "dryGain" );
		addPort( output = myMixer.output );
		dryGain.set( 0.7 );
		compile();
	}
}
