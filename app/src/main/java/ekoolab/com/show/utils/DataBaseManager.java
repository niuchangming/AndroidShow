package ekoolab.com.show.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.luck.picture.lib.tools.Constant;

import java.util.concurrent.atomic.AtomicInteger;

public class DataBaseManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "show_db";
    private static final int DB_VERSION = 1;
    private static DataBaseManager instance;
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase;

    public static synchronized DataBaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataBaseManager(context.getApplicationContext());
        }
        return instance;
    }

    private DataBaseManager(Context context) {
        super(context, DB_NAME,null, DB_VERSION);
    }

    public synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            mDatabase = getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            mDatabase.close();
        }
    }

    String friendSql = "create table if not exists " + Constants.FRIEND_TB + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + Constants.FriendTableColumns.userId + " VARCHAR, "
            + Constants.FriendTableColumns.name + " VARCHAR(50), "
            + Constants.FriendTableColumns.nickname + " VARCHAR(50), "
            + Constants.FriendTableColumns.channelUrl + " TEXT, "
            + Constants.FriendTableColumns.countryCode + " VARCHAR(5), "
            + Constants.FriendTableColumns.mobile + " VARCHAR(20), "
            + Constants.FriendTableColumns.isAppUser + " BOOLEAN, "
            + Constants.FriendTableColumns.isMyFollowing + " BOOLEAN, "
            + Constants.FriendTableColumns.isMyFollower + " BOOLEAN, "
            + "UNIQUE(" + Constants.FriendTableColumns.mobile + "," + Constants.FriendTableColumns.userId + ") ON CONFLICT REPLACE);";


    String photoSql = "create table if not exists " + Constants.PHOTO_TB + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + Constants.PhotoTableColumns.userId + " VARCHAR UNIQUE ON CONFLICT REPLACE, "
            + Constants.PhotoTableColumns.messageId + " VARCHAR UNIQUE ON CONFLICT REPLACE, "
            + Constants.PhotoTableColumns.smallUrl + " VARCHAR, "
            + Constants.PhotoTableColumns.originUrl + " VARCHAR, "
            + Constants.PhotoTableColumns.mediumUrl + " VARCHAR);";

    String chatMessageSql = "create table if not exists " + Constants.CHAT_MESSAGE_TB + " ("+
            "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            Constants.ChatMessageTableColumns.messageId + " INTEGER, "
            + Constants.ChatMessageTableColumns.senderId + " VARCHAR, "
            + Constants.ChatMessageTableColumns.senderName+ " VARCHAR, "
            + Constants.ChatMessageTableColumns.senderProfileUrl+ " TEXT, "
            + Constants.ChatMessageTableColumns.message + " TEXT, "
            + Constants.ChatMessageTableColumns.channelUrl + " TEXT, "
            + Constants.ChatMessageTableColumns.createAt + " INTEGER, "
            + Constants.ChatMessageTableColumns.updateAt + " INTEGER, "
            + Constants.ChatMessageTableColumns.requestId + " VARCHAR, "
            + Constants.ChatMessageTableColumns.messageType + " INTEGER NOT NULL default 0, "
            + Constants.ChatMessageTableColumns.sendState + " INTEGER NOT NULL default 0, "
            + "UNIQUE(" + Constants.ChatMessageTableColumns.messageId + "," + Constants.ChatMessageTableColumns.requestId + ") ON CONFLICT REPLACE);";

    @Override
    public synchronized void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(friendSql);
        sqLiteDatabase.execSQL(photoSql);
        sqLiteDatabase.execSQL(chatMessageSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.FRIEND_TB);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.PHOTO_TB);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.CHAT_MESSAGE_TB);
    }
}
