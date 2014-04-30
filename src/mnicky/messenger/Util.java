package mnicky.messenger;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	private static String dateDelimiter = "[\\.\\-/ ]{1,3}";
	private static String timeDelimiter = "[\\.:]";
	private static String dateTimeDelimiter = "[ ]";
	private static String day = "(\\d{1,2})";
	private static String month = "(\\d{1,2})";
	private static String year = "(\\d{4})";
	private static String hours = "(\\d{1,2})";
	private static String minutes = "(\\d{2})";
	private static String seconds = "(\\d{2})";
	private static String date = "(?:" + day + dateDelimiter + month + dateDelimiter + year + ")";
	private static String time = "(?:" + hours + timeDelimiter + minutes + "(?:" +  timeDelimiter + seconds + ")?" + ")";
	private static Pattern datePattern = Pattern.compile(date + "(?:" +  dateTimeDelimiter + time + ")?");

	/** Returns date or null if can't parse the given string. */
	public static Date parseDate(final String dateString) {
		Date date = null;

		final Matcher m = datePattern.matcher(dateString);

		if (m.matches()) {
			int matchedGroups = 0;
			for (int i = 1; i <= m.groupCount(); i++) {
				if (m.group(i) != null)
					matchedGroups++;
			}
			Calendar cal = null;
			if (matchedGroups == 6) {
				cal = new GregorianCalendar(Integer.parseInt(m.group(3)),
											Integer.parseInt(m.group(2))-1,
											Integer.parseInt(m.group(1)),
											Integer.parseInt(m.group(4)),
											Integer.parseInt(m.group(5)),
											Integer.parseInt(m.group(6)));
			}
			else if (matchedGroups == 5) {
				cal = new GregorianCalendar(Integer.parseInt(m.group(3)),
											Integer.parseInt(m.group(2))-1,
											Integer.parseInt(m.group(1)),
											Integer.parseInt(m.group(4)),
											Integer.parseInt(m.group(5)));
			}
			else if (matchedGroups == 3) {
				cal = new GregorianCalendar(Integer.parseInt(m.group(3)),
											Integer.parseInt(m.group(2))-1,
											Integer.parseInt(m.group(1)));
			}
			if (cal != null)
				date = cal.getTime();
		}
		
		return date;
	}
	
	//just for tests
	public static void main(String[] args) {
		System.out.println(parseDate("28. 4. 2014 17:18:19"));
		System.out.println(parseDate("28. 4. 2014 17:18"));
		System.out.println(parseDate("28. 4. 2014"));

		System.out.println(parseDate("28-04-2014 17:18:19"));
		System.out.println(parseDate("28/04/2014 17.18"));
		System.out.println(parseDate("28-04-2014"));

		System.out.println(parseDate("28.4.2014 17.18.19"));
		System.out.println(parseDate("28 4 2014 5:18"));
		System.out.println(parseDate("28/4/2014"));

		System.out.println(parseDate("28.4.2014 07.08.09"));
		System.out.println(parseDate("28 4 2014 07:08"));
		System.out.println(parseDate("28 / 4 / 2014"));
	}
	
}
