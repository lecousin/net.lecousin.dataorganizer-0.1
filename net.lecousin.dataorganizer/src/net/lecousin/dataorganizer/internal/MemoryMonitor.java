package net.lecousin.dataorganizer.internal;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.strings.StringUtil;

public class MemoryMonitor implements Runnable {

	private static final int GCTimer = 15*60*1000;
	
	private long lastGC = 0;
	
	public void run() {
		if (System.currentTimeMillis() - lastGC > GCTimer) {
			Log.info(this, "Memory monitor: Garbage collection");
			Runtime.getRuntime().gc();
			lastGC = System.currentTimeMillis();
		}
		StringBuilder str = new StringBuilder();
		long max = Runtime.getRuntime().maxMemory();
		long cur = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		str.append("Memory monitor: ")
			.append("max=").append(StringUtil.sizeString(max))
			.append(", current=").append(StringUtil.sizeString(cur)).append(" (").append(StringUtil.percent(cur, max, 2)).append("%)")
			.append(", free=").append(StringUtil.sizeString(free)).append(" (").append(StringUtil.percent(free, cur, 2)).append("%)")
			;
		Log.info(this, str.toString());
	}
	
}
