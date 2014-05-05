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
	private static Pattern datePattern = Pattern.compile(".*" + date + "(?:" +  dateTimeDelimiter + time + ")?" + ".*");

	/** Returns date or null if can't parse the given string. */
	public static Date parseDate(final String givenString) {

		String dateString = preprocessDateString(givenString);
		//System.out.println(dateString);
		Date date = null;

		final Matcher m = datePattern.matcher(dateString);

		if (m.matches()) {
			int matchedGroups = 0;
			for (int i = 1; i <= m.groupCount(); i++) {
				if (m.group(i) != null)
					matchedGroups++;
			}
			Calendar cal = null;
			//System.out.println(matchedGroups);
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
		//else System.out.println("no match");

		return date;
	}

	private static String preprocessDateString(final String given) {

		//TODO: use StringBuilder
		String processed = given;

		final Calendar today = new GregorianCalendar();
		final Calendar yesterday = new GregorianCalendar();
		yesterday.add(Calendar.DATE, -1);

		processed = processed.toLowerCase();
		processed = processed.replaceFirst("dnes", today.get(Calendar.DATE) + "." + (today.get(Calendar.MONTH)+1) + "." + today.get(Calendar.YEAR));
		processed = processed.replaceFirst("včera", yesterday.get(Calendar.DATE) + "." + (yesterday.get(Calendar.MONTH)+1) + "." + yesterday.get(Calendar.YEAR));
		processed = processed.replaceAll("januára?", "1.");
		processed = processed.replaceAll("februára?", "2.");
		processed = processed.replaceAll("mare?ca?", "3.");
		processed = processed.replaceAll("apríla?", "4.");
		processed = processed.replaceAll("mája?", "5.");
		processed = processed.replaceAll("júna?", "6.");
		processed = processed.replaceAll("júla?", "7.");
		processed = processed.replaceAll("augusta?", "8.");
		processed = processed.replaceAll("septembe?ra?", "9.");
		processed = processed.replaceAll("októbe?ra?", "10.");
		processed = processed.replaceAll("novembe?ra?", "11.");
		processed = processed.replaceAll("decembe?ra?", "12.");

		return processed;
	}

	public static void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
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

		System.out.println(parseDate("dnes 17:18"));
		System.out.println(parseDate("5.5.2014 11:34"));
		System.out.println(parseDate("Autor: SITA, dnes 11:34, aktualizované: dnes 22:15"));
		System.out.println(parseDate("Autor: SITA, včera 22:15"));
		System.out.println(parseDate("Autor: SITA, 22. február 2014 12:12"));
		System.out.println(parseDate("Autor: SITA, 22. Februára 2015 12:12"));
		System.out.println(parseDate("Autor: SITA, 22. November 2016 12:12"));
		System.out.println(parseDate("Autor: SITA, 22. novembra 2017 12:12"));
	}

}
