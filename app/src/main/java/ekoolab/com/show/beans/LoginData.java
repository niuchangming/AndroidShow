package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/10/1
 * @description
 */
public class LoginData implements Parcelable {
    public int roleType;
    public int countryCode;
    public int gender;
    public String accountType;
    public String mobile;
    public String name;
    public String nickName;
    public String token;
    public String userCode;
    public Photo avatar;

    public LoginData() {}

    protected LoginData(Parcel in) {
        this.roleType = in.readInt();
        this.countryCode = in.readInt();
        this.gender = in.readInt();
        this.accountType = in.readString();
        this.mobile = in.readString();
        this.name = in.readString();
        this.nickName = in.readString();
        this.token = in.readString();
        this.userCode = in.readString();
        this.avatar = in.readParcelable(Photo.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(this.roleType);
        dest.writeInt(this.countryCode);
        dest.writeInt(this.gender);
        dest.writeString(this.accountType);
        dest.writeString(this.mobile);
        dest.writeString(this.name);
        dest.writeString(this.nickName);
        dest.writeString(this.token);
        dest.writeString(this.userCode);
        dest.writeParcelable(this.avatar, i);
    }

    public static final Creator<LoginData> CREATOR = new Creator<LoginData>() {
        @Override
        public LoginData createFromParcel(Parcel source) {
            return new LoginData(source);
        }

        @Override
        public LoginData[] newArray(int size) {
            return new LoginData[size];
        }
    };
}
