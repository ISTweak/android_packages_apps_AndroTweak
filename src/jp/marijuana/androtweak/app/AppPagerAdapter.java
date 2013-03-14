package jp.marijuana.androtweak.app;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AppPagerAdapter  extends PagerAdapter
{
	private final int PageCount = 2;
	
	@Override
	public LinearLayout instantiateItem(ViewGroup container, int position)
	{
		LinearLayout layout = null;
		switch (position) {
		case 0:
			layout = AppListActivity.dataLayout;
			break;
		case 1:
			layout = AppListActivity.systemLayout;
			break;
		}

		((ViewPager) container).addView(layout);
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