package ekoolab.com.show.beans;

import java.util.List;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/28
 * @description
 */
public class Moment {
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

    public static class CommentsBean {
        public String body;
        public Friend creator;
        public long postTime;
        public String replyTo;
        public String replyToName;
        public String commentId;
        public int likeCount;
        public List<CommentsBean> comments;
        public boolean ishasParentComment;
    }
}
