package com.team4278.robots.honken;

import com.team4278.SequenceOpMode;
import com.team4278.SequenceStep;
import com.team4278.utils.Units;

import java.util.LinkedList;

/**
 * Created by Jamie on 2/9/2016.
 */
public class HonkenDrivingTestAuto extends SequenceOpMode
{
	RobotHonken robot;
	public HonkenDrivingTestAuto()
	{
		robot = new RobotHonken(this);
	}

	@Override
	public void addSteps(LinkedList<SequenceStep> steps)
	{
		steps.add(robot.drivetrain.new MoveForwardStep(1 * Units.M, 10000));
		steps.add(robot.drivetrain.new MoveForwardStep(-1 * Units.M, 10000));

	}
}
