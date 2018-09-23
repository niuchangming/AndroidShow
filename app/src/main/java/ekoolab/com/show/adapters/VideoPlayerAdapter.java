package ekoolab.com.show.adapters;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.Video;

public class VideoPlayerAdapter extends RecyclerView.Adapter<VideoPlayerAdapter.ViewHolder> {
    private final String TAG = "VideoPlayerAdapter";
    private Activity activity;
    private List<Video> videos;

    public VideoPlayerAdapter(Activity activity, List<Video> videos) {
        this.activity = activity;
        this.videos = videos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_videoplayer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video video = videos.get(position % videos.size());
        holder.bind(video,position);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        ImageView previewIv;
        TextView descTv;
        TextView tv_share;
        TextView tv_like;
        TextView tv_zan;
        TextView tv_comment;
        TextView tv_title;
        TextView tv_name;
        ImageView avatar_iv;
        TextView tv_follow;
        ImageView iv_del,iv_zan,iv_like;
        LinearLayout ll_comment,ll_like,ll_zan,ll_share;
        public ViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.video_view);
            previewIv = itemView.findViewById(R.id.preview_iv);
            descTv = itemView.findViewById(R.id.desc_tv);
            tv_like = itemView.findViewById(R.id.tv_like);
            tv_zan = itemView.findViewById(R.id.tv_zan);
            tv_comment = itemView.findViewById(R.id.tv_comment);
            tv_share = itemView.findViewById(R.id.tv_share);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_name = itemView.findViewById(R.id.tv_name);
            avatar_iv = itemView.findViewById(R.id.avatar_iv);
            tv_follow = itemView.findViewById(R.id.tv_follow);
            iv_del = itemView.findViewById(R.id.iv_del);
            ll_comment = itemView.findViewById(R.id.ll_comment);
            ll_like = itemView.findViewById(R.id.ll_like);
            ll_share = itemView.findViewById(R.id.ll_share);
            ll_zan = itemView.findViewById(R.id.ll_zan);
            iv_zan = itemView.findViewById(R.id.iv_zan);
            iv_like = itemView.findViewById(R.id.iv_like);
        }

        public void bind(final Video video,final int position) {
            Glide.with(activity).load(video.preview.origin).into(previewIv);
            videoView.setVideoURI(Uri.parse(video.videoUrl));
            descTv.setText(video.description);
            tv_like.setText(video.likeCount + "");
            tv_zan.setText(video.favouriteCount + "");
            tv_comment.setText(video.commentCount + "");
            tv_share.setText(video.likeCount + "");
            tv_title.setText(video.title + "");
            tv_name.setText(video.creator.name + "");
            if(video.isMyLike){
                iv_zan.setBackgroundResource(R.mipmap.heart_red);
            }else{
                iv_zan.setBackgroundResource(R.mipmap.heart_line);
            }
            if(video.isMyFavourite){
                iv_like.setBackgroundResource(R.mipmap.star_fill);
            }else{
                iv_like.setBackgroundResource(R.mipmap.star);
            }
//            Glide.with(activity).load(video.creator.avatar.origin).into(avatar_iv);
            ll_zan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            ll_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            ll_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            ll_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            iv_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(position);
                }
            });
            tv_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(video.creator.isMyFollowing){
                        tv_follow.setText("关注");
                        video.creator.isMyFollowing = false;
                    }else{
                        tv_follow.setText("已关注");
                        video.creator.isMyFollowing = true;
                    }
                    mOnItemFollowClickListener.onClick(position);
                }
            });
        }
    }

    private OnItemClickListener mOnItemClickListener = null;

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }


    private OnItemFollowClickListener mOnItemFollowClickListener = null;

    public interface OnItemFollowClickListener {
        void onClick(int position);
    }

    public void setOnItemFollowClickListener(OnItemFollowClickListener onItemfollowClickListener) {
        this.mOnItemFollowClickListener = onItemfollowClickListener;
    }

}
