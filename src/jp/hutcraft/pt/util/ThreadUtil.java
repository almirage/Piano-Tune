package jp.hutcraft.pt.util;

public class ThreadUtil {
	public static void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
}
