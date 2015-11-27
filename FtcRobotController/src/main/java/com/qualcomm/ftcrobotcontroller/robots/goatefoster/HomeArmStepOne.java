package com.qualcomm.ftcrobotcontroller.robots.goatefoster;

import com.qualcomm.ftcrobotcontroller.sequence.SequenceStep;

/**
 * Created by Jamie on 11/26/2015.
 */
public class HomeArmStepOne implements SequenceStep
{
	RobotGoatEFoster robot;
	public HomeArmStepOne(RobotGoatEFoster robot)
	{
		this.robot = robot;

	}

	@Override
	public void init()
	{
		
	}

	@Override
	public void loop()
	{

	}

	@Override
	public boolean isFinished()
	{
		return false;
	}

	@Override
	public void end()
	{

	}
}
