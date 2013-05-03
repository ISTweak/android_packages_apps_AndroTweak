package jp.marijuana.androtweak;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.RemoteViews;

public class AdbUtils
{
	private static final String TAG = AndroTweakActivity.TAG;

	public static void ClickAdb(Context ctx)
	{
		DoAdbChange(ctx);
		RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(), R.layout.adb_widget);
		AdbWidgetUpdate(ctx.getApplicationContext(), remoteViews);
	}
	
	public static void AdbWidgetUpdate(Context ctx, RemoteViews rViews)
	{
		int imgid;
		switch ( adbType(ctx) ) {
		case 1:
			Log.d(TAG, "USB");
			imgid = R.drawable.adbusb;
			break;
		case 2:
			Log.d(TAG, "Wifi");
			imgid = R.drawable.adbwifi;
			break;
		default:
			imgid = R.drawable.adboff;
		}

		rViews.setImageViewResource(R.id.adbwidget_icon, imgid);
		ComponentName thisWidget = new ComponentName(ctx, AdbSwitchWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
		manager.updateAppWidget(thisWidget, rViews);
	}
	
	public static int adbType(Context ctx)
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		int wifiState = wifiManager.getWifiState();
		NativeCmd nCmd = NativeCmd.getInstance();
		String port = nCmd.getProperties("service.adb.tcp.port");
		
		if (wifiState == WifiManager.WIFI_STATE_ENABLED && port.length() == 0) {
			return 1;
		} else if( port.length() == 0 ) {
			return 0;
		}
		return 2;
	}
	
	public static void DoAdbChange(Context ctx)
	{
		Boolean wifi = false;
		switch ( adbType(ctx) ) {
		case 1:
			wifi = true;
			break;
		case 2:
			wifi = false;
			break;
		default:
			return;
		}
		
		String[] cmds = new String[3];
		if (wifi) {
			cmds[0] = "setprop service.adb.tcp.port 5555";
		} else {
			cmds[0] = "setprop service.adb.tcp.port ''";
		}
		cmds[1] = "stop adbd";
		cmds[2] = "start adbd";
		NativeCmd nCmd = NativeCmd.getInstance();
		nCmd.ExecuteCommands(cmds, true);
	}
}
