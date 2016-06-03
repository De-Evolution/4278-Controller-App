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

		State state;

		SequenceStep.EndReason endReason; //used to store the reason the step was killed so it can be passed to end() in the next cycle

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

	/**
	 * Similar to addSteps, override this to add steps which will be run during the init phase (when the user has pressed init, but not start)
	 */
	public void addInitSteps(LinkedList<SequenceStep> steps)
	{}

	@Override
	public void init()
	{
		//set global telemetry variable
		RoboLog.telemetryToUse = telemetry;

		LinkedList<SequenceStep> initStepsToExecute = new LinkedList<SequenceStep>();

		addInitSteps(initStepsToExecute);

		if(!initStepsToExecute.isEmpty())
		{
			spawnThread(initStepsToExecute);
		}
	}

	@Override
	public void start()
	{
		//halt any sequences running during init
		stop();


		LinkedList<SequenceStep> stepsToExecute = new LinkedList<SequenceStep>();

		addSteps(stepsToExecute);

		if(!stepsToExecute.isEmpty())
		{
			spawnThread(stepsToExecute);
		}

	}

	@Override
	public void init_loop()
	{
		sequenceLoop();
	}

	@Override
	public void loop()
	{
		sequenceLoop();
	}

	private void sequenceLoop()
	{
		SequenceThread thread;

		StringBuilder runningStepsMessage = new StringBuilder();

		for(Iterator<SequenceThread> threadIter = threads.iterator(); threadIter.hasNext();)
		{
			thread = threadIter.next();

			runningStepsMessage.append(thread.currentStep.className);
			runningStepsMessage.append(" ");

			switch(thread.state)
			{
				case INITIALIZING:
					thread.currentStep.init();
					thread.state = State.POSTINIT;
					thread.currentStep.setStartTime(System.currentTimeMillis());
					break;
				case POSTINIT:
					thread.currentStep.second_init();
					thread.state = State.RUNNING;
					break;
				case RUNNING:
					boolean overtime = thread.currentStep.isTimed() && thread.currentStep.getRunTime() > thread.currentStep.getTimeLimit();

					if(overtime)
					{
						thread.state = State.ENDING;
						thread.endReason = SequenceStep.EndReason.TIME_KILLED;
					}
					else if(!thread.currentStep.loop())
					{
						thread.state = State.ENDING;
						thread.endReason = SequenceStep.EndReason.FINISHED;
					}
					break;
				case ENDING:
					thread.currentStep.end(thread.endReason);

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

		telemetry.addData("Running Steps", runningStepsMessage.toString());
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

		RoboLog.info("Running SequenceStep " + newStep.getClass().getSimpleName());

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
		for(SequenceThread thread : threads)
		{
			thread.currentStep.end(SequenceStep.EndReason.INTERRUPTED);
		}

		threads.clear();
	}

	/**
	 * Checks if a step of the provided class (or one of its subclasses) is running in any of the SequenceOpMode's threads
	 * @param stepClass
	 * @return
	 */
	public boolean isStepRunning(Class<? extends SequenceStep> stepClass)
	{
		for (SequenceThread thread : threads)
		{
			if (stepClass.isInstance(thread.currentStep))
			{
				return true;
			}
		}

		return false;
	}
}
