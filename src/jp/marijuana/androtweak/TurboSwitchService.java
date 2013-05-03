package jp.marijuana.androtweak;

import jp.marijuana.androtweak.utils.utilsLayout;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class TurboSwitchService extends Service
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

		if (MSG_CHANGE.equals(intent.getAction())) {
			switch ( utilsLayout.TurboMode(this) ) {
			case 1:
				Log.d(AndroTweakActivity.TAG, "Turbo ON");
				utilsLayout.DoTurboMode(this, 0);
				break;
			default:
				Log.d(AndroTweakActivity.TAG, "Turbo OFF");
				utilsLayout.DoTurboMode(this, 1);
				break;
			}
		}

		utilsLayout.TurboWidgetUpdate(this, remoteViews);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
