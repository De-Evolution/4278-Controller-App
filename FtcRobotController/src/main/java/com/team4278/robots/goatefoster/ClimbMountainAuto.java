package com.team4278.robots.goatefoster;

import com.team4278.SequenceOpMode;
import com.team4278.SequenceStep;

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
		super.init();
		robot = new RobotGoatEFoster(this);
	}

	@Override
	public void addInitSteps(LinkedList<SequenceStep> steps)
	{
		steps.add(new ArmHomeStep(robot));
	}

	@Override
	public void addSteps(LinkedList<SequenceStep> steps)
	{
		steps.add(robot.drivetrain.new MoveForwardStep(200, 2000));
		//steps.add(robot.drivetrain.new InPlaceTurnStep(Side.LEFT, 135, 2000));
		//steps.add(robot.drivetrain.new MoveForwardStep(50, 2000));
	}
}
