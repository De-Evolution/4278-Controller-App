package com.team4278.robots.goatefoster;

import com.team4278.motion.Drivetrain;
import com.team4278.motion.MotorGroup;
import com.team4278.utils.RoboLog;
import com.team4278.utils.Units;
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
