package com.qualcomm.ftcrobotcontroller;

import com.qualcomm.ftcrobotcontroller.motion.Drivetrain;
import com.qualcomm.ftcrobotcontroller.utils.RoboLog;
import com.qualcomm.ftcrobotcontroller.utils.Units;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.robocol.Telemetry;

/**
 * Robot class for Honken
 */
public class RobotHonken
{
	public Drivetrain drivetrain;

	public RobotHonken(OpMode opMode)
	{
		drivetrain = Drivetrain.make(false, 25, 7.3 * Units.CM * Math.PI, opMode);

		RoboLog.telemetryToUse = opMode.telemetry;
	}
}
