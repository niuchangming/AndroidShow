package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class Live implements Parcelable {

    public String userCode;
    public String resourceUri;
    public String author;
    public String nickname;
    public String description;
    public String channelId;
    public double lon;
    public double lat;
    public int userLikeCount;
    public int ranking;
    public int commentCount;
    public int followingCount;
    public int audienceCount;
    public int coinAmount;
    public Photo avatar;
    public Photo coverImage;

    public Live() {}

    protected Live(Parcel in) {
        this.userCode = in.readString();
        this.resourceUri = in.readString();
        this.author = in.readString();
        this.nickname = in.readString();
        this.description = in.readString();
        this.channelId = in.readString();
        this.lon = in.readDouble();
        this.lat = in.readDouble();
        this.userLikeCount = in.readInt();
        this.ranking = in.readInt();
        this.commentCount = in.readInt();
        this.followingCount = in.readInt();
        this.audienceCount = in.readInt();
        this.coinAmount = in.readInt();
        this.avatar = in.readParcelable(Photo.class.getClassLoader());
        this.coverImage = in.readParcelable(Photo.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userCode);
        dest.writeString(this.resourceUri);
        dest.writeString(this.author);
        dest.writeString(this.nickname);
        dest.writeString(this.description);
        dest.writeString(this.channelId);
        dest.writeDouble(this.lon);
        dest.writeDouble(this.lat);
        dest.writeInt(this.userLikeCount);
        dest.writeInt(this.ranking);
        dest.writeInt(this.commentCount);
        dest.writeInt(this.followingCount);
        dest.writeInt(this.audienceCount);
        dest.writeInt(this.coinAmount);
        dest.writeParcelable(this.avatar, flags);
        dest.writeParcelable(this.coverImage, flags);
    }

    public static final Creator<Live> CREATOR = new Creator<Live>() {
        @Override
        public Live createFromParcel(Parcel source) {
            return new Live(source);
        }

        @Override
        public Live[] newArray(int size) {
            return new Live[size];
        }
    };
}
