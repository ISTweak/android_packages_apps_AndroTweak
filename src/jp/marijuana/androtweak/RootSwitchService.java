/**
 * su有効/無効切り替えサービス
 * http://mobilehackerz.jp/contents/Software/Android/IS01root のソースから頂きました
 */
package jp.marijuana.androtweak;

import jp.marijuana.androtweak.utils.utilsLayout;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.PendingIntent;
import android.widget.RemoteViews;

public class RootSwitchService extends Service
{
	final public String MSG_CHANGE = "jp.marijuana.androtweak.CHANGE";

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);

		Intent buttonIntent = new Intent();
		buttonIntent.setAction(MSG_CHANGE);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, buttonIntent, 0);
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget);
		remoteViews.setOnClickPendingIntent(R.id.ImageView01, pendingIntent);

		if (MSG_CHANGE.equals(intent.getAction())) {
			if (NativeCmd.fileExists("/sbin/su") == false) {
				utilsLayout.DoEnableSu(this);
			} else if (NativeCmd.fileExists("/sbin/su")) {
				utilsLayout.DoDisableSu(this);
			} else {
				return;
			}
		}

		utilsLayout.WidgetUpdate(this, remoteViews);
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

}
