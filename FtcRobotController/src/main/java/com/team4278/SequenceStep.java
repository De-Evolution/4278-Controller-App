package com.team4278;

import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.utils.RoboLog;

import java.util.LinkedList;

/**
 * Class representing a step in a sequence.
 *
 * It uses a structure very similar to an OpMode, as well as the Commands in FRC WPILib.
 * The hardware will be updated after init() and every invocation of loop().
 */
public abstract class SequenceStep
{
	protected String className;

	//set to the current time when the step is initialized
	private long startTime;

	private long timeLimit;

	private boolean isTimed;

	boolean wasTimeKilled;

	//flag used during execution to tell if the step's prerequisites have already been added to the queue
	boolean stepsBeforeAdded;

	//these three lists are lazily evaluated to determine which steps will be executed next
	private LinkedList<SequenceStep> stepsBefore, stepsAfter, stepsParallel;

	public SequenceStep()
	{
		className = getClass().getSimpleName();

		stepsBefore = new LinkedList<SequenceStep>();
		stepsParallel = new LinkedList<SequenceStep>();
		stepsAfter = new LinkedList<SequenceStep>();

		stepsBeforeAdded = false;

		isTimed = false;
	}

	/**
	 * Construct the step with a time limit after which it will be killed.
	 *
	 * You can call wasTimeKilled() in end() to determine if the command was stopped because it hit the time limit.
	 * @param timeLimit The time limit in milliseconds.
	 */
	public SequenceStep(long timeLimit)
	{
		this();

		this.timeLimit = timeLimit;

		isTimed = true;
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
			return timeLimit;
		}
		else
		{
			return -1;
		}
	}

	/**
	 * @return How long the command has been running, in milliseconds
	 */
	public long getRunTime()
	{
		return System.currentTimeMillis() - startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public boolean isTimed()
	{
		return isTimed;
	}

	public LinkedList<SequenceStep> getStepsBefore()
	{
		return stepsBefore;
	}

	public LinkedList<SequenceStep> getStepsAfter()
	{
		return stepsAfter;
	}

	public LinkedList<SequenceStep> getStepsParallel()
	{
		return stepsParallel;
	}

	/**
	 * Add a step which will be executed before this one.
	 * @param stepToAdd
	 */
	public void addStepBefore(SequenceStep stepToAdd)
	{
		stepsBefore.add(stepToAdd);
	}

	/**
	 * Add a step which will be executed directly after this one.
	 * @param stepToAdd
	 */
	public void addStepAfter(SequenceStep stepToAdd)
	{
		stepsAfter.add(stepToAdd);
	}

	/**
	 * Add a step which will be executed at the same time as this one.
	 * @param stepToAdd
	 */
	public void addStepParallel(SequenceStep stepToAdd)
	{
		stepsParallel.add(stepToAdd);
	}

	/**
	 * Called once when the step is started.
	 */
	public abstract void init();

	/**
	 * Because sometimes, you just need two hardware cycles to init properly.
	 */
	public void second_init() {};

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
