package com.team4278;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Sequence element which contains several other steps.
 *
 * Useful when one complete operation involves multiple logical steps.
 */
public class MultiStep
{
	private Object[] stepsVarargs;
	public MultiStep(Object... stepsVarargs)
	{
		this.stepsVarargs = stepsVarargs;
	}

	/**
	 * Adds the actual steps from a varargs array containing both steps and subsequences to the proided list
	 * @param steps
	 * @return
	 */
	static void addActualSteps(LinkedList<SequenceStep> list, Object... steps)
	{
		for(Object object : steps)
		{
			if(object instanceof SequenceStep)
			{
				list.add((SequenceStep) object);
			}
			else if(object instanceof MultiStep)
			{
				addActualSteps(list, ((MultiStep) object).stepsVarargs);
			}
			else
			{
				throw new IllegalArgumentException("Invalid object " + object.getClass().getSimpleName() +
						" (" + object.toString() + ") provided as sequence step!");
			}
		}
	}
}
