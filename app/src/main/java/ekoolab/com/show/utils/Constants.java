package ekoolab.com.show.utils;

import android.os.Environment;

import java.io.File;

public class Constants {
    public static final int PAGE_SIZE = 20;
    public static final String SBD_APP_ID = "9FFC187F-1F31-46B3-A77D-BB96008A8EED";
    public static final String SBD_TOKEN = "ea52b46dfb6f05f404fc15735557b321c85efd6d";

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
    public static final String My_FOllOWER =  HOST + "follows/followerlist";
    public static final String My_FOllOWING =  HOST + "follows/followinglist";
    public static final String IS_FOLLOWED = HOST + "follows/isfollow";
    public static final String FRIENDS = HOST + "user/getfriends";
    public static final String UPLOAD_CONTACT_BOOK = HOST + "user/addressbook";
    public static final String COMMENT = HOST + "comments/comment";
    public static final String COMMENTDEL = HOST + "comments/delete";
    public static final String COMMENTLIST = HOST + "comments/listall";
    public static final String MOMENTLIST = HOST + "moment/listall";
    public static final String MOMENT_SENDGIFT = HOST + "moment/sendgift";
    public static final String LIVE_LIST = HOST + "broadcast/getlist";
    public static final String GIFTLIST = HOST + "gift/getGifts";
    public static final String TextPost = HOST + "moment/publish";

    public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "AndroidShow" + File.separator;
    public static final String VIDEO_PATH = ROOT_PATH + "videos" + File.separator;
    public static final String IMAGE_PATH = ROOT_PATH + "images" + File.separator;
    public static final String IMAGE_CACHE_PATH = ROOT_PATH + "imageCache" + File.separator;

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
        public static final String NICKNAME = "nickname";
        public static final String ROLE = "role";

        public static final String FB_ACCESS_TOKEN = "fb_access_token";
        public static final String FB_USER_ID = "fb_user_id";
        public static final String WX_ACCESS_TOKEN = "wx_access_token";
        public static final String WX_UNION_ID = "wx_union_id";
    }


    public static final String FRIEND_TB = "friend";
    public static final class FriendTableColumns {
        public final static String userId = "user_id";
        public final static String name = "name";
        public final static String nickname= "nickname";
        public final static String channelUrl = "channel_url";
        public final static String countryCode = "country_code";
        public final static String mobile = "mobile";
        public final static String isAppUser = "is_app_user";
        public final static String isMyFollowing = "is_my_following";
        public final static String isMyFollower = "is_my_follower";
    }

    public static final String PHOTO_TB = "photo";
    public static final class PhotoTableColumns{
        public final static String userId = "user_id";
        public final static String messageId = "message_id";
        public final static String smallUrl = "small_url";
        public final static String mediumUrl = "medium_url";
        public final static String originUrl = "origin_url";
    }

}
