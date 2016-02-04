package com.team4278.robots.goatefoster;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.team4278.SequenceStep;
import com.team4278.genericsteps.HardResetEncodersStep;
import com.team4278.genericsteps.LambdaStep;
import com.team4278.motion.Drivetrain;
import com.team4278.motion.MotorGroup;
import com.team4278.utils.RoboLog;
import com.team4278.utils.Units;

/**
 * Robot class for Goat-E Foster
 */
public class RobotGoatEFoster
{

	public static double BRAKE_POSITION_BRAKING = .7;//.8;
	public static double BRAKE_POSITION_RELEASED = 0;//BRAKE_POSITION_BRAKING - .25;

	public Drivetrain drivetrain;

	public Servo armBrake;


	public DigitalChannel homingHallEffect;

	public MotorGroup armMotors;

	public MotorGroup measuringTapeMotors;

	public Arm arm;

	//flag to tell the teleop code not to run the arm from controller input
	boolean armIsAutoControlled;

	public RobotGoatEFoster(OpMode opMode)
	{
		drivetrain = Drivetrain.make(true, 15.7 * Units.INCH, 4.0 * Units.INCH * Math.PI, 1120 * 42/30 /* gear ratio */ * .95, opMode);

		armBrake = opMode.hardwareMap.servo.get("armBrakeServo");

		armMotors = new MotorGroup(true, 1440 * (18 / 14) * (60/18), opMode.hardwareMap.dcMotor.get("mArmTop"), opMode.hardwareMap.dcMotor.get("mArmBottom"));
		measuringTapeMotors = new MotorGroup(false, 0, opMode.hardwareMap.dcMotor.get("mHookTop"), opMode.hardwareMap.dcMotor.get("mHookBottom"));


		homingHallEffect = opMode.hardwareMap.digitalChannel.get("armEndstop");

		//NeverRest 40 motors only produce 78% of the encoder pulses as standard encoders, so 78% power is full speed when running with encoders
		drivetrain.getLeftMotors().setScaleFactor(.78);
		drivetrain.getRightMotors().setScaleFactor(.78);

		arm = new Arm();
	}

	public class Arm
	{
		public class HomeStep extends SequenceStep {

			public HomeStep()
			{
				addStepBefore(new LambdaStep(new Runnable() {
					@Override
					public void run() {
						armIsAutoControlled = true;
					}
				}));
				//addStepBefore(new HomeArmReverseStep(-.5));
				addStepBefore(new HardResetEncodersStep(armMotors));
				addStepBefore(new HomeArmForwardStep());
				//addStepAfter(new HomeArmReverseStep(robot, -.5));
				addStepBefore(new HardResetEncodersStep(armMotors));

				addStepBefore(new LambdaStep(new Runnable() {
					@Override
					public void run() {
						armIsAutoControlled = false;
					}
				}));

			}

			@Override
			public void init() {

			}

			@Override
			public boolean loop() {
				return false;
			}

			@Override
			public void end(EndReason reason) {

			}
		}

		//the outer class is the second step of the homing process, and the inner class is steps 1 and 3
		private class HomeArmReverseStep extends SequenceStep
		{
			double power;

			public HomeArmReverseStep(double power)
			{
				super();

				this.power = power;
			}

			@Override
			public void init()
			{
				RoboLog.debug("Motors set to " + power);
			}

			@Override
			public void second_init()
			{
				armMotors.setPower(power);
			}

			@Override
			public boolean loop()
			{
				return homingHallEffect.getState(); //active low
			}


			@Override
			public void end(EndReason reason)
			{
				armMotors.stop();
			}

		}



		private class HomeArmForwardStep extends SequenceStep {

			public HomeArmForwardStep() {
				super(3000);
			}

			@Override
			public void init() {
				setPosition(.05, .5);
			}

			@Override
			public boolean loop() {
				return true;
			}


			@Override
			public void end(SequenceStep.EndReason reason) {
				armMotors.stop();
			}
		}

		/**
		 * Set the position of the arm, relative to the home point (all the way back)
		 * @param rotations
		 */
		public void setPosition(double rotations, double power)
		{
			armMotors.setTargetPosition(rotations, power);
		}

	}

}
