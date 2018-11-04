package ekoolab.com.show.utils;

import android.os.Build;
import android.os.Environment;

import com.faceunity.utils.MiscUtil;

import java.io.File;
import java.util.regex.Pattern;

public class Constants {
    public static final int PAGE_SIZE = 20;
    public static final int CHAT_LIMIT = 20;
    public static final String SBD_APP_ID = "9FFC187F-1F31-46B3-A77D-BB96008A8EED";
    public static final String SBD_TOKEN = "ea52b46dfb6f05f404fc15735557b321c85efd6d";
    public static final String TOKBOX_APP_ID = "46182032";
    public static final String TOKBOX_APP_SECRET = "60af838781cb8441b88f1434ed213f09a4b6a72d";
    public static final String WECHAT_APP_ID = "wxeda3982509da49ac";
    public static final String WECHAT_SECRET = "715b77b4345364d97659f34cb74a9d7b";

    public static final String HOST = "http://api.ccmsshow.com:8081/api/";
    public static final String LOGIN = HOST + "user/login";
    public static final String LOGOUT = HOST + "user/logout";
    public static final String SIGNUP = HOST + "user/signup";
    public static final String VERIFY_2FA = HOST + "user/2fa";
    public static final String GET_USERPROFILE = HOST + "user/getUserprofile";
    public static final String UPDATE_USERPROFILE = HOST + "user/updateUserprofile";
    public static final String UPDATE_BROADCASTPROFILE = HOST + "broadcast/infoUpdate";

    public static final String VIDEO_LIST = HOST + "video/listall";
    public static final String MY_VIDEO_LIST = HOST + "video/list";
    public static final String FAVOURITE = HOST + "favourites/favourite";
    public static final String FAVOURITECANEL = HOST + "favourites/cancel";
    public static final String MYFAVOURITELIST = HOST + "favourites/favlist";
    public static final String UPLOAD_VIDEO = HOST + "video/upload";
    public static final String UPLOAD_AUDIO = HOST + "chat/upload";

    public static final String LIKE = HOST + "likes/like";
    public static final String UNLIKE = HOST + "likes/unlike";
    public static final String FOLLOW = HOST + "follows/add";
    public static final String FOLLOWCANCEL = HOST + "follows/cancel";
    public static final String MY_FOllOWER =  HOST + "follows/followerlist";
    public static final String MY_FOllOWING =  HOST + "follows/followinglist";
    public static final String IS_FOLLOWED = HOST + "follows/isfollow";
    public static final String FRIENDS = HOST + "user/getfriends";
    public static final String UPLOAD_CONTACT_BOOK = HOST + "user/addressbook";
    public static final String COMMENT = HOST + "comments/comment";
    public static final String COMMENTDEL = HOST + "comments/delete";
    public static final String COMMENTLIST = HOST + "comments/listall";
    public static final String MOMENTLIST = HOST + "moment/listall";
    public static final String MYMOMENTLIST = HOST + "moment/list";
    public static final String MOMENTDEL = HOST + "moment/delete";
    public static final String MOMENT_SENDGIFT = HOST + "moment/sendgift";
    public static final String LIVE_LIST = HOST + "broadcast/getlist";
    public static final String GIFTLIST = HOST + "gift/getGifts";
    public static final String TextPost = HOST + "moment/publish";
    public static final String BROADCAST_INFO = HOST + "broadcast/session";
    public static final String UPLOAD_BROADCAST_INFO = HOST + "broadcast/broadcastId";
    public static final String UPLOAD_CHANNEL_URL = HOST + "broadcast/uploadChannelId";

    public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "AndroidShow" + File.separator;
    public static final String VIDEO_PATH = ROOT_PATH + "videos" + File.separator;
    public static final String IMAGE_PATH = ROOT_PATH + "images" + File.separator;
    public static final String AUDIO_PATH = ROOT_PATH + "audios" + File.separator;
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
        public static final String CHANNEL_URL = "channel_url";
        public static final String TEMP_USER_ID = "temp_user_id";
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

    public static final String CHAT_MESSAGE_TB = "chat_message";
    public static final class ChatMessageTableColumns{
        public final static String message = "message";
        public final static String channelUrl = "channel_url";
        public final static String senderId = "sender_id";
        public final static String senderName = "sender_name";
        public final static String senderProfileUrl = "send_profile_url";
        public final static String createAt = "create_at";
        public final static String updateAt = "update_at";
        public final static String messageId = "message_id";
        public final static String requestId = "request_id";
        public final static String messageType = "message_type";
        public final static String sendState = "send_state";
    }

    public static final String RESOURCE_FILE_TB = "resource_file";
    public static final class ResourceFileTableColumns{
        public final static String fileName = "filename";
        public final static String fileUrl = "url";
        public final static String filePath = "path";
        public final static String fileType = "type";
        public final static String chatMessageId = "chat_message_id";
        public final static String fileSize = "file_size";
        public final static String mimeType = "mime_type";
        public final static String extension = "extension";
        public final static String duration = "duration";
    }

}
