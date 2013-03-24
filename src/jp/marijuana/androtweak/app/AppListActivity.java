/**
 * プリインストールアプリのリスト
 */
package jp.marijuana.androtweak.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.marijuana.androtweak.app.AppPagerAdapter;
import jp.marijuana.androtweak.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class AppListActivity extends Activity implements Runnable
{
	private static AppListActivity my;
	private ViewPager PagerLayout;
	private LinearLayout AppListLayout;
	public static LinearLayout dataLayout;
	public static LinearLayout systemLayout;
	
	private static ProgressDialog waitDialog;
	private Thread thread;
	
	public PackageManager pm;
	public static ListData item;

	@SuppressWarnings("rawtypes")
	private List<MyAdapter> ListAdp = new ArrayList<MyAdapter>();
	
	private final int white = Color.rgb(255, 255, 255);
	private final int blue = Color.rgb(0, 0, 255);
	private final String datadir = "/data/app";
	private final String systemdir = "/system/app";
	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	
	private int SelPosition = -1;
	private final int LVData = 0;
	private final int LVSystem = 1;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		my = this;
		super.onCreate(savedInstanceState);
		AppListLayout = new LinearLayout(this);
		AppListLayout.setOrientation(LinearLayout.VERTICAL);
		setContentView(AppListLayout);

		makePage();
		ShowWait();
	}
	
    @Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		@SuppressWarnings("unchecked")
		MyAdapter<ListData> myA = ListAdp.get(requestCode);
		switch(resultCode) {
		case 0:	
			if ( getPackageManager().getApplicationEnabledSetting(item.bodyText) == 2 ) {
				item.txtColor = blue;
				item.tv.setTextColor(blue);
				myA.ChangColor(SelPosition, blue);
			} else {
				item.txtColor = white;
				item.tv.setTextColor(white);
				myA.ChangColor(SelPosition, white);
			}
			myA.notifyDataSetChanged();
			break;
		case 1:	
			makePage();
			ShowWait();
			break;
		}
	}

    
	private void makePage()
	{
		dataLayout = new LinearLayout(this);
		dataLayout.setOrientation(LinearLayout.VERTICAL);
		systemLayout = new LinearLayout(this);
		systemLayout.setOrientation(LinearLayout.VERTICAL);
		
		PagerLayout = new ViewPager(this);
		PagerLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				makeMainHead(position);
			}
		});
		PagerAdapter mPagerAdapter = new AppPagerAdapter();
		PagerLayout.setAdapter(mPagerAdapter);
		
		makeMainHead(0);
	}
	
	private void makeMainHead(int position)
	{
		AppListLayout.removeAllViews();
		
		LinearLayout HeadTab = new LinearLayout(this);
		HeadTab.addView(makeHeadText(R.string.tabmenu_data, 0, (position == 0)));
		HeadTab.addView(makeHeadText(R.string.tabmenu_system, 1, (position == 1)));
		
		AppListLayout.addView(HeadTab);
		makeHeader();
		AppListLayout.addView(PagerLayout);
	}
	
	private void makeHeader()
	{
		TextView dsc = new TextView(this);
		dsc.setText(R.string.ActionDefAppDisable);
		
		TextView dsc2 = new TextView(this);
		dsc2.setText(R.string.ActionDefAppDesc);

		LinearLayout body = new LinearLayout(this);
		body.setOrientation(LinearLayout.VERTICAL);
		body.addView(dsc);
		body.addView(dsc2);
		
		AppListLayout.addView(body);
	}
	
	private TextView makeHeadText(int text, final int item, Boolean enable)
	{
		LinearLayout.LayoutParams lParam = new TableRow.LayoutParams(FP, WC);
		lParam.weight = 1;
		lParam.setMargins(1, 5, 1, 10);
		
		TextView txtV = new TextView(this);
		txtV.setText(text);
		txtV.setLayoutParams(lParam);
		txtV.setPadding(1, 10, 1, 10);
		txtV.setGravity(Gravity.CENTER);
		if ( enable ) {
			txtV.setBackgroundColor(Color.LTGRAY);
			txtV.setTextColor(Color.BLACK);
		} else {
			txtV.setBackgroundColor(Color.DKGRAY);
			txtV.setTextColor(Color.GRAY);
			txtV.setClickable(true);
			txtV.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v)
				{
					PagerLayout.setCurrentItem(item);
				}
			});
		}
		
		return txtV;
	}

	private void makeBotom()
	{
		dataLayout.addView(makeTray());
		systemLayout.addView(makeTray());
	}
	
	private LinearLayout makeTray()
	{
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
		return tray;
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
	
	private void ShowWait()
	{
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(getString(R.string.AppWaitAction));
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		waitDialog.show();
		thread = new Thread(this);
		thread.start();
	}

	private static Handler mhandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			my.getAppList();
			my.makeBotom();
			waitDialog.dismiss();
			waitDialog = null;
		}
	};
	
	public class MyAdapter<T extends ListData> extends ArrayAdapter<T>
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
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view;
			if (convertView == null) {
				view = inflater.inflate(this.textViewResourceId, null);
			} else {
				view = convertView;
			}
			T item = items.get(position);
			if (item != null) {
				ImageView icon = (ImageView) view.findViewById(R.id.Icon);
				icon.setImageDrawable(item.icon);
				item.tv = (TextView) view.findViewById(R.id.HeaderText);
				item.tv.setText(item.headerText);
				item.tv.setTextColor(item.txtColor);
				TextView bT = (TextView) view.findViewById(R.id.BodyText);
				bT.setText(item.bodyText);
			}
			return view;
		}
		
		public void ChangColor(int position, int color)
		{
			T item = items.get(position);
			item.txtColor = color;
			item.tv.setTextColor(item.txtColor);
		}
	}

	private class ListData
	{
		public Drawable icon;
		public String headerText;
		public String bodyText;
		public int txtColor = 0;
		public TextView tv;

		public ListData(Drawable icon, String headerText, String bodyText, int cl)
		{
			this.icon = icon;
			this.headerText = headerText.replace("\n", "");
			this.bodyText = bodyText;
			this.txtColor = cl;
		}
	}
	
	private void setListView(List<ApplicationInfo> listApps, LinearLayout layout, final int lvposition)
	{
		layout.removeAllViews();
		ListView lv = new ListView(this);

		MyAdapter<ListData>  myA = new MyAdapter<ListData>(this, R.layout.applist_tray, new ArrayList<ListData>());
		for (ApplicationInfo lapp : listApps) {
			if (pm.getApplicationEnabledSetting(lapp.packageName)== 2) {
				myA.add(new ListData(lapp.loadIcon(pm), lapp.loadLabel(pm).toString(), lapp.packageName, blue));
			} else {
				myA.add(new ListData(lapp.loadIcon(pm), lapp.loadLabel(pm).toString(), lapp.packageName, white));
			}
		}
		lv.setAdapter(myA);
		ListAdp.add(lvposition, myA);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ListView listView = (ListView) parent;
				item = (ListData)listView.getItemAtPosition(position);
				SelPosition = position;
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.androtweak", "jp.marijuana.androtweak.app.AppDetailsActivity");
				intent.putExtra("appName", item.bodyText);
				startActivityForResult(intent, lvposition);
			}
		});
		
		LinearLayout.LayoutParams trlp = new LinearLayout.LayoutParams(FP, WC);
		trlp.weight = 1;
		layout.addView(lv, trlp);
	}
	
	private void getAppList()
	{
		pm = this.getPackageManager();
		int l1 = datadir.length();
		int l2 = systemdir.length();
		List<ApplicationInfo> list = pm.getInstalledApplications(0);
		List<ApplicationInfo> dataApps = new ArrayList<ApplicationInfo>();
		List<ApplicationInfo> systemApps = new ArrayList<ApplicationInfo>();
		for (ApplicationInfo ai : list) {
			if ( ai.publicSourceDir.substring(0, l1).equals(datadir) ) {
				dataApps.add(ai);
			} else if ( ai.publicSourceDir.substring(0, l2).equals(systemdir) ) {
				systemApps.add(ai);
			}
		}
		Collections.sort(dataApps, new ApplicationInfo.DisplayNameComparator(pm));
		Collections.sort(systemApps, new ApplicationInfo.DisplayNameComparator(pm));
		
		setListView(dataApps, dataLayout, LVData);
		setListView(systemApps, systemLayout, LVSystem);
	}
}

