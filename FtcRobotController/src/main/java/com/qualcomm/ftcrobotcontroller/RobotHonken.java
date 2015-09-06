package com.qualcomm.ftcrobotcontroller;

import com.qualcomm.ftcrobotcontroller.motion.Drivetrain;
import com.qualcomm.ftcrobotcontroller.utils.RoboLog;
import com.qualcomm.ftcrobotcontroller.utils.Units;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.robocol.Telemetry;

/**
 * Created by Jamie on 9/4/2015.
 */
public class RobotHonken
{
	public Drivetrain drivetrain;

	public RobotHonken(Telemetry telemetry, HardwareMap map)
	{
		drivetrain = Drivetrain.make(true, 25, 7.3 * Units.CM * Math.PI, telemetry, map);

		RoboLog.telemetryToUse = telemetry;
	}
}
