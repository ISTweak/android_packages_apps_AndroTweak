package jp.marijuana.androtweak;


import java.io.File;

import jp.marijuana.androtweak.app.appLayout;
import jp.marijuana.androtweak.kernel.kernelLayout;
import jp.marijuana.androtweak.utils.utilsLayout;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AndroTweakActivity extends Activity
{
	public static final String TAG = "AndroTweak";
	public static String Model = "";

	public LinearLayout MainLayout;
	public ViewPager PagerLayout;
	
	public static utilsLayout uLayout;
	public static appLayout aLayout;
	public static kernelLayout kLayout;
	private static NativeCmd nCmd;

	private static final int MENUID_MENU1 = (Menu.FIRST + 1);
	private static final int MENUID_MENU2 = (Menu.FIRST + 2);
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		MainLayout = new LinearLayout(this);
		MainLayout.setOrientation(LinearLayout.VERTICAL);
		setContentView(MainLayout);
		
		makeTitle();
		nCmd = NativeCmd.getInstance();
		if ( nCmd.su ) {
			assertBinaries(false);
			makePage();
		} else {
			TextView label = new TextView(this);
			label.setText(R.string.msg_abort);
			MainLayout.addView(label);
		}
	}
	
	private void assertBinaries(Boolean Compulsion)
	{
		File bindir = this.getDir("bin", 0);
		if ( Compulsion ) {
			String[] children = bindir.list();
			for (int i=0; i<children.length; i++) { 
				File fs = new File(bindir, children[i]);
				fs.delete();
			}
		}

		File file = new File(bindir, "reboot");
		if ( !file.exists() || Compulsion ) {
			nCmd.copyRawFile(getResources().openRawResource(R.raw.reboot), file, "0755", false);
			Toast.makeText(this, "copy reboot", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void makePage()
	{
		PagerLayout = new ViewPager(this);
		PagerLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				makeMainHead(position);
			}
		});
		PagerAdapter mPagerAdapter = new MyPagerAdapter();
		PagerLayout.setAdapter(mPagerAdapter);
		
		makeMainHead(0);
		makeLayout();
	}
	
	private void makeMainHead(int position)
	{
		MainLayout.removeAllViews();
		
		LinearLayout HeadTab = new LinearLayout(this);
		HeadTab.addView(makeHeadText(R.string.tabmenu_utility, 0, (position == 0)));
		HeadTab.addView(makeHeadText(R.string.tabmenu_application, 1, (position == 1)));
		HeadTab.addView(makeHeadText(R.string.tabmenu_kernel, 2, (position == 2)));
		
		MainLayout.addView(HeadTab);
		MainLayout.addView(PagerLayout);
	}
	
	private void makeLayout()
	{
		uLayout = new utilsLayout(this);
		aLayout = new appLayout(this);
		kLayout = new kernelLayout(this);
	}
	
	private TextView makeHeadText(int text, final int item, Boolean enable)
	{
		LinearLayout.LayoutParams lParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

	private void makeTitle()
	{
		String Version = "";
		PackageManager packageManager = this.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			Version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
		}

		setTitle(getString(R.string.app_name) + " - " + Version);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, MENUID_MENU1, Menu.NONE, R.string.menu_Close);
		menu.add(Menu.NONE, MENUID_MENU2, Menu.NONE, R.string.menu_Update);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case MENUID_MENU1:
				finish();
				break;
			case MENUID_MENU2:
				assertBinaries(true);
				break;
		}
		return true;
	}
}
