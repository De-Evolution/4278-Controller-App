package com.team4278.robots.goatefoster;

import com.team4278.SequenceOpMode;
import com.team4278.SequenceStep;
import com.team4278.genericsteps.DelayStep;
import com.team4278.utils.Alliance;
import com.team4278.utils.Side;

import java.util.LinkedList;

/**
 * Autonomous program to home the arm nto its rest position
 */
public class ClimbMountainAuto extends SequenceOpMode
{
	RobotGoatEFoster robot;

	Alliance currentAlliance;

	public ClimbMountainAuto(Alliance currentAlliance)
	{
		super();
		this.currentAlliance = currentAlliance;
	}

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
		steps.add(robot.drivetrain.new MoveForwardPollingStep(165, .45,  7000));
		//steps.add(robot.drivetrain.new InPlaceTurnStep(Side.RIGHT, 90, 3000));
		steps.add(new DelayStep(500));
		steps.add(robot.drivetrain.new InPlaceTurnPollingStep(currentAlliance == Alliance.BLUE ? Side.LEFT : Side.RIGHT, 80, 2000));
		steps.add(robot.drivetrain.new MoveForwardPollingStep(-200, .6, 10000));
	}
}
