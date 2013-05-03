package jp.marijuana.androtweak.utils;

import jp.marijuana.androtweak.AdbUtils;
import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;
import jp.marijuana.androtweak.RootUtils;
import jp.marijuana.androtweak.TurboUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
	private static NativeCmd nCmd = NativeCmd.getInstance();
	
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
		String Model = nCmd.getProperties("ro.product.model");
		if ( Model.equals("IS06") ) {
			TetheringCmd = "/system/bin/am start -n com.pantech.app.wifitest/.TetheringActivation";
		} else if ( Model.equals("IS11PT") ) {
			TetheringCmd = "/system/bin/am start -n com.android.settings/.wifi.TetheringActivation";
		} else if ( Model.equals("IS11N") ) {
			TetheringCmd = "/system/bin/am start -n com.android.settings/.TetherSettings";
		}
		AndroTweakActivity.Model = Model;
		
		String[] p = new String[5];
		p[0] = nCmd.getProperties("ro.product.brand");
		p[1] = nCmd.getProperties("ro.product.manufacturer");
		p[2] = nCmd.getProperties("ro.build.version.release");
		p[3] = nCmd.getProperties("gsm.version.baseband");

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
		if ( nCmd.ChangeAu ) {
			layout.addView(makeSuBtn());
		}
		layout.addView(Reboot.getButton(ctx));
		layout.addView(CacheClear.getButton(ctx));
		if ( TetheringCmd.length() > 0 ) {
			layout.addView(makeTethering());
		}
		layout.addView(makeAdbBtn());
		layout.addView(makeTurboBtn());
	}
	
	private Button makeSuBtn()
	{
		Button btnsu = new Button(ctx);
		if (nCmd.fileExists("/sbin/su")) {
			btnsu.setText(R.string.btn_DelSu);
		} else {
			btnsu.setText(R.string.btn_PutSu);
		}
		
		btnsu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RootUtils.ClickSu(ctx.getApplicationContext());
				ShowWait();
			}
		});
		return btnsu;
	}
	
	private Button makeAdbBtn()
	{
		Button btnadb = new Button(ctx);
		
		switch ( AdbUtils.adbType(ctx) ) {
		case 1:
			btnadb.setText(R.string.btn_AdbPortOn);
			break;
		case 2:
			btnadb.setText(R.string.btn_AdbPortOff);
			break;
		default:
			btnadb.setText(R.string.btn_AdbPortOn);
			btnadb.setEnabled(false);
		}
		
		btnadb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AdbUtils.ClickAdb(ctx);
				ShowWait();
			}
		});
		return btnadb;
	}
	
	private Button makeTurboBtn()
	{
		Button btntb = new Button(ctx);
		
		switch ( TurboUtils.TurboMode(ctx) ) {
		case 1:
			btntb.setText(R.string.btn_TurboOff);
			break;
		default:
			btntb.setText(R.string.btn_TurboOn);
		}
		
		btntb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TurboUtils.ClickTurbo(ctx);
				ShowWait();
			}
		});
		return btntb;
	}
	
	private Button makeTethering()
	{
		Button btnr = new Button(ctx);
		btnr.setText(R.string.btn_Tethering);
		btnr.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nCmd.ExecuteCommand(TetheringCmd, true);
			}
		});
		return btnr;
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
