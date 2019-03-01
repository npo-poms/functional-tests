package nl.vpro.poms.selenium.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFactory {
	private static final Format sdf = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	private static final Format todayFormat =
			new SimpleDateFormat("dd-MM-yyyy");
	
	public static String getNow() {
		Date now = Calendar.getInstance().getTime();
		return sdf.format(now);
	}
	
	public static String getToday() {
		Date today = Calendar.getInstance().getTime();
		return todayFormat.format(today);
	}
}
