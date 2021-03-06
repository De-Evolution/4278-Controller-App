package com.team4278.robots.honken;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.team4278.motion.Drivetrain;
import com.team4278.utils.Units;

/**
 * Robot class for Honken
 */
public class RobotHonken
{
	final static public double LEFTHOOK_UP = 240 / 255.0;
	final static public double LEFTHOOK_DOWN = 80 / 255.0;
	final static public double RIGHTHOOK_UP = 16 / 255.0;
	final static public double RIGHTHOOK_DOWN = 176 / 255.0;

	public Drivetrain drivetrain;

	public Servo leftHook, rightHook;

	public UltrasonicSensor ultrasonic;

	public RobotHonken(OpMode opMode)
	{
		drivetrain = Drivetrain.make(true, 25, 7.3 * Units.CM * Math.PI, 1440, opMode);

		leftHook = opMode.hardwareMap.servo.get("leftHook"); //servo 3
		rightHook = opMode.hardwareMap.servo.get("rightHook"); 

		opMode.hardwareMap.dcMotor.get("leftFrontMotor").setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
		opMode.hardwareMap.dcMotor.get("rightFrontMotor").setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);

		//ultrasonic = opMode.hardwareMap.ultrasonicSensor.get("ultrasonic");
	}

	public void retractHooks()
	{
		leftHook.setPosition(LEFTHOOK_UP);
		rightHook.setPosition(RIGHTHOOK_UP);
	}
}
