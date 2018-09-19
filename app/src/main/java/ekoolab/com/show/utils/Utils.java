package ekoolab.com.show.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static boolean isBlank(final String str) {
        if (null == str)
            return true;
        if (str.isEmpty())
            return true;

        return str.trim().isEmpty();
    }

    public static Date getDateByMillis(long millis) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return cal.getTime();
    }
}
