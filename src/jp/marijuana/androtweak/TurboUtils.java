package jp.marijuana.androtweak;

import java.util.ArrayList;

import jp.marijuana.androtweak.kernel.KernelUtils;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.RemoteViews;
import android.widget.Toast;

public class TurboUtils
{
	public static void ClickTurbo(Context ctx)
	{
		DoTurboMode(ctx);
		WidgetUpdate(ctx);
	}
	
	public static void WidgetUpdate(Context ctx)
	{
		RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(), R.layout.turbo_widget);
		TurboWidgetUpdate(ctx.getApplicationContext(), remoteViews);
	}
	
	public static void TurboWidgetUpdate(Context ctx, RemoteViews rViews)
	{
		int imgid = R.drawable.turbo;
		switch ( TurboMode(ctx) ) {
		case 1:
			imgid = R.drawable.turboon;
			break;
		case 0:
			imgid = R.drawable.turbooff;
			break;
		}

		rViews.setImageViewResource(R.id.turbowidget_icon , imgid);
		ComponentName thisWidget = new ComponentName(ctx, TurboSwitchProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
		manager.updateAppWidget(thisWidget, rViews);
	}
	
	public static int TurboMode(Context ctx)
	{
		SharedPreferences pref = ctx.getSharedPreferences("turbo_mode", Context.MODE_PRIVATE);
		return pref.getInt("mode", -1);
	}
	
	public static void DoTurboMode(Context ctx)
	{
		SharedPreferences pref = ctx.getSharedPreferences("turbo_mode", Context.MODE_PRIVATE);
		int mode = pref.getInt("mode", -1);
		if (mode == -1) {
			return;
		}
		KernelUtils oc = KernelUtils.getInstance();
		Editor editor = pref.edit();
		if ( mode == 0 ) {
			editor.putInt("mode", 1);
			TurboSet(pref.getInt("tb_clock_min", oc.def_minclock),
					 pref.getInt("tb_clock_max", oc.def_maxclock),
					 pref.getString("tb_scheduler", oc.def_scheduler),
					 pref.getString("tb_governor", oc.def_governor)
			);
			Toast.makeText(ctx.getApplicationContext(), ctx.getString(R.string.tst_TurboModeE), Toast.LENGTH_SHORT ).show();
		} else {
			editor.putInt("mode", 0);
			TurboSet(pref.getInt("nr_clock_min", oc.def_minclock),
					 pref.getInt("nr_clock_max", oc.def_maxclock),
					 pref.getString("nr_scheduler", oc.def_scheduler),
					 pref.getString("nr_governor", oc.def_governor)
			);
			Toast.makeText(ctx.getApplicationContext(), ctx.getString(R.string.tst_TurboModeD), Toast.LENGTH_SHORT ).show();
		}
		editor.commit();
	}
	
	public static void TurboOff(Context ctx)
	{
		KernelUtils oc = KernelUtils.getInstance();
		SharedPreferences pref = ctx.getSharedPreferences("turbo_mode", Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt("mode", 0);
		TurboSet(pref.getInt("nr_clock_min", oc.def_minclock),
				 pref.getInt("nr_clock_max", oc.def_maxclock),
				 pref.getString("nr_scheduler", oc.def_scheduler),
				 pref.getString("nr_governor", oc.def_governor)
		);
		editor.commit();
	}
	
	private static void TurboSet(int minclock, int maxclock, String scheduler, String governor)
	{
		KernelUtils oc = KernelUtils.getInstance();
		ArrayList<String> blocks = oc.getAllBlockDevice();
		int b = blocks.size();
		String[] cmds = new String[b + 4];
		int i = 0;
		for ( i = 0; i < b; i++ ) {
			cmds[i] = "echo " + scheduler + " > " + blocks.get(i);
		}
		cmds[i++] = "echo " + String.valueOf(minclock) + " > " + oc.scaling_min_freq;
		cmds[i++] = "echo " + String.valueOf(maxclock) + " > " + oc.scaling_max_freq;
		cmds[i++] = "echo " + governor + " > " + oc.scaling_governor;
		cmds[i++] = "echo 3 > /proc/sys/vm/drop_caches";
		NativeCmd nCmd = NativeCmd.getInstance();
		nCmd.ExecuteCommands(cmds, true);
	}
}
