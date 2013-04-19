package jp.marijuana.androtweak.kernel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class OrverClockActivity extends Activity
{
	private KernelUtils oc;
	private final TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
	private TableLayout layout;
	private final String TAG = AndroTweakActivity.TAG;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		layout = new TableLayout(this);
		layout.setOrientation(TableLayout.VERTICAL);
		oc = KernelUtils.getInstance();

		makeClockTray();
		makeGovernorsTray();
		makeSchedulerTray();
		makeBottomTray();
		
		setContentView(layout);
	}
	
	private void makeBottomTray()
	{
		CheckBox chkbox = new CheckBox(this);
		chkbox.setId(R.string.id_CheckBoot);
		chkbox.setText(R.string.ChkBoot);
		chkbox.setChecked(NativeCmd.fileExists(oc.mydir + "/boot.sh"));
		
		//設定ボタン
		Button btn = new Button(this);
		btn.setText(R.string.btn_Execute);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveStat();
			}
		});
		
		TableRow row = new TableRow(this);
		row.setLayoutParams(trlp);
 		row.addView(chkbox, trlp);
 		row.addView(btn, trlp);
		layout.addView(row);

		Button btncl = new Button(this);
		btncl.setText(R.string.btn_Close);
		btncl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		LinearLayout tray = new LinearLayout(this);
		tray.setGravity(Gravity.LEFT|Gravity.BOTTOM);
		tray.addView(btncl);
		
		layout.addView(tray, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
	}
	
	private void makeClockTray()
	{
		ArrayAdapter<String> minadp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		minadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter<String> maxadp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		maxadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		int min = oc.def_minclock;
		int max = oc.def_maxclock;
		int defmin = 0;
		int defmax = 0;
		HashMap<Integer, String> clockmap = oc.clockmap;
		Object[] key = clockmap.keySet().toArray();
		Arrays.sort(key);
		for (int i = 0 ; i < key.length ; i ++ ) {
			minadp.add(clockmap.get(key[i]));
			maxadp.add(clockmap.get(key[i]));
			if ( min == Integer.parseInt(key[i].toString()) ) {
				defmin = i;
			} else if ( max == Integer.parseInt(key[i].toString()) ) {
				defmax = i;
			}
		}
		
		setTableRow(R.string.id_MinClock, R.string.MinClock, minadp, defmin);
		setTableRow(R.string.id_MaxClock, R.string.MaxClock, maxadp, defmax);
	}
	
	private void makeGovernorsTray()
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		String defsc = oc.def_governor;
		int def = 0;
		String[] sl = oc.getGovernorsList();
 		for ( int i = 0; i < sl.length; i++ ) {
 			adapter.add(sl[i]);
 			if ( defsc.equals(sl[i].trim()) ) {
 				def = i;
 			}
 		}
		
 		setTableRow(R.string.id_Governor, R.string.cGovernor, adapter, def);
	}
	
	private void makeSchedulerTray()
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		int def = 0;
		String str = oc.def_scheduler;
		String[] sl = oc.schedmap;
 		for ( int i = 0; i < sl.length; i++ ) {
 			adapter.add(sl[i]);
 			if ( str.equals(sl[i].trim()) ) {
 				def = i;
 			}
 		}
 		
 		setTableRow(R.string.id_Scheduler, R.string.cScheduler, adapter, def);
	}
	
	private void setTableRow(int id, int lbl, ArrayAdapter<String> ad, int def)
	{
		TextView text = new TextView(this);
		text.setText(lbl);
		
		Spinner spi = new Spinner(this);
		spi.setPromptId(lbl);
		spi.setId(id);
		spi.setAdapter(ad);
		spi.setSelection(def);
		
		TableRow row = new TableRow(this);
		row.setLayoutParams(trlp);
 		row.addView(text, trlp);
 		row.addView(spi, trlp);

		layout.addView(row);	 		
	}
	
	private int makeMinFreq()
	{
		Spinner spinner = (Spinner) findViewById(R.string.id_MinClock);
		String item = (String) spinner.getSelectedItem();
		int min = 192000;
		for (Entry<Integer, String> e : oc.clockmap.entrySet()){
			if( e.getValue().equals(item) ) {
				min = e.getKey();
				break;
			}
		}
		return Math.max(min, oc.getMinClock());
	}
	
	private int makeMaxFreq()
	{
		Spinner spinner = (Spinner) findViewById(R.string.id_MaxClock);
		String item = (String) spinner.getSelectedItem();
		int max = 1152000;
		for (Entry<Integer, String> e : oc.clockmap.entrySet()) {
			if( e.getValue().equals(item) ) {
				max = e.getKey();
				break;
			}
		}
		return Math.min(max, oc.getMaxClock());
	}
	
	private String makeScheduler()
	{
		Spinner spinner = (Spinner) findViewById(R.string.id_Scheduler);
		return (String) spinner.getSelectedItem();
	}
	
	private String makeGovernor()
	{
		Spinner spinner = (Spinner) findViewById(R.string.id_Governor);
	 	return (String) spinner.getSelectedItem();
	}
	
	private void saveStat()
	{
		ArrayList<String> blocks = oc.getAllBlockDevice();
		int b = blocks.size();
		int c = b + 4;
		String sc = makeScheduler();
		int i = 0;
		String[] cmds = new String[c];
		for ( i = 0; i < b; i++ ) {
			cmds[i] = "echo " + sc + " > " + blocks.get(i);
		}

		cmds[i++] = "echo " + String.valueOf(makeMinFreq()) + " > " + oc.scaling_min_freq;
		cmds[i++] = "echo " + String.valueOf(makeMaxFreq()) + " > " + oc.scaling_max_freq;
		cmds[i++] = "echo " + makeGovernor() + " > " + oc.scaling_governor;
		cmds[i++] = "echo 3 > /proc/sys/vm/drop_caches";
		
		String[] ret = NativeCmd.ExecCommands(cmds, true);
		if (ret[2].trim().replace("\n", "").length() == 0) {
			Log.i(TAG, "set cpu freq and scaling");
		} else {
			Log.e(TAG, ret[2]);
		}
		
		CheckBox chkbox = (CheckBox) findViewById(R.string.id_CheckBoot);
		if (chkbox.isChecked()) {
			NativeCmd.createExecFile(cmds, oc.mydir + "/boot.sh");
			Log.i(TAG, "make: boot.sh");
		} else {
			File file = new File(oc.mydir + "/boot.sh");
			if (file.exists()) {
				file.delete();
				Log.i(TAG, "delete: boot.sh");
			}
		}
		
		oc.flg_update = true;
		oc.reload();
		kernelLayout.my.ShowWait();
		Toast.makeText(this, R.string.SettingEnd, Toast.LENGTH_SHORT).show();
	}
}
