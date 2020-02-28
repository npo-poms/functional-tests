package nl.specialisterren.fitnesse.fixture.slim;

import java.util.Calendar;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendarFixture {
	public String decreaseTodayWithMonthsWithFormatWithLocal(String months, String format, String local) throws ParseException {
        Calendar cal = Calendar.getInstance();
        Locale loc = new Locale(local);
        Integer intMonths = Integer.valueOf(months);
        cal.add(Calendar.MONTH, -intMonths);
		cal.add(Calendar.DAY_OF_YEAR, 1);
        return new SimpleDateFormat(format,loc).format(cal.getTime());
    }
	
	public String increaseDateWithHours(String datetime, int hours) {
		String regex = "(?<day>\\d{2})-(?<month>\\d{2})-(?<year>\\d{4}) (?<hour>\\d{2}):(?<minute>\\d{2})";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(datetime);

		if (!m.find())
			return "";
		
		int day = Integer.valueOf(m.group("day"));
		int month = Integer.valueOf(m.group("month"));
		int year = Integer.valueOf(m.group("year"));
		int hour = Integer.valueOf(m.group("hour"));
		int minute = Integer.valueOf(m.group("minute"));
		
        Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, day, hour, minute);
		cal.add(Calendar.HOUR, hours);

		return new SimpleDateFormat("dd-MM-yyyy HH:mm", new Locale("nl")).format(cal.getTime());
	}
}
