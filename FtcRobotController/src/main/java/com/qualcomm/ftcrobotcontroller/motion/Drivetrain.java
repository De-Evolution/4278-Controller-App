package com.qualcomm.ftcrobotcontroller.motion;

import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.ftcrobotcontroller.FtcRobotControllerActivity;
import com.qualcomm.ftcrobotcontroller.motion.MotorGroup;
import com.qualcomm.ftcrobotcontroller.utils.RoboLog;
import com.qualcomm.ftcrobotcontroller.utils.RobotMath;
import com.qualcomm.ftcrobotcontroller.utils.Stopper;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.robocol.Telemetry;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Jamie on 8/23/2015.
 */
public class Drivetrain
{

	final static int MAX_TURN_TIME = 5000;

	//360 for most encoders, 280 for NeveRest motor encoders
	final static double ENC_COUNTS_PER_R = 280;
	final static int PAUSE_TIME = 250;

	private double wheelCircumference;

	private double turningCircleCircumference;

	MotorGroup leftMotors;
	MotorGroup rightMotors;

	Telemetry telemetry;

	static Pattern leftMotorPattern, rightMotorPattern;

	static
	{
		leftMotorPattern =  Pattern.compile("left.*", Pattern.CASE_INSENSITIVE);
		rightMotorPattern =  Pattern.compile("right.*", Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Builds a drivetrain, finding all motors that start with "left" or "right".
	 * @param useEncoders
	 * @param wheelbase
	 * @param wheelCircumference
	 * @param telemetry
	 * @param map
	 * @return
	 */
	public static Drivetrain make(boolean useEncoders, double wheelbase, double wheelCircumference, Telemetry telemetry, HardwareMap map)
	{
		MotorGroup leftMotorGroup = new MotorGroup(useEncoders);
		MotorGroup rightMotorGroup = new MotorGroup(useEncoders);

		for(Map.Entry<String, DcMotor> motorEntry : map.dcMotor.entrySet())
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

		leftMotorGroup.setInverted(true);

		return new Drivetrain(wheelbase, wheelCircumference, leftMotorGroup, rightMotorGroup, telemetry);
	}

	/**
	 *
	 * @param wheelbase the diagonal distance between the front left and back right wheels, measured from where they touch the ground.
	 *
	 * @param wheelCircumference the circumference of the robot's (power) wheels
	 */
	public Drivetrain(double wheelbase, double wheelCircumference, MotorGroup leftMotors, MotorGroup rightMotors, Telemetry telemetry)
	{
		this.wheelCircumference = wheelCircumference;

		this.turningCircleCircumference = wheelCircumference * Math.PI;

		this.leftMotors = leftMotors;
		this.rightMotors = rightMotors;

		this.telemetry = telemetry;
	}

	void pause() {
		pause(PAUSE_TIME);
	}

	void pause(long millis) {
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}



	int getEncoderByCm(double cm)
	{
		return RobotMath.floor_double_int((ENC_COUNTS_PER_R) * (cm) / wheelCircumference);
	}
	
	double getCmByEncoder(double encode) 
	{
		return (encode/ENC_COUNTS_PER_R)*wheelCircumference;
	}

	/**
	 * Clear both sides' encoders.
	 */
	public void clearEncoders()
	{
		leftMotors.resetEncoderBlocking();
		rightMotors.resetEncoderBlocking();
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
	 * Move forward a distance.
	 * @param cm
	 * @param msec  Timeout after which the robot will shut down (because it got stuck or otherwise failed). Set to 0 to disable.
	 */
	void moveForward(double cm, int msec)
	{
		clearEncoders();
		int enc = Math.abs(getEncoderByCm(cm));
		double norm = RobotMath.sgn(cm);
		
		long startTime = System.currentTimeMillis();
		
		leftMotors.setTargetPosition(enc);
		rightMotors.setTargetPosition(enc);

		int leftPos, rightPos;
		
		while((leftPos = leftMotors.getCurrentPosition()) < enc && (rightPos = rightMotors.getCurrentPosition()) < enc)
		{
			telemetry.addData("moveForward()", String.format("left: %d%%, right: %d%%", leftPos * 100 / enc, rightPos * 100 / enc));
			if (System.currentTimeMillis() - startTime > msec)
			{
				lockdownRobot();
				return;
			}

			pause(5);
		}

		telemetry.addData("moveForward()", "Done!");
		
		stopMotors();
		
		pause();
	}

	//arc to the right (set LEFT motors) a given amount of degrees
	public void arcRight(double degs) {
		int enc = getEncoderByCm(2 * turningCircleCircumference * (Math.abs(degs) / 360.0));

		leftMotors.resetEncoderBlocking();
		leftMotors.setTargetPosition(enc);

		long startTime = System.currentTimeMillis();

		while(leftMotors.getCurrentPosition() < enc)
		{
			if(System.currentTimeMillis() - startTime > MAX_TURN_TIME)
				lockdownRobot();

			pause(5);
		}

		leftMotors.stopMotors();
		pause();
	}
	
	//arc to the left (set RIGHT motors) a given amount of degrees
	public void arcLeft(double degs) {
		int enc = getEncoderByCm(2 * turningCircleCircumference * (Math.abs(degs) / 360.0));
		
		rightMotors.resetEncoderBlocking();
		rightMotors.setTargetPosition(enc);
		
		long startTime = System.currentTimeMillis();
		
		while(rightMotors.getCurrentPosition() < enc)
		{
			if(System.currentTimeMillis() - startTime > MAX_TURN_TIME)
				lockdownRobot();

			pause(5);
		}
		
		rightMotors.stopMotors();
		pause();
	}


	/**
	 * Perform an in-place turn to the right.
	 *
	 * Accepts negative values.
	 * @param degs
	 */
	public void turnRight(double degs)
	{
		//always positive
		int enc = getEncoderByCm(turningCircleCircumference * (Math.abs(degs) / 360.0));

		clearEncoders();

		leftMotors.setTargetPosition(RobotMath.floor_double_int(-1 * RobotMath.sgn(degs) * enc));
		rightMotors.setTargetPosition(RobotMath.floor_double_int(RobotMath.sgn(degs) * enc));

		long startTime = System.currentTimeMillis();

		while(Math.abs(leftMotors.getCurrentPosition()) < enc && Math.abs(rightMotors.getCurrentPosition()) < enc)
		{
			if(System.currentTimeMillis() - startTime > MAX_TURN_TIME)
			{
				lockdownRobot();
				return;
			}

			pause(5);
		}

		stopMotors();
		pause();
	}

	/**
	 * Perform an in-place turn to the left.
	 *
	 * Accepts negative values.
	 *
	 * @param degs
	 */
	public void turnLeft(double degs)
	{
		//always positive
		int enc = getEncoderByCm(turningCircleCircumference * (Math.abs(degs) / 360.0));

		clearEncoders();

		leftMotors.setTargetPosition(RobotMath.floor_double_int(RobotMath.sgn(degs) * enc));
		rightMotors.setTargetPosition(RobotMath.floor_double_int( -1 *RobotMath.sgn(degs) * enc));

		long startTime = System.currentTimeMillis();

		while(Math.abs(leftMotors.getCurrentPosition()) < enc && Math.abs(rightMotors.getCurrentPosition()) < enc)
		{
			if(System.currentTimeMillis() - startTime > MAX_TURN_TIME)
			{
				lockdownRobot();
				return;
			}

			pause(5);
		}

		stopMotors();
		pause();
	}

	/**
	 *  Drive by specifying powers for the left and right wheels.
	 * @param powLeft
	 * @param powRight
	 */
	public void tankDrive(double powLeft, double powRight)
	{
		leftMotors.setPower(powLeft);
		rightMotors.setPower(powRight);
	}

	/**
	 * Drive by specifying a forward and a turn power.
	 *
	 * @param powX
	 * @param powY
	 */
	public void arcadeDrive(double powX, double powY)
	{
		leftMotors.setPower(RobotMath.clampDouble(powY + powX, -1.0, 1.0));

		rightMotors.setPower(RobotMath.clampDouble(powY - powX, -1.0, 1.0));
	}
}
