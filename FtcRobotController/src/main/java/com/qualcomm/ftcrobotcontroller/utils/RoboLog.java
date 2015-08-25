package com.qualcomm.ftcrobotcontroller.utils;

import android.util.Log;

public class RoboLog
{

	/**
	 * Log a FATAL error, after which the robot cannot (properly) function. <br>
	 * @param message
	 */
	public static void fatal( String message)
	{
		Log.e("Robot-Fatal", message);
	}
	
	/**
	 * Log a failure which may kill one function or one thread, however the robot as a whole can keep functioning.
	 * @param message
	 */
	public static void recoverable(String message)
	{
		Log.e("Robot-Recoverable", message);
	}
	
	/**
	 * Log something which should not happen under normal circumstances and probably is a bug, but does not cause anything to crash.
	 * @param message
	 */
	public static void unusual(String message)
	{
		Log.w("Robot-Unusual", message);
	}
	
	/**
	 * Log a semi-important message which the user should probably see, but does not indicate anything is broken.
	 */
	public static void info(String message)
	{
		Log.i("Robot-Info", message);
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
	 * Log an event which shouldn't happen in normal operation, but soehow has.
	 * @param message
	 */
	public static void unexpected( String message)
	{
		Log.wtf("Robot-Unexpected", "...what? " + message);
	}
}
