package com.team4278.utils;

import android.util.Log;

import com.qualcomm.robotcore.robocol.Telemetry;

public class RoboLog
{

	public static Telemetry telemetryToUse;

	/**
	 * Log a FATAL error, after which the robot cannot (properly) function. <br>
	 * @param message
	 */
	public static void fatal( String message)
	{
		Log.e("Robot-Fatal", message);
		if(telemetryToUse != null)
		{

			telemetryToUse.addData("\u1f4a3\udca3", message); //bomb emoji
		}
	}
	
	/**
	 * Log a failure which may kill one function or one thread, however the robot as a whole can keep functioning.
	 * @param message
	 */
	public static void recoverable(String message)
	{
		Log.e("Robot-Recoverable", message);
		if(telemetryToUse != null)
		{
			telemetryToUse.addData("\ud83d\udeab", message); //no-entry emoji
		}
	}
	
	/**
	 * Log something which should not happen under normal circumstances and probably is a bug, but does not cause anything to crash.
	 * @param message
	 */
	public static void unusual(String message)
	{
		Log.w("Robot-Unusual", message);
		if(telemetryToUse != null)
		{
			telemetryToUse.addData("\u26a0", message);
		}
	}
	
	/**
	 * Log a semi-important message which the user should probably see, but does not indicate anything is broken.
	 */
	public static void info(String message)
	{
		Log.i("Robot-Info", message);
		if(telemetryToUse != null)
		{
			telemetryToUse.addData("\u2709", message);
		}
	}
	
	/**
	 * Log a message which is not important during normal operation, but is useful if you're trying to debug the robot.
	 * @param message
	 */
	public static void debug(String message)
	{
		Log.d("Robot-Debug", message);
	}

	/**
	 * Log an event which shouldn't happen in normal operation, but somehow has.
	 * @param message
	 */
	public static void unexpected( String message)
	{
		Log.wtf("Robot-Unexpected", "...what? " + message);
		if(telemetryToUse != null)
		{
			telemetryToUse.addData("\ud83d\ude27", message);
		}
	}
}
