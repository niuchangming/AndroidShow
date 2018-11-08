package ekoolab.com.show.utils;

import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import com.auth0.jwt.JWTSigner;

public class Utils {

    public static boolean isBlank(final String str) {
        if (null == str)
            return true;
        if (str.isEmpty())
            return true;

        return str.trim().isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }


    public static String generateUniqueCode(Context context){
        String code = "";
        String uuid = UUID.randomUUID().toString();
        String[] strBlk = uuid.split("-");
        if(strBlk.length == 5) {
            code = strBlk[strBlk.length-1];
        }else{
            code = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        return code;
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

    public static String getDisplayName(String name, String nickname) {
        String displayName = nickname;
        if (Utils.isBlank(displayName)) {
            displayName = name;
        }

        return Utils.isBlank(displayName) ? "" : displayName;
    }

    public static String formatMobile(String mobile, String region){
        if (equals(region, "CN") && mobile.length() > 11){
            mobile = mobile.substring(mobile.length() - 11);
        } else if(equals(region, "SG") && mobile.length() > 8){
            mobile = mobile.substring(mobile.length() - 8);
        }

        return mobile;
    }

    public static String getJWTString(int min){
        final long iat = System.currentTimeMillis() / 1000L - 180;
        final long exp = iat + min * 60L;

        JWTSigner signer = new JWTSigner(Constants.TOKBOX_APP_SECRET);
        final HashMap<String, Object> claims = new HashMap<String, Object>();
        claims.put("iss", Constants.TOKBOX_APP_ID);
        claims.put("exp", exp);
        claims.put("iat", iat);
        claims.put("ist", "project");
        claims.put("jti", UUID.randomUUID().toString());

        return signer.sign(claims);
    }
}
