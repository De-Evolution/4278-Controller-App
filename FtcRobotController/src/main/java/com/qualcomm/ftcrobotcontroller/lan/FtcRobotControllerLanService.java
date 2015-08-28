package com.qualcomm.ftcrobotcontroller.lan;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.factory.RobotFactory;
import com.qualcomm.robotcore.robocol.RobocolDatagramSocket;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Originall decompiled from the FtcCommon aar.
 */
public class FtcRobotControllerLanService extends Service
{
	private final IBinder binder = new FtcRobotControllerLanBinder();
	private Robot robot;
	private EventLoop loop;
	private String status;
	private UpdateUI.Callback callback;
	private final RobotMonitor monitor;
	private Thread initThread;

	public FtcRobotControllerLanService() {
		this.status = "Robot Status: null";
		this.callback = null;
		this.monitor = new RobotMonitor();
	}

	public String getRobotStatus() {
		return this.status;
	}

	public IBinder onBind(Intent intent) {
		DbgLog.msg("Starting FTC Controller Service");
		DbgLog.msg("Android device is " + Build.MANUFACTURER + ", " + Build.MODEL);

		return this.binder;
	}

	public boolean onUnbind(Intent intent) {
		DbgLog.msg("Stopping FTC Controller Service");
		this.shutdownRobot();
		return false;
	}

	public synchronized void setCallback(UpdateUI.Callback callback) {
		this.callback = callback;
	}

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
		this.initThread = new Thread(new Initializer());
		this.initThread.start();

		while(this.initThread.getState() == Thread.State.NEW) {
			Thread.yield();
		}

	}

	public synchronized void shutdownRobot() {
		if(this.initThread != null && this.initThread.isAlive()) {
			this.initThread.interrupt();
		}

		if(this.robot != null) {
			this.robot.shutdown();
		}

		this.robot = null;
		this.setStatus("Robot Status: null");
	}

	private void setStatus(String var1) {
		this.status = var1;
		if(this.callback != null) {
			this.callback.robotUpdate(var1);
		}

	}

	private class Initializer implements Runnable {
		private Initializer() {
		}

		public void run() {

			if(FtcRobotControllerLanService.this.robot != null) {
				FtcRobotControllerLanService.this.robot.shutdown();
				FtcRobotControllerLanService.this.robot = null;
			}

			FtcRobotControllerLanService.this.setStatus("Robot Status: scanning for USB devices");

			try {
				Thread.sleep(2000L);
			} catch (InterruptedException var4) {
				FtcRobotControllerLanService.this.setStatus("Robot Status: abort due to interrupt");
				return;
			}

			FtcRobotControllerLanService.this.setStatus("Robot Status: starting robot");

			try {
				FtcRobotControllerLanService.this.robot.eventLoopManager.setMonitor(FtcRobotControllerLanService.this.monitor);
				try {
					//we can't use Robot.start() because we need to bind the socket to localhost

					FtcRobotControllerLanService.this.robot.socket.bind(new InetSocketAddress(20884));
					FtcRobotControllerLanService.this.robot.eventLoopManager.start(FtcRobotControllerLanService.this.loop);
				} catch (SocketException var4) {
					RobotLog.logStacktrace(var4);
					throw new RobotCoreException("Robot start failed: " + var4.toString());
				}
			} catch (RobotCoreException var2) {
				FtcRobotControllerLanService.this.setStatus("Robot Status: failed to start robot");
				RobotLog.setGlobalErrorMsg(var2.getMessage());
			}

		}
	}

	private class RobotMonitor implements EventLoopManager.EventLoopMonitor
	{
		private RobotMonitor() {
		}

		public void onStateChange(EventLoopManager.State state) {
			if(FtcRobotControllerLanService.this.callback != null) {
				switch(state) {
					case INIT:
						FtcRobotControllerLanService.this.callback.robotUpdate("Robot Status: init");
						break;
					case NOT_STARTED:
						FtcRobotControllerLanService.this.callback.robotUpdate("Robot Status: not started");
						break;
					case RUNNING:
						FtcRobotControllerLanService.this.callback.robotUpdate("Robot Status: running");
						break;
					case STOPPED:
						FtcRobotControllerLanService.this.callback.robotUpdate("Robot Status: stopped");
						break;
					case EMERGENCY_STOP:
						FtcRobotControllerLanService.this.callback.robotUpdate("Robot Status: EMERGENCY STOP");
						break;
					case DROPPED_CONNECTION:
						FtcRobotControllerLanService.this.callback.robotUpdate("Robot Status: dropped connection");
				}

			}
		}
	}

	public class FtcRobotControllerLanBinder extends Binder
	{
		public FtcRobotControllerLanBinder() {
		}

		public FtcRobotControllerLanService getService() {
			return FtcRobotControllerLanService.this;
		}
	}
}
