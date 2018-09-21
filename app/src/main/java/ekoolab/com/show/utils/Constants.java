package ekoolab.com.show.utils;

public class Constants {
    public static final String HOST = "http://ec2-34-220-129-171.us-west-2.compute.amazonaws.com:8081/api/";
    public static final String LOGIN = HOST + "user/login";
    public static final String SIGNUP = HOST + "user/signup";
    public static final String VERIFY_2FA = HOST + "user/2fa";
    public static final String VIDEO_LIST = HOST + "video/listall";

    public static final int PAGE_SIZE = 20;


    public static final String[] tabBarTitles = {"Home", "ZSC", "e-Mart", "Profile"};
    public static final String[] homeIndicatorTitles = {"Video", "Live", "Moment"};
    public static final String[] profileIndicatorTitles = {"MyVideos", "MyCollects", "MyMoments"};




    public static final class Auth {
        public static final String LOGGED_IN = "logged_in";
        public static final String API_TOKEN = "api_token";
        public static final String LOGIN_TYPE = "logged_type";
        public static final String DIAL_NO = "dial_no";
        public static final String MOBILE = "logged_mobile";
        public static final String USER_CODE = "user_code";
        public static final String ROLE = "role";
        public static final String CHANNEL_ID = "channel_id";
        public static final String FB_TOKEN_EXPIRED = "fb_expired";
        public static final String FB_ACCESS_TOKEN = "fb_access_token";
        public static final String FB_USER_ID = "fb_user_id";
        public static final String WX_TOKEN_EXPIRED = "wx_expired";
        public static final String WX_ACCESS_TOKEN = "wx_access_token";
        public static final String WX_UNION_ID = "wx_union_id";
    }
}