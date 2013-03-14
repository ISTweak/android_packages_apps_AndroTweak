package jp.marijuana.androtweak.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class VddActivity extends Activity
{
	private LinearLayout layout;
	private final KernelUtils oc = KernelUtils.getInstance();
	private final String TAG = AndroTweakActivity.TAG;
	private AlertDialog VddDlg = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);
		
		showVddList();
		setbtn();
	}
	
	private void reload()
	{
		oc.flg_update = true;
		oc.reload();
		layout.removeAllViews();
		showVddList();
		setbtn();
	}

	private void setbtn()
	{
		Button btncl = new Button(this);
		btncl.setText(R.string.btn_Close);
		btncl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Button btnex = new Button(this);
		btnex.setText(R.string.btn_SaveVDD);
		btnex.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveVDD();
				finish();
			}
		});
		
		LinearLayout.LayoutParams trlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		trlp.weight = 1;
		TextView dummy = new TextView(this);
		dummy.setLayoutParams(trlp);

		LinearLayout tray = new LinearLayout(this);
		tray.addView(btncl);
		tray.addView(dummy);
		
		if (NativeCmd.fileExists(oc.mydir + "/vdd.sh")) {
			Button btnd = new Button(this);
			btnd.setText(R.string.btn_DelVDD);
			btnd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteVDD();
					finish();
				}
			});
			tray.addView(btnd);
		}
		
		tray.addView(btnex);

		layout.addView(tray);
	}
	
	private void showVddList()
	{
		final ArrayList<String> rows = new ArrayList<String>();
		for (int i = 0; i < oc.vddmap.size(); i ++) {
 			String[] cl = oc.vddmap.get(i).split(":");
 			int s = Integer.parseInt(cl[0].trim());
 			String s1 = String.valueOf(s / 1000) + "MHz";
			String s2 = s1 + ":" + String.valueOf(Integer.parseInt(cl[1].trim())) + "mV";
			rows.add(s2);
		}
		
		ListView lv = new ListView(this);
		ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rows );
		lv.setAdapter(adp);
		lv.setScrollingCacheEnabled(false);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			 public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				showVddDialog(position);
			}
		});
		
		TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		trlp.weight = 1;
		lv.setLayoutParams(trlp);

		layout.addView(lv);
	}
	
	/**
	 * VDDダイアログ
	 * @param i
	 */
	private void showVddDialog(int i)
	{
		LinearLayout dgllayout = new LinearLayout(this);
		dgllayout.setOrientation(LinearLayout.HORIZONTAL);
		
		final View entryView = dgllayout.getRootView();
		final EditText edit = new EditText(this);
		
		String[] cl = oc.vddmap.get(i).split(":");
		edit.setText(String.valueOf(Integer.parseInt(cl[1].trim())));
		edit.setTag(String.valueOf(Integer.parseInt(cl[0].trim())));
		dgllayout.addView(edit);
		
		TextView tit = new TextView(this);
		tit.setText("mV");
		tit.setTextColor(Color.WHITE);
		dgllayout.addView(tit);
		
		VddDlg = new AlertDialog.Builder(this)
			.setTitle(String.valueOf(Integer.parseInt(cl[0].trim()) / 1000) + "MHz")
			.setView(entryView)
			.setNeutralButton(R.string.btn_Cancel, null)
			.setPositiveButton(R.string.btn_Execute, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					VddDlg.dismiss();
					String cmd = "echo '" + edit.getTag().toString() + " " + edit.getText() + "' > " + oc.vdd_levels;
					NativeCmd.ExecuteCommand(cmd, true);
					Log.i(TAG, cmd);
					reload();
				}
			}).create();
		VddDlg.show();
	}
	
	private void saveVDD()
	{
	 	try {
			final FileReader fr = new FileReader(new File(oc.vdd_levels));
			final BufferedReader br = new BufferedReader(fr);

	 		String str = "";
	 		String cmd = "";
	 		while((str = br.readLine()) != null){
	 			String[] line = str.split(":");
	 			int cl = Integer.parseInt(line[0].trim());
	 			int vl = Integer.parseInt(line[1].trim());
	 			cmd+= "echo '" + cl + " " + vl + "' > " + oc.vdd_levels + "\n";
	 		}
	 		br.close();
	 		fr.close();

	 		NativeCmd.createExecFile(cmd, oc.mydir + "/vdd.sh");
	 	} catch (FileNotFoundException e) {
	 		Log.e(TAG, e.toString());
	 	} catch (IOException e) {
	 		Log.e(TAG, e.toString());
		}
	 	Toast.makeText(this, R.string.msg_SaveVDD, Toast.LENGTH_SHORT).show();
	}
	
	private void deleteVDD()
	{
		File file = new File(oc.mydir + "/vdd.sh");
		if (file.exists()) {
			file.delete();
			Toast.makeText(this, R.string.msg_DelVDD, Toast.LENGTH_SHORT).show();
		}
		
	}
}
