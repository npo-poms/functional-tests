package nl.specialisterren.fitnesse.fixture.slim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CalendarFixture {
	private Calendar stringToCalendar(String date, SimpleDateFormat sdf) {
		Calendar c = Calendar.getInstance();
		
		try {
			c.setTime(sdf.parse(date));
		} catch(ParseException e) {
			e.printStackTrace();
		}
		
		return c;
	}
	
	public String decreaseDateByOneDayIfMarchOnALeapYear(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Calendar c = stringToCalendar(date, sdf);   
		int month = c.get(Calendar.MONTH);
		int year = c.get(Calendar.YEAR);
		if (year % 4 == 0 && month == 3)
		    c.add(Calendar.DAY_OF_YEAR, -1);
		
		return sdf.format(c.getTime());  
	}
	
	public String increaseDateWithHours(String datetime, int hours) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Calendar c = stringToCalendar(datetime, sdf);
		c.add(Calendar.HOUR, hours);
		
		return sdf.format(c.getTime());  
	}

	public String increaseDateWithMinutes(String datetime, int minutes) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Calendar c = stringToCalendar(datetime, sdf);   
		c.add(Calendar.MINUTE, minutes);
		
		return sdf.format(c.getTime());  
	}
}
