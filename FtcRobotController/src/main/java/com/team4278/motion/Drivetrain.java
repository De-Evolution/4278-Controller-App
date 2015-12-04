package com.team4278.motion;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.SequenceStep;
import com.team4278.utils.RoboLog;
import com.team4278.utils.RobotMath;
import com.team4278.utils.Side;
import com.team4278.utils.Stopper;

import java.util.Map;
import java.util.regex.Pattern;

public class Drivetrain
{

	final static int MAX_TURN_TIME = 5000;

	final static int PAUSE_TIME = 250;

	//motor power to use for run to position moves
	final static double MOTOR_POWER_FOR_AUTO_MOVES = .4;

	final static double MOVE_COMPLETE_TOLERANCE_CM = 3;

	double completionToleranceRotations;

	private double wheelCircumference;

	private double turningCircleCircumference;

	public MotorGroup getLeftMotors()
	{
		return leftMotors;
	}

	public MotorGroup getRightMotors()
	{
		return rightMotors;
	}

	MotorGroup leftMotors;
	MotorGroup rightMotors;

	LinearOpMode linearOpMode;
	Telemetry telemetry;

	static Pattern leftMotorPattern, rightMotorPattern;

	static
	{
		leftMotorPattern =  Pattern.compile("(left.*motor.*)|((drv|drive).*left.*)", Pattern.CASE_INSENSITIVE);
		rightMotorPattern =  Pattern.compile("(right.*motor.*)|((drv|drive).*right.*)", Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Builds a drivetrain, finding all motors like "leftFooMotor" or "drvLeft1"
	 * Also autodetects legacy motor controllers and uses LegacyMotorGroups if a LinearOpMode is provided
	 * @param useEncoders whether or not to set the motors to run in closed-loop, encoder-backed mode
	 * @param wheelbase the diagonal distance from the left front wheel to the right back one.  Basically, the diameter of the turning circle.
	 * @param wheelCircumference the circumference of the wheels
	 * @param encoderCountsPerRev the number of counts the encoder reads per revolution
	 * @param opMode the OpMode currently running
	 * @return a Drivetrain with the auto-added motors
	 */
	public static Drivetrain make(boolean useEncoders, double wheelbase, double wheelCircumference, double encoderCountsPerRev, OpMode opMode)
	{

		MotorGroup leftMotorGroup;
		MotorGroup rightMotorGroup;

		leftMotorGroup = new MotorGroup(useEncoders, encoderCountsPerRev);
		rightMotorGroup = new MotorGroup(useEncoders, encoderCountsPerRev);

		for(Map.Entry<String, DcMotor> motorEntry : opMode.hardwareMap.dcMotor.entrySet())
		{
			if(leftMotorPattern.matcher(motorEntry.getKey()).matches())
			{
				leftMotorGroup.addMotor(motorEntry.getValue());
				RoboLog.debug("Adding left motor " + motorEntry.getKey() + " to drivetrain");
			}
			else if(rightMotorPattern.matcher(motorEntry.getKey()).matches())
			{
				rightMotorGroup.addMotor(motorEntry.getValue());

				RoboLog.debug("Adding right motor " + motorEntry.getKey() + " to drivetrain");
			}
		}

		leftMotorGroup.setReversed(true);

		return new Drivetrain(wheelbase, wheelCircumference, encoderCountsPerRev, leftMotorGroup, rightMotorGroup, opMode);
	}

	/**
	 *
	 * @param wheelbase the diagonal distance between the front left and back right wheels, measured from where they touch the ground.
	 * @param encoderCountsPerRev the number of counts the encoder reads per revolution
	 * @param wheelCircumference the circumference of the robot's (power) wheels
	 *
	 * NOTE: both motorgroups are assumed to go forward when set to positive powers. This will probably require you to invert one of them.  Drivetrain.make() already does this.
	 */
	public Drivetrain(double wheelbase, double wheelCircumference, double encoderCountsPerRev, MotorGroup leftMotors, MotorGroup rightMotors, OpMode opMode)
	{
		this.wheelCircumference = wheelCircumference;

		this.turningCircleCircumference = wheelbase * Math.PI;

		this.leftMotors = leftMotors;
		this.rightMotors = rightMotors;

		if(opMode instanceof LinearOpMode)
		{
			this.linearOpMode = (LinearOpMode) opMode;
		}
		this.telemetry = opMode.telemetry;

		completionToleranceRotations = getRotationsByCm(MOVE_COMPLETE_TOLERANCE_CM);
	}

	/**
	 * Get the number of rotations for the provided distance of linear movement.
	 * @param cm
	 * @return
	 */
	double getRotationsByCm(double cm)
	{
		return cm / wheelCircumference;
	}

	/**
	 * Clear both sides' encoders.
	 */
	public void clearEncoders()
	{
		leftMotors.softResetEncoders();
		rightMotors.softResetEncoders();
	}

	/**
	 * Stop both sides' motors.
	 */
	public void stopMotors()
	{
		leftMotors.stop();
		rightMotors.stop();
	}


	void lockdownRobot() {

		Stopper.lockdownRobot(leftMotors, rightMotors);

	}

	private MotorGroup getMotorsForSide(Side side)
	{
		if(side == Side.LEFT)
		{
			return leftMotors;
		}
		else
		{
			return rightMotors;
		}
	}

	/**
	 * Checks if the provided distances values are close enough according to the tolerance for the move (or at least that side) to be considered done.
	 * @param current current distance in r
	 * @param desired desired distance in r
	 * @return
	 */
	private boolean isCloseEnough(double current, double desired)
	{
		return Math.abs(current - desired)  < completionToleranceRotations;
	}

	public class MoveForwardStep extends SequenceStep
	{
		double targetDistanceRotations;

		double leftPos, rightPos = 0;

		/**
		 * Move forward a distance.
		 * @param cm How far to move
		 * @param msec  Timeout after which the robot will shut down (because it got stuck or otherwise failed). Set to 0 to disable.
		 */
		public MoveForwardStep(double cm, int msec)
		{
			super(msec);


			clearEncoders();
			targetDistanceRotations = getRotationsByCm(cm);

		}

		@Override
		public boolean loop()
		{
			leftPos = leftMotors.getCurrentPosition();
			rightPos = rightMotors.getCurrentPosition();
			Log.d("moveForward()", String.format("left: %f%%, right: %f%%", leftPos * 100 /targetDistanceRotations, rightPos * 100 / targetDistanceRotations));

			return !isCloseEnough(leftPos, targetDistanceRotations) && !isCloseEnough(rightPos, targetDistanceRotations);
		}

		@Override
		public void init()
		{
			leftMotors.setTargetPosition(targetDistanceRotations, MOTOR_POWER_FOR_AUTO_MOVES);
			rightMotors.setTargetPosition(targetDistanceRotations, MOTOR_POWER_FOR_AUTO_MOVES);
		}

		@Override
		public void end()
		{
			if(wasTimeKilled())
			{
				lockdownRobot();
				telemetryMessage("Emergency Killed!");
			}
			else
			{
				telemetryMessage("Done!");
			}
			stopMotors();

		}

	}


	/**
	 * Step for an arc turn.  Note that this requires omni wheels (the ones with the rollers) on the front or back of the robot to work properly.
	 */
	public class ArcTurnStep extends SequenceStep
	{
		double targetRotations;

		double motorPos = 0;

		MotorGroup motorsToTurnWith;

		public ArcTurnStep(Side directionToTurn, double degs, int msec)
		{
			super(msec);

			clearEncoders();
			targetRotations = getRotationsByCm(2 * turningCircleCircumference * (degs / 360.0));

			motorsToTurnWith = getMotorsForSide(directionToTurn.getOpposite());

		}

		@Override
		public boolean loop()
		{
			motorPos = motorsToTurnWith.getCurrentPosition();
			telemetryMessage((motorPos * 100 / targetRotations) + "%");

			return !isCloseEnough(motorPos, targetRotations);
		}

		@Override
		public void init()
		{
			motorsToTurnWith.setTargetPosition(targetRotations, MOTOR_POWER_FOR_AUTO_MOVES);
		}

		@Override
		public void end()
		{
			if(wasTimeKilled())
			{
				lockdownRobot();
				telemetryMessage("Emergency Killed!");
			}
			else
			{
				telemetryMessage("Done!");
			}
			motorsToTurnWith.stop();

		}

	}

	public class InPlaceTurnStep extends SequenceStep
	{
		double targetRotations;

		double forwardPos, backwardsPos = 0;

		MotorGroup backwardsMotors;  //the motors that move backwards.  When turning LEFT, these would be the LEFT motors.
		MotorGroup forwardsMotors;

		public InPlaceTurnStep(Side directionToTurn, double degs, int msec)
		{
			super(msec);

			clearEncoders();
			targetRotations = getRotationsByCm(turningCircleCircumference * (degs / 360.0));

			backwardsMotors = getMotorsForSide(directionToTurn);
			forwardsMotors = getMotorsForSide(directionToTurn.getOpposite());

		}

		@Override
		public boolean loop()
		{
			forwardPos = forwardsMotors.getCurrentPosition();
			backwardsPos = backwardsMotors.getCurrentPosition();

			telemetryMessage(String.format("fwd: %f%%, back: %f%%", forwardPos * 100 / targetRotations, backwardsPos * 100 / targetRotations));

			return !isCloseEnough(forwardPos, targetRotations) && !isCloseEnough(backwardsPos, -targetRotations);
		}

		@Override
		public void init()
		{
			forwardsMotors.setTargetPosition(targetRotations, MOTOR_POWER_FOR_AUTO_MOVES);
			backwardsMotors.setTargetPosition(-1 * targetRotations, MOTOR_POWER_FOR_AUTO_MOVES);
		}

		@Override
		public void end()
		{
			if(wasTimeKilled())
			{
				lockdownRobot();
				telemetryMessage("Emergency Killed!");
			}
			else
			{
				telemetryMessage("Done!");
			}
			stopMotors();

		}

	}

	/**
	 *  Drive by specifying powers for the left and right wheels.
	 * @param powLeft the left motor power, from -1 to 1
	 * @param powRight the right motor power, from -1 to 1
	 */
	public void tankDrive(double powLeft, double powRight)
	{
		leftMotors.setPower(powLeft);
		rightMotors.setPower(powRight);
	}

	/**
	 * Drive by specifying a forward and a turn power.
	 *
	 * @param powX the forward power, from -1 to 1
	 * @param powY the turn power, from -1 to 1
	 */
	public void arcadeDrive(double powX, double powY)
	{
		leftMotors.setPower(RobotMath.clampDouble(powY + powX, -1.0, 1.0));

		rightMotors.setPower(RobotMath.clampDouble(powY - powX, -1.0, 1.0));
	}

	//joystick inputs less than this value will be considered zero.
	//used to compensate for joystick drift
	public static double JOYSTICK_THRESHOLD_AMOUNT = .1;

	/**
	 * Thresholds the provided joystick input based on the provided value.
	 */
	public static double thresholdJoystickInput(double input)
	{
		if(Math.abs(input) < JOYSTICK_THRESHOLD_AMOUNT)
		{
			return 0;
		}

		return input;
	}
}
