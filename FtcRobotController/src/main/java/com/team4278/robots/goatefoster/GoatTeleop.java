package com.team4278.robots.goatefoster;

import com.team4278.ButtonListenerTeleop;
import com.team4278.motion.Drivetrain;

/**
 * Created by Jamie on 11/29/2015.
 */
public class GoatTeleop extends ButtonListenerTeleop
{

	RobotGoatEFoster robot;

	boolean armServoIsBraking = false;

	@Override
	public void onButtonPressed(Button button)
	{
		switch(button)
		{
			case RBUMPER:
				armServoIsBraking = !armServoIsBraking;
				robot.armBrake.setPosition(armServoIsBraking ? RobotGoatEFoster.BRAKE_POSITION_BRAKING : RobotGoatEFoster.BRAKE_POSITION_RELEASED);
				break;
			case A:
				executeSequenceSteps(new ArmHomeStep(robot));
				break;
		}

	}

	@Override
	public void onButtonReleased(Button button)
	{

	}

	@Override
	public void init()
	{
		super.init();
		robot = new RobotGoatEFoster(this);
	}

	@Override
	public void loop()
	{
		super.loop();

		robot.drivetrain.arcadeDrive(gamepad1.left_stick_x, gamepad1.left_stick_y);

		if(!robot.armIsAutoControlled)
		{
			robot.armMotors.setPower(Drivetrain.thresholdJoystickInput(gamepad1.right_stick_y));
		}
	}
}
