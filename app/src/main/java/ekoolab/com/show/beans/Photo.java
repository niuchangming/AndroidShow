package ekoolab.com.show.beans;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DataBaseManager;
import ekoolab.com.show.utils.JsonParser.FieldExclude;

public class Photo implements Parcelable {

    public String userId;
    public String origin;
    public String medium;
    public String small;

    public Photo() {}
    public Photo(Parcel source) {
        userId = source.readString();
        origin = source.readString();
        medium = source.readString();
        small = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(origin);
        parcel.writeString(medium);
        parcel.writeString(small);
    }

    @FieldExclude
    public static final Parcelable.Creator<Photo> CREATOR = new Creator<Photo>() {

        @Override
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public void save(Context context) {
        SQLiteDatabase db = DataBaseManager.getInstance(context).openDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.PhotoTableColumns.userId, this.userId);
        values.put(Constants.PhotoTableColumns.smallUrl, this.small);
        values.put(Constants.PhotoTableColumns.mediumUrl, this.medium);
        values.put(Constants.PhotoTableColumns.originUrl, this.origin);

        synchronized (context) {
            long id = db.insertWithOnConflict(Constants.PHOTO_TB, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                db.update(Constants.PHOTO_TB, values, Constants.PhotoTableColumns.userId + "=?", new String[]{this.userId});
            }
        }

        if (db != null){
            DataBaseManager.getInstance(context).closeDatabase();
        }
    }

    public static Photo getPhotoByUserId(Context context, String userId){
        SQLiteDatabase db = DataBaseManager.getInstance(context).openDatabase();
        Cursor cursor = db.query(Constants.PHOTO_TB, null, Constants.PhotoTableColumns.userId+"=?", new String[]{userId}, null, null, null);
        cursor.moveToFirst();
        Photo photo = null;
        if (!cursor.isAfterLast()) {
            photo = new Photo();
            photo.userId = cursor.getString(cursor.getColumnIndexOrThrow(Constants.PhotoTableColumns.userId));
            photo.small = cursor.getString(cursor.getColumnIndexOrThrow(Constants.PhotoTableColumns.smallUrl));
            photo.medium = cursor.getString(cursor.getColumnIndexOrThrow(Constants.PhotoTableColumns.mediumUrl));
            photo.origin = cursor.getString(cursor.getColumnIndexOrThrow(Constants.PhotoTableColumns.originUrl));
        }

        if (db != null){
            DataBaseManager.getInstance(context).closeDatabase();
        }

        return photo;
    }
}
