package com.team4278.genericsteps;

import com.team4278.SequenceStep;
import com.team4278.utils.RoboLog;

/**
 * Step which logs a message as info when it executes
 */
public class LogMessageStep extends SequenceStep
{
	String messageToLog;

	public LogMessageStep(String messageToLog)
	{
		this.messageToLog = messageToLog;
	}

	@Override
	public void init()
	{

	}

	@Override
	public boolean loop()
	{
		RoboLog.info(messageToLog);
		return false;
	}

	@Override
	public void end(EndReason reason)
	{

	}
}
