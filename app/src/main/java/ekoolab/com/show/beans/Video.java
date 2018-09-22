package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable {
    public String resourceId;
    public long uploadTime;
    public Photo preview;
    public String resourceUri;
    public String title;
    public String description;
    public double lat;
    public double lon;
    public String permission;
    public String category;
    public int likeCount;
    public int favouriteCount;
    public boolean isMyLike;
    public boolean isMyFavourite;
    public User creator;
    public int commentCount;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.resourceId);
        dest.writeLong(this.uploadTime);
        dest.writeParcelable(this.preview, flags);
        dest.writeString(this.resourceUri);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lon);
        dest.writeString(this.permission);
        dest.writeString(this.category);
        dest.writeInt(this.likeCount);
        dest.writeInt(this.favouriteCount);
        dest.writeByte(this.isMyLike ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isMyFavourite ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.creator, flags);
        dest.writeInt(this.commentCount);
    }

    public Video() {
    }

    protected Video(Parcel in) {
        this.resourceId = in.readString();
        this.uploadTime = in.readLong();
        this.preview = in.readParcelable(Photo.class.getClassLoader());
        this.resourceUri = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.lat = in.readDouble();
        this.lon = in.readDouble();
        this.permission = in.readString();
        this.category = in.readString();
        this.likeCount = in.readInt();
        this.favouriteCount = in.readInt();
        this.isMyLike = in.readByte() != 0;
        this.isMyFavourite = in.readByte() != 0;
        this.creator = in.readParcelable(User.class.getClassLoader());
        this.commentCount = in.readInt();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
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
