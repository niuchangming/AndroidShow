package ekoolab.com.show.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.luck.picture.lib.tools.Constant;

import ekoolab.com.show.beans.AuthInfo;
import ekoolab.com.show.beans.LoginData;

public class AuthUtils {
    public static final int ORIGIN = 0;
    public static final int MEDIUM = 1;
    public static final int SMALL = 2;
    public static final String LOGGED_IN = "ekoolab.com.show.logged.in";

    private static volatile AuthUtils instance;
    private Context context;

    public static AuthUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (AuthUtils.class) {
                if (instance == null) {
                    instance = new AuthUtils();
                }
            }
        }
        instance.context = context.getApplicationContext();
        return instance;
    }

    public AuthType loginState() {
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        boolean isLogged = sp.getBoolean(Constants.Auth.LOGGED_IN, false);

        AuthType type = AuthType.UN_AUTH;
        if (isLogged) {
            type = AuthType.LOGGED;
        }

        return type;
    }

    public String getApiToken() {
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sp.getString(Constants.Auth.API_TOKEN, "");
    }

    public String getUserCode() {
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sp.getString(Constants.Auth.USER_CODE, "");
    }

    public String getName() {
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sp.getString(Constants.Auth.USERNAME, "");
    }

    public String getTempUserId(){
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String tempUserId = sp.getString(Constants.Auth.TEMP_USER_ID, "");

        if (Utils.isBlank(tempUserId)) {
            tempUserId = Utils.generateUniqueCode(context);

            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putString(Constants.Auth.TEMP_USER_ID, tempUserId);
        }

        return tempUserId;
    }

    public void saveLoginInfo(LoginData data) {
        SharedPreferences.Editor spEditor = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit();
        spEditor.putBoolean(Constants.Auth.LOGGED_IN, true);
        spEditor.putString(Constants.Auth.API_TOKEN, data.token);
        spEditor.putString(Constants.Auth.USER_CODE, data.userCode);
        spEditor.putString(Constants.Auth.USERNAME, data.name);
        spEditor.putString(Constants.Auth.NICKNAME, data.nickName);
        spEditor.putInt(Constants.Auth.ROLE, data.roleType);
        spEditor.putString(Constants.Auth.LOGIN_TYPE, data.accountType);
        spEditor.putString(Constants.Auth.MOBILE, data.mobile);
        spEditor.putInt(Constants.Auth.DIAL_NO, data.countryCode);
        spEditor.apply();
    }

    public void logout(){
        SharedPreferences.Editor spEditor = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit();
        spEditor.clear();
        spEditor.apply();
    }

    public enum AuthType {
        UN_AUTH,
        LOGGED
    }

}
