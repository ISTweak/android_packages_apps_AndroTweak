package jp.marijuana.androtweak.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import jp.marijuana.androtweak.NativeCmd;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseArray;


@SuppressLint("UseSparseArrays")
public class KernelUtils
{
	public String vdd_levels = "/sys/devices/system/cpu/cpu0/cpufreq/vdd_levels";
	
	public final String scaling_min_freq = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
	public final String scaling_max_freq = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	public final String cpuinfo_min_freq = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";	
	public final String cpuinfo_max_freq = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
	public final String scaling_available_frequencies = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
	public final String scaling_governor = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	public final String scaling_available_governors = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
	
	public final String disksize = "/sys/block/zram0/disksize";
	public final String swappiness = "/proc/sys/vm/swappiness";
	public final String zramsize = "/data/root/compsize";
	
	public String scheduler = "/sys/block/mtdblock1/queue/scheduler";
	
	public HashMap<Integer, String> clockmap = new HashMap<Integer, String>();
	public SparseArray<String> vddmap = new SparseArray<String>();
	public String[] schedmap;
	
	public int def_minclock = 0;
	public int def_maxclock = 0;
	public String def_governor = "";
	public String def_scheduler = "";
	public String mydir = "";
	public Boolean flg_update = true;
	
	public Boolean is_clock = false;
	public Boolean is_vdd = false;
	public Boolean is_zram = false;
	public int def_swappiness = 0;
	private NativeCmd nCmd = NativeCmd.getInstance();
	
	private static KernelUtils instance = new KernelUtils();
	
	private KernelUtils()
	{
		if ( !nCmd.fileExists(scheduler) ) {
			scheduler = "/sys/block/mmcblk0/queue/scheduler";
		}
		if ( !nCmd.fileExists(vdd_levels) ) {
			vdd_levels = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";
		}
		reload();
	}
	
	public static KernelUtils getInstance()
	{
		return instance;
	}
	
	public void reload()
	{
		if ( flg_update ) {
			read_clock();
			read_vdd();
			read_scheduler();
			readDefaults();
			flg_update = false;
		}
	}
	
	private void readDefaults()
	{
		if ( is_clock ) {
			def_minclock = Integer.parseInt(nCmd.getCat(scaling_min_freq));
			def_maxclock = Integer.parseInt(nCmd.getCat(scaling_max_freq));
		}
		
		if (nCmd.fileExists(scaling_governor)) {
			def_governor = nCmd.readFile(scaling_governor);
		}
	}
	
	public String getClock()
	{
		readDefaults();
		String clockrate = "";
		if ( is_clock ) {
			clockrate = clockmap.get(def_minclock) + " - " + clockmap.get(def_maxclock);
		}
		return clockrate;
	}

	public String getGovernor()
	{
		return def_governor;
	}
	
	public String getScheduler()
	{
		return def_scheduler;
	}

	public String getSwappiness()
	{
		String swap = "";
		if (nCmd.fileExists(swappiness)) {
			swap = nCmd.readFile(swappiness);
			def_swappiness = Integer.parseInt(swap);
		}
		return swap;
	}
	
	public String[] getGovernorsList()
	{
		String str = nCmd.readFile(scaling_available_governors);
		return str.split(" ");
	}
	
	public String getZramsize()
	{
		String strsize = "";
		if (nCmd.fileExists(disksize)) {
			int size = Integer.parseInt(nCmd.readFile(disksize));
			strsize = String.valueOf(size / 1024 /1024) + "MB";
			is_zram = true;
		}
		return strsize;
	}
		
	public int getZramSettingSize()
	{
		int size = 0;
		if (nCmd.fileExists(zramsize)) {
			size = Integer.parseInt(nCmd.readFile(zramsize))  / 1024 /1024;
		}
		return size;
	}
	
	public ArrayList<String> getAllBlockDevice()
	{
		String path = "/sys/block";
		File dir = new File(path);
		File[] files = dir.listFiles();
		ArrayList<String> blocks = new ArrayList<String>();

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if ( file.toString().indexOf("/sys/block/mtd") == 0 || 
					file.toString().indexOf("/sys/block/mmc") == 0 || 
					file.toString().indexOf("/sys/block/stheno") == 0) {
						blocks.add(file.toString() + "/queue/scheduler");
			}
		}
	
		return blocks;
	}
	
	public int getMaxClock()
	{
		return Integer.parseInt(nCmd.readFile(cpuinfo_max_freq));
	}
	
	public int getMinClock()
	{
		return Integer.parseInt(nCmd.readFile(cpuinfo_min_freq));
	}
	
	private void read_clock()
	{
		if (nCmd.fileExists(scaling_available_frequencies)) {
			is_clock = true;
			String str = nCmd.readFile(scaling_available_frequencies);
			String[] freqmap = str.split(" ");
			for ( int i = 0; i < freqmap.length; i++ ) {
				int s = Integer.parseInt(freqmap[i].trim());
				String s1 = String.valueOf(s / 1000) + "MHz";
				clockmap.put(s, s1);
			}
		}
	}
	
	private void read_vdd()
	{
		if (nCmd.fileExists(vdd_levels)) {
		 	try {
		 		FileReader fr = new FileReader(new File(vdd_levels));
		 		BufferedReader br = new BufferedReader(fr);
		 		String str;
		 		int i = 0;
		 		if ( is_clock == false ) {
		 			is_clock = true;
			 		while ((str = br.readLine()) != null) {
			 			String[] cl = str.split(":");
			 			int s = Integer.parseInt(cl[0].trim()); 
			 			String s1 = String.valueOf(s / 1000) + "MHz";
			 			clockmap.put(s, s1);
			 				
			 			vddmap.put(i , str);
			 			i++;
			 		}
			 	} else {
			 		while ((str = br.readLine()) != null) {
			 			vddmap.put(i , str);
			 			i++;
			 		}
			 	}
		 		
		 		br.close();
		 		fr.close();
		 		is_vdd = true;
		 	} catch (FileNotFoundException e) {
		 		Log.e("ISTweak", e.toString());
		 	} catch (IOException e) {
		 		Log.e("ISTweak", e.toString());
		 	}
		}
	}
	
	private void read_scheduler()
	{
		if ( nCmd.fileExists(scheduler) ) {
			String str = nCmd.readFile(scheduler);
			schedmap = str.split(" ");
	 		for ( int i = 0; i < schedmap.length; i++ ) {
	 			if ( schedmap[i].substring(0, 1).equals(new String("[")) ) {
	 				schedmap[i] = schedmap[i].replace("[", "").replace("]", "");
	 				def_scheduler = schedmap[i];
	 			}
	 		}
		}
	}
}
