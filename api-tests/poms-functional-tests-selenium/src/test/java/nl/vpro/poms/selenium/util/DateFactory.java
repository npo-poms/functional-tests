package nl.vpro.poms.selenium.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class DateFactory {
	private static final Format SDF =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static final String TODAY_DATE_FORMAT = "dd-MM-yyyy";
	private static final Format TODAY_FORMAT =
			new SimpleDateFormat(TODAY_DATE_FORMAT);


	public static String getNow() {
		Date now = Calendar.getInstance().getTime();
		return SDF.format(now);
	}

	public static String getToday() {
		Date today = Calendar.getInstance().getTime();
		return TODAY_FORMAT.format(today);
	}

}
