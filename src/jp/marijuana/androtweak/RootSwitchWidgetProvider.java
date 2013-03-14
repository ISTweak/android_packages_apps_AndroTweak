/**
 * su有効/無効切り替えウィジット
 * http://mobilehackerz.jp/contents/Software/Android/IS01root のソースから頂きました
 */

package jp.marijuana.androtweak;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class RootSwitchWidgetProvider extends AppWidgetProvider
{
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Intent intent = new Intent(context, RootSwitchService.class);
		context.startService(intent);
	}
}
