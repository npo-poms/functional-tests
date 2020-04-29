package nl.specialisterren.fitnesse.fixture.slim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// TODO useless use of  boxing
public class CalendarFixture {
	public String decreaseTodayWithMonthsWithFormatWithLocal(String months, String format, String local) throws ParseException {
        Calendar cal = Calendar.getInstance();
        Locale loc = new Locale(local);

        Integer intMonths = Integer.valueOf(months);
        cal.add(Calendar.DAY_OF_YEAR, -intMonths*30);

		int currentMonth = cal.get(Calendar.MONTH);
		int currentYear = cal.get(Calendar.YEAR);
		if (currentYear % 4 == 0 && currentMonth == 3)
		    cal.add(Calendar.DAY_OF_YEAR, -1);

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

	public String increaseDateWithMinutes(String datetime, int minutes) {
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
		cal.add(Calendar.MINUTE, minutes);

		return new SimpleDateFormat("dd-MM-yyyy HH:mm", new Locale("nl")).format(cal.getTime());
	}
}
