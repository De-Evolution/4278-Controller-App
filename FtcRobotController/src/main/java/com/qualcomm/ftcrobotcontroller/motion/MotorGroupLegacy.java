package com.qualcomm.ftcrobotcontroller.motion;

import com.qualcomm.ftcrobotcontroller.utils.RoboLog;
import com.qualcomm.hardware.HiTechnicNxtDcMotorController;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

/**
 * Class to hold a group of motors which run at the same speed.
 */
public class MotorGroupLegacy extends MotorGroup
{
	LinearOpMode delayer;

	/**
	 *
	 * @param encoded whether the group has an encoder attached.  This controls whether the motors will be set to open or closed loop mode.
	 * @param toAdd the DcMotor to add to the group
	 */
	public MotorGroupLegacy(boolean encoded, LinearOpMode opMode, DcMotor... toAdd)
	{
		super(encoded, toAdd);
		this.delayer = opMode;
	}

	/**
	 *
	 * @return  The position of the motor being used for encoder data
	 *
	 * NOTE: THis method is pretty time-expensive; it waits two full hardware cycles
	 */
	@Override
	public int getCurrentPosition()
	{
		DcMotor motorWithEncoder = getMotorWithEncoder();
		DcMotorController legacyController = null;

		try
		{
			if(motorWithEncoder.getController() instanceof HiTechnicNxtDcMotorController)
			{
				legacyController = motorWithEncoder.getController();
				legacyController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
				delayer.waitOneFullHardwareCycle();
				Thread.sleep(10);
			}

			int position = super.getCurrentPosition();

			if(legacyController != null)
			{
				legacyController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
				delayer.waitOneFullHardwareCycle();
			}
			return position;
		}
		catch(InterruptedException ex)
		{
			RoboLog.fatal("getCurrentPosition() Interrupted");
			return 0;
		}

	}



}
