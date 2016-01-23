package com.team4278.genericsteps;

import com.team4278.SequenceStep;
import com.team4278.motion.MotorGroup;

/**
 * SequenceStep to reset the encoders of the supplied MotorGroup
 */
public class HardResetEncodersStep extends SequenceStep
{
	MotorGroup motorsToReset;

	//according to the forums, an encoder reset takes 1-2 cycles to complete

	public HardResetEncodersStep(MotorGroup motorsToReset)
	{
		super();
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
		return false;
	}

	@Override
	public void end(EndReason reason)
	{
		motorsToReset.stop();
	}
}
