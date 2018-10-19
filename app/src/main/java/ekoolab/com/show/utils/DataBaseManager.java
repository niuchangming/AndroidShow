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
            + Constants.FriendTableColumns.userId + " varchar UNIQUE ON CONFLICT REPLACE, "
            + Constants.FriendTableColumns.name + " varchar(50), "
            + Constants.FriendTableColumns.nickname + " varchar(50), "
            + Constants.FriendTableColumns.channelUrl + " varchar, "
            + Constants.FriendTableColumns.countryCode + " varchar(5), "
            + Constants.FriendTableColumns.mobile + " varchar(20) UNIQUE ON CONFLICT REPLACE, "
            + Constants.FriendTableColumns.isAppUser + " boolean, "
            + Constants.FriendTableColumns.isMyFollowing + " boolean, "
            + Constants.FriendTableColumns.isMyFollower + " boolean); ";


    String photoSql = "create table if not exists " + Constants.PHOTO_TB + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + Constants.PhotoTableColumns.userId + " varchar UNIQUE ON CONFLICT REPLACE, "
            + Constants.PhotoTableColumns.messageId + " varchar UNIQUE ON CONFLICT REPLACE, "
            + Constants.PhotoTableColumns.smallUrl + " varchar, "
            + Constants.PhotoTableColumns.originUrl + " varchar, "
            + Constants.PhotoTableColumns.mediumUrl + " varchar);";

    @Override
    public synchronized void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(friendSql);
        sqLiteDatabase.execSQL(photoSql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.FRIEND_TB);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.PHOTO_TB);
    }
}
