package com.team4278.test;

import com.team4278.SequenceOpMode;
import com.team4278.SequenceStep;
import com.team4278.genericsteps.LogMessageStep;

import java.util.LinkedList;

/**
 * Shows if SequenceOpMode's handling of pre- and post-requisites is working properly
 */
public class PrereqTestOpMode extends SequenceOpMode
{

	@Override
	public void addSteps(LinkedList<SequenceStep> steps)
	{

		/*
		 Should produce output:
		 First
		 Second
		 Third
		 Fourth
		 Fifth
		 Sixth
		 Seventh
		 */
		SequenceStep seedStep = new LogMessageStep("Seventh");
		SequenceStep prereq1 = new LogMessageStep("Fifth");
		SequenceStep prereq2 = new LogMessageStep("Fourth");
		SequenceStep prereq3 = new LogMessageStep("First");
		SequenceStep prereq4 = new LogMessageStep("Second");
		SequenceStep postreq1 = new LogMessageStep("Third");
		SequenceStep postreq2 = new LogMessageStep("Sixth");

		seedStep.addStepBefore(prereq1);
		prereq1.addStepBefore(prereq2);
		prereq2.addStepBefore(prereq3);
		prereq3.addStepAfter(postreq1);
		prereq1.addStepAfter(postreq2);
		postreq1.addStepBefore(prereq4);

		steps.add(seedStep);
	}
}
