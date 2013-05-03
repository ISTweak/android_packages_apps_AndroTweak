/**
 * マーケット偽装
 */
package jp.marijuana.androtweak.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Market
{
	private Context ctx;
	private appLayout aly;
	
	private final String db = "/data/data/com.google.android.gsf/databases/gservices.db";
	private Button btn;
	private final String TAG = AndroTweakActivity.TAG;
	private NativeCmd nCmd = NativeCmd.getInstance();
	
	public static Button getButton(Context c, appLayout a)
	{
		Market ins = new Market(c, a);
		return ins.makeButton();
	}
	
	private Market(Context c, appLayout a)
	{
		ctx = c;
		aly = a;
	}
	
	private Button makeButton()
	{
		btn = new Button(ctx);
		DebugCheck();
		return btn;
	}
	
	private void DebugCheck()
	{
		String cmd = "sqlite3 " + db + " \"select value from main where name = 'finsky.debug_options_enabled';\"";
		String[] line = nCmd.ExecCommand(cmd, true);
		String ret = line[1].trim().replace("\n", "");
		if (ret.length() > 0 ) {
			btn.setText(R.string.btn_MarketRe);
			btn.setOnClickListener(mEnable());
		} else {
			btn.setText(R.string.btn_Market);
			btn.setOnClickListener(mDisable());
		}
	}
	
	private OnClickListener mDisable()
	{
		return new OnClickListener(){
			@Override
			public void onClick(View v) {
				goCamouflage();
				aly.ShowWait();
			}
		};
	}
	
	private OnClickListener mEnable()
	{
		return new OnClickListener(){
			@Override
			public void onClick(View v) {
				reCamouflage();
				aly.ShowWait();
			}
		};
	}
	
	private void goCamouflage()
	{
		String[] cmds = new String[2];
		cmds[0] = "sqlite3 " + db + " \"insert into main (name,value) values('finsky.debug_options_enabled', 'true');\"";
		cmds[1] = nCmd.cmdPkill + " -9 com.android.vending";
		nCmd.ExecuteCommands(cmds, true);

		MakeCarrierList();
	}
	
	private void reCamouflage()
	{
		String[] cmds = new String[2];
		cmds[0] = "sqlite3 " + db + " \"delete from main where name = 'finsky.debug_options_enabled';\"";
		cmds[1] = nCmd.cmdPkill + " -9 com.android.vending";
		nCmd.ExecuteCommands(cmds, true);
	}

	private void MakeCarrierList()
	{
		String sdcard = Environment.getExternalStorageDirectory().getPath();
		File dr = new File(sdcard + "/download");
		if ( !dr.exists() ) {
			dr.mkdir();
		}
		String fn = sdcard + "/download/carriers.csv";
		if ( !nCmd.fileExists(fn) ) {
			try {
				final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fn));
				out.write("DOCOMO,jp,44010\n");
				out.write("KDDI,jp,44054\n");
				out.write("SOFTBANK,jp,44020\n");
				out.write("EMOBILE,jp,44000\n");
				out.write("T-Mobile,us,310260\n");
				out.flush();
				out.close();
				Runtime.getRuntime().exec("chmod 0666 " + fn).waitFor();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}
}
