package ekoolab.com.show.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

import ekoolab.com.show.beans.AuthInfo;

public class AuthUtils {

    private static AuthUtils instance;
    private Context context;

    public static AuthUtils getInstance(Context context){
        if(instance == null){
            synchronized (AuthUtils.class) {
                if(instance == null){
                    instance = new AuthUtils();
                }
            }
        }
        instance.context = context;
        return instance;
    }

    public AuthType loginState(){
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        boolean isLogged = sp.getBoolean(Constants.Auth.LOGGED_IN, false);
        String loggedType = sp.getString(Constants.Auth.LOGIN_TYPE, "mobile");

        AuthType type = AuthType.UN_AUTH;
        if(loggedType == "facebook"){
            Date expireDate = new Date(sp.getLong(Constants.Auth.FB_TOKEN_EXPIRED, 0));
            boolean isExpired = new Date().after(expireDate);
            if(isExpired){
                type = AuthType.EXPIRED;
            }else{
                type = AuthType.LOGGED;
            }
        }else if(loggedType == "wechat"){
            Date expireDate = new Date(sp.getLong(Constants.Auth.WX_TOKEN_EXPIRED, 0));
            boolean isExpired = new Date().after(expireDate);
            if(isExpired){
                type = AuthType.EXPIRED;
            }else{
                type = AuthType.LOGGED;
            }
        }else{
            if(isLogged){
                type = AuthType.LOGGED;
            }
        }

        return type;
    }


    public String getApiToken(){
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sp.getString(Constants.Auth.API_TOKEN, "");
    }

    public String getUserCode(){
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sp.getString(Constants.Auth.USER_CODE, "");
    }

    public String getName(){
        SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sp.getString(Constants.Auth.USERNAME, "");
    }

    public void saveAuthInfo(AuthInfo info){
        SharedPreferences.Editor spEditor = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit();
        spEditor.putBoolean(Constants.Auth.LOGGED_IN, true);
        spEditor.putString(Constants.Auth.API_TOKEN, info.getApiToken());
        spEditor.putString(Constants.Auth.USER_CODE, info.getUserCode());
        spEditor.putString(Constants.Auth.USERNAME, info.getName());
        spEditor.putInt(Constants.Auth.ROLE, info.getRole());
        spEditor.putString(Constants.Auth.LOGIN_TYPE, info.getAccountType());

        if(info.getAccountType().equalsIgnoreCase("facebook")){
            spEditor.putString(Constants.Auth.FB_ACCESS_TOKEN, info.getFbAccessToken());
            spEditor.putLong(Constants.Auth.FB_TOKEN_EXPIRED, info.getFbExpiredDate().getTime());
            spEditor.putString(Constants.Auth.FB_USER_ID, info.getFbUserId());
        }else if(info.getAccountType().equalsIgnoreCase("wechat")){
            spEditor.putString(Constants.Auth.WX_ACCESS_TOKEN, info.getWxAccessToken());
            spEditor.putLong(Constants.Auth.WX_TOKEN_EXPIRED, info.getWxExpiredDate().getTime());
            spEditor.putString(Constants.Auth.WX_UNION_ID, info.getWxUnionId());
        }else{
            spEditor.putString(Constants.Auth.MOBILE, info.getMobile());
            spEditor.putString(Constants.Auth.DIAL_NO, info.getDialNo());
        }

        spEditor.apply();
    }

    public enum AuthType {
        UN_AUTH,
        LOGGED,
        EXPIRED
    }

}
