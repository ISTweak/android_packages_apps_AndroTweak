package jp.marijuana.androtweak;

import jp.marijuana.androtweak.AndroTweakActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class MyPagerAdapter extends PagerAdapter
{
	private final int PageCount = 3;
	private final String TAG = AndroTweakActivity.TAG;
	
	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		Object layout = null;
		switch (position) {
		case 0:
			layout = AndroTweakActivity.uLayout.makeLayout();
			Log.i(TAG, "Utils Layput");
			break;
		case 1:
			layout = AndroTweakActivity.aLayout.makeLayout();
			Log.i(TAG, "App Layput");
			break;
		case 2:
			layout = AndroTweakActivity.kLayout.makeLayout();
			Log.i(TAG, "Kernel Layput");
			break;
		}
		
		((ViewPager) container).addView((View) layout);
		return layout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		((ViewPager)container).removeView((View)object);
	}

	@Override
	public int getCount()
	{
		return PageCount;
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view.equals(object);
	}
}
