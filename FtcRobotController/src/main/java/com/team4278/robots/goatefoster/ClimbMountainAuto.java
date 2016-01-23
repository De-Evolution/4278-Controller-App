package com.team4278.robots.goatefoster;

import com.team4278.SequenceOpMode;
import com.team4278.SequenceStep;
import com.team4278.genericsteps.DelayStep;
import com.team4278.utils.Side;

import java.util.LinkedList;

/**
 * Autonomous program to home the arm nto its rest position
 */
public class ClimbMountainAuto extends SequenceOpMode
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
		steps.add(robot.drivetrain.new MoveForwardPollingStep(85, 5000));
		//steps.add(robot.drivetrain.new InPlaceTurnStep(Side.RIGHT, 90, 3000));
		steps.add(new DelayStep(500));
		steps.add(robot.drivetrain.new InPlaceTurnPollingStep(Side.RIGHT, 90, 2000));
		steps.add(robot.drivetrain.new MoveForwardPollingStep(200, 5000));
	}
}
