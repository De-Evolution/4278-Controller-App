package com.team4278.robots.goatefoster;

import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.MultiStep;
import com.team4278.SequenceStep;
import com.team4278.genericsteps.HardResetEncodersStep;

/**
 *
 */
public class ArmHomeSequence
{

	static class HomeArmReverseStep extends SequenceStep
	{
		double power;

		RobotGoatEFoster robot;

		public HomeArmReverseStep(Telemetry telemetry, RobotGoatEFoster robot, double power)
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

	static class HomeArmForwardsStep extends TimedSequenceStep
	{
		RobotGoatEFoster robot;

		public HomeArmForwardsStep(Telemetry telemetry, RobotGoatEFoster robot)
		{
			//TODO actual value
			super(300);

			this.robot = robot;
		}

		@Override
		public void init()
		{
			robot.armMotors.setTargetPosition(100, .25);
		}


		@Override
		public void end()
		{
			robot.armMotors.stop();
		}

		@Override
		protected boolean loopTimed()
		{
			return false;
		}
	}

	public static MultiStep buildSequence(RobotGoatEFoster robot, Telemetry telemetry)
	{
		return new MultiStep(new HomeArmReverseStep(telemetry, robot, -.5),
				new HomeArmForwardsStep(telemetry, robot),
				new HomeArmReverseStep(telemetry, robot, -.25),
				new HardResetEncodersStep(telemetry, robot.armMotors));

	}
}