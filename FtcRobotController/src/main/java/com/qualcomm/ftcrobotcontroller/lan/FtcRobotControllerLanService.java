package com.qualcomm.ftcrobotcontroller.lan;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.FtcRobotControllerService;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.factory.RobotFactory;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.RobotLog;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Service which connects to the DS via LAN instead of Wifi Direct
 */
public class FtcRobotControllerLanService extends FtcRobotControllerService
{
	/*
	 * Variables which are used instead of the private ones in the superclass.
	 */
	Thread initThread;
	EventLoop loop;
	StatusUpdater statusUpdater;

	UpdateUI.Callback updateCallback;
	
	Field binderField;

	public FtcRobotControllerLanService()
	{
		super();

		statusUpdater = new StatusUpdater();

		binderField = getClass().getSuperclass().getDeclaredFields()[0];
		binderField.setAccessible(true);
	}

	@Override
	public IBinder onBind(Intent intent) {
		DbgLog.msg("Starting FTC Controller Service");
		DbgLog.msg("Android device is " + Build.MANUFACTURER + ", " + Build.MODEL);

		//do not start WifiDirectAssistant

		try
		{
			return (IBinder) binderField.get(this);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		DbgLog.msg("Stopping FTC Controller Service");
		//do not shut down Wifi Direct Assistant
		this.shutdownRobot();
		return false;
	}

	//proxy to super.a
	public void setStatus(String newStatus)
	{
		updateCallback.robotUpdate(newStatus);
	}

	@Override
	public void setCallback(UpdateUI.Callback callback)
	{
		this.updateCallback = callback;
	}

	@Override
	public synchronized void setupRobot(EventLoop eventLoop) {
		if(this.initThread != null && this.initThread.isAlive()) {
			DbgLog.msg("FtcRobotControllerLanService.setupRobot() is currently running, stopping old setup");
			this.initThread.interrupt();

			while(this.initThread.isAlive()) {
				Thread.yield();
			}

			DbgLog.msg("Old setup stopped; restarting setup");
		}

		RobotLog.clearGlobalErrorMsg();
		DbgLog.msg("Processing robot setup");
		this.loop = eventLoop;
		this.initThread = new Thread(new LanInitializer());
		this.initThread.start();

		while(this.initThread.getState() == Thread.State.NEW) {
			Thread.yield();
		}

	}

	public synchronized void shutdownRobot() {
		super.shutdownRobot(); //TODO: According to the debugger socket seems to be being shut down twice, at least. Investigate this.
		if(this.initThread != null && this.initThread.isAlive()) {
			this.initThread.interrupt();
			try
			{
				this.initThread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class LanInitializer implements Runnable {
		private LanInitializer() {
		}

		public void run() {

			//get things that we need to access from superclass
			Field robotField;
			Robot robot;
			try
			{
				robotField = FtcRobotControllerService.class.getDeclaredFields()[2];
				robotField.setAccessible(true);
				robot = (Robot) robotField.get(FtcRobotControllerLanService.this);
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
				return;
			}
			
			//now, init the robot.
			if (robot != null)
			{
				robot.shutdown();
			}

			try
			{
				robot = RobotFactory.createRobot();
			}
			catch (RobotCoreException e)
			{
				e.printStackTrace();
				setStatus("Robot Status: abort due to error creating robot: " + e.getClass().getSimpleName());
				return;
			}
			
			setStatus("Robot Status: scanning for USB devices");
			
			try
			{
				Thread.sleep(2000L);
			}
			catch (InterruptedException var4)
			{
				setStatus("Robot Status: abort due to interrupt");
				return;
			}
			
			setStatus("Robot Status: starting robot");
			
			try
			{
				robot.eventLoopManager.setMonitor(FtcRobotControllerLanService.this.statusUpdater);
				try
				{
					//we can't use Robot.start() because we need to bind the socket to localhost

					robot.socket.bind(new InetSocketAddress(20884));
				}
				catch (SocketException var4)
				{
					RobotLog.logStacktrace(var4);
					throw new RobotCoreException("Robot start failed: " + var4.toString());
				}

				robot.eventLoopManager.start(FtcRobotControllerLanService.this.loop);

				robotField.set(FtcRobotControllerLanService.this, robot);
			}
			catch (RobotCoreException var2)
			{
				setStatus("Robot Status: failed to start robot");
				RobotLog.setGlobalErrorMsg(var2.getMessage());

				robot.socket.close(); //remember to close the socket on failure!  Not having this line caused a very annoying issue where the robot start would fail with an address in use error
															//if it had failed to start previously until the app was restarted.
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	class StatusUpdater implements EventLoopManager.EventLoopMonitor
	{
		private StatusUpdater() {
		}

		@Override
		public void onStateChange(RobotState state) {
				switch(state)
				{
					case INIT:
						setStatus("Robot Status: init");
						break;
					case NOT_STARTED:
						setStatus("Robot Status: not started");
						break;
					case RUNNING:
						setStatus("Robot Status: running");
						break;
					case STOPPED:
						setStatus("Robot Status: stopped");
						break;
					case EMERGENCY_STOP:
						setStatus("Robot Status: EMERGENCY STOP");
						break;
					case DROPPED_CONNECTION:
						setStatus("Robot Status: dropped connection");
				}
		}

		@Override
		public void onErrorOrWarning()
		{
			updateCallback.refreshErrorTextOnUiThread();
		}
	}
}
