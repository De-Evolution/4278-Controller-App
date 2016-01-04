package com.team4278.robots.goatefoster;

import com.team4278.SequenceStep;
import com.team4278.genericsteps.HardResetEncodersStep;
import com.team4278.genericsteps.LambdaStep;
import com.team4278.utils.RoboLog;

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
			RoboLog.debug("Motors set to " + power);
		}

		@Override
		public void second_init()
		{
			robot.armMotors.setPower(power);
		}

		@Override
		public boolean loop()
		{
			return robot.homingHallEffect.getState(); //active low
		}


		@Override
		public void end(EndReason reason)
		{
			robot.armMotors.stop();
		}

	}

	RobotGoatEFoster robot;

	public ArmHomeStep(final RobotGoatEFoster robot)
	{
		//TODO actual value
		super(2000);

		addStepBefore(new LambdaStep(new Runnable()
		{
			@Override
			public void run()
			{
				robot.armIsAutoControlled = true;
			}
		}));
		addStepBefore(new HomeArmReverseStep(robot, -.5));
		addStepBefore(new HardResetEncodersStep(robot.armMotors));
		addStepAfter(new HomeArmReverseStep(robot, -.5));
		addStepAfter(new HardResetEncodersStep(robot.armMotors));

		addStepAfter(new LambdaStep(new Runnable()
		{
			@Override
			public void run()
			{
				robot.armIsAutoControlled = false;
			}
		}));

		this.robot = robot;
	}

	@Override
	public void init()
	{
		robot.armMotors.setTargetPosition(10, .25);
	}

	@Override
	public boolean loop()
	{
		return true;
	}


	@Override
	public void end(EndReason reason)
	{
		robot.armMotors.stop();
	}
}