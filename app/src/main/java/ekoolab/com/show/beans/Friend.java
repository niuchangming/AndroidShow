package ekoolab.com.show.beans;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Parcel;
import android.os.Parcelable;

import com.luck.picture.lib.tools.Constant;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DataBaseManager;
import ekoolab.com.show.utils.Utils;

public class Friend implements Parcelable {
    public String name;
    public String nickName;
    public String userCode;
    public String mobile;
    public String countryCode;
    public String channelUrl;
    public Photo avatar;
    public int followingCount;
    public boolean isMyFollowing;
    public boolean isMyFollower;
    public boolean isAppUser;

    public boolean save(Context context){
        synchronized(context){
            SQLiteDatabase db = DataBaseManager.getInstance(context).openDatabase();

            ContentValues values = new ContentValues();
            values.put(Constants.FriendTableColumns.name, this.name);
            values.put(Constants.FriendTableColumns.nickname, this.nickName);
            values.put(Constants.FriendTableColumns.userId, this.userCode);
            values.put(Constants.FriendTableColumns.mobile, this.mobile);
            values.put(Constants.FriendTableColumns.countryCode, this.countryCode);
            if (!Utils.isBlank(this.channelUrl)) {
                values.put(Constants.FriendTableColumns.channelUrl, this.channelUrl);
            }
            if(this.avatar != null){
                this.avatar.userId = this.userCode;
                this.avatar.save(context);
            }
            values.put(Constants.FriendTableColumns.isMyFollowing, this.isMyFollowing ? 1 : 0);
            values.put(Constants.FriendTableColumns.isMyFollower, this.isMyFollower ? 1 : 0);
            values.put(Constants.FriendTableColumns.isAppUser, this.isAppUser ? 1 : 0);

            try{
                long id = db.insertWithOnConflict(Constants.FRIEND_TB, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (id == -1) {
                    db.update(Constants.FRIEND_TB, values,
                            Constants.FriendTableColumns.userId + "=?", new String[]{this.userCode});
                }
            }catch(SQLiteException e){
                e.printStackTrace();
            }finally {
                if (db != null){
                    DataBaseManager.getInstance(context).closeDatabase();
                }
            }

            return true;
        }
    }

    public static void batchSave(Context context, List<Friend> friends){
        SQLiteDatabase db = DataBaseManager.getInstance(context).openDatabase();
        try {
            for(Friend friend : friends){
                ContentValues values = new ContentValues();
                values.put(Constants.FriendTableColumns.name, friend.name);
                values.put(Constants.FriendTableColumns.nickname, friend.nickName);
                values.put(Constants.FriendTableColumns.userId, friend.userCode);
                values.put(Constants.FriendTableColumns.mobile, friend.mobile);
                values.put(Constants.FriendTableColumns.countryCode, friend.countryCode);
                if (!Utils.isBlank(friend.channelUrl)) {
                    values.put(Constants.FriendTableColumns.channelUrl, friend.channelUrl);
                }
                if(friend.avatar != null){
                    friend.avatar.userId = friend.userCode;
                    friend.avatar.save(context);
                }
                values.put(Constants.FriendTableColumns.isMyFollowing, friend.isMyFollowing);
                values.put(Constants.FriendTableColumns.isMyFollower, friend.isMyFollower);
                values.put(Constants.FriendTableColumns.isAppUser, friend.isAppUser);

                long id = db.insertWithOnConflict(Constants.FRIEND_TB, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                if(id == -1){
                    db.update(Constants.FRIEND_TB, values,
                            Constants.FriendTableColumns.userId + "=? or " + Constants.FriendTableColumns.mobile + "=?",
                            new String[]{friend.userCode, friend.mobile});
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }finally {
            if (db != null){
                DataBaseManager.getInstance(context).closeDatabase();
            }
        }

    }

    public static Friend getFriendByUserCode(Context context, String userCode){
        SQLiteDatabase db = DataBaseManager.getInstance(context).openDatabase();
        Friend friend = null;
        Cursor cursor = null;
        try{
            cursor = db.query(Constants.FRIEND_TB, null, Constants.FriendTableColumns.userId + "=?",
                    new String[]{userCode}, null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                friend = new Friend();
                friend.name = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.name));
                friend.nickName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.nickname));
                friend.userCode = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.userId));
                friend.mobile = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.mobile));
                friend.countryCode = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.countryCode));
                friend.channelUrl = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.channelUrl));

                if(!Utils.isBlank(friend.userCode)){
                    friend.avatar = Photo.getPhotoByUserId(context, friend.userCode);
                }

                friend.isMyFollowing = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.isMyFollowing)) == 1;
                friend.isMyFollower = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.isMyFollower)) == 1;
                friend.isAppUser = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.isAppUser)) == 1;
            }
        }catch(SQLiteException e){
            Logger.e(e.getLocalizedMessage());
        }finally{
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
            if (db != null){
                DataBaseManager.getInstance(context).closeDatabase();
            }
        }
        return friend;
    }

    public static List<Friend> getAllFriends(Context context, String selection, String[] args){
        SQLiteDatabase db = DataBaseManager.getInstance(context).openDatabase();
        List<Friend> friends = new ArrayList<>();
        Cursor cursor = null;
        try{
            cursor = db.query(Constants.FRIEND_TB, null, selection, args, null, null, null);
            cursor.moveToFirst();
            Friend friend = null;
            while(!cursor.isAfterLast()){
                friend = new Friend();
                friend.name = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.name));
                friend.nickName = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.nickname));
                friend.userCode = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.userId));
                friend.mobile = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.mobile));
                friend.countryCode = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.countryCode));
                friend.channelUrl = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.channelUrl));

                if(!Utils.isBlank(friend.userCode)){
                    friend.avatar = Photo.getPhotoByUserId(context, friend.userCode);
                }

                friend.isMyFollowing = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.isMyFollowing)) == 1;
                friend.isMyFollower = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.isMyFollower)) == 1;
                friend.isAppUser = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FriendTableColumns.isAppUser)) == 1;

                friends.add(friend);
                cursor.moveToNext();
            }
        }catch(SQLiteException e){
            Logger.e(e.getLocalizedMessage());
        }finally{
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
            if (db != null){
                DataBaseManager.getInstance(context).closeDatabase();
            }
        }
        return friends;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.nickName);
        dest.writeString(this.userCode);
        dest.writeString(this.mobile);
        dest.writeString(this.countryCode);
        dest.writeString(this.channelUrl);
        dest.writeParcelable(this.avatar, flags);
        dest.writeInt(this.followingCount);
        dest.writeByte(this.isMyFollowing ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isMyFollower ? (byte) 1 : (byte) 0);
        dest.writeByte((byte) (isAppUser ? 1 : 0));
    }

    public Friend() {}

    protected Friend(Parcel in) {
        this.name = in.readString();
        this.nickName = in.readString();
        this.userCode = in.readString();
        this.mobile = in.readString();
        this.countryCode = in.readString();
        this.channelUrl = in.readString();
        this.avatar = in.readParcelable(Photo.class.getClassLoader());
        this.followingCount = in.readInt();
        this.isMyFollowing = in.readByte() != 0;
        this.isMyFollower = in.readByte() != 0;
        this.isAppUser = in.readByte() != 0;
    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel source) {
            return new Friend(source);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };
}
