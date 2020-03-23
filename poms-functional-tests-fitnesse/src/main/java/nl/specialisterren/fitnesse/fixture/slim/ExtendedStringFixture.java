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
}
