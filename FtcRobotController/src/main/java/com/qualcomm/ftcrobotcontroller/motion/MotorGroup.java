package com.qualcomm.ftcrobotcontroller.motion;

import com.qualcomm.ftcrobotcontroller.utils.RoboLog;
import com.qualcomm.ftcrobotcontroller.utils.RobotMath;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

import java.util.ArrayList;

/**
 * Class to hold a group of motors which run at the same speed.
 */
public class MotorGroup
{
	private ArrayList<DcMotor> motors;

	private int preferredEncoderNum = 0;

	/**
	 * Scale factor to multiply motor power sets by.  if negative, the motor is inverted.
	 *
	 */
	double scaleFactor = 1;

	private DcMotorController.RunMode currentMode;

	boolean hasEncoder;

	//stored position of the encoder so that it can be pseudo-reset.
	int encoderIgnoreDistance;

	/**
	 *
	 * @return  The scale factor of the motor.
	 */
	public double getScaleFactor()
	{
		return Math.abs(scaleFactor);
	}

	/**
	 * Set the scaling factor to apply to all new motor power changes
	 * @param scaleFactor the new motor scaling factor, must be positive.
	 */
	public void setScaleFactor(double scaleFactor)
	{
		if(scaleFactor < 0)
		{
			throw new IllegalArgumentException("Scale Factor out of range: " + scaleFactor);
		}

		this.scaleFactor = scaleFactor * RobotMath.sgn(this.scaleFactor);
	}

	public void setInverted(boolean inverted)
	{
		scaleFactor = Math.abs(scaleFactor) * (inverted ? -1 : 1);
	}

	public boolean isInverted()
	{
		return scaleFactor < 0;
	}

	/**
	 * Set which motor to pull encoder data from.
	 *
	 * By default, the first one added is used.
	 * @param toUse the motor to use for encoder data.
	 */
	public void setEncoderMotor(DcMotor toUse)
	{
		int motorIndex = motors.indexOf(toUse);

		if(motorIndex != -1)
		{
			preferredEncoderNum = motorIndex;

			//encoderIgnoreDistance = toUse.getCurrentPosition();
		}
		else
		{
			RoboLog.unexpected("MotorGroup.setEncoderMotor() called with a motor that wasn't in the group!");
		}

	}

	/**
	 *
	 * @param encoded whether the group has an encoder attached.  This controls whether the motors will be set to open or closed loop mode.
	 * @param toAdd the DcMotor to add to the group
	 */
	public MotorGroup(boolean encoded, DcMotor... toAdd)
	{
		motors = new ArrayList<DcMotor>();

		currentMode = encoded ? DcMotorController.RunMode.RUN_USING_ENCODERS : DcMotorController.RunMode.RUN_WITHOUT_ENCODERS;

		hasEncoder = encoded;

		if(toAdd != null && toAdd.length > 0)
		{
			for(DcMotor dcMotor : toAdd)
			{
				addMotor(dcMotor);
			}
		}

	}

	public void addMotor(DcMotor motor)
	{
		motors.add(motor);
		motor.setPower(0);
		motor.setChannelMode(currentMode);
	}

	/**
	 * Set the power of all motors in the group.
	 *
	 * Resets the RunMode if it was set to RUN_TO_POSITION.
	 * @param newPower the power to set, from -1.0 to 1.0
	 */
	public void setPower(double newPower)
	{
		if(currentMode == DcMotorController.RunMode.RUN_TO_POSITION)
		{
			setRunMode(hasEncoder ? DcMotorController.RunMode.RUN_USING_ENCODERS : DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
		}

		for(DcMotor currentMotor : motors)
		{
			currentMotor.setPower(newPower * scaleFactor);
		}
	}

	/**
	 * Disable the magnetic lock on ("float") the motors in the group
	 */
	public void setUnlocked()
	{
		for(DcMotor currentMotor : motors)
		{
			currentMotor.setPowerFloat();
		}
	}

	public void stopMotors()
	{
		setPower(0);
	}

	public void setRunMode(DcMotorController.RunMode newMode)
	{
		this.currentMode = newMode;
		for(DcMotor currentMotor : motors)
		{
			currentMotor.setChannelMode(newMode);
		}
	}

	public DcMotorController.RunMode getRunMode()
	{
		return this.currentMode;
	}

	/**
	 * @return the DcMotor being used for encoder data.
	 */
	public DcMotor getMotorWithEncoder()
	{
		if(motors.size() <= preferredEncoderNum)
		{
			preferredEncoderNum = 0;
			RoboLog.unusual("Tried to use a nonexistant motor to get encoder data!");
		}
		else if(!hasEncoder)
		{
			RoboLog.recoverable("Attempted to access encoder on a MotorGroup without one!");
		}

		return motors.get(preferredEncoderNum);
	}

	/**
	 * Send the reset command to the encoder and wait for it to be reset.
	 *
	 * THIS ONLY WORKS WHEN RUN FROM A LINEAR OPMODE, OTHERWISE IT WILL HANG YOUR PROGRAM!!
	 */
	public void resetEncoderBlocking()
	{
		DcMotor motorWithEncoder = getMotorWithEncoder();

		motorWithEncoder.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);

		long startTime = System.nanoTime();

		while(motorWithEncoder.getCurrentPosition() != 0)
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
				RoboLog.unusual("Encoder reset interrupted!");
				motorWithEncoder.setChannelMode(currentMode);
				resetEncoder();
				return;
			}
		}

		RoboLog.debug("Encoder reset took " + (System.nanoTime() - startTime) + " ns");


		encoderIgnoreDistance = 0;
		motorWithEncoder.setChannelMode(currentMode);
	}

	/**
	 *
	 * @return  The position of the motor beig used for encoder data
	 */
	public int getCurrentPosition()
	{
		return getMotorWithEncoder().getCurrentPosition() - encoderIgnoreDistance;
	}

	/**
	 * Set the target encoder position of the motor
	 *
	 * Automatically sets the motors to position mode.
	 * @param position the encoder position to move to in degrees.  Does not have to be between 0 and 359.
	 */
	public void setTargetPosition(int position)
	{
		if(currentMode != DcMotorController.RunMode.RUN_TO_POSITION)
		{
			setRunMode(DcMotorController.RunMode.RUN_TO_POSITION);
		}

		for(DcMotor currentMotor : motors)
		{
			currentMotor.setTargetPosition(position);
		}
	}

	/**
	 * Pseudo-resets the encoder distance using an internal counter.
	 */
	public void resetEncoder()
	{
		encoderIgnoreDistance = getMotorWithEncoder().getCurrentPosition();
	}
}
