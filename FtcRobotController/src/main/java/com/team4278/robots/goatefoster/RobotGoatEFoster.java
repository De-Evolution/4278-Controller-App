package com.team4278.robots.goatefoster;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.team4278.motion.Drivetrain;
import com.team4278.motion.MotorGroup;
import com.team4278.utils.Units;

/**
 * Robot class for Goat-E Foster
 */
public class RobotGoatEFoster
{

	public static double BRAKE_POSITION_BRAKING = .75;
	public static double BRAKE_POSITION_RELEASED = .25;

	public Drivetrain drivetrain;

	public Servo armBrake;

	public DigitalChannel homingHallEffect;

	public MotorGroup armMotors;

	//flag to tell the teleop code not to run the arm from controller input
	boolean armIsAutoControlled;

	public RobotGoatEFoster(OpMode opMode)
	{
		drivetrain = Drivetrain.make(true, 25, 9.9 * Units.CM * Math.PI, 1120, opMode);

		armBrake = opMode.hardwareMap.servo.get("armBrakeServo");

		armMotors = new MotorGroup(true, 1120, opMode.hardwareMap.dcMotor.get("mArmTop"), opMode.hardwareMap.dcMotor.get("mArmBottom"));

		homingHallEffect = opMode.hardwareMap.digitalChannel.get("armEndstop");
	}
}
