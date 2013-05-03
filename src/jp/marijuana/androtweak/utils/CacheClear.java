/**
 * キャッシュクリア
 */
package jp.marijuana.androtweak.utils;

import java.util.ArrayList;

import jp.marijuana.androtweak.R;
import jp.marijuana.androtweak.NativeCmd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CacheClear
{
	private Context ctx;
	
	public static Button getButton(Context c)
	{
		CacheClear ins = new CacheClear(c);
		return ins.makeButton();
	}
	
	private CacheClear(Context c)
	{
		ctx = c;
	}
	
	private Button makeButton()
	{
		Button btn = new Button(ctx);
		btn.setText(R.string.btn_CacheClear);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showCacheClear();
			}
		});
		return btn;
	}
	
	private void showCacheClear()
	{
		AlertDialog.Builder confirm = new AlertDialog.Builder(ctx); 
		confirm.setTitle(R.string.CACHE_CLEAR);
		confirm.setMessage(R.string.CACHE_CLEAR_DESC);
		confirm.setPositiveButton(R.string.btn_CLEAR, new DialogInterface.OnClickListener(){  
			@Override
			public void onClick(DialogInterface dialog, int which) {
				execClear();
			}});
		confirm.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}});
		confirm.show();
	}
	
	private void execClear()
	{
		NativeCmd nCmd = NativeCmd.getInstance();
		ArrayList<String> cmds = new ArrayList<String>();
		cmds.add(nCmd.cmdRm + " -rf /data/dalvik-cache/*");
		if ( nCmd.fileExists("/data/bugreports/") ) {
			cmds.add(nCmd.cmdRm + " -rf /data/bugreports/*");
		}
		if ( nCmd.fileExists("/data/cache/") ) {
			cmds.add(nCmd.cmdRm + " -rf /data/cache/*");
		}
		nCmd.ExecCommands(cmds.toArray(new String[0]) , true);
	}
}
