package com.qualcomm.ftcrobotcontroller.robots.goatefoster;

import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.SequenceStep;

/**
 * Created by Jamie on 11/26/2015.
 */
public class HomeArmStepOne extends SequenceStep
{
	RobotGoatEFoster robot;
	public HomeArmStepOne(Telemetry telemetry, RobotGoatEFoster robot)
	{
		super(telemetry);
		this.robot = robot;

	}

	@Override
	public void init()
	{
		robot.armMotors.setPower(-.5);
	}

	@Override
	public boolean loop()
	{
		return robot.homingHallEffect.getState(); //active low
	}


	@Override
	public void end()
	{
		robot.armMotors.stopMotors();
	}
}
