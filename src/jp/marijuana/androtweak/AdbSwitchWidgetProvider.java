package jp.marijuana.androtweak;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class AdbSwitchWidgetProvider extends AppWidgetProvider
{
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Intent intent = new Intent(context, AdbSwitchService.class);
		context.startService(intent);
	}
	
	public static class AdbSwitchService extends Service
	{
		final public String MSG_CHANGE = "jp.marijuana.androtweak.ADBWIFI";
		
		@Override
		public void onStart(Intent intent, int startId)
		{
			super.onStart(intent, startId);

			Intent buttonIntent = new Intent();
			buttonIntent.setAction(MSG_CHANGE);
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, buttonIntent, 0);
			RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.adb_widget);
			remoteViews.setOnClickPendingIntent(R.id.adbwidget_icon, pendingIntent);

			String act = intent.getAction();
			if (MSG_CHANGE.equals(act)) {
				AdbUtils.DoAdbChange(this);
			}
			
		    try {
		    	Thread.sleep(1000);
		    } catch (InterruptedException e) {}
			
		    AdbUtils.AdbWidgetUpdate(this, remoteViews);
		}
		
		@Override
		public IBinder onBind(Intent arg0) {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}
	}
}
