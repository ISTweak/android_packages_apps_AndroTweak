package jp.marijuana.androtweak.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.marijuana.androtweak.AndroTweakActivity;
import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AppDetailsActivity  extends Activity implements Runnable
{
	private static String appName;
	private static String TAG;
	private static AppDetailsActivity my;

	private LinearLayout AppLayout;
	private static ListView lv;
	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	
	private final int white = Color.rgb(255, 255, 255);
	private final int red = Color.rgb(255, 0, 0);
	
	private static ProgressDialog waitDialog;
	private Thread thread;
	private static int ExecType = 0;
	private final static int EXEC_REINSTALL = 0;
	private final static int EXEC_ENABLE = 1;
	private final static int EXEC_DISABLE = 2;
	private final static int EXEC_DELPERM = 3;
	private static NativeCmd nCmd = NativeCmd.getInstance();
	
	private static String MatchPattern = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		TAG = AndroTweakActivity.TAG;
		my = this;
		super.onCreate(savedInstanceState);
		AppLayout = new LinearLayout(this);
		AppLayout.setOrientation(LinearLayout.VERTICAL);
		setContentView(AppLayout);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			appName = extras.getString("appName");
			makeAppInfo();
		}
	}
	
	private void makeButton(int b)
	{
		LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(144, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		LinearLayout llh = new LinearLayout(this);
		llh.setOrientation(LinearLayout.HORIZONTAL);
		
		Button btnd = new Button(this);
		btnd.setText(R.string.btn_Details);
		btnd.setLayoutParams(blp);
		btnd.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
				intent.setData(Uri.parse("package:" + appName));
				startActivity(intent);
				finish();
			}
		});
		llh.addView(btnd);
		
		Button btnr = new Button(this);
		btnr.setText(R.string.btn_Reinstall);
		btnr.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				WaitExecute(EXEC_REINSTALL);
			}
		});
		llh.addView(btnr);
		
		Button btnf = new Button(this);
		btnd.setLayoutParams(blp);
		
		if ( b != 2 ) {
			btnf.setText(R.string.DisableAction);
			btnf.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					WaitExecute(EXEC_DISABLE);
				}
			});
		} else {
			btnf.setText(R.string.EnableAction);
			btnf.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					WaitExecute(EXEC_ENABLE);
				}
			});
		}

		llh.addView(btnf);
		
		AppLayout.addView(llh);
	}
	
	private void makeHead(String apname, String apversion, Drawable icon)
	{
		//Name
		TextView tvn = new TextView(this);
		tvn.setText(apname);
		tvn.setTextSize(16.0f);
		tvn.setTextColor(Color.rgb(255, 255, 255));
		
		//Version
		TextView tvv = new TextView(this);
		tvv.setText(apversion);
		
		LinearLayout llv = new LinearLayout(this);
		llv.setOrientation(LinearLayout.VERTICAL);
		llv.addView(tvn);
		llv.addView(tvv);
		llv.setPadding(2, 4, 4, 4);
		
		//icon
		LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(72, 72);
		ImageView img = new ImageView(this);
		img.setImageDrawable(icon);
		img.setLayoutParams(ilp);
		img.setPadding(4, 4, 10, 4);
		
		LinearLayout llh = new LinearLayout(this);
		llh.setOrientation(LinearLayout.HORIZONTAL);
		llh.addView(img);
		llh.addView(llv);
		llh.setPadding(0, 0, 0, 10);
		
		AppLayout.addView(llh);
	}
	
	private void makePermBar()
	{
		LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		TextView tvp = new TextView(this);
		tvp.setText(R.string.lbl_permission);
		tvp.setGravity(Gravity.CENTER);
		tvp.setBackgroundColor(Color.rgb(180, 180, 188));
		tvp.setTextColor(Color.rgb(0, 0, 0));
		tvp.setPadding(10, 8, 10, 8);
		tvp.setLayoutParams(tlp);
		tlp.setMargins(0, 10, 0, 4);
		AppLayout.addView(tvp);
	}
	
	private void makeError()
	{
		TextView tv = new TextView(this);
		tv.setText(R.string.str_apperror);
		AppLayout.addView(tv);
		
		Button btncl = new Button(this);
		btncl.setText(R.string.btn_Close);
		btncl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(1);
				finish();
			}
		});
		AppLayout.addView(btncl);
	}
	
	private void makeAppInfo()
	{
		AppLayout.removeAllViews();
		
		PackageManager pm = getPackageManager();
		ApplicationInfo ai = null;
		PackageInfo pi = null;
		try {
			ai = pm.getApplicationInfo(appName, PackageManager.GET_META_DATA);
			pi = pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e1) {
			Log.e(TAG, e1.toString());
			makeError();
			return;
		}
		
		String version = getString(R.string.lbl_version) + pi.versionName + " (uid:" + ai.uid + ")";
		makeHead(ai.loadLabel(pm).toString(), version, pm.getApplicationIcon(ai));
		makeButton(pm.getApplicationEnabledSetting(appName));

		Boolean bolList = true;
		lv = new ListView(this);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		MyAdapter<ListData> myA = new MyAdapter<ListData>(this, R.layout.permission_tray, new ArrayList<ListData>());
		try {
			PackageInfo packageInfo = pm.getPackageInfo(appName, PackageManager.GET_PERMISSIONS);
			String[] reqperm = packageInfo.requestedPermissions;
			if (reqperm != null) {
				ArrayList<String> array = ReadPermission();
				for (int i = 0; i < reqperm.length; i++) {
					int color = white;
					Boolean bol = (array.indexOf(reqperm[i]) != -1);
					try {
						PermissionInfo permI = pm.getPermissionInfo(reqperm[i], PackageManager.GET_META_DATA);
						if ( permI.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS ) {
							color = red;
						}
						if ( permI.packageName.equals("android") ) {
							myA.add(new ListData(reqperm[i], getString(permI.labelRes), getString(permI.descriptionRes), color, bol));
						} else {
							myA.add(new ListData(reqperm[i], reqperm[i], "", color, bol));
						}
					} catch (NameNotFoundException e) {
						myA.add(new ListData(reqperm[i], reqperm[i], "", white, bol));
					} catch (NotFoundException e2) {
						myA.add(new ListData(reqperm[i], reqperm[i], "", white, bol));
					}
				}
			} else {
				bolList = false;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.d(AndroTweakActivity.TAG, e.toString());
			bolList = false;
		}
		
		if ( bolList ) {
			makePermBar();
			lv.setAdapter(myA);
			LinearLayout.LayoutParams trlp = new LinearLayout.LayoutParams(FP, FP);
			trlp.weight = 1;
			AppLayout.addView(lv, trlp);
			if ( ai.publicSourceDir.substring(0, 5).equals("/data") ) {
				AppLayout.addView(makeDelete());
			}
		}
		
		LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(FP, FP);
		blp.weight = 1.8f;
		AppLayout.addView(makeClose(), blp);
	}
	
	private LinearLayout makeDelete()
	{
		Button btncl = new Button(this);
		btncl.setText(R.string.btn_permdel);
		btncl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WaitExecute(EXEC_DELPERM);
			}
		});
		LinearLayout tray = new LinearLayout(this);
		tray.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		tray.addView(btncl);
		return tray;
	}
	
	private LinearLayout makeClose()
	{
		Button btncl = new Button(this);
		btncl.setText(R.string.btn_Close);
		btncl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(0);
				finish();
			}
		});
		LinearLayout tray = new LinearLayout(this);
		tray.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(WC, WC);
		tray.addView(btncl, blp);
		return tray;
	}
	
	private class MyAdapter<T extends ListData> extends ArrayAdapter<T>
	{
		private ArrayList<T> items;
		private LayoutInflater inflater;
		private int textViewResourceId;
		
		public MyAdapter(Context context, int textViewResourceId, ArrayList<T> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
			this.textViewResourceId = textViewResourceId;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position,View convertView, ViewGroup parent)
		{
			View view;
			if (convertView == null) {
				view = inflater.inflate(this.textViewResourceId, null);
			} else {
				view = convertView;
			}
			
			final int p = position;
			final T item = items.get(p);
			if (item != null) {
				TextView labelText = (TextView) view.findViewById(R.id.perm_lbl);
				labelText.setText(item.labelText);
				labelText.setTextColor(item.txtColor);
				
				TextView descText = (TextView) view.findViewById(R.id.perm_desc);
				descText.setText(item.descText);
				
				CheckBox chkbox = (CheckBox) view.findViewById(R.id.chkbox);
				if (item.isenabled) {
					chkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
							item.ischecked = isChecked;
						}
		    		});
				} else {
					item.ischecked = false;
				}
				chkbox.setChecked(item.ischecked);
				chkbox.setEnabled(item.isenabled);
			}

			return view;
		}
	}
	
	private class ListData
	{
		public String permName;
		public String labelText;
		public String descText;
		public int txtColor = 0;
		public Boolean ischecked = false;
		public Boolean isenabled = true;

		public ListData(String permName, String labelText, String descText, int cl, Boolean ena)
		{
			this.isenabled = ena;
			this.permName = permName;
			this.labelText = labelText;
			this.descText = descText;
			this.txtColor = cl;
		}
	}

	@Override
	public void run()
	{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		mhandler.sendEmptyMessage(0);	
	}
	
	private static Handler mhandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(ExecType) {
			case EXEC_REINSTALL:
				my.reinstall();
				break;
			case EXEC_ENABLE:
				exec_pm("enable", appName);
				break;
			case EXEC_DISABLE:
				exec_pm("disable", appName);
				break;
			case EXEC_DELPERM:
				DeletePermission();
			}
			my.makeAppInfo();
			waitDialog.dismiss();
			waitDialog = null;
		}
	};

	private void WaitExecute(int type)
	{
		ExecType = type;
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(getString(R.string.msg_Executing));
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		waitDialog.show();
		thread = new Thread(this);
		thread.start();
	}
	
	private void reinstall()
	{
		PackageManager pm = getPackageManager();
		try {
			ApplicationInfo ai = pm.getApplicationInfo(appName, PackageManager.GET_META_DATA);
			exec_pm("install -r", ai.publicSourceDir);
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	private static void exec_pm(String cmd, String arg)
	{
		nCmd.ExecCommand("pm " + cmd + " " + arg, true);
	}
	
	private static void DeletePermission()
	{
		ArrayList<String> array = new ArrayList<String>();
		Boolean ischeck = false;
		for (int i = 0; i < lv.getCount(); i++) {
			final ListData item = (ListData)lv.getItemAtPosition(i);
			if ( item.ischecked ) {
				array.add(item.permName);
				ischeck = true;
			}
		}
		if ( ischeck ) {
			MakePackagesXml(array);
		}
	}
	
	private ArrayList<String> ReadPermission()
	{
		ArrayList<String> array = new ArrayList<String>();
		nCmd.ExecCommand("cat /data/system/packages.xml > /data/data/jp.marijuana.androtweak/files/packages.xml",  false);
		String readString = "";
		try {
			FileInputStream  fileInputStream = my.openFileInput("packages.xml");
			byte[] readBytes = new byte[fileInputStream.available()];
			fileInputStream.read(readBytes);
			readString = new String(readBytes);
			fileInputStream.close();
			
			String str = "";
			String[] patstr = {
					"<shared\\-user\\sname=\"" + appName + "([\\s|\\S])*?</shared\\-user>",
					"<updated\\-package\\sname=\"" + appName + "\"\\s([\\s|\\S])*?</updated\\-package>",
					"<package\\sname=\"" + appName + "\"\\s([\\s|\\S])*?</package>"
			};
			Pattern pattern;
			Matcher match;
			for ( String pat: patstr ) {
				pattern = Pattern.compile(pat);
				match = pattern.matcher(readString);
				if (match.find()) {
					MatchPattern = pat;
					str = match.group();
					Log.d(TAG, pat);
					break;
				}
			}
			
			if ( str.length() > 0 ) {
				pattern = Pattern.compile("<perms>([\\s|\\S])*?</perms>");
				match = pattern.matcher(str);
				if (match.find()) {
					str = match.group();
					pattern = Pattern.compile("<item name=\"(\\S+)?\"\\s/>\n");
					match = pattern.matcher(str);
					while(match.find()){
						array.add(match.group(1));
						Log.d(TAG, match.group(1));
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}

		return array;
	}
	
	private static void MakePackagesXml(ArrayList<String> array)
	{
		nCmd.ExecCommand("cat /data/system/packages.xml > /data/data/jp.marijuana.androtweak/files/packages.xml",  false);

		String readString = "";
		try {
			FileInputStream  fileInputStream = my.openFileInput("packages.xml");
			byte[] readBytes = new byte[fileInputStream.available()];
			fileInputStream.read(readBytes);
			readString = new String(readBytes);
			fileInputStream.close();

			Pattern pattern = Pattern.compile(MatchPattern);
			Matcher match = pattern.matcher(readString);
			if (match.find()) {
				final String str = match.group();
				String strrep = str;
				String find;
				for ( String perm: array ) {
					find = "<item name=\"" + perm + "\" />\n";
					strrep = strrep.replace(find, "");
				}
				
				String writeString = readString.replace(str, strrep);
				FileOutputStream fileOutputStream = my.openFileOutput("packages.xml", MODE_PRIVATE);
				fileOutputStream.write(writeString.getBytes());
				fileOutputStream.close();
				
				nCmd.ExecCommand("cat /data/data/jp.marijuana.androtweak/files/packages.xml > /data/system/packages.xml",  true);
			}
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		nCmd.ExecCommand("rm /data/data/jp.marijuana.androtweak/files/packages.xml",  true);
		nCmd.ExecCommand("rm /data/dalvik-cache/data@app@" + appName + "*",  true);
	}
	
}
