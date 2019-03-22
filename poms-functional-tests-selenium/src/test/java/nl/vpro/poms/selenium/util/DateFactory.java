package nl.vpro.poms.selenium.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFactory {
	private static final Format SDF =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	private static final Format TODAY_FORMAT =
			new SimpleDateFormat("dd-MM-yyyy");
	
	public static String getNow() {
		Date now = Calendar.getInstance().getTime();
		return SDF.format(now);
	}
	
	public static String getToday() {
		Date today = Calendar.getInstance().getTime();
		return TODAY_FORMAT.format(today);
	}
}
