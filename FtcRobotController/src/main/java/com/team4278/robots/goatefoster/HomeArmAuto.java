package com.team4278.robots.goatefoster;

import com.qualcomm.robotcore.robot.Robot;
import com.team4278.SequenceOpMode;
import com.team4278.SequenceStep;

import java.util.LinkedList;

/**
 * Autonomous program to home the arm nto its rest position
 */
public class HomeArmAuto extends SequenceOpMode
{

	@Override
	public void addSteps(LinkedList<SequenceStep> steps)
	{
		RobotGoatEFoster robot = new RobotGoatEFoster(this);
		steps.add(new ArmHomeStep(robot));
	}
}
