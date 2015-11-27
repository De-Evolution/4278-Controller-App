package com.team4278.motion;

import android.util.Pair;

import com.team4278.utils.RoboLog;
import com.team4278.utils.RobotMath;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to hold a group of motors which run at the same speed.
 *
 * NOTE: Updates here will not take effect until the OpMode / SequenceStep returns from loop()
 */
public class MotorGroup
{
	protected Set<Pair<DcMotor, Integer>> motors;

	private DcMotor preferredEncoderMotor;

	DcMotor.Direction direction = DcMotor.Direction.FORWARD;

	/**
	 * Scale factor to multiply motor power sets by.  if negative, the motor is inverted.
	 *
	 */
	double scaleFactor = 1;

	private DcMotorController.RunMode currentMode;

	boolean hasEncoder;

	//stored position of the encoder so that it can be pseudo-reset.

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

	public void setReversed(boolean inverted)
	{
		direction = inverted ? DcMotor.Direction.REVERSE : DcMotor.Direction.FORWARD;
		for(Pair<DcMotor, Integer> motorPair : motors)
		{
			motorPair.first.setDirection(direction);
		}
	}

	/**
	 * Get whether the motor group direction is inverted
	 */
	public boolean getInverted()
	{
		return direction == DcMotor.Direction.REVERSE;
	}

	/**
	 *
	 * @param encoded whether the group has an encoder attached.  This controls whether the motors will be set to open or closed loop mode.
	 * @param toAdd the DcMotor to add to the group
	 */
	public MotorGroup(boolean encoded, DcMotor... toAdd)
	{
		motors = new HashSet<Pair<DcMotor, Integer>>();

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
		motors.add(new Pair<DcMotor, Integer>(motor, 0));
		motor.setPower(0);
		motor.setMode(currentMode);
		motor.setDirection(direction);
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

		for(Pair<DcMotor, Integer> currentMotor : motors)
		{
			currentMotor.first.setPower(newPower * scaleFactor);
		}
	}

	/**
	 * Disable the magnetic lock on ("float") the motors in the group
	 */
	public void setUnlocked()
	{
		for(Pair<DcMotor, Integer> currentMotor : motors)
		{
			currentMotor.first.setPowerFloat();
		}
	}

	/**
	 * Sets all motors in the group to be stopped and locked.
	 */
	public void stopMotors()
	{
		setPower(0);
	}

	private void setRunMode(DcMotorController.RunMode newMode)
	{
		this.currentMode = newMode;
		for(Pair<DcMotor, Integer> currentMotor : motors)
		{
			currentMotor.first.setMode(newMode);
		}
	}

	private DcMotorController.RunMode getRunMode()
	{
		return this.currentMode;
	}

	/**
	 * Send the reset command to every motor.  Annoyingly, however, it will not be processed for a hardware cycle or two.
	 *
	 * Send a setPower() command a few loops later to set the motor back to normal.
	 */
	public void startEncoderReset()
	{
		setRunMode(DcMotorController.RunMode.RESET_ENCODERS);

		//reset encoder ignore distances
		for(Pair<DcMotor, Integer> motorPair : motors)
		{
			motors.remove(motorPair);

			motors.add(new Pair<DcMotor, Integer>(motorPair.first, 0));
		}
	}

	/**
	 *
	 * @return  The position of a motor in the group
	 */
	public int getCurrentPosition()
	{
		Pair<DcMotor, Integer> motorPair = motors.iterator().next();

		return motorPair.first.getCurrentPosition() - motorPair.second;
	}

	/**
	 * Set the target encoder position of the motor
	 *
	 * Automatically sets the motors to position mode.
	 *
	 * NOTE: calling setPower() will return the motor to speed mode.  If you want to change the speed but keep moving to a position,
	 * call this function again.
	 *
	 * @param position the encoder position to move to in degrees.  Does not have to be between 0 and 359.
	 * @param power the motor power to use to move to this position.
	 */
	public void setTargetPosition(int position, double power)
	{
		if(currentMode != DcMotorController.RunMode.RUN_TO_POSITION)
		{
			setRunMode(DcMotorController.RunMode.RUN_TO_POSITION);
		}

		for(Pair<DcMotor, Integer> motorPair : motors)
		{
			motorPair.first.setTargetPosition(position + motorPair.second);
			motorPair.first.setPower(power * scaleFactor);
		}
	}

	/**
	 * Pseudo-resets the encoder distance of each motor using an internal counter.  Unlike the the other
	 * reset function, this completes immediately.
	 */
	public void softResetEncoder()
	{
		for(Pair<DcMotor, Integer> motorPair : motors)
		{
			motors.remove(motorPair);

			motors.add(new Pair<DcMotor, Integer>(motorPair.first, motorPair.first.getCurrentPosition()));
		}
	}
}
