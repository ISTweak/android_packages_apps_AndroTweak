package jp.marijuana.androtweak;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.content.Context;
import android.util.Log;


public class NativeCmd
{
	public static String au = "/sbin/au";
	public static String sh = "/system/bin/sh";
	public static String cmdGrep = "grep";
	public static String cmdSed = "sed";
	public static String cmdRm = "rm";
	public static String cmdPkill = "pkill";
	public static String cmdPareL = "[";
	public static String cmdPareR = "]";

	private final static String TAG = AndroTweakActivity.TAG;
	
	private static class StreamGobbler extends Thread
	{
		InputStream is;
		OutputStream os;

		StreamGobbler(InputStream is, OutputStream redirect)
		{
			this.is = is;
			this.os = redirect;
		}
		
		public void run()
		{
			PrintWriter pw = new PrintWriter(os);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					pw.println(line);
				}
				br.close();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			pw.flush();
		}
	}	

	public static String getCmdPath(String cmd)
	{
		if ( NativeCmd.fileExists("/data/root/bin/" + cmd) ) {
			return "/data/root/bin/" + cmd;
		} else if ( NativeCmd.fileExists("/data/local/bin/" + cmd) ) {
			return "/data/local/bin/" + cmd;
		} else if ( NativeCmd.fileExists("/system/xbin/" + cmd) ) {
			return "/system/xbin/" + cmd;
		} else if ( NativeCmd.fileExists("/sbin/" + cmd) ) {
			return "/sbin/" + cmd;
		}
		return cmd;
	}

	/**
	 * ファイル（ディレクト）の存在チェック
	 * @param String fn
	 * @return boolean
	 */
	public static boolean fileExists(String fn)
	{
		return new File(fn).exists();
	}

	/**
	 * 複数コマンドの実行
	 * @param String[] cmds
	 * @param Boolean su
	 * @return String[]
	 */
	public static String[] ExecCommands(String[] cmds, Boolean su)
	{
		String[] rets = new String[3];
		String shell = NativeCmd.sh;
		if ( su ) {
			shell = NativeCmd.au;
		}

		ByteArrayOutputStream std;
		ByteArrayOutputStream err;
		StreamGobbler stdGobbler;
		StreamGobbler errGobbler;
		
		try {
			Process proc = Runtime.getRuntime().exec(shell);
			OutputStream os = proc.getOutputStream();
			String cmd = "";
			for (int i = 0; i < cmds.length; i++) {
				cmd = cmds[i] + "\n";
				os.write(cmd.getBytes());
			}
			os.flush();
			os.close();
			os.flush();
			std = new ByteArrayOutputStream();
			stdGobbler = new StreamGobbler(proc.getInputStream(), std);
			stdGobbler.start();

			err = new ByteArrayOutputStream();
			errGobbler = new StreamGobbler(proc.getErrorStream(), err);
			errGobbler.start();

			rets[0] = String.valueOf(proc.waitFor());
			stdGobbler.join();
			errGobbler.join();
			
			rets[1] = new String(std.toByteArray());
			rets[2] = new String(err.toByteArray());

			std.close();
			err.close();
		} catch (Throwable t) {
			Log.e(TAG, t.toString());
		}

		return rets;
	}

	/**
	 * コマンドの実行
	 * @param String　cmd
	 * @param Boolean　su
	 * @return　String[]
	 */
	public static String[] ExecCommand(String cmd, Boolean su)
	{
		String[] arrayOfCmd = new String[1];
		arrayOfCmd[0] = cmd;
		return ExecCommands(arrayOfCmd, su);
	}

	/**
	 * プロパティ値の取得
	 * @param key
	 * @return　String
	 */
	public static String getProperties(String key)
	{
		String[] ret = new String[3];
		ret = ExecCommand("getprop " + key, false);
		if ( ret[1].length() > 0 ) {
			return  ret[1].trim().replace("\n", "");
		}
		return "";
	}
	
	public static String getCat(String fn)
	{
		String[] ret = new String[3];
		ret = ExecCommand("cat " + fn, false);
		if ( ret[1].length() > 0 ) {
			return  ret[1].trim().replace("\n", "");
		}
		return "";
	}

	/**
	 * コマンドを実行する(戻り値がある場合はアラート)
	 * @param Context ctx
	 * @param String cmd
	 * @param Boolean su
	 */
	public static boolean ExecuteCmdAlert(Context ctx, String cmd, Boolean su)
	{
		String[] ret = new String[3];
    	ret = NativeCmd.ExecCommand(cmd, su);
    	
    	if ( ret[1].length() > 0 ) {
    		//ISTweakActivity.alert(ctx, ret[1]);
    		Log.d(TAG, ret[1]);
    	}
    	if ( ret[2].length() > 0 ) {
    		//ISTweakActivity.alert(ctx, ret[2]);
    		Log.e(TAG, ret[2]);
    		return false;
    	} 
    	return true;
	}

	/**
	 * 戻り値なしのコマンドを実行する
	 * @param String　paramCommand
	 * @param Boolean su
	 */
	public static void ExecuteCommand(String cmd, Boolean su)
	{
		String[] arrayOfString = new String[1];
		arrayOfString[0] = cmd;
		ExecuteCommands(arrayOfString, su);
	}

	/**
	 * 戻り値なしのコマンドを複数実行する
	 * @param String[] cmds
	 * @param Boolean su
	 */
	public static void ExecuteCommands(String[] cmds, Boolean su)
	{
		String shell = NativeCmd.sh;
		if ( su ) {
			shell = NativeCmd.au;
		}
		
		try {
			Process proc = Runtime.getRuntime().exec(shell);
			OutputStream os = proc.getOutputStream();
			
			String cmd = "";
			for (int i = 0; i < cmds.length; i++) {
				cmd = cmds[i] + "\n\n";
				os.write(cmd.getBytes());
			}
			os.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}

 	/**
	 * ファイルを1行読み込み
	 * @param fn ファイル名
	 * @return
	 */
	public static String readFile(String fn)
	{
		String str = "";
		try {
			final File file = new File(fn);
			final FileReader fr = new FileReader(file);
			final BufferedReader br = new BufferedReader(fr);
			str = br.readLine().trim().replace("\n", "");
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
	 		Log.e(TAG, e.toString());
	 	} catch (IOException e) {
	 		Log.e(TAG, e.toString());
	 	}
		
		return str;
	}

	/**
	 * 実行ファイルを作成
	 * @param cmd コマンド
	 * @param fn	ファイル名（フルパス）
	 */
	public static void createExecFile(String cmd, String fn)
	{
		try {
			final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fn));
			out.write("#!/system/bin/sh\n");
			out.write("export PATH=/data/root/bin:\"$PATH\"\n");
			out.write(cmd);
			out.write("\nexit 0\n");
			out.flush();
			out.close();
			ExecuteCommand("chmod 0777 " + fn, true);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	public static void createExecFile(String[] cmds, String fn)
	{
		try {
			final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fn));
			out.write("#!/system/bin/sh\n");
			out.write("export PATH=/data/root/bin:\"$PATH\"\n");
			for ( int i = 0; i < cmds.length; i++) {
				out.write(cmds[i] + "\n");
			}
			out.write("\nexit 0\n");
			out.flush();
			out.close();
			ExecuteCommand("chmod 0777 " + fn, true);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	public static void copyRawFile(InputStream is, File file, String mode, boolean root)
	{
		final String abspath = file.getAbsolutePath();
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			byte buf[] = new byte[1024];
			int len;
			while ((len = is.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			is.close();
			if ( root ) {
				Runtime.getRuntime().exec("chown root.root " + abspath).waitFor();
			}
			Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} catch (InterruptedException e) {
			Log.e(TAG, e.toString());
		}
	}

	public static void sleep(int time)
	{
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Log.d(TAG, e.toString());
		}
	}
}
