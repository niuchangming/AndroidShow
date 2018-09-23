package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

import ekoolab.com.show.utils.JsonParser.FieldExclude;
import ekoolab.com.show.utils.JsonParser.JSOName;

public class Video implements Parcelable {
    public String resourceId;
    public long uploadTime;
    public Photo preview;
    @JSOName(name = "resourceUri")
    public String videoUrl;
    public String title;
    public String description;
    public double lat;
    public double lon;
    public String permission;
    public int category;
    public User creator;
    public int likeCount;
    public int favouriteCount;
    public boolean isMyLike;
    public boolean isMyFavourite;
    public int commentCount;

    public Video() {}
    public Video(Parcel source) {
        resourceId = source.readString();
        uploadTime = source.readLong();
        preview = source.readParcelable(Photo.class.getClassLoader());
        videoUrl = source.readString();
        title = source.readString();
        description = source.readString();
        lat = source.readDouble();
        lon = source.readDouble();
        permission = source.readString();
        category = source.readInt();
        creator = source.readParcelable(User.class.getClassLoader());
        likeCount = source.readInt();
        favouriteCount = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(resourceId);
        parcel.writeLong(uploadTime);
        parcel.writeParcelable(preview, i);
        parcel.writeString(videoUrl);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeDouble(lat);
        parcel.writeDouble(lon);
        parcel.writeString(permission);
        parcel.writeInt(category);
        parcel.writeParcelable(creator, i);
        parcel.writeInt(likeCount);
        parcel.writeInt(favouriteCount);
    }

    @FieldExclude
    public static final Parcelable.Creator<Video> CREATOR = new Creator<Video>() {

        @Override
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}
