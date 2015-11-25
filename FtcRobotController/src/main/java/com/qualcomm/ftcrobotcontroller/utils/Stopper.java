package com.qualcomm.ftcrobotcontroller.utils;

import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.ftcrobotcontroller.FtcRobotControllerActivity;
import com.qualcomm.ftcrobotcontroller.motion.MotorGroup;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.exception.RobotCoreException;

import java.lang.reflect.Field;

/**
 * HELP! JANE! STOP THIS CRAZY THING!
 */
public class Stopper
{
	/**
	 * Stops the opmode queue, killing the robot for the rest of the match.
	 *
	 * @param motorsToStop a list of motors to manually stop
	 */
	public static void estop(MotorGroup... motorsToStop)
	{
		try
		{
			if(motorsToStop != null)
			{
				for(MotorGroup group: motorsToStop)
				{
					if(group != null)
					{
						group.stopMotors();
					}
				}
			}
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
		if(motorsToStop != null)
		{
			for(MotorGroup group: motorsToStop)
			{
				if(group != null)
				{
					group.stopMotors();
				}
			}
		}

		//stop current opmode
		try
		{
			Field opModeManagerField = FtcEventLoop.class.getDeclaredFields()[1]; //why is this not a public variable?
			opModeManagerField.setAccessible(true);
			OpModeManager opModeManager = (OpModeManager) opModeManagerField.get(FtcRobotControllerActivity.eventLoop);
			opModeManager.stopActiveOpMode();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			estop();
		}

		RoboLog.recoverable("Robot locked down.");

	}
}
