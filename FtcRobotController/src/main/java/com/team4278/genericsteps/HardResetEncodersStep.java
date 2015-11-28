package com.team4278.genericsteps;

import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.SequenceStep;
import com.team4278.motion.MotorGroup;

/**
 * class which resets the encoder counts of motors on a legacy controller
 */
public class HardResetEncodersStep extends SequenceStep
{

	long startTime;

	MotorGroup motorsToReset;

	public HardResetEncodersStep(Telemetry telemetry, MotorGroup motorsToReset)
	{
		super(telemetry);
		startTime = System.currentTimeMillis();
		this.motorsToReset = motorsToReset;
	}

	@Override
	public void init()
	{
		motorsToReset.startEncoderReset();
	}

	@Override
	public boolean loop()
	{
		//this might seem really hacky, but I've actually found it to be the best way.
		//Trying to read the value
		return System.currentTimeMillis() - startTime > 50;
	}

	@Override
	public void end()
	{

	}
}
