package jp.marijuana.androtweak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;

public class ChangeWifi extends BroadcastReceiver
{
	@Override
	public void onReceive(Context ctx, Intent intent)
	{
		String action = intent.getAction();
		if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(), R.layout.adb_widget);
			AdbUtils.AdbWidgetUpdate(ctx, remoteViews);
		}
	}
}
