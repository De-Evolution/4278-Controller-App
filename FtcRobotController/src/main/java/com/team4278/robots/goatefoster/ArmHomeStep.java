package com.team4278.robots.goatefoster;

import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.SequenceStep;
import com.team4278.genericsteps.HardResetEncodersStep;

/**
 *
 */
public class ArmHomeStep extends SequenceStep
{

	//the outer class is the second step of the homing process, and the inner class is steps 1 and 3
	private static class HomeArmReverseStep extends SequenceStep
	{
		double power;

		RobotGoatEFoster robot;

		public HomeArmReverseStep(RobotGoatEFoster robot, double power)
		{
			super();

			this.power = power;

			this.robot = robot;
		}

		@Override
		public void init()
		{
			robot.armMotors.setPower(power);
		}

		@Override
		public boolean loop()
		{
			return robot.homingHallEffect.getState(); //active low
		}


		@Override
		public void end()
		{
			robot.armMotors.stop();
		}

	}

	RobotGoatEFoster robot;

	public ArmHomeStep(RobotGoatEFoster robot)
	{
		//TODO actual value
		super(300);

		addStepBefore(new HomeArmReverseStep(robot, -.5));
		addStepAfter(new HomeArmReverseStep(robot, -.25));
		addStepAfter(new HardResetEncodersStep(robot.armMotors));

		this.robot = robot;
	}

	@Override
	public void init()
	{
		robot.armMotors.setTargetPosition(100, .25);
	}

	@Override
	public boolean loop()
	{
		return false;
	}


	@Override
	public void end()
	{
		robot.armMotors.stop();
	}
}