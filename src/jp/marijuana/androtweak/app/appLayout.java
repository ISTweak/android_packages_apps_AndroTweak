package jp.marijuana.androtweak.app;

import java.text.DecimalFormat;

import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class appLayout implements Runnable
{
	private static Context ctx;
	private static appLayout my;
	private static final String TAG = AndroTweakActivity.TAG;
	private static LinearLayout layout;
	private TableLayout tb;
	private final TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
	private NativeCmd nCmd = NativeCmd.getInstance();
	
	private static ProgressDialog waitDialog;
	private Thread thread;

	public appLayout(Context ctx)
	{
		appLayout.ctx = ctx;
		appLayout.my = this;
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
		String data = Environment.getDataDirectory().getAbsolutePath();
		StatFs sf = new StatFs(data);
		long bs = sf.getBlockSize();
		long total = sf.getBlockCount();
		long avail = sf.getAvailableBlocks();
		long all = bs * total;
		long emp = bs * avail;
		double dall = all / 1024d / 1024d;
		double demp = emp / 1024d / 1024d;
		double duse = dall - demp;
		DecimalFormat df = new DecimalFormat("0.00");
		double dataapp = DirectorySize(data + "/app") / 1024d;
		double datadata = DirectorySize(data + "/data") / 1024d;
		
		tb = new TableLayout(ctx);
		tb.setOrientation(TableLayout.VERTICAL);
		
		setTableRow(R.string.lbl_datasize, df.format(dall) + "MB");
		setTableRow(R.string.lbl_datause, df.format(duse) + "MB");
		setTableRow(R.string.lbl_dataempty, df.format(demp) + "MB");
		setTableRow(R.string.lbl_dataapp, df.format(dataapp) + "MB");
		setTableRow(R.string.lbl_datadata, df.format(datadata) + "MB");
		
		Log.i(TAG, "Get data size");
 		layout.addView(tb);
	}
	
	private long DirectorySize(String dir)
	{
		long size = 0;
		String[] ret = nCmd.ExecCommand(nCmd.cmdDu + " -s -k " + dir + "|" + nCmd.cmdGrep + " -o [0-9]*", true);
		String str = ret[1].replace("\n", "").trim();
		if (str.length() > 0) {
			size = Long.parseLong(str);
		}
		return size;
	}

	private void setTableRow(int lbl, String str)
	{
		TextView label = new TextView(ctx);
		label.setText(lbl);
		label.setPadding(5, 3, 20, 3);
		
		TextView text = new TextView(ctx);
		text.setText(str);
		text.setGravity(Gravity.RIGHT);
		text.setPadding(5, 3, 5, 3);

		TableRow row = new TableRow(ctx);
		row.setLayoutParams(trlp);
 		row.addView(label, trlp);
 		row.addView(text, trlp);

		tb.addView(row);	 		
	}
	
	private void makeView()
	{
		layout.addView(btnAppList.getButton(ctx));
		layout.addView(Market.getButton(ctx, this));
		if (nCmd.fileExists("/ldb/")) {
			layout.addView(LifeLog.getButton(ctx, my));
		}
	}

	@Override
	public void run()
	{
		try {
			Thread.sleep(800);
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
			if ( waitDialog != null ) {
				waitDialog.dismiss();
				waitDialog = null;
			}
		}
	};
}
