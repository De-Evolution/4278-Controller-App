package com.team4278.robots.goatefoster;

import android.util.Pair;

import com.team4278.ButtonListenerTeleop;
import com.team4278.genericsteps.HardResetEncodersStep;
import com.team4278.motion.Drivetrain;
import com.team4278.utils.RoboLog;
import com.team4278.utils.RobotMath;

/**
 * Created by Jamie on 11/29/2015.
 */
public class GoatTeleop extends ButtonListenerTeleop
{

	RobotGoatEFoster robot;

	boolean armServoIsBraking = false;

	boolean inSynchrochainMode = false; //true if robot is currently executing a commannd to drive with the chains synchronized

	boolean fullSpeedMode = false;

	final static double REDUCED_SPEED_SCALING_FACTOR = .6;

	@Override
	public void onButtonPressed(Button button)
	{
		switch(button)
		{
			case RBUMPER:
				//armServoIsBraking = !armServoIsBraking;
				//robot.armBrake.setPosition(armServoIsBraking ? RobotGoatEFoster.BRAKE_POSITION_BRAKING : RobotGoatEFoster.BRAKE_POSITION_RELEASED);
				fullSpeedMode = !fullSpeedMode;

				break;
			case A:
			//executeSequenceSteps(robot.arm.new HomeStep());
				break;
			case DPAD_UP:
				if(!inSynchrochainMode)
				{
					synchronizedChainMove(true, 0, true, 0);
				}
				break;
			case DPAD_DOWN:
				if(!inSynchrochainMode)
				{
					synchronizedChainMove(false, -1, false, -1);
				}
				break;
			case DPAD_LEFT:
				if(!inSynchrochainMode)
				{
					synchronizedChainMove(false, -1, true, 1);
				}
				break;
			case DPAD_RIGHT:
				if(!inSynchrochainMode)
				{
					synchronizedChainMove(true, 1, false, -1);
				}
				break;
			case RIGHT_STICK:
				executeSequenceSteps(new HardResetEncodersStep(robot.drivetrain.getLeftMotors()), new HardResetEncodersStep(robot.drivetrain.getRightMotors()));
				RoboLog.info("Drive encoders hard reset");
				break;

		}

	}

	@Override
	public void onButtonReleased(Button button)
	{

	}

	@Override
	public void init()
	{
		super.init();
		robot = new RobotGoatEFoster(this);

		robot.armBrake.setPosition(RobotGoatEFoster.BRAKE_POSITION_RELEASED);
	}

	@Override
	public void loop()
	{
		inSynchrochainMode = isStepRunning(Drivetrain.MoveArbitraryDistancesStep.class);
		super.loop();

		double powLeft = -gamepad1.left_stick_y;
		double powRight = -gamepad1.right_stick_y;


		if(!fullSpeedMode)
		{
				powLeft *= REDUCED_SPEED_SCALING_FACTOR;
				powRight *= REDUCED_SPEED_SCALING_FACTOR;
		}


		if(!inSynchrochainMode)
		{
			if(gamepad1.left_bumper)
			{
				robot.drivetrain.tankDrive(powRight, powRight);
			}
			else
			{
				robot.drivetrain.tankDrive(powLeft, powRight);
			}

		}

		if(!robot.armIsAutoControlled)
		{
			robot.armMotors.setPower(Drivetrain.thresholdJoystickInput(gamepad1.left_trigger - gamepad1.right_trigger));

//			robot.armBrake.setPosition((gamepad1.left_stick_y + 1) / 2);
//			telemetry.addData("Arm Brake Servo Value", (gamepad1.left_stick_y + 1) / 2);
		}

		for(int counter = 0; counter < 0; ++counter)
		{
			RoboLog.debug("Reading encoder value test #" + counter + ": " + robot.drivetrain.getRightMotors().getPosition());
		}
	}
	//---------------------------------------------------------------------------------------------------------------------------------
	// Synchrochain code
	//---------------------------------------------------------------------------------------------------------------------------------

	static final int TEETH_PER_SPROCKET  = 15;
	static final int LINKS_PER_SEGMENT = 12; //last segment in the chain has 1 more link
	static final int SEGMENTS_PER_CHAIN = 7;
	static final int LAST_SEGMENT_EXTRA_LINKS = 4;
	static final int LINKS_PER_CHAIN = SEGMENTS_PER_CHAIN * LINKS_PER_SEGMENT + LAST_SEGMENT_EXTRA_LINKS;

	static final double ROTATIONS_PER_LINK = 1.0/TEETH_PER_SPROCKET;

	/**
	 * Does some math to figure out where the chain is based on its motor's encoder value.  Relies on the chain position being right at the start of the match,
	 * and the encoders not having been hard reset ever since.
	 * @param currentAbsolutePosition
	 * @return ai Pair of the number of links the chain is into its current segment, and the current segment of the chain (1-indexed).
	 */
	private Pair<Double, Integer> getChainPosition(double currentAbsolutePosition)
	{
		double positionLinks = currentAbsolutePosition * TEETH_PER_SPROCKET;
		double linksInCurrentChain = positionLinks % LINKS_PER_CHAIN;

		if(linksInCurrentChain < 0)
		{
			linksInCurrentChain = LINKS_PER_CHAIN + linksInCurrentChain;
		}

		int currentSegment = RobotMath.floor_double_int(linksInCurrentChain / LINKS_PER_SEGMENT) + 1; //1 indexed
		double linksIntoCurrentSegment = linksInCurrentChain - ((currentSegment - 1) * LINKS_PER_SEGMENT);

		return new Pair<Double, Integer>(linksIntoCurrentSegment, currentSegment);
	}

	/**
	 * Move the two chains so that their paddles are lined up
	 * @param alignLeftForward whether to align the left chain's paddled by moving it forward or backward
	 * @param driveDistanceLeft the number of segments to move on the left side.  If negative, the left side will move backwards, regardless alignLeftForward
	 */
	void synchronizedChainMove(boolean alignLeftForward, int driveDistanceLeft, boolean alignRightForward, int driveDistanceRight)
	{
		double leftDistance, rightDistance;
		//left side
		//--------------------------------------------------------------------------------------------------------
		robot.drivetrain.getLeftMotors().clearSoftReset();
		robot.drivetrain.getRightMotors().clearSoftReset();

		Pair<Double, Integer> leftChainPosition = getChainPosition(robot.drivetrain.getLeftMotors().getPosition());
		Pair<Double, Integer> rightChainPosition = getChainPosition(robot.drivetrain.getRightMotors().getPosition());

		RoboLog.debug("Chain position in links: left: " + leftChainPosition.first + ", right: " + rightChainPosition.first);

		if(Math.abs(leftChainPosition.first) < .25)
		{
			leftDistance = 0;
		}
		else if(alignLeftForward && leftChainPosition.first > .5)
		{
			//get chain paddles lined up (this should do little or nothing if they were already lined up)
			leftDistance = ROTATIONS_PER_LINK * (getTotalLinksInSegment(leftChainPosition.second) - leftChainPosition.first);

			leftDistance += ROTATIONS_PER_LINK * getTotalLinksInSegment(leftChainPosition.second + 1) * driveDistanceLeft;
		}
		else
		{
			leftDistance = ROTATIONS_PER_LINK * -1 * leftChainPosition.first;

			leftDistance += ROTATIONS_PER_LINK * getTotalLinksInSegment(leftChainPosition.second - 1) * driveDistanceLeft;
		}

		if(Math.abs(rightChainPosition.first) < .25)
		{
			rightDistance = 0;
		}
		if(alignRightForward && rightChainPosition.first > .5)
		{
			rightDistance = ROTATIONS_PER_LINK * (getTotalLinksInSegment(rightChainPosition.second) - rightChainPosition.first);

			rightDistance += ROTATIONS_PER_LINK * getTotalLinksInSegment(rightChainPosition.second + 1) * driveDistanceLeft;
		}
		else
		{
			rightDistance = ROTATIONS_PER_LINK * -1 * rightChainPosition.first;

			rightDistance += ROTATIONS_PER_LINK * getTotalLinksInSegment(rightChainPosition.second - 1) * driveDistanceRight;
		}

		RoboLog.debug("Chain desired distance: left: " + leftDistance / ROTATIONS_PER_LINK + ", right: " + rightDistance / ROTATIONS_PER_LINK);

		executeSequenceSteps(robot.drivetrain.new MoveArbitraryDistancesStep(leftDistance, rightDistance, false, 0));
	}

	/**
	 * Gets the total links in a segment of chain.  If the provided value is bigger than the number of segments, it loops back around.
	 * @return the number of links in the segment
	 */
	int getTotalLinksInSegment(int segment)
	{
		while(segment > SEGMENTS_PER_CHAIN)
		{
			segment -= SEGMENTS_PER_CHAIN;
		}
		return LINKS_PER_SEGMENT + (segment == SEGMENTS_PER_CHAIN ? LAST_SEGMENT_EXTRA_LINKS : 0); //if it is the last segment, it has more links
	}

}
