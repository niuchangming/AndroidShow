package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author ztn
 * @version V_1.0.0
 * @date 2018/10/3
 * @description
 */
public class TextPicture implements Parcelable {
    public String resourceId;
    public long uploadTime;
    public String body;
    public double lat;
    public double lon;
    public String type;
    public String permission;
    public String userName;
    public String userCode;
    public List<Photo> photoArray;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.resourceId);
        dest.writeLong(this.uploadTime);
        dest.writeString(this.body);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lon);
        dest.writeString(this.type);
        dest.writeString(this.permission);
        dest.writeString(this.userName);
        dest.writeString(this.userCode);
        dest.writeTypedList(this.photoArray);
    }

    public TextPicture() {
    }

    protected TextPicture(Parcel in) {
        this.resourceId = in.readString();
        this.uploadTime = in.readLong();
        this.body = in.readString();
        this.lat = in.readDouble();
        this.lon = in.readDouble();
        this.type = in.readString();
        this.permission = in.readString();
        this.userName = in.readString();
        this.userCode = in.readString();
        this.photoArray = in.createTypedArrayList(Photo.CREATOR);
    }

    public static final Parcelable.Creator<TextPicture> CREATOR = new Parcelable.Creator<TextPicture>() {
        @Override
        public TextPicture createFromParcel(Parcel source) {
            return new TextPicture(source);
        }

        @Override
        public TextPicture[] newArray(int size) {
            return new TextPicture[size];
        }
    };
}
