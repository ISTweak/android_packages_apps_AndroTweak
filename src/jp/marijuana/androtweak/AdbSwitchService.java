package jp.marijuana.androtweak;

import jp.marijuana.androtweak.utils.utilsLayout;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class AdbSwitchService  extends Service
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

		if (MSG_CHANGE.equals(intent.getAction())) {
			switch ( utilsLayout.adbType(this) ) {
			case 1:
				utilsLayout.DoAdbChange(this, true);
				break;
			case 2:
				utilsLayout.DoAdbChange(this, false);
				break;
			}
		}
		
	    try {
	    	Thread.sleep(1000);
	    } catch (InterruptedException e) {}
		
		utilsLayout.AdbWidgetUpdate(this, remoteViews);
	}
	
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
