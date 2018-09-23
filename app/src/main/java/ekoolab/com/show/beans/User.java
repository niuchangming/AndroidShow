package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

import ekoolab.com.show.utils.JsonParser.FieldExclude;

public class User implements Parcelable{
    public String name;
    public String userCode;
    public transient String avatar;
    public int followingCount;
    public Photo avatar;
    public boolean isMyFollowing;
    public boolean isMyFollower;

    public User(){}
    public User(Parcel source) {
        name = source.readString();
        userCode = source.readString();
        avatar = source.readParcelable(Photo.class.getClassLoader());

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(userCode);
        parcel.writeParcelable(avatar, i);
    }

    @FieldExclude
    public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {

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
