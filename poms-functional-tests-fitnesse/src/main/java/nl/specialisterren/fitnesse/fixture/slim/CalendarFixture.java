package nl.specialisterren.fitnesse.fixture.slim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;


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
	
	public String increaseDatetimeWithMilliSeconds(String datetime, int milliSeconds) {
		SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy H:mm");
		Calendar c = stringToCalendar(datetime, sdf);   
		c.add(Calendar.MILLISECOND, milliSeconds);
		
		return sdf.format(c.getTime());  
	}
	

	public long convertDatetimeToEpoch(String datetime) {
		LocalDateTime localDateTime = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("d-M-yyyy H:mm") );
		long millis = localDateTime.atZone(ZoneId.of("Europe/Amsterdam")).toInstant().toEpochMilli();
		
		return millis;
	}
	
	public String convertEpochToDatetime(long epoch) {
		Date date = new Date(epoch);
        SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy H:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
		
        return sdf.format(date);
	}
	
	public boolean epochEquals(long epoch, String datetime) {
		return convertDatetimeToEpoch(datetime) == epoch;
	}
	
	public boolean datetimeIsGreaterThanOrEqualTo(String datetime1, String datetime2) {
		SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy H:mm");
		Calendar c1 = stringToCalendar(datetime1, sdf);
		Calendar c2 = stringToCalendar(datetime2, sdf);
		
		return (c1.compareTo(c2) >= 0);
	}
	
	public boolean datetimeIsBetweenAnd(String datetime1, String datetime2, String datetime3) {
		SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy H:mm");
		Calendar c1 = stringToCalendar(datetime1, sdf);
		Calendar c2 = stringToCalendar(datetime2, sdf);
		Calendar c3 = stringToCalendar(datetime3, sdf);
		
		return (c1.compareTo(c2) >= 0 && c1.compareTo(c3) < 0);
	}
	
	public String convertDatetimeFromTo(String datetime, String format1, String format2) {
		SimpleDateFormat sdf1 = new SimpleDateFormat(format1);
		SimpleDateFormat sdf2 = new SimpleDateFormat(format2);
		Calendar c = stringToCalendar(datetime, sdf1);
		
		return sdf2.format(c.getTime()); 
	}
	
	public String convertDatetimeToIso(String datetime) {
		if (datetime.equals(""))
			return "";
		
		LocalDateTime localDateTime = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("d-M-yyyy H:mm"));
		ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/Amsterdam"));
		return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime);
	}
	
	public boolean isoEquals(String iso, String datetime) {		
		return convertDatetimeToIso(datetime).equals(iso);
	}
}
