package jp.marijuana.androtweak.app;

import jp.marijuana.androtweak.NativeCmd;
import jp.marijuana.androtweak.R;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LifeLog
{
	private Context ctx;
	private appLayout aly;
	
	private final String[] tb = {"T_COMMUNICATION", "T_INFORMATION", "T_LIFELOG", "T_MAIL", "T_PERSON", "T_PHONE_BOOK", "T_SPAREPARTS", "T_SUMMARY"};
	private final String[] tbb = {"T_LIFELOG", "T_PERSON", "T_PHONE_BOOK"};
	private final String db = "/ldb/ldb.db";
	private final String dbb = "/ldb/ldbbackup.db";
	private Button btn;
	private NativeCmd nCmd = NativeCmd.getInstance();
	
	public static Button getButton(Context c, appLayout a)
	{
		LifeLog ins = new LifeLog(c, a);
		return ins.makeButton();
	}
	
	private LifeLog(Context c, appLayout a)
	{
		ctx = c;
		aly = a;
	}
	
	private Button makeButton()
	{
		btn = new Button(ctx);
		TrigerCheck();
		return btn;
	}
	
	private void TrigerCheck()
	{
		String cmd = "sqlite3 " + db + " \".schema T_COMMUNICATION\"|" + nCmd.cmdGrep + " TRIGGER";
		String[] line = nCmd.ExecCommand(cmd, true);
		String ret = line[1].trim().replace("\n", "");
		if (ret.length() > 0 ) {
			btn.setText(R.string.btn_LifeLogTri);
			btn.setOnClickListener(mEnable());
		} else {
			btn.setText(R.string.btn_LifeLogRec);
			btn.setOnClickListener(mDisable());
		}
	}
	
	private OnClickListener mDisable()
	{
		return new OnClickListener(){
			@Override
			public void onClick(View v) {
				addTriger();
				aly.ShowWait();
			}
		};
	}
	
	private OnClickListener mEnable()
	{
		return new OnClickListener(){
			@Override
			public void onClick(View v) {
				delTriger();
				aly.ShowWait();
			}
		};
	}
	
	private void addTriger()
	{
		String cmd = "";
		for (int i = 0; i < tb.length; i++) {
			cmd += "sqlite3 " + db + " \"delete from " + tb[i] + ";\"\n";
			cmd += "sqlite3 " + db + " \"" + makeTrigger(tb[i]) + "\"\n";
		}
		cmd += "sqlite3 " + db + " \"vacuum\"\n";
		
		for (int i = 0; i < tbb.length; i++) {
			cmd += "sqlite3 " + dbb + " \"delete from " + tbb[i] + ";\"\n";
			cmd += "sqlite3 " + dbb + " \"" + makeTrigger(tbb[i]) + "\"\n";
		}		
		cmd += "sqlite3 " + dbb + " \"vacuum\"\n";
		
		nCmd.ExecuteCommands(cmd.split("\n"), true);
	}
	
	private void delTriger()
	{
		String cmd = "";
		for (int i = 0; i < tb.length; i++) {
			cmd += "sqlite3 " + db + " \"" + dropTriger(tb[i]) + "\"\n";
			cmd += "sqlite3 " + db + " \"delete from " + tb[i] + ";\"\n";
		}
		cmd += "sqlite3 " + db + " \"vacuum\"\n";
		
		for (int i = 0; i < tbb.length; i++) {
			cmd += "sqlite3 " + dbb + " \"" + dropTriger(tbb[i]) + "\"\n";
			cmd += "sqlite3 " + dbb + " \"delete from " + tbb[i] + ";\"\n";
		}		
		cmd += "sqlite3 " + dbb + " \"vacuum\"\n";
		
		nCmd.ExecuteCommands(cmd.split("\n"), true);
	}
	
	private String makeTrigger(String t)
	{
		return String.format("create trigger %s_TRI insert on %s begin delete from %s; end;", t, t, t);
	}
	
	private String dropTriger(String t)
	{
		return String.format("drop trigger %s_TRI", t);
	}
}
