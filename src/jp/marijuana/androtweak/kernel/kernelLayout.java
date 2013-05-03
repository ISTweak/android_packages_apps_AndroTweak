package jp.marijuana.androtweak.kernel;

import jp.marijuana.androtweak.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class kernelLayout implements Runnable
{
	private static Context ctx;
	public static kernelLayout my;
	private final KernelUtils oc = KernelUtils.getInstance();
	
	private static LinearLayout layout;
	private TableLayout tb;
	private final TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
	
	private static ProgressDialog waitDialog;
	private Thread thread;
	
	public kernelLayout(Context ctx)
	{
		kernelLayout.ctx = ctx;
		kernelLayout.my = this;
		oc.mydir = ctx.getDir("bin", 0).getAbsolutePath();
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
		tb = new TableLayout(ctx);
		tb.setOrientation(TableLayout.VERTICAL);
		
		setTableRow(R.string.clockrate, oc.getClock());
		setTableRow(R.string.cGovernor, oc.getGovernor());
		setTableRow(R.string.cScheduler, oc.getScheduler());
		setTableRow(R.string.cZramsize, oc.getZramsize());
		setTableRow(R.string.cSwappiness, oc.getSwappiness());
		
		layout.addView(tb);
	}
	
	public void makeView()
	{
		if (oc.is_clock) {
			layout.addView(makeOverClock());
		}
		
		if ( oc.is_zram ) {
			layout.addView(makeZram());
		}
		
		if (oc.is_vdd) {
			layout.addView(makeVdd());
		}
		
		if (oc.is_clock) {
			layout.addView(makeTurboMode());
		}
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

		layout.addView(row);	 		
	}
	
	private Button makeOverClock()
	{
		Button btnoc = new Button(ctx);
		btnoc.setText(R.string.btn_OverClock);
		btnoc.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.androtweak", "jp.marijuana.androtweak.kernel.OrverClockActivity");
				ctx.startActivity(intent);
			}
		});
		return btnoc;
	}
	
	private Button makeTurboMode()
	{
		Button btnoc = new Button(ctx);
		btnoc.setText(R.string.btn_TurboMode);
		btnoc.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.androtweak", "jp.marijuana.androtweak.kernel.TurboActivity");
				ctx.startActivity(intent);
			}
		});
		return btnoc;
	}

	private Button makeZram()
	{
		Button btnz = new Button(ctx);
		btnz.setText(R.string.btn_zram);
		btnz.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.androtweak", "jp.marijuana.androtweak.kernel.ZramActivity");
				ctx.startActivity(intent);
			}
		});
		return btnz;
	}

	private Button makeVdd()
	{
		Button btnz = new Button(ctx);
		btnz.setText(R.string.btn_vdd);
		btnz.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.androtweak", "jp.marijuana.androtweak.kernel.VddActivity");
				ctx.startActivity(intent);
			}
		});
		return btnz;
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
