package com.team4278;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.team4278.utils.RoboLog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Autonomous program which runs SequenceSteps
 */
public abstract class SequenceOpMode extends OpMode
{

	enum State
	{
		INITIALIZING,
		POSTINIT,
		RUNNING,
		ENDING,
		DONE
	}

	class SequenceThread
	{
		SequenceStep currentStep;

		LinkedList<SequenceStep> steps;

		long currentStepStartTime;

		State state;

		public SequenceThread(LinkedList<SequenceStep> steps)
		{
			this.steps = steps;

			state = State.INITIALIZING;
		}


		/**
		 * Recursively expands prerequisites of the first step in the list, leaving the correct next step at position 0.
		 */
		private void expandFirstStep()
		{
			SequenceStep nextStep = steps.get(0);

			if(!nextStep.stepsBeforeAdded && !nextStep.getStepsBefore().isEmpty())
			{
				steps.addAll(0, nextStep.getStepsBefore());

				nextStep.stepsBeforeAdded = true;

				expandFirstStep();
			}
		}
	}

	protected HashSet<SequenceThread> threads;

	public SequenceOpMode()
	{
		super();
		threads = new HashSet<SequenceThread>();
	}

	/**
	 * Allows opmodes to add their steps to the sequence
	 */
	public abstract void addSteps(LinkedList<SequenceStep> steps);

	@Override
	public void init()
	{
		//set global telemetry variable
		RoboLog.telemetryToUse = telemetry;

		LinkedList<SequenceStep> stepsToExecute = new LinkedList<SequenceStep>();

		addSteps(stepsToExecute);

		if(!stepsToExecute.isEmpty())
		{
			spawnThread(stepsToExecute);
		}
	}

	@Override
	public void loop()
	{
		SequenceThread thread;
		for(Iterator<SequenceThread> threadIter = threads.iterator(); threadIter.hasNext();)
		{
			thread = threadIter.next();
			switch(thread.state)
			{
				case INITIALIZING:
					thread.currentStep.init();
					thread.state = State.POSTINIT;
					thread.currentStepStartTime = System.currentTimeMillis();
					break;
				case POSTINIT:
					thread.currentStep.second_init();
					thread.state = State.RUNNING;
					break;
				case RUNNING:
					boolean overtime = thread.currentStep.isTimed() &&
							System.currentTimeMillis() - thread.currentStepStartTime < thread.currentStep.getTimeLimit();

					if(overtime || !thread.currentStep.loop())
					{
						thread.currentStep.wasTimeKilled = overtime;
						thread.state = State.ENDING;
					}
					break;
				case ENDING:
					thread.currentStep.end();

					if(thread.steps.isEmpty())
					{
						threadIter.remove();
					}
					else
					{
						advanceToNextStep(thread);
					}
					break;
			}
		}
	}

	/**
	 * Moves the provided thread to the next command, and spawns additional threads as needed.
	 *
	 * Cannot handle threads with empty command lists.
	 */
	private void advanceToNextStep(SequenceThread thread)
	{
		thread.expandFirstStep();

		//remove the next step from the front of the queue
		SequenceStep newStep = thread.steps.pop();

		//Add all of its post-requisites
		thread.steps.addAll(0, newStep.getStepsAfter());

		//spawn the other threads it specifies
		for(SequenceStep step : newStep.getStepsParallel())
		{
			LinkedList<SequenceStep> threadSteps = new LinkedList<SequenceStep>();
			threadSteps.add(step);
			spawnThread(threadSteps);
		}

		//finally, actually advance to the next step
		thread.currentStep = newStep;
		thread.state = State.INITIALIZING;
	}

	protected void spawnThread(LinkedList<SequenceStep> stepsList)
	{
		SequenceThread newThread = new SequenceThread(stepsList);
		advanceToNextStep(newThread);
		threads.add(newThread);
	}


	@Override
	public void stop()
	{
		super.stop();

		for(SequenceThread thread : threads)
		{
			thread.currentStep.end();
		}
	}
}
