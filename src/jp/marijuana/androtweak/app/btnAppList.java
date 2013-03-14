package jp.marijuana.androtweak.app;

import jp.marijuana.androtweak.R;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class btnAppList
{
	private Context ctx;
	
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
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.androtweak", "jp.marijuana.androtweak.app.AppListActivity");
				ctx.startActivity(intent);
			}
		});
		return btn;
	}
}
