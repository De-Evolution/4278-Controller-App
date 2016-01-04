package com.team4278.robots.goatefoster;

import com.team4278.SequenceStep;

/**
 * Command to apply or release the arm brake
 */
public class ArmBrakeStep extends SequenceStep
{


	RobotGoatEFoster robot;

	boolean shouldBrake;

	/**
	 *
	 * @param robot
	 * @param brake if true, apply the brake.  If false, release the brake
	 */
	public ArmBrakeStep(RobotGoatEFoster robot, boolean brake)
	{
		super(100);
		this.robot = robot;

		this.shouldBrake = brake;
	}

	@Override
	public void init()
	{
		robot.armBrake.setPosition(shouldBrake ? RobotGoatEFoster.BRAKE_POSITION_BRAKING : RobotGoatEFoster.BRAKE_POSITION_RELEASED);
	}

	@Override
	public boolean loop()
	{
		return false;
	}

	@Override
	public void end(EndReason reason)
	{

	}
}
