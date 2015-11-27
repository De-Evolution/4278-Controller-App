package com.qualcomm.ftcrobotcontroller.sequence;

/**
 * Created by Jamie on 11/25/2015.
 */
public interface SequenceStep
{
	public void init();

	public void loop();

	public boolean isFinished();

	public void end();
}
