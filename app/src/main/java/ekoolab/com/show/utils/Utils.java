package ekoolab.com.show.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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

    public static String outputError(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();

        if (cause != null) {
            cause.printStackTrace(printWriter);
        }
        return writer.toString();
    }

    public static boolean containInt(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * 显示软键盘
     */
    public static void showInput(EditText et_msg) {
        try {
            et_msg.setFocusable(true);
            et_msg.setFocusableInTouchMode(true);
            et_msg.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) et_msg.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(et_msg, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏软键盘
     */
    public static void hideInput(EditText et_msg) {
        try {
            InputMethodManager imm = (InputMethodManager) et_msg.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_msg.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean equals(String str1, String str2) {
        return str1 != null && str1.equals(str2);
    }
}
