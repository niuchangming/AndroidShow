package ekoolab.com.show.utils;

import android.os.Environment;

import java.io.File;

public class Constants {
    public static final String HOST = "http://api.ccmsshow.com:8081/api/";
    public static final String LOGIN = HOST + "user/login";
    public static final String SIGNUP = HOST + "user/signup";
    public static final String VERIFY_2FA = HOST + "user/2fa";
    public static final String GET_USERPROFILE = HOST + "user/getUserprofile";

    public static final String VIDEO_LIST = HOST + "video/listall";
    public static final String FAVOURITE = HOST + "favourites/favourite";
    public static final String FAVOURITECANEL = HOST + "favourites/cancel";
    public static final String UPLOAD_VIDEO = HOST + "video/upload";

    public static final String LIKE = HOST + "likes/like";
    public static final String UNLIKE = HOST + "likes/unlike";

    public static final String FOLLOW = HOST + "follows/add";
    public static final String FOLLOWCANCEL = HOST + "follows/cancel";

    public static final String COMMENT = HOST + "comments/comment";
    public static final String COMMENTDEL = HOST + "comments/delete";
    public static final String COMMENTLIST = HOST + "comments/listall";

    public static final String MOMENTLIST = HOST + "moment/listall";
    public static final String MOMENT_SENDGIFT = HOST + "moment/sendgift";

    public static final String GIFTLIST = HOST + "gift/getGifts";

    public static final String TextPost = HOST + "moment/publish";

    public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "AndroidShow" + File.separator;

    public static final String VIDEO_PATH = ROOT_PATH + "videos" + File.separator;
    public static final String IMAGE_PATH = ROOT_PATH + "images" + File.separator;
    public static final String IMAGE_CACHE_PATH = ROOT_PATH + "imageCache" + File.separator;

    public static final int PAGE_SIZE = 20;


    public static final String[] tabBarTitles = {"Home", "ZSC", "e-Mart", "Profile"};
    public static final String[] homeIndicatorTitles = {"Moment", "Video", "Live"};
    public static final String[] profileIndicatorTitles = {"MyVideos", "MyCollects", "MyMoments"};




    public static final class Auth {
        public static final String LOGGED_IN = "logged_in";
        public static final String API_TOKEN = "api_token";
        public static final String LOGIN_TYPE = "logged_type";
        public static final String DIAL_NO = "dial_no";
        public static final String MOBILE = "logged_mobile";
        public static final String USER_CODE = "user_code";
        public static final String USERNAME = "user_name";
        public static final String GENDER = "gender";
        public static final String ROLE = "role";
        public static final String AVATAR_MEDIUM = "avatar_medium";
        public static final String AVATAR_ORIGIN = "avatar_origin";
        public static final String AVATAR_SMALL = "avatar_small";
        public static final String CHANNEL_ID = "channel_id";
        public static final String FB_TOKEN_EXPIRED = "fb_expired";
        public static final String FB_ACCESS_TOKEN = "fb_access_token";
        public static final String FB_USER_ID = "fb_user_id";
        public static final String WX_TOKEN_EXPIRED = "wx_expired";
        public static final String WX_ACCESS_TOKEN = "wx_access_token";
        public static final String WX_UNION_ID = "wx_union_id";

        //new for profile
        public static final String NICKNAME = "nickname";
        public static final String BIRTHDAY = "birthday";
        public static final String FOLLOWERS = "followers";
        public static final String FOLLOWING = "following";
        public static final String REGION = "region";
        public static final String WHATSUP = "whatsup";
        public static final String CATEGORY = "category";
        public static final String DESCRIPTION = "description";
    }
}
