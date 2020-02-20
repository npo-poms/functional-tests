package nl.specialisterren.fitnesse.fixture.slim;

import java.util.Calendar;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class CalendarFixture {
	public String decreaseTodayWithMonthsWithFormatWithLocal(String months, String format, String local) throws ParseException {
        Calendar cal = Calendar.getInstance();
        Locale loc = new Locale(local);
        Integer intMonths = Integer.valueOf(months);
        cal.add(Calendar.MONTH, -intMonths);
		cal.add(Calendar.DAY_OF_YEAR, 1);
        return new SimpleDateFormat(format,loc).format(cal.getTime());
    }
}
