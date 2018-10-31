package ekoolab.com.show.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/28
 * @description
 */
public class Moment implements Parcelable {
    public String resourceId;
    public long uploadTime;
    public String body;
    public double lat;
    public double lon;
    public String type;
    public String permission;
    public int likeCount;
    public boolean isMyLike;
    public Friend creator;
    public List<Photo> photoArray;
    public List<CommentsBean> comments;

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
        dest.writeInt(this.likeCount);
        dest.writeByte((byte) (isMyLike ? 1 : 0));
        dest.writeParcelable(this.creator, flags);
        dest.writeTypedList(this.photoArray);
        dest.writeTypedList(this.comments);
    }

    public Moment(){

    }
    protected Moment(Parcel in) {
        this.resourceId = in.readString();
        this.uploadTime = in.readLong();
        this.body = in.readString();
        this.lat = in.readDouble();
        this.lon = in.readDouble();
        this.type = in.readString();
        this.permission = in.readString();
        this.likeCount = in.readInt();
        this.isMyLike = in.readByte() != 0;
        this.creator = in.readParcelable(getClass().getClassLoader());
        this.photoArray = in.createTypedArrayList(Photo.CREATOR);
        this.comments = in.createTypedArrayList(CommentsBean.CREATOR);
//        in.readTypedList(this.photoArray, Photo.CREATOR);
//        in.readTypedList(this.comments, CommentsBean.CREATOR);
    }

    public static final Parcelable.Creator<Moment> CREATOR = new Parcelable.Creator<Moment>() {
        @Override
        public Moment createFromParcel(Parcel source) {
            return new Moment(source);
        }

        @Override
        public Moment[] newArray(int size) {
            return new Moment[size];
        }
    };


    public static class CommentsBean implements Parcelable {
        public String body;
        public Friend creator;
        public long postTime;
        public String replyTo;
        public String replyToName;
        public String commentId;
        public int likeCount;
        public List<CommentsBean> comments;
        public boolean ishasParentComment;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.body);
            dest.writeParcelable(this.creator, flags);
            dest.writeLong(this.postTime);
            dest.writeString(this.replyTo);
            dest.writeString(this.replyToName);
            dest.writeString(this.commentId);
            dest.writeInt(this.likeCount);
            dest.writeTypedList(this.comments);
            dest.writeByte((byte) (ishasParentComment ? 1 : 0));
        }

        public CommentsBean(){

        }
        protected CommentsBean(Parcel in) {
            this.body = in.readString();
            this.creator = in.readParcelable(getClass().getClassLoader());
            this.postTime = in.readLong();
            this.replyTo = in.readString();
            this.replyToName = in.readString();
            this.commentId = in.readString();
            this.likeCount = in.readInt();
            this.comments = in.createTypedArrayList(CommentsBean.CREATOR);
//            in.readTypedList(this.comments, CommentsBean.CREATOR);
            this.ishasParentComment = in.readByte() != 0;
        }

        public static final Parcelable.Creator<CommentsBean> CREATOR = new Parcelable.Creator<CommentsBean>() {
            @Override
            public CommentsBean createFromParcel(Parcel source) {
                return new CommentsBean(source);
            }

            @Override
            public CommentsBean[] newArray(int size) {
                return new CommentsBean[size];
            }
        };
    }

}
