package nl.specialisterren.fitnesse.fixture.slim;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendedStringFixture {
	public String uppercaseFirst(String value) {
        char[] array = value.toCharArray();
        array[0] = Character.toUpperCase(array[0]);
        return new String(array);
    }
	
	public String extractStringFromUsingGroup(String value, String regEx, int groupIndex) {
        String result = null;
        if (value != null) {
            Matcher matcher = getMatcher(regEx, value);
            if (matcher.matches()) {
                result = matcher.group(groupIndex);
            }
        }
        return result;
    }
	
	protected Matcher getMatcher(String regEx, String value) {
        return Pattern.compile(regEx, Pattern.DOTALL).matcher(value);
    }

    public boolean stringStartsWith(String haystack, String needle) {
	    return haystack.startsWith(needle);
    }
	
	public String convertToTimestamp(String timestamp) {
		String regEx = "(\\d{2}):(\\d{2}):(\\d{2}).(\\d{3})";
		
		int dt = Integer.parseInt(extractStringFromUsingGroup(timestamp, regEx, 1));
		int h = Integer.parseInt(extractStringFromUsingGroup(timestamp, regEx, 2));
		int m = Integer.parseInt(extractStringFromUsingGroup(timestamp, regEx, 3));
		String s = extractStringFromUsingGroup(timestamp, regEx, 4);
		
		return String.format("P0DT%sH%sM%s.%sS", dt, h, m, s);
	}
	
	public boolean valueIsLessThan(String value1, String value2) {
		return (value1.compareTo(value2) < 0);
	}
	
	public boolean valueIsGreaterThan(String value1, String value2) {
		return (value1.compareTo(value2) > 0);
	}
	
	public String trim(String value) {
		return value.trim();
	}
}
