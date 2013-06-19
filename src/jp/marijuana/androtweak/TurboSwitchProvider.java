package jp.marijuana.androtweak;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class TurboSwitchProvider extends AppWidgetProvider
{
	@Override
	public void onEnabled(Context context) {
		Log.d("AndroTweak", "onEnabled");
		super.onEnabled(context);
		TurboUtils.TurboOff(context);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.d("AndroTweak", "onUpdate");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Intent intent = new Intent(context, TurboSwitchService.class);
		context.startService(intent);
	}
	
	public static class TurboSwitchService extends Service
	{
		final public String MSG_CHANGE = "jp.marijuana.androtweak.TURBO";
		
		@Override
		public void onStart(Intent intent, int startId)
		{
			super.onStart(intent, startId);

			Intent buttonIntent = new Intent();
			buttonIntent.setAction(MSG_CHANGE);
			
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, buttonIntent, 0);
			RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.turbo_widget);
			remoteViews.setOnClickPendingIntent(R.id.turbowidget_icon, pendingIntent);
			
			String act = intent.getAction().toString();
			if (MSG_CHANGE.equals(act)) {
				TurboUtils.DoTurboMode(this);
			}

			TurboUtils.TurboWidgetUpdate(this, remoteViews);
		}

		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}
	}
}
