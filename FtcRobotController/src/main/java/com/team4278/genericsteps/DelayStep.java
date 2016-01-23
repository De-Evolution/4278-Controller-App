package com.team4278.genericsteps;

import com.team4278.SequenceStep;

/**
 * Step which waits for a certain amount of time
 */
public class DelayStep extends SequenceStep
{

	public DelayStep(long delayMilliseconds)
	{
		super(delayMilliseconds);
	}

	@Override
	public void init()
	{

	}

	@Override
	public boolean loop()
	{

		return true;
	}

	@Override
	public void end(EndReason reason)
	{

	}
}
