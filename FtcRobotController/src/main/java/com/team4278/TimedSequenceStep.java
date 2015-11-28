package com.team4278;

import com.qualcomm.robotcore.robocol.Telemetry;

/**
 * SequenceStep which will terminate after a certain time.
 *
 * Instead of loop(), it has loopTimed().  If loopTimed() returns false, the step will end like normal.
 *
 */
public abstract class TimedSequenceStep extends SequenceStep
{
	private final long timeToRun;

	protected long startTime;

	/**
	 *
	 * @param telemetry
	 * @param timeToRun How long, in milliseconds, the step should run for.
	 */
	public TimedSequenceStep(Telemetry telemetry, long timeToRun)
	{
		super(telemetry);

		startTime = System.currentTimeMillis();

		this.timeToRun = timeToRun;
	}

	@Override
	public boolean loop()
	{
		return System.currentTimeMillis() - startTime < timeToRun && loopTimed();
	}

	protected abstract boolean loopTimed();
}
