package com.team4278.test;

import android.util.Log;

import com.team4278.SequenceOpMode;
import com.team4278.SequenceStep;
import com.team4278.utils.RoboLog;

import java.util.LinkedList;

/**
 * Tests running sequence steps in parallel with each other, as well as the time limit system
 */
public class ParallelismTestOpMode extends SequenceOpMode
{

	/**
	 * Step which prints messages for a certain time.
	 *
	 * Used to test parallel opmodes.
	 */
	private static class ThreadingTestStep extends SequenceStep
	{

		String name;

		public ThreadingTestStep(String name, long time)
		{
			super(time);

			this.name = name;
		}

		@Override
		public void init()
		{
			RoboLog.info("Step starting: " + name);
		}

		@Override
		public boolean loop()
		{
			RoboLog.info("Step running: " + name);
			return true;
		}

		@Override
		public void end()
		{
			RoboLog.info("Step " + name + " ran for " + getRunTime() + " ms");
		}
	}

	@Override
	public void addSteps(LinkedList<SequenceStep> steps)
	{
		SequenceStep seedStep = new ThreadingTestStep("initial", 500);
		SequenceStep parallelOne = new ThreadingTestStep("child1", 5000);
		SequenceStep parallelTwo = new ThreadingTestStep("child2", 1000);
		SequenceStep p2Prereq = new ThreadingTestStep("child2prereq", 300);
		SequenceStep p2Postreq = new ThreadingTestStep("child2postreq", 1500);

		seedStep.addStepParallel(parallelOne);
		parallelOne.addStepParallel(parallelTwo);
		parallelTwo.addStepBefore(p2Prereq);
		parallelTwo.addStepAfter(p2Postreq);

		steps.add(seedStep);
	}
}
