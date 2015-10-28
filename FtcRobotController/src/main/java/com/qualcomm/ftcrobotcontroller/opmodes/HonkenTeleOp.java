package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.ftcrobotcontroller.RobotHonken;
import com.qualcomm.ftcrobotcontroller.button.ButtonListenerOpMode;

/**
 * Teleop program for our 2014-15 robot, Honken.  Teally hust a drivetrain and some servo-based hooks.
 */
public class HonkenTeleOp extends ButtonListenerOpMode {


	RobotHonken robot;

	boolean leftHookDown = false, rightHookDown = false;

	boolean useTankDrive = false;
	/**
	 * Constructor
	 */
	public HonkenTeleOp() {
	}

	/*
	 * Code to run when the op mode is initialized goes here
	 * 
	 * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#init()
	 */
	@Override
	public void init() {


		/*
		 * Use the hardwareMap to get the dc motors and servos by name. Note
		 * that the names of the devices must match the names used when you
		 * configured your robot and created the configuration file.
		 */
		robot = new RobotHonken(this);

		robot.retractHooks();
	}

	/*
	 * This method will be called repeatedly in a loop
	 * 
	 * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#run()
	 */
	@Override
	public void loop() {
		super.loop();
		if(useTankDrive)
		{
			robot.drivetrain.tankDrive(-gamepad1.left_stick_y, -gamepad1.right_stick_y);
		}
		else
		{
			robot.drivetrain.arcadeDrive(gamepad1.left_stick_x, -gamepad1.left_stick_y);
		}

	}

	@Override
	public void onButtonPressed(Button button)
	{
		switch(button)
		{
			case Y:
				robot.retractHooks();
				break;
			case X:
				if(leftHookDown)
				{
					robot.leftHook.setPosition(RobotHonken.LEFTHOOK_UP);
					leftHookDown = false;
				}
				else
				{
					robot.leftHook.setPosition(RobotHonken.LEFTHOOK_DOWN);
					leftHookDown = true;
				}
				break;
			case A:
				if(rightHookDown)
				{
					robot.rightHook.setPosition(RobotHonken.RIGHTHOOK_UP);
					rightHookDown = false;
				}
				else
				{
					robot.rightHook.setPosition(RobotHonken.RIGHTHOOK_DOWN);
					rightHookDown = true;
				}
				break;
			case B:
				useTankDrive = !useTankDrive;
				break;

		}
	}

	@Override
	public void onButtonReleased(Button button)
	{

	}

	/*
	 * Code to run when the op mode is first disabled goes here
	 * 
	 * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#stop()
	 */
	@Override
	public void stop() {

	}
}
