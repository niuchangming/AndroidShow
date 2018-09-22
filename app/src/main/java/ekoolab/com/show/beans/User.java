package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

import ekoolab.com.show.utils.JsonParser.FieldExclude;

public class User implements Parcelable{
    public String name;
    public String nickname;
    public String userCode;
    public transient String avatar;
    public int followingCount;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.nickname);
        dest.writeString(this.userCode);
        dest.writeString(this.avatar);
        dest.writeInt(this.followingCount);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.name = in.readString();
        this.nickname = in.readString();
        this.userCode = in.readString();
        this.avatar = in.readString();
        this.followingCount = in.readInt();
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
