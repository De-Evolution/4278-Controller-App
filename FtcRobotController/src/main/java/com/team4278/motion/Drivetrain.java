package com.team4278.motion;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.utils.RoboLog;
import com.team4278.utils.RobotMath;
import com.team4278.utils.Stopper;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * NOTE: ALL CONTROLLED MOVEMENT FUNCTIONS IN THIS CLASS MUST BE RUN FROM A LinearOpMode!
 */
public class Drivetrain
{

	final static int MAX_TURN_TIME = 5000;

	final static int PAUSE_TIME = 250;

	//motor power to use for run to position moves
	final static double MOTOR_POWER_FOR_AUTO_MOVES = .4;

	final static double MOVE_COMPLETE_TOLERANCE_CM = 3;

	double completionToleranceCounts;

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

	//360 for most encoders, excep the NeveRest ones
	private double encoderCountsPerRev;

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
		leftMotorGroup  = new MotorGroup(useEncoders);
		rightMotorGroup  = new MotorGroup(useEncoders);


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

		completionToleranceCounts = getEncoderByCm(MOVE_COMPLETE_TOLERANCE_CM);

		this.encoderCountsPerRev = encoderCountsPerRev;
	}

	/**
	 * Wait for the next hardware cycle.
	 * @return false if the wait was interrupted (because the driver pressed stop)
	 */
	boolean pauseForReading()
	{
		try
		{
			linearOpMode.waitForNextHardwareCycle();
			return true;
		}
		catch (InterruptedException e)
		{
			RoboLog.unusual("Autonomous Move Interrupted!");
			return false;
		}

	}

	/**
	 * Wait for the next hardware cycle.
	 * @return false if the wait was interrupted (because the driver pressed stop)
	 */
	boolean pauseForWriting()
	{
		try
		{
			linearOpMode.waitOneFullHardwareCycle();
			return true;
		}
		catch (InterruptedException e)
		{
			RoboLog.unusual("Autonomous Move Interrupted!");
			return false;
		}

	}


	/**
	 * Get the number of encoder ticks for the provided distance of linear movement.
	 * @param cm
	 * @return
	 */
	int getEncoderByCm(double cm)
	{
		return RobotMath.floor_double_int((encoderCountsPerRev) * (cm) / wheelCircumference);
	}
	
	double getCmByEncoder(double encode) 
	{
		return (encode/ encoderCountsPerRev)*wheelCircumference;
	}

	/**
	 * Clear both sides' encoders.
	 */
	public void clearEncoders()
	{
		leftMotors.startEncoderReset();
		rightMotors.startEncoderReset();
	}

	/**
	 * Stop both sides' motors.
	 */
	public void stopMotors()
	{
		leftMotors.stopMotors();
		rightMotors.stopMotors();
	}


	void lockdownRobot() {

		Stopper.lockdownRobot(leftMotors, rightMotors);

	}

	/**
	 * Checks if the provided encoder values are close enough according to the tolerance for the move (or at least that side) to be considered done.
	 * @param current
	 * @param desired
	 * @return
	 */
	private boolean isCloseEnough(int current, int desired)
	{
		return Math.abs(current - desired)  < completionToleranceCounts;
	}

	/**
	 * Move forward a distance.
	 * @param cm How far to move
	 * @param msec  Timeout after which the robot will shut down (because it got stuck or otherwise failed). Set to 0 to disable.
	 */
	public void moveForward(double cm, int msec)
	{
		clearEncoders();
		int enc = Math.abs(getEncoderByCm(cm));
		double norm = RobotMath.sgn(cm);
		
		long startTime = System.currentTimeMillis();
		
		leftMotors.setTargetPosition(enc, MOTOR_POWER_FOR_AUTO_MOVES);
		rightMotors.setTargetPosition(enc, MOTOR_POWER_FOR_AUTO_MOVES);

		int leftPos, rightPos = 0;
		
		while(!isCloseEnough(leftPos = leftMotors.getCurrentPosition(), enc) && !isCloseEnough(rightPos = rightMotors.getCurrentPosition(), enc))
		{
			Log.d("moveForward()", String.format("left: %d%%, right: %d%%", leftPos * 100 / enc, rightPos * 100 / enc));
			if (System.currentTimeMillis() - startTime > msec)
			{
				lockdownRobot();
				return;
			}

			if(!pauseForReading())
			{
				break;
			}
	}

		telemetry.addData("moveForward()", "Done!");
		
		stopMotors();
		
		pauseForWriting();
	}

	//arc to the right (set LEFT motors) a given amount of degrees
	public void arcRight(double degs) {
		int enc = getEncoderByCm(2 * turningCircleCircumference * (Math.abs(degs) / 360.0));

		leftMotors.startEncoderReset();
		leftMotors.setTargetPosition(enc, MOTOR_POWER_FOR_AUTO_MOVES);

		long startTime = System.currentTimeMillis();

		while(leftMotors.getCurrentPosition() < enc)
		{
			if(System.currentTimeMillis() - startTime > MAX_TURN_TIME)
				lockdownRobot();

			if(!pauseForReading())
			{
				break;
			}
		}

		leftMotors.stopMotors();
		pauseForWriting();
	}
	
	//arc to the left (set RIGHT motors) a given amount of degrees
	public void arcLeft(double degs)
	{
		int enc = getEncoderByCm(2 * turningCircleCircumference * (Math.abs(degs) / 360.0));
		
		rightMotors.startEncoderReset();
		rightMotors.setTargetPosition(enc, MOTOR_POWER_FOR_AUTO_MOVES);
		
		long startTime = System.currentTimeMillis();
		
		while(rightMotors.getCurrentPosition() < enc)
		{
			if(System.currentTimeMillis() - startTime > MAX_TURN_TIME)
				lockdownRobot();

			if(!pauseForReading())
			{
				break;
			}
		}
		
		rightMotors.stopMotors();
		pauseForWriting();
	}


	/**
	 * Perform an in-place turn to the right.
	 *
	 * Accepts negative values.
	 * @param degs the number of degree to turn
	 */
	public void turnRight(double degs)
	{
		turnLeft(-degs);
	}

	/**
	 * Perform an in-place turn to the left.
	 *
	 * Accepts negative values.
	 *
	 * @param degs the number of degrees to turn
	 */
	public void turnLeft(double degs)
	{
		//always positive
		int enc = getEncoderByCm(turningCircleCircumference * (Math.abs(degs) / 360.0));

		clearEncoders();

		leftMotors.setTargetPosition(RobotMath.floor_double_int(RobotMath.sgn(degs) * enc), MOTOR_POWER_FOR_AUTO_MOVES);
		rightMotors.setTargetPosition(RobotMath.floor_double_int( -1 *RobotMath.sgn(degs) * enc), MOTOR_POWER_FOR_AUTO_MOVES);

		long startTime = System.currentTimeMillis();

		while(Math.abs(leftMotors.getCurrentPosition()) < enc && Math.abs(rightMotors.getCurrentPosition()) < enc)
		{
			if(System.currentTimeMillis() - startTime > MAX_TURN_TIME)
			{
				lockdownRobot();
				return;
			}

			if(!pauseForReading())
			{
				break;
			}
		}

		stopMotors();
		pauseForWriting();
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
}
