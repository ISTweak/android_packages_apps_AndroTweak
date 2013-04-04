package jp.marijuana.androtweak.utils;

import jp.marijuana.androtweak.AdbSwitchWidgetProvider;
import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;
import jp.marijuana.androtweak.RootSwitchWidgetProvider;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class utilsLayout implements Runnable
{
	private static Context ctx;
	private static utilsLayout my;
	
	private static LinearLayout layout;
	private TableLayout tb;
	private final TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
	
	private static ProgressDialog waitDialog;
	private Thread thread;
	
	private static final String TAG = AndroTweakActivity.TAG;
	public static String TetheringCmd = "";
	public static Boolean ChangeAu = true;

	public utilsLayout(Context ctx)
	{
		utilsLayout.ctx = ctx;
		utilsLayout.my = this;
	}
	
	public LinearLayout makeLayout()
	{
		layout = new LinearLayout(ctx);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		makeInfo();
		makeView();
		
		return layout;
	}
	
	private void makeInfo()
	{
		String Model = NativeCmd.getProperties("ro.product.model");
		if ( Model.equals("IS06") ) {
			TetheringCmd = "/system/bin/am start -n com.pantech.app.wifitest/.TetheringActivation";
		} else if ( Model.equals("IS11PT") ) {
			TetheringCmd = "/system/bin/am start -n com.android.settings/.wifi.TetheringActivation";
		} else if ( Model.equals("IS11N") ) {
			TetheringCmd = "/system/bin/am start -n com.android.settings/.TetherSettings";
		}
		AndroTweakActivity.Model = Model;
		
		String[] p = new String[5];
		p[0] = NativeCmd.getProperties("ro.product.brand");
		p[1] = NativeCmd.getProperties("ro.product.manufacturer");
		p[2] = NativeCmd.getProperties("ro.build.version.release");
		p[3] = NativeCmd.getProperties("gsm.version.baseband");

		tb = new TableLayout(ctx);
		tb.setOrientation(TableLayout.VERTICAL);
		
		setTableRow(R.string.lbl_Carrier, p[0]);
		setTableRow(R.string.lbl_Maker, p[1]);
		setTableRow(R.string.lbl_Model, Model);
		setTableRow(R.string.lbl_AndroidVer, p[2]);
		setTableRow(R.string.lbl_BaseVer, p[3]);
		setTableRow(R.string.lbl_IpAddress, getIpAddress());

 		layout.addView(tb);
	}
	
	private String getIpAddress()
	{
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		String ipString = (ip & 0xff) + "." + (ip >> 8 & 0xff) + "." + (ip >> 16 & 0xff) + "." + (ip >> 24 & 0xff);
		Log.i(TAG, ipString);
		return ipString;
	}
	
	
	private void setTableRow(int lbl, String str)
	{
		TextView label = new TextView(ctx);
		label.setText(lbl);
		label.setPadding(5, 3, 20, 3);
		
		TextView text = new TextView(ctx);
		text.setText(str);
		text.setPadding(5, 3, 5, 3);

		TableRow row = new TableRow(ctx);
		row.setLayoutParams(trlp);
 		row.addView(label, trlp);
 		row.addView(text, trlp);

		tb.addView(row);	 		
	}
	
	private void makeView()
	{
		if ( !ChangeAu ) {
			layout.addView(makeSuBtn());
		}
		layout.addView(Reboot.getButton(ctx));
		layout.addView(CacheClear.getButton(ctx));
		if ( TetheringCmd.length() > 0 ) {
			layout.addView(makeTethering());
		}
		layout.addView(makeAdbBtn());
	}
	
	private Button makeSuBtn()
	{
		Button btnsu = new Button(ctx);
		if (NativeCmd.fileExists("/sbin/su")) {
			btnsu.setText(R.string.btn_DelSu);
			btnsu.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DoDisableSu(ctx.getApplicationContext());
					UpdateWidget();
					ShowWait();
				}
			});
		} else {
			btnsu.setText(R.string.btn_PutSu);
			btnsu.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DoEnableSu(ctx.getApplicationContext());
					UpdateWidget();
					ShowWait();
				}
			});
		}
		return btnsu;
	}
	public static void DoAdbChange(Context ctx, Boolean wifi)
	{
		String[] cmds = new String[3];
		if (wifi) {
			cmds[0] = "setprop service.adb.tcp.port 5555";
		} else {
			cmds[0] = "setprop service.adb.tcp.port ''";
		}
		cmds[1] = "stop adbd";
		cmds[2] = "start adbd";
		NativeCmd.ExecuteCommands(cmds, true);
	}
	
	private Button makeAdbBtn()
	{
		Button btnadb = new Button(ctx);
		
		switch ( adbType(ctx) ) {
		case 1:
			btnadb.setText(R.string.btn_AdbPortOn);
			btnadb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DoAdbChange(ctx, true);
					AdbUpdateWidget();
					ShowWait();
				}
			});
			break;
		case 2:
			btnadb.setText(R.string.btn_AdbPortOff);
			btnadb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DoAdbChange(ctx, false);
					AdbUpdateWidget();
					ShowWait();
				}
			});
			break;
		default:
			btnadb.setText(R.string.btn_AdbPortOn);
			btnadb.setEnabled(false);
		}

		return btnadb;
	}
	
	private Button makeTethering()
	{
		Button btnr = new Button(ctx);
		btnr.setText(R.string.btn_Tethering);
		btnr.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				NativeCmd.ExecuteCommand(TetheringCmd, true);
			}
		});
		return btnr;
	}
	
	public static void DoEnableSu(Context ctx)
	{
		if (NativeCmd.ExecuteCmdAlert(ctx, "ln -s /sbin/au /sbin/su", true)) {
			Toast.makeText(ctx.getApplicationContext(), ctx.getString(R.string.EnableSu), Toast.LENGTH_SHORT ).show();
			Log.i(TAG, "Enable su");
		}
	}
	
	public static void DoDisableSu(Context ctx)
	{
		if (NativeCmd.ExecuteCmdAlert(ctx, "rm /sbin/su", true)) {
			Toast.makeText(ctx.getApplicationContext(), ctx.getString(R.string.DisableSu), Toast.LENGTH_SHORT ).show();
			Log.i(TAG, "Disable su");
		}
	}
	
	private void UpdateWidget()
	{
		RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(), R.layout.root_widget);
		WidgetUpdate(ctx.getApplicationContext(), remoteViews);
	}
	
	public static void WidgetUpdate(Context ctx, RemoteViews rViews)
	{
		int imgid;
		if (NativeCmd.fileExists("/sbin/su")) {
			imgid = R.drawable.widget_on;
			NUpdate(ctx, true);
		} else if (NativeCmd.fileExists("/sbin/pu")) {
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
	
	private void AdbUpdateWidget()
	{
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
		String port = NativeCmd.getProperties("service.adb.tcp.port");
		
		if (wifiState == WifiManager.WIFI_STATE_ENABLED && port.length() == 0) {
			return 1;
		} else if( port.length() == 0 ) {
			return 0;
		}
		return 2;
	}
	
	@Override
	public void run()
	{
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		mhandler.sendEmptyMessage(0);
	}
	
	public void ShowWait()
	{
		waitDialog = new ProgressDialog(ctx);
		waitDialog.setMessage(ctx.getString(R.string.msg_Loading));
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		waitDialog.show();
		thread = new Thread(this);
		thread.start();
	}

	private static Handler mhandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			layout.removeAllViews();
			my.makeInfo();
			my.makeView();
			waitDialog.dismiss();
			waitDialog = null;
		}
	};
}
