package com.qualcomm.ftcrobotcontroller.lan;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.FtcRobotControllerService;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.util.RobotLog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	
	Method setStatus;
	
	public FtcRobotControllerLanService()
	{
		super();

		statusUpdater = new StatusUpdater();
		
		try
		{
			setStatus = super.getClass().getDeclaredMethod("a", String.class);
			setStatus.setAccessible(true);
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
	}
	
	//proxy to super.a
	public void setStatus(String newStatus)
	{
		try
		{
			setStatus.invoke(this, newStatus);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
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
		this.initThread = new Thread(new lanInitializer());
		this.initThread.start();

		while(this.initThread.getState() == Thread.State.NEW) {
			Thread.yield();
		}

	}

	public synchronized void shutdownRobot() {
		super.shutdownRobot();
		if(this.initThread != null && this.initThread.isAlive()) {
			this.initThread.interrupt();
		}
	}

	private class lanInitializer implements Runnable {
		private lanInitializer() {
		}

		public void run() {

			//get things that we need to access from superclass
			Robot robot = null;
			try
			{
				Field robotField = super.getClass().getDeclaredFields()[2];
				robotField.setAccessible(true);
				robot = (Robot) robotField.get(this);
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
				robot = null;
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
					robot.eventLoopManager.start(FtcRobotControllerLanService.this.loop);
				}
				catch (SocketException var4)
				{
					RobotLog.logStacktrace(var4);
					throw new RobotCoreException("Robot start failed: " + var4.toString());
				}
			}
			catch (RobotCoreException var2)
			{
				setStatus("Robot Status: failed to start robot");
				RobotLog.setGlobalErrorMsg(var2.getMessage());
			}
			
		}
	}
	
	class StatusUpdater implements EventLoopManager.EventLoopMonitor
	{
		private StatusUpdater() {
		}
		
		public void onStateChange(com.qualcomm.robotcore.eventloop.EventLoopManager.State state) {
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
	}
}
