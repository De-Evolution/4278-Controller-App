package com.team4278.utils;

/**
 * Enum describing left and right.  Used for turns at the moment.
 */
public enum Side
{
	LEFT,
	RIGHT;

	public Side getOpposite()
	{
		if(this == LEFT)
		{
			return RIGHT;
		}
		else
		{
			return LEFT;
		}
	}
}
