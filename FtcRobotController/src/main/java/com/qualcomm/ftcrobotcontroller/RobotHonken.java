package com.qualcomm.ftcrobotcontroller;

import com.qualcomm.ftcrobotcontroller.motion.Drivetrain;
import com.qualcomm.ftcrobotcontroller.utils.RoboLog;
import com.qualcomm.ftcrobotcontroller.utils.Units;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

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
		RoboLog.telemetryToUse = opMode.telemetry;

		drivetrain = Drivetrain.make(true, 25, 7.3 * Units.CM * Math.PI, 360, opMode);

		leftHook = opMode.hardwareMap.servo.get("leftHook"); //servo 3
		rightHook = opMode.hardwareMap.servo.get("rightHook");

		//only front motors have encoders
		drivetrain.getLeftMotors().setEncoderMotor(opMode.hardwareMap.dcMotor.get("leftBackMotor"));
		drivetrain.getRightMotors().setEncoderMotor(opMode.hardwareMap.dcMotor.get("rightBackMotor"));

		opMode.hardwareMap.dcMotor.get("leftFrontMotor").setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
		opMode.hardwareMap.dcMotor.get("rightFrontMotor").setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);

		ultrasonic = opMode.hardwareMap.ultrasonicSensor.get("ultrasonic");
	}

	public void retractHooks()
	{
		leftHook.setPosition(LEFTHOOK_UP);
		rightHook.setPosition(RIGHTHOOK_UP);
	}
}
