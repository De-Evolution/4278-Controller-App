package com.team4278.robots.goatefoster;

import com.team4278.SequenceOpMode;
import com.team4278.SequenceStep;

import java.util.LinkedList;

/**
 * Autonomous program to home the arm nto its rest position
 */
public class RepairZoneAuto extends SequenceOpMode
{
	RobotGoatEFoster robot;

	@Override
	public void init()
	{
		robot = new RobotGoatEFoster(this);
		super.init();
	}

	@Override
	public void addInitSteps(LinkedList<SequenceStep> steps)
	{
		//steps.add(robot.arm.new HomeStep());
	}

	@Override
	public void addSteps(LinkedList<SequenceStep> steps)
	{
		steps.add(robot.drivetrain.new MoveForwardPollingStep(260, .45, 10000));
	}
}
