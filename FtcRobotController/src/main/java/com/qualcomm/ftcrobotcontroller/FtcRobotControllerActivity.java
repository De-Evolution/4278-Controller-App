/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.ftccommon.FtcRobotControllerService;
import com.qualcomm.ftccommon.FtcRobotControllerService.FtcRobotControllerBinder;
import com.qualcomm.ftccommon.FtcWifiChannelSelectorActivity;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.ftccommon.Restarter;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.ftccommon.configuration.FtcLoadFileActivity;
import com.qualcomm.ftcrobotcontroller.lan.FtcRobotControllerLanService;
import com.qualcomm.ftcrobotcontroller.lan.WifiIPUpdaterReceiver;
import com.qualcomm.ftcrobotcontroller.opmodes.FtcOpModeRegister;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.util.Dimmer;
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Field;

public class FtcRobotControllerActivity extends Activity {

  private static final int REQUEST_CONFIG_WIFI_CHANNEL = 1;
  private static final boolean USE_DEVICE_EMULATION = false;
  private static final int NUM_GAMEPADS = 2;

  public static final String CONFIGURE_FILENAME = "CONFIGURE_FILENAME";

  protected SharedPreferences preferences;

  protected UpdateUI.Callback callback;
  protected Context context;
  private Utility utility;
  protected ImageButton buttonMenu;

  protected TextView textDeviceName;
  protected TextView textWifiDirectStatus;
  protected TextView textRobotStatus;
  protected TextView[] textGamepad = new TextView[NUM_GAMEPADS];
  protected TextView textOpMode;
  protected TextView textErrorMessage;
  protected ImmersiveMode immersion;

  protected UpdateUI updateUI;
  protected Dimmer dimmer;
  protected LinearLayout entireScreenLayout;

	Intent wdServiceIntent;
	Intent lanServiceIntent;

	IntentFilter networkUpdateFilter;
	protected WifiIPUpdaterReceiver ipUpdaterReceiver;

  protected FtcRobotControllerService controllerService;

	protected boolean useLANconnection;

  public static FtcEventLoop eventLoop;

  protected class RobotRestarter implements Restarter {

    public void requestRestart() {
      requestRobotRestart();
    }

  }

  protected ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      FtcRobotControllerBinder binder = (FtcRobotControllerBinder) service;
      onServiceBind(binder.getService());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      controllerService = null;
    }
  };

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
      // a new USB device has been attached
      DbgLog.msg("USB Device attached; app restart may be needed");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_ftc_controller);

    utility = new Utility(this);
    context = this;
    entireScreenLayout = (LinearLayout) findViewById(R.id.entire_screen);
    buttonMenu = (ImageButton) findViewById(R.id.menu_buttons);
    buttonMenu.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        openOptionsMenu();
      }
    });

    textDeviceName = (TextView) findViewById(R.id.textDeviceName);
    textWifiDirectStatus = (TextView) findViewById(R.id.textWifiDirectStatus);
    textRobotStatus = (TextView) findViewById(R.id.textRobotStatus);
    textOpMode = (TextView) findViewById(R.id.textOpMode);
    textErrorMessage = (TextView) findViewById(R.id.textErrorMessage);
    textGamepad[0] = (TextView) findViewById(R.id.textGamepad1);
    textGamepad[1] = (TextView) findViewById(R.id.textGamepad2);
    immersion = new ImmersiveMode(getWindow().getDecorView());
    dimmer = new Dimmer(this);
    dimmer.longBright();
    Restarter restarter = new RobotRestarter();

    updateUI = new UpdateUI(this, dimmer);
    updateUI.setRestarter(restarter);
    updateUI.setTextViews(textWifiDirectStatus, textRobotStatus,
		    textGamepad, textOpMode, textErrorMessage, textDeviceName);
    callback = updateUI.new Callback();

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    preferences = PreferenceManager.getDefaultSharedPreferences(this);

    hittingMenuButtonBrightensScreen();


	  wdServiceIntent = new Intent(this, FtcRobotControllerService.class);
	  lanServiceIntent = new Intent(this, FtcRobotControllerLanService.class);
	  networkUpdateFilter = new IntentFilter();
	  networkUpdateFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

	  ipUpdaterReceiver = new WifiIPUpdaterReceiver(this, textWifiDirectStatus);


    if (USE_DEVICE_EMULATION) { HardwareFactory.enableDeviceEmulation(); }

	  useLANconnection = preferences.getBoolean("use_lan_connection", false);

  }
	//connection starting and stopping functions
	private void startLAN()
	{
		bindService(lanServiceIntent, connection, Context.BIND_AUTO_CREATE);
		registerReceiver(ipUpdaterReceiver, networkUpdateFilter);
	}
	private void stopLAN()
	{
		stopService(lanServiceIntent);
		unbindService(connection);
		unregisterReceiver(ipUpdaterReceiver);
	}


	private void startWD()
	{
		bindService(wdServiceIntent, connection, Context.BIND_AUTO_CREATE);
	}
	private void stopWD()
	{
		stopService(wdServiceIntent);
		unbindService(connection);
	}


  @Override
  protected void onStart() {
    super.onStart();

    // save 4MB of logcat to the SD card
    RobotLog.writeLogcatToDisk(this, 4 * 1024);

    if(useLANconnection)
    {
	    startLAN();
    }
    else
    {
	    startWD();
    }

    utility.updateHeader(Utility.NO_FILE, R.string.pref_hardware_config_filename, R.id.active_filename, R.id.included_header);

	  if(!useLANconnection)
	  {
		  callback.wifiDirectUpdate(WifiDirectAssistant.Event.DISCONNECTED);
	  }

    entireScreenLayout.setOnTouchListener(new View.OnTouchListener()
    {
	    @Override
	    public boolean onTouch(View v, MotionEvent event)
	    {
		    dimmer.handleDimTimer();
		    return false;
	    }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (controllerService != null) unbindService(connection);

    RobotLog.cancelWriteLogcatToDisk(this);

    preferences.edit().putBoolean("use_lan_connection", useLANconnection).apply();
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus){
    super.onWindowFocusChanged(hasFocus);
    // When the window loses focus (e.g., the action overflow is shown),
    // cancel any pending hide action. When the window gains focus,
    // hide the system UI.
    if (hasFocus) {
      if (ImmersiveMode.apiOver19()){
        // Immersive flag only works on API 19 and above.
        immersion.hideSystemUI();
      }
    } else {
      immersion.cancelSystemUIHide();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
	  getMenuInflater().inflate(R.menu.ftc_robot_controller, menu);
	  menu.findItem(R.id.action_toggle_lan).setTitle(useLANconnection ? R.string.lan_disable_menu_item : R.string.lan_enable_menu_item);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_restart_robot:
        dimmer.handleDimTimer();
        Toast.makeText(context, "Restarting Robot", Toast.LENGTH_SHORT).show();
        requestRobotRestart();
        return true;
      case R.id.action_choose_config_file:
        startActivityForResult(new Intent(this, FtcLoadFileActivity.class), LaunchActivityConstantsList.FTC_ROBOT_CONTROLLER_ACTIVITY_CONFIGURE_ROBOT);
	      return true;
      case R.id.action_about:
        // The string to launch this activity must match what's in AndroidManifest of FtcCommon for this activity.
        Intent intent = new Intent("com.qualcomm.ftccommon.configuration.AboutActivity.intent.action.Launch");
        startActivity(intent);
        return true;
      case R.id.action_exit_app:
        finish();
        return true;
      case R.id.action_view_logs:
        // The string to launch this activity must match what's in AndroidManifest of FtcCommon for this activity.
        Intent viewLogsIntent = new Intent("com.qualcomm.ftccommon.ViewLogsActivity.intent.action.Launch");
        viewLogsIntent.putExtra(LaunchActivityConstantsList.VIEW_LOGS_ACTIVITY_FILENAME, RobotLog.getLogFilename(this));
        startActivity(viewLogsIntent);
        return true;
      case R.id.action_change_wifi_channel:
		  startActivity(new Intent(this, FtcWifiChannelSelectorActivity.class));
	    case R.id.action_toggle_lan:
		    if(useLANconnection)
		    {
			    stopLAN();
			    startWD();

			    item.setTitle(R.string.lan_enable_menu_item);
			    useLANconnection = false;
		    }
		    else
		    {
			    stopWD();
			    startLAN();

			    item.setTitle(R.string.lan_disable_menu_item);
			    useLANconnection = true;
		    }
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // don't destroy assets on screen rotation
  }

  @Override
  protected void onActivityResult(int request, int result, Intent intent) {
    if (request == REQUEST_CONFIG_WIFI_CHANNEL) {
      if (result == RESULT_OK) {
        Toast toast = Toast.makeText(context, "Configuration Complete", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        showToast(toast);
      }
    }
    if (request == LaunchActivityConstantsList.FTC_ROBOT_CONTROLLER_ACTIVITY_CONFIGURE_ROBOT) {
      if (result == RESULT_OK) {
        Serializable extra = intent.getSerializableExtra(FtcRobotControllerActivity.CONFIGURE_FILENAME);
        if (extra != null) {
          utility.saveToPreferences(extra.toString(), R.string.pref_hardware_config_filename);
          utility.updateHeader(Utility.NO_FILE, R.string.pref_hardware_config_filename, R.id.active_filename, R.id.included_header);
        }
      }

    }
  }

  public void onServiceBind(FtcRobotControllerService service) {
    DbgLog.msg("Bound to Ftc Controller Service");
    controllerService = service;
    updateUI.setControllerService(controllerService);

	  if(!useLANconnection)
	  {
		  callback.wifiDirectUpdate(controllerService.getWifiDirectStatus());
	  }
    callback.robotUpdate(controllerService.getRobotStatus());
    requestRobotSetup();
  }

  private void requestRobotSetup() {
    if (controllerService == null) return;

    FileInputStream fis = fileSetup();
    // if we can't find the file, don't try and build the robot.
    if (fis == null) { return; }

    HardwareFactory factory;

    // Modern Robotics Factory for use with Modern Robotics hardware
    HardwareFactory modernRoboticsFactory = new HardwareFactory(context);
    modernRoboticsFactory.setXmlInputStream(fis);
    factory = modernRoboticsFactory;

    eventLoop = new FtcEventLoop(factory, new FtcOpModeRegister(), callback, this);

    controllerService.setCallback(callback);
    controllerService.setupRobot(eventLoop);
  }

  private FileInputStream fileSetup() {

    final String filename = Utility.CONFIG_FILES_DIR
        + utility.getFilenameFromPrefs(R.string.pref_hardware_config_filename, Utility.NO_FILE) + Utility.FILE_EXT;

    FileInputStream fis;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e) {
      String msg = "Cannot open robot configuration file - " + filename;
      utility.complainToast(msg, context);
      DbgLog.msg(msg);
      utility.saveToPreferences(Utility.NO_FILE, R.string.pref_hardware_config_filename);
      fis = null;
    }
    utility.updateHeader(Utility.NO_FILE, R.string.pref_hardware_config_filename, R.id.active_filename, R.id.included_header);
    return fis;
  }

  private void requestRobotShutdown() {
    if (controllerService == null) return;
    controllerService.shutdownRobot();

	  Field robotField;
	  Robot robot = null;
	  try
	  {
		  robotField = FtcRobotControllerService.class.getDeclaredFields()[2];
		  robotField.setAccessible(true);
		  robot = (Robot) robotField.get(controllerService);
	  }
	  catch (IllegalAccessException e)
	  {
		  e.printStackTrace();
	  }

	  if((robot != null) && (!robot.socket.isClosed()))
	  {
		  Log.e("FtcRobotControllerActivity", "Error in service code! Socket was not shut down properly!");
	  }
  }

  private void requestRobotRestart() {
    requestRobotShutdown();
    requestRobotSetup();
  }

  protected void hittingMenuButtonBrightensScreen() {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
        @Override
        public void onMenuVisibilityChanged(boolean isVisible) {
          if (isVisible) {
            dimmer.handleDimTimer();
          }
        }
      });
    }
  }

  public void showToast(final Toast toast) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast.show();
      }
    });
  }
}
