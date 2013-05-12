/**
 * 再起動
 */
package jp.marijuana.androtweak.utils;

import java.io.File;
import java.util.ArrayList;

import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.R;
import jp.marijuana.androtweak.NativeCmd;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Reboot
{
	private Context ctx;
	private AlertDialog mDlg = null;
	private final NativeCmd nCmd = NativeCmd.getInstance();
	private int[] types = {0, 0, 0};
	
	public static Button getButton(Context c)
	{
		Reboot ins = new Reboot(c);
		return ins.makeButton();
	}
	
	private Reboot(Context c)
	{
		ctx = c;
	}
	
	private Button makeButton()
	{
		Button btn = new Button(ctx);
		btn.setText(R.string.btn_Reboot);
		btn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showRebootList();
			}
		});
		return btn;
	}
	
	private void showRebootList()
	{
		int i = 0;
		final ArrayList<String> rows = new ArrayList<String>();
		rows.add(ctx.getString(R.string.RebootNormal));
		types[i++] = 1;
		if (!AndroTweakActivity.Model.equals("IS12S")) {
			rows.add(ctx.getString(R.string.RebootRecovery));
			types[i++] = 2;
		}
		if (nCmd.getProperties("androtweak.cwm").equals("1")) {
			rows.add(ctx.getString(R.string.RebootCWM));
			types[i++] = 3;
		}
		
		ListView lv = new ListView(ctx);
		lv.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1  , rows));
		lv.setScrollingCacheEnabled(false);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> items, View view, int position, long id)
			{
				mDlg.dismiss();
				switch (types[position]) {
					case 1: DoReboot(); break;
					case 2: DoRebootRec(); break;
					case 3: DoRebootCwm(); break;
				}
			}
		});

		mDlg = new AlertDialog.Builder(ctx)
			.setTitle(ctx.getString(R.string.RebootMode))
			.setPositiveButton(R.string.btn_Cancel, null)
			.setView(lv)
			.create();
		mDlg.show();
	}
	
	public void DoReboot()
	{
		ExecuteReboot("");
	}
	
	public void DoRebootRec()
	{
		ExecuteReboot("recovery");
	}
	
	public void DoRebootCwm()
	{
		nCmd.ExecuteCommand("touch /cache/recovery/boot", true);
		ExecuteReboot("");
	}
	
	private void ExecuteReboot(String arg)
	{
		File ff = new File(ctx.getDir("bin", 0), "reboot");
		nCmd.ExecuteCommand(ff.getAbsolutePath() + " " + arg, true);
	}
}
