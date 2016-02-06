package com.team4278.motion;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.team4278.utils.MutablePair;
import com.team4278.utils.RobotMath;
import com.qualcomm.hardware.hitechnic.HiTechnicNxtDcMotorController;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to hold a group of motors which run at the same speed.
 *
 * NOTE: Updates here will not take effect until the OpMode / SequenceStep returns from loop()
 */
public class MotorGroup
{
	//set of motors and their respective encoder ignore distances (in counts)
	protected Set<MutablePair<DcMotor, Integer>> motors;
	protected Set<HiTechnicNxtDcMotorController> legacyControllers;

	private DcMotor preferredEncoderMotor;

	DcMotor.Direction direction = DcMotor.Direction.FORWARD;

	double scaleFactor = 1.0;

	private DcMotorController.RunMode currentMode;

	boolean hasEncoder;

	double encCountsPerRevolution;

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
		for(MutablePair<DcMotor, Integer> motorPair : motors)
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
	 * @param encCountsPerRevolution the number of counts the encoder produces per revolution each time the output shaft goes around once.
	 * @param toAdd the DcMotor to add to the group
	 */
	public MotorGroup(boolean encoded, double encCountsPerRevolution, DcMotor... toAdd)
	{
		motors = new HashSet<MutablePair<DcMotor, Integer>>();

		currentMode = encoded ? DcMotorController.RunMode.RUN_USING_ENCODERS : DcMotorController.RunMode.RUN_WITHOUT_ENCODERS;

		hasEncoder = encoded;

		legacyControllers = new HashSet<HiTechnicNxtDcMotorController>();

		this.encCountsPerRevolution = encCountsPerRevolution;

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
		motors.add(new MutablePair<DcMotor, Integer>(motor, 0));
		motor.setPower(0);
		motor.setMode(currentMode);
		motor.setDirection(direction);

		if(motor.getController() instanceof HiTechnicNxtDcMotorController)
		{
			legacyControllers.add((HiTechnicNxtDcMotorController) motor.getController());
		}
	}

	/**
	 * Set the power of all motors in the group.
	 *
	 * Resets the RunMode if it was set to RUN_TO_POSITION.
	 * @param newPower the power to set, from -1.0 to 1.0
	 */
	public void setPower(double newPower)
	{
		//check if in reset or position mode
		if(currentMode != DcMotorController.RunMode.RUN_WITHOUT_ENCODERS && currentMode != DcMotorController.RunMode.RUN_USING_ENCODERS)
		{
			setRunMode(hasEncoder ? DcMotorController.RunMode.RUN_USING_ENCODERS : DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
		}

		for(MutablePair<DcMotor, Integer> currentMotor : motors)
		{
			currentMotor.first.setPower(newPower * scaleFactor);
		}
	}

	/**
	 * Disable the magnetic lock on ("float") the motors in the group
	 */
	public void setUnlocked()
	{
		for(MutablePair<DcMotor, Integer> currentMotor : motors)
		{
			currentMotor.first.setPowerFloat();
		}
	}

	/**
	 * Sets all motors in the group to be stopped and locked.
	 */
	public void stop()
	{
		setPower(0);
	}

	private void setRunMode(DcMotorController.RunMode newMode)
	{
		this.currentMode = newMode;
		for(MutablePair<DcMotor, Integer> currentMotor : motors)
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
		for(MutablePair<DcMotor, Integer> motorPair : motors)
		{
			motorPair.second = 0;
		}
	}

	/**
	 *
	 * @return  The position (that is, distance since the last reset) of a motor in the group in rotations
	 */
	public double getPosition()
	{
		MutablePair<DcMotor, Integer> motorPair = motors.iterator().next();

		int currentCount = motorPair.first.getCurrentPosition();

		return (currentCount - (RobotMath.sgn(currentCount) * motorPair.second)) / encCountsPerRevolution;
	}

	/**
	 * Set the target encoder position of the motor
	 *
	 * Automatically sets the motors to position mode.
	 *
	 * NOTE: calling setPower() will return the motor to speed mode.  If you want to change the speed but keep moving to a position,
	 * call this function again.
	 *
	 * @param position the encoder position to move to in rotations.  Can be negative or positive.
	 * @param power the motor power to use to move to this position.
	 */
	public void setTargetPosition(double position, double power)
	{
		int desiredCounts = RobotMath.floor_double_int(encCountsPerRevolution * position);

		if(currentMode != DcMotorController.RunMode.RUN_TO_POSITION)
		{
			setRunMode(DcMotorController.RunMode.RUN_TO_POSITION);
		}

		for(MutablePair<DcMotor, Integer> motorPair : motors)
		{
			motorPair.first.setTargetPosition(desiredCounts + motorPair.second);
			motorPair.first.setPower(power * scaleFactor);
		}
	}

	/**
	 * Pseudo-resets the encoder distance of each motor using an internal counter.  Unlike the the other
	 * reset function, this completes immediately.
	 */
	public void softResetEncoders()
	{
		for(MutablePair<DcMotor, Integer> motorPair : motors)
		{
			motorPair.second = motorPair.first.getCurrentPosition();
		}
	}

	/**
	 * Clears any soft rests, going back to the motor group's actual value.
	 */
	public void clearSoftReset()
	{
		for(MutablePair<DcMotor, Integer> motorPair : motors)
		{
			motorPair.second = 0;
		}
	}

	/**
	 * Sets all legacy motor controllers to read mode.
	 *
	 * This means that the encoders can be read and you can safely call getPosition()
	 *
	 * NOTE: This method affects CONTROLLERS, not motors.  If the two motors from one controller are split across different MotorGroups, then BOTH motors
	 * will be affected by this call and the other group might act strange.
	 */
	public void setReadMode()
	{
		for(HiTechnicNxtDcMotorController controller : legacyControllers)
		{
			controller.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
		}
	}

	/**
	 * Sets all legacy motor controllers to write mode.
	 *
	 * This means that data can be written and you can safely call setPower() and setTargetPosition()
	 *
	 * NOTE: This method affects CONTROLLERS, not motors.  If the two motors from one controller are split across different MotorGroups, then BOTH motors
	 * will be affected by this call and the other group might act strange.
	 */
	public void setWriteMode()
	{
		for(HiTechnicNxtDcMotorController controller : legacyControllers)
		{
			controller.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
		}
	}

	public boolean isBusy()
	{
		for(MutablePair<DcMotor, Integer> motorPair : motors)
		{
			if(motorPair.first.isBusy())
			{
				return true;
			}
		}

		return false;
	}
}
