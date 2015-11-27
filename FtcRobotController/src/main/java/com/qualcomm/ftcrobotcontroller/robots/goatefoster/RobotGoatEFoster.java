package com.qualcomm.ftcrobotcontroller.robots.goatefoster;

import com.qualcomm.ftcrobotcontroller.motion.Drivetrain;
import com.qualcomm.ftcrobotcontroller.motion.MotorGroup;
import com.qualcomm.ftcrobotcontroller.utils.RoboLog;
import com.qualcomm.ftcrobotcontroller.utils.Units;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Robot class for Goat-E Foster
 */
public class RobotGoatEFoster
{

	public Drivetrain drivetrain;

	public Servo armBrake;

	public DigitalChannel homingHallEffect;

	public MotorGroup armMotors;

	public RobotGoatEFoster(OpMode opMode)
	{
		RoboLog.telemetryToUse = opMode.telemetry;

		drivetrain = Drivetrain.make(true, 25, 9.9 * Units.CM * Math.PI, 1120, opMode);

		armBrake = opMode.hardwareMap.servo.get("armBrakeServo");

		armMotors = new MotorGroup(true, opMode.hardwareMap.dcMotor.get("mArmTop"), opMode.hardwareMap.dcMotor.get("mArmBottom"));

		homingHallEffect = opMode.hardwareMap.digitalChannel.get("armEndstop");
	}




}
