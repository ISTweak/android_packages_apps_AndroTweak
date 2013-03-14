package jp.marijuana.androtweak.kernel;

import java.io.File;

import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ZramActivity extends Activity
{
	private LinearLayout layout;
	private final KernelUtils oc = KernelUtils.getInstance();
	private final LayoutParams lparm = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	private final String TAG = AndroTweakActivity.TAG;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		setContentView(layout);

		TextView tit = new TextView(this);
		tit.setText(getString(R.string.ZramSettings) + " " + oc.getZramsize());
		layout.addView(tit);
		
		layout.addView(TextBoxZram());
		layout.addView(SpinnerSwappiness());
		layout.addView(ButtonSetting());
		
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
	
	private LinearLayout TextBoxZram()
	{
		LinearLayout tray = new LinearLayout(this);
		TextView sctit = new TextView(this);
		sctit.setText(R.string.Str_Zramsize);
		tray.addView(sctit, lparm);
		
		int zramsize = oc.getZramSettingSize();
		EditText edit = new EditText(this);
		edit.setId(R.string.id_Zram);
		edit.setWidth(110);
		edit.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		InputFilter[] inlength = new InputFilter[1];
		inlength[0] = new InputFilter.LengthFilter(3);
		edit.setFilters(inlength);
		
		edit.setText(String.valueOf(zramsize));
		tray.addView(edit, lparm);
		
		TextView par = new TextView(this);
		par.setText("MB");
		tray.addView(par, lparm);
		return tray;
	}
	
	private LinearLayout SpinnerSwappiness()
	{
		LinearLayout tray = new LinearLayout(this);
		TextView sctit = new TextView(this);
		sctit.setText(R.string.cSwappiness);
		tray.addView(sctit, lparm);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		int spval = oc.def_swappiness;
		int spdef = 0;
		for (int i = 1; i < 11; i++ ) {
			if ( spval == (i * 10) ) {
				spdef = i - 1;
			}
			adapter.add(String.valueOf(i * 10));
		}
		
		Spinner swap = new Spinner(this);
		swap.setPromptId(R.string.cSwappiness);
		swap.setId(R.string.id_Swappiness);
		swap.setAdapter(adapter);
		swap.setSelection(spdef);
		tray.addView(swap, lparm);
	
		CheckBox chkbox = new CheckBox(this);
		chkbox.setId(R.string.id_CheckBoot);
		chkbox.setText(R.string.ChkBoot);
		chkbox.setChecked(NativeCmd.fileExists(oc.mydir + "/swappiness.sh"));
		tray.addView(chkbox, lparm);
		return tray;
	}
	
	private LinearLayout ButtonSetting()
	{
		LinearLayout tray = new LinearLayout(this);
		
		Button btn = new Button(this);
		btn.setText(R.string.btn_Execute);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edit = (EditText)findViewById(R.string.id_Zram);
				if ( edit.getText().length() > 0 ) {
					SaveSettings();
				} else {
					DeleteSettings();
				}
			}
		});
		tray.addView(btn, lparm);
		
		if (NativeCmd.fileExists(oc.zramsize)) {
			Button btnd = new Button(this);
			btnd.setText(R.string.SettingDelete);
			btnd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DeleteSettings();
				}
			});
			tray.addView(btnd, lparm);
		}
		return tray;
	}
	
	private void SaveSettings()
	{
		String[] cmds = new String[3];
		
		EditText edit = (EditText)findViewById(R.string.id_Zram);
		cmds[0] = "echo " + Integer.parseInt(edit.getText().toString()) * 1024 * 1024 + " > " + oc.zramsize;
		cmds[1] = "chmod 0666 " + oc.zramsize;
		
		Spinner spinner = (Spinner)findViewById(R.string.id_Swappiness);
		String item = (String)spinner.getSelectedItem();
		cmds[2] = "echo " + item + " > " + oc.swappiness;
		
		String[] ret = NativeCmd.ExecCommands(cmds, true);
		if (ret[2].trim().replace("\n", "").length() == 0) {
			Log.i(TAG, "set zram size");
		} else {
			Log.e(TAG, ret[2]);
		}

		SaveSwap(cmds[2]);
		
		oc.flg_update = true;
		oc.reload();
		kernelLayout.my.ShowWait();
		Toast.makeText(this, R.string.msg_SaveZram, Toast.LENGTH_SHORT).show();
	}
	
	private void SaveSwap(String cmd)
	{
		CheckBox chkbox = (CheckBox) findViewById(R.string.id_CheckBoot);
		if (chkbox.isChecked()) {
			NativeCmd.createExecFile(cmd, oc.mydir + "/swappiness.sh");
		} else {
			File file = new File(oc.mydir + "/swappiness.sh");
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	private void DeleteSettings()
	{
		Spinner spinner = (Spinner)findViewById(R.string.id_Swappiness);
		String item = (String)spinner.getSelectedItem();
		
		String[] cmds = new String[2];
		cmds[0] = "rm " + oc.zramsize;
		cmds[1] = "echo " + item + " > " + oc.swappiness;
		
		NativeCmd.ExecuteCommands(cmds, true);
		SaveSwap(cmds[1]);
		oc.flg_update = true;
		oc.reload();
		kernelLayout.my.ShowWait();
	}
}
