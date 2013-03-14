package jp.marijuana.androtweak.app;

import java.util.ArrayList;

import jp.marijuana.androtweak.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class btnAppList
{
	private Context ctx;
	private AlertDialog mDlg = null;
	private final String datadir = "/data/app";
	private final String systemdir = "/system/app";
	
	public static Button getButton(Context c)
	{
		btnAppList ins = new btnAppList(c);
		return ins.makeButton();
	}
	
	private btnAppList(Context c)
	{
		ctx = c;
	}
	
	private Button makeButton()
	{
		Button btn = new Button(ctx);
		btn.setText(R.string.btn_AppList);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SelectDir();
			}
		});
		return btn;
	}
	
	private void SelectDir()
	{
		final ArrayList<String> rows = new ArrayList<String>();
		rows.add(datadir);
		rows.add(systemdir);
		
		ListView lv = new ListView(ctx);
		lv.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, rows));
		lv.setScrollingCacheEnabled(false);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				mDlg.dismiss();
				switch (position) {
					case 0: AppListActivity.AppDir = datadir; break;
					case 1: AppListActivity.AppDir = systemdir; break;
				}
				
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.androtweak", "jp.marijuana.androtweak.app.AppListActivity");
				ctx.startActivity(intent);
			}
		});

		mDlg = new AlertDialog.Builder(ctx)
			.setTitle(ctx.getString(R.string.SelectDirTittle))
			.setPositiveButton(R.string.btn_Cancel, null)
			.setView(lv)
			.create();
		mDlg.show();
	}
}
