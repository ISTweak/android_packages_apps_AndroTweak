package jp.marijuana.androtweak;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class RootUtils
{
	private static NativeCmd nCmd = NativeCmd.getInstance();
	private static final String TAG = AndroTweakActivity.TAG;
	
	public static void ClickSu(Context ctx)
	{
		AuSu(ctx);
		RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(), R.layout.root_widget);
		WidgetUpdate(ctx.getApplicationContext(), remoteViews);
	}
	
	public static void WidgetUpdate(Context ctx, RemoteViews rViews)
	{
		int imgid;
		if (nCmd.fileExists("/sbin/su")) {
			imgid = R.drawable.widget_on;
			NUpdate(ctx, true);
		} else if (nCmd.fileExists("/sbin/pu")) {
			imgid = R.drawable.widget_off;
			NUpdate(ctx, false);
		} else {
			imgid = R.drawable.widget_blank;
		}
		rViews.setImageViewResource(R.id.rootwidget_icon, imgid);
		ComponentName thisWidget = new ComponentName(ctx, RootSwitchWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
		manager.updateAppWidget(thisWidget, rViews);
	}
	
	public static void NUpdate(Context ctx, Boolean cl)
	{
		NotificationManager manager = (NotificationManager)(ctx.getSystemService(Context.NOTIFICATION_SERVICE));
		Resources res = ctx.getResources();
		
		if ( cl ) {
			Notification notification = new Notification(R.drawable.notification, res.getString(R.string.EnableSu), System.currentTimeMillis());
			Intent intent = new Intent(ctx, AndroTweakActivity.class);
			PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, 0);
			notification.setLatestEventInfo(ctx, res.getString(R.string.EnableSuSubject), res.getString(R.string.EnableSuDesc), pendingIntent);
			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			manager.notify(R.string.app_name, notification);
		} else {
			manager.cancel(R.string.app_name);
		}
	}
	
	public static void AuSu(Context ctx)
	{
		if (nCmd.fileExists("/sbin/pu")) {
			if (nCmd.fileExists("/sbin/su")) {
				DoDisableSu(ctx);
			} else if (!nCmd.fileExists("/sbin/su")) {
				DoEnableSu(ctx);
			}
		}
	}
	
	public static void DoEnableSu(Context ctx)
	{
		nCmd.ExecuteCommand("ln -s /sbin/au /sbin/su", true);
		Toast.makeText(ctx.getApplicationContext(), ctx.getString(R.string.EnableSu), Toast.LENGTH_SHORT ).show();
		Log.i(TAG, "Enable su");
	}
	
	public static void DoDisableSu(Context ctx)
	{
		nCmd.ExecuteCommand("rm /sbin/su", true);
		Toast.makeText(ctx.getApplicationContext(), ctx.getString(R.string.DisableSu), Toast.LENGTH_SHORT ).show();
		Log.i(TAG, "Disable su");
	}
}
