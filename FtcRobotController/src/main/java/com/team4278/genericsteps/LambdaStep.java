package com.team4278.genericsteps;

import com.team4278.SequenceStep;

/**
 * Step which runs a Runnable during init when it executes
 */
public class LambdaStep extends SequenceStep
{
	Runnable toRun;

	public LambdaStep(Runnable toRun)
	{
		this.toRun = toRun;
	}

	@Override
	public void init()
	{

	}

	@Override
	public boolean loop()
	{
		toRun.run();
		return false;
	}

	@Override
	public void end()
	{

	}
}
