package com.team4278;

import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.utils.RoboLog;

/**
 * Class representing a step in a sequence.
 *
 * It uses a structure very similar to an OpMode, as well as the Commands in FRC WPILib.
 * The hardware will be updated after init() and every invocation of loop().
 */
public abstract class SequenceStep
{
	protected String className;

	private long timeLimit;

	private boolean isTimed;

	boolean wasTimeKilled;

	public SequenceStep()
	{
		className = getClass().getSimpleName();
	}

	/**
	 * Construct the step with a time limit after which it will be killed.
	 *
	 * You can call wasTimeKilled() in end() to determine if the command was stopped because it hit the time limit.
	 * @param timeLimit THe time limit in milliseconds.
	 */
	public SequenceStep(long timeLimit)
	{
		this.timeLimit = timeLimit;
		className = getClass().getSimpleName();
	}


	/**
	 * Print a telemetry message on the DS with the sequence's name as its tag.
	 *
	 * Useful for showing progress information, etc.
	 * @param message
	 */
	protected void telemetryMessage(String message)
	{
		if(RoboLog.telemetryToUse != null)
		{
			RoboLog.telemetryToUse.addData(className, message);
		}
	}

	protected boolean wasTimeKilled()
	{
		return wasTimeKilled;
	}

	/**
	 * Gets the time limit of the command in milliseconds.
	 *
	 * If it is not timed, returns -1
	 * @return
	 */
	public long getTimeLimit()
	{
		if(isTimed)
		{
			return -1;
		}
		else
		{
			return timeLimit;
		}
	}

	public boolean isTimed()
	{
		return isTimed;
	}


	/**
	 * Called once when the step is started.
	 */
	public abstract void init();

	/**
	 * Called repeatedly while the step is running.
	 *
	 * @return true if the command should keep running, false if it should stop.
	 */
	public abstract boolean loop();

	/**
	 * Called to shut down the step, either because loop()returned false or because the user aborted the program.
	 */
	public abstract void end();
}
