package com.team4278.utils;

import com.qualcomm.ftcrobotcontroller.FtcRobotControllerActivity;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.team4278.motion.MotorGroup;

/**
 * HELP! JANE! STOP THIS CRAZY THING!
 */
public class Stopper
{
	/**
	 * Stops each motor in the provided array
	 * @param motorsToStop
	 */
	private static void stopMotors(MotorGroup[] motorsToStop)
	{
		if(motorsToStop != null)
		{
			for(MotorGroup group: motorsToStop)
			{
				if(group != null)
				{
					group.stop();
				}
			}
		}
	}

	/**
	 * Stops the opmode queue, killing the robot for the rest of the match.
	 *
	 * @param motorsToStop a list of motors to manually stop
	 */
	public static void estop(MotorGroup... motorsToStop)
	{
		try
		{
			stopMotors(motorsToStop);
			RoboLog.fatal("Emergency stopped!");
			FtcRobotControllerActivity.eventLoop.teardown();
		}
		catch (RobotCoreException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Stops the current opmode.  The robot will resume when the next opmode starts.
	 *
	 * @param motorsToStop a list of motors to manually stop
	 */
	public static void lockdownRobot(MotorGroup... motorsToStop)
	{
		//stop motors
		stopMotors(motorsToStop);

		//stop current opmode
//		try
//		{
//			Field opModeManagerField = FtcEventLoop.class.getDeclaredFields()[1]; //why is this not a public variable?
//			opModeManagerField.setAccessible(true);
//			OpModeManager opModeManager = (OpModeManager) opModeManagerField.get(FtcRobotControllerActivity.eventLoop);
//			opModeManager.stopActiveOpMode();
//		}
//		catch (Exception ex)
//		{
//			ex.printStackTrace();
//			estop();
//		}

		RoboLog.recoverable("Robot locked down.");

	}
}
