package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

import ekoolab.com.show.utils.JsonParser.FieldExclude;

public class User implements Parcelable{
    public String name;
    public String userCode;
    public transient String avatar;
    public int followingCount;
    public boolean isMyFollowing;
    public boolean isMyFollower;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.userCode);
        dest.writeInt(this.followingCount);
        dest.writeByte(this.isMyFollowing ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isMyFollower ? (byte) 1 : (byte) 0);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.name = in.readString();
        this.userCode = in.readString();
        this.followingCount = in.readInt();
        this.isMyFollowing = in.readByte() != 0;
        this.isMyFollower = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
