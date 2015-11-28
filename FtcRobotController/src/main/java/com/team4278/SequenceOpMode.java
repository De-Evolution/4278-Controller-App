package com.team4278;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import java.util.LinkedList;

/**
 * Autonomous program which runs SequenceSteps
 */
public class SequenceOpMode extends OpMode
{

	enum State
	{
		INITIALIZING,
		RUNNING,
		DONE
	}

	protected LinkedList<SequenceStep> stepsList;

	protected SequenceStep currentStep;

	protected State currentState;

	/**
	 *
	 * @param steps the list of SequenceSteps, and MultiSteps, to execute
	 */
	public SequenceOpMode(Object... steps)
	{
		stepsList = new LinkedList<SequenceStep>();

		MultiStep.addActualSteps(stepsList, steps);

		//don't start if we have no commands
		if(stepsList.isEmpty()) //this is normal if we are used by ButtonListenerTeleop, so it's not an error case
		{
			currentState = State.DONE;
		}
		else
		{
			currentState = State.INITIALIZING;
		}
	}

	@Override
	public void init()
	{
		//do nothing
	}

	@Override
	public void loop()
	{
		switch(currentState)
		{
			case INITIALIZING:
				currentStep = stepsList.pop();
				currentStep.init();
				currentState = State.RUNNING;
				break;

			case RUNNING:
				if(!currentStep.loop())
				{
					currentStep.end();

					if(stepsList.isEmpty())
					{
						currentState = State.DONE;
					}
					else
					{
						currentState = State.INITIALIZING;
					}
				}
		}
	}

	@Override
	public void stop()
	{
		super.stop();

		//if we were aborted in the middle of a step, halt it prematurely
		if(currentState == State.RUNNING)
		{
			currentStep.end();
		}
	}
}
