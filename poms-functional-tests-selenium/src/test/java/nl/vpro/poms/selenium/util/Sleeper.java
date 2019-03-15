package nl.vpro.poms.selenium.util;

public class Sleeper {

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {} // Do nothing
	}
}
