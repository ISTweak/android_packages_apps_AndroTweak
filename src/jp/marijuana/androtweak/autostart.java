/**
 * 起動時の自動実行
 */
package jp.marijuana.androtweak;

import java.io.File;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class autostart extends BroadcastReceiver
{
	private String au;
	@Override
	public void onReceive(Context context, Intent intent)
	{
		onCreate(context);
	}

	public void onCreate(Context ctx)
	{
		if ( checkSuCmd() ) {
			if ( fileExists("/data/root/autostart.sh") ) {
				ExecuteCommand("/data/root/autostart.sh");
				Log.i("AndroTweak", "run autostart.sh");
			}
			
			final String dir = ctx.getDir("bin", 0).getAbsolutePath();
			if ( fileExists(dir + "/boot.sh") ) {
				ExecuteCommand(dir + "/boot.sh");
				Log.i("AndroTweak", "run boot.sh");
			}
			
			if ( fileExists(dir + "/vdd.sh") ) {
				ExecuteCommand(dir + "/vdd.sh");
				Log.i("AndroTweak", "run vdd.sh");
			}
			
			if ( fileExists(dir + "/swappiness.sh") ) {
				ExecuteCommand(dir + "/swappiness.sh");
				Log.i("AndroTweak", "run swappiness.sh");
			}
		} else {
			Log.d("AndroTweak", "su command is not found!");
		}
	}
	
	private boolean fileExists(String fn)
	{
		return new File(fn).exists();
	}
	
	private boolean checkSuCmd()
	{
		if (fileExists("/sbin/au")) {
			au = "/sbin/au";
		} else if (fileExists("/system/xbin/su")) {
			au = "/system/xbin/su";
		} else if (fileExists("/system/bin/su")) {
			au = "/system/bin/su";
		} else if (fileExists("/sbin/su")) {
			au = "/sbin/su";
		} else {
			return false;
		}
		
		Log.i("AndroTweak", au + " command found");
		return true;
	}
	
	public void ExecuteCommand(String cmd)
	{
		try {
			Runtime.getRuntime().exec(au + " -c " + cmd);
		} catch (IOException e) {
			Log.e("AndroTweak", e.toString());
		}
	}
}
