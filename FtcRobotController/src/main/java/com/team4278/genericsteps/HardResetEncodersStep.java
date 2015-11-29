package com.team4278.genericsteps;

import com.qualcomm.robotcore.robocol.Telemetry;
import com.team4278.SequenceStep;
import com.team4278.motion.MotorGroup;

/**
 * SequenceStep to reset the encoders of the supplied MotorGroup
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
		//Trying to read the value and see when it changes to 0 seems like a good idea, but I've had issues where the
		//value initially reads as 0 and then jumps to the actual number after a certain amount of time.
		//Also, there's the issue of what to do if the encoder REALLY IS at 0 when you run reset.
		return System.currentTimeMillis() - startTime > 75;
	}

	@Override
	public void end()
	{
		motorsToReset.stop();
	}
}
