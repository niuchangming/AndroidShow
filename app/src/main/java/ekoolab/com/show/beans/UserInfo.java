package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

//public class UserInfo implements Parcelable {
public class UserInfo {

    public int roleType;
    public int countryCode;
    public String mobile;
    public String name;
    public String nickName;
    public String userCode;
    public int gender;

    public String region;
    public Long birthday;
    public String whatsup;

    public List<String> hobby;
    public String category;
    public String description;
    public int followers;
    public int following;
    public Photo avatar;
    public Photo coverImage;
//
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(this.name);
//        dest.writeString(this.nickName);
//        dest.writeString(this.mobile);
//        dest.writeString(this.region);
//        dest.writeString(this.whatsup);
//        dest.writeString(this.category);
//        dest.writeString(this.description);
//        dest.writeInt(this.gender);
//        dest.writeInt(this.followers);
//        dest.writeInt(this.following);
//        dest.writeList(this.hobby);
//        dest.writeLong(this.birthday);
//        dest.writeParcelable(this.avatar, flags);
//        dest.writeParcelable(this.coverImage, flags);
//    }
//
//    public UserInfo(){
//
//    }
//    protected UserInfo(Parcel in) {
//        this.name = in.readString();
//        this.nickName = in.readString();
//        this.mobile = in.readString();
//        this.region = in.readString();
//        this.whatsup = in.readString();
//        this.category = in.readString();
//        this.description = in.readString();
//        this.gender = in.readInt();
//        this.followers = in.readInt();
//        this.following = in.readInt();
//        in.readList(this.hobby, null);
//        this.birthday = in.readLong();
//        this.avatar = in.readParcelable(Photo.class.getClassLoader());
//        this.coverImage = in.readParcelable(Photo.class.getClassLoader());
//    }
//
//    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
//        @Override
//        public UserInfo createFromParcel(Parcel source) {
//            return new UserInfo(source);
//        }
//
//        @Override
//        public UserInfo[] newArray(int size) {
//            return new UserInfo[size];
//        }
//    };

}
