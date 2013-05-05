package jp.marijuana.androtweak.kernel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import jp.marijuana.androtweak.R;
import jp.marijuana.androtweak.TurboUtils;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class TurboActivity  extends Activity
{
	//private final String TAG = AndroTweakActivity.TAG;
	private KernelUtils oc;
	private final TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
	private TableLayout layout;
	private SharedPreferences pref;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		layout = new TableLayout(this);
		layout.setOrientation(TableLayout.VERTICAL);
		oc = KernelUtils.getInstance();

		pref = this.getSharedPreferences("turbo_mode", Context.MODE_PRIVATE);
		
		setLabelRow(R.string.lbl_TurboMode);

		makeClockTray();
		makeGovernorsTray();
		makeSchedulerTray();
		makeBottomTray();
		
		setContentView(layout);
	}
	
	private void setLabelRow(int lbl)
	{
		TextView text = new TextView(this);
		text.setTextColor(Color.WHITE);
		text.setGravity(Gravity.CENTER);
		text.setText(lbl);

		layout.addView(text);	 		
	}
	
	private void makeBottomTray()
	{
		//設定ボタン
		Button btn = new Button(this);
		btn.setText(R.string.btn_Execute);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveStat();
			}
		});
		LinearLayout ll = new LinearLayout(this);
		ll.setGravity(Gravity.RIGHT);
		ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		ll.addView(btn);
		layout.addView(ll);

		//閉じるボタン
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

		int min = pref.getInt("tb_clock_min", oc.def_minclock);
		int max = pref.getInt("tb_clock_max", oc.def_maxclock);
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
		String defsc = pref.getString("tb_governor", oc.def_governor);
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
		String str = pref.getString("tb_scheduler", oc.def_scheduler);
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
		Editor editor = pref.edit();
		editor.clear();
		editor.putInt("tb_clock_min", makeMinFreq());
		editor.putInt("tb_clock_max", makeMaxFreq());
		editor.putString("tb_scheduler", makeScheduler());
		editor.putString("tb_governor", makeGovernor());
		
		editor.putInt("nr_clock_min", oc.def_minclock);
		editor.putInt("nr_clock_max", oc.def_maxclock);
		editor.putString("nr_scheduler", oc.def_scheduler);
		editor.putString("nr_governor", oc.def_governor);
		
		editor.putInt("mode", 0);
		editor.commit();
		
		TurboUtils.WidgetUpdate(this);
		
		Toast.makeText(this, R.string.SettingEnd, Toast.LENGTH_SHORT).show();
		finish();
	}
}
