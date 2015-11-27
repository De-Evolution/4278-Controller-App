package com.qualcomm.ftcrobotcontroller.sequence;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jamie on 11/25/2015.
 */
public class SequenceAutonomous extends OpMode
{

	enum State
	{
		INITIALIZING,
		RUNNING
	}

	List<SequenceStep> stepsList;

	Iterator<SequenceStep> stepsIterator;

	SequenceStep currentStep;

	State currentState;

	public SequenceAutonomous(SequenceStep... steps)
	{
		stepsList = Arrays.asList(steps);

		stepsIterator = stepsList.iterator();

		currentState = State.INITIALIZING;
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
				currentStep = stepsIterator.next();
				currentStep.init();
				currentState = State.RUNNING;
				break;

			case RUNNING:
				currentStep.loop();

				if(currentStep.isFinished())
				{
					currentStep.end();
					currentState = State.INITIALIZING;
				}
		}
	}
}
