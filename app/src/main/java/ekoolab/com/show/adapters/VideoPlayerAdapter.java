package ekoolab.com.show.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Video;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.views.FixedTextureVideoView;

public class VideoPlayerAdapter extends RecyclerView.Adapter<VideoPlayerAdapter.ViewHolder> {
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
        holder.bind(video, position);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FixedTextureVideoView videoView;
        ImageView previewIv;
        TextView tv_share;
        TextView tv_like;
        TextView tv_zan;
        TextView tv_comment;
        TextView tv_title;
        TextView tv_name;
        ImageView avatar_iv;
        Button followBtn;
        ImageView iv_del, iv_zan, iv_like;
        LinearLayout ll_comment, ll_like, ll_zan, ll_share;

        public ViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.video_view);
            previewIv = itemView.findViewById(R.id.preview_iv);
            tv_like = itemView.findViewById(R.id.tv_like);
            tv_zan = itemView.findViewById(R.id.tv_zan);
            tv_comment = itemView.findViewById(R.id.tv_comment);
            tv_share = itemView.findViewById(R.id.tv_share);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_name = itemView.findViewById(R.id.tv_name);
            avatar_iv = itemView.findViewById(R.id.avatar_iv);
            followBtn = itemView.findViewById(R.id.follow_btn);
            iv_del = itemView.findViewById(R.id.iv_del);
            ll_comment = itemView.findViewById(R.id.ll_comment);
            ll_like = itemView.findViewById(R.id.ll_like);
            ll_share = itemView.findViewById(R.id.ll_share);
            ll_zan = itemView.findViewById(R.id.ll_zan);
            iv_zan = itemView.findViewById(R.id.iv_zan);
            iv_like = itemView.findViewById(R.id.iv_like);
        }

        public void bind(final Video video, final int position) {
            Glide.with(activity).load(video.preview.origin).into(previewIv);
            videoView.setVideoPath(video.resourceUri);
            tv_like.setText(video.favouriteCount + "");
            tv_zan.setText(video.likeCount + "");
            tv_comment.setText(video.commentCount + "");
            tv_share.setText(video.likeCount + "");
            tv_title.setText(video.title + "");
            tv_name.setText(video.creator.name + "");
            if (video.isMyLike) {
                iv_zan.setBackgroundResource(R.mipmap.ic_heart);
            } else {
                iv_zan.setBackgroundResource(R.mipmap.ic_heart_line);
            }
            if (video.isMyFavourite) {
                iv_like.setBackgroundResource(R.mipmap.star_fill);
            } else {
                iv_like.setBackgroundResource(R.mipmap.star);
            }
            if (video.creator.isMyFollowing) {
                followBtn.setText(activity.getString(R.string.un_follow));
            } else {
                followBtn.setText(activity.getString(R.string.follow));
            }
            ImageLoader.displayImageAsCircle(video.creator.avatar.small, avatar_iv);
            ll_zan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (video.isMyLike) {
                        getLike(ViewHolder.this, video, false);
                    } else {
                        getLike(ViewHolder.this, video, true);
                    }
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
                    if (video.isMyFavourite) {
                        getFavourite(ViewHolder.this, video, false);
                    } else {
                        getFavourite(ViewHolder.this, video, true);
                    }
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
            followBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (video.creator.isMyFollowing) {
                        getFollow(ViewHolder.this, video, false);
                    } else {
                        getFollow(ViewHolder.this, video, false);
                    }
                }
            });
        }
    }

    private void getFollow(ViewHolder viewHolder, Video video, boolean flag) {
        HashMap<String, String> map = new HashMap<>(2);
        map.put("resourceId", video.resourceId);
        map.put("token", AuthUtils.getInstance(activity).getApiToken());
        ApiServer.basePostRequest((BaseActivity) activity, flag ? Constants.FOLLOW : Constants.FOLLOWCANCEL, map,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        try {
                            video.creator.isMyFollowing = !video.creator.isMyFollowing;
                            if (video.creator.isMyFollowing) {
                                viewHolder.followBtn.setText(activity.getString(R.string.follow));
                            } else {
                                viewHolder.followBtn.setText(activity.getString(R.string.un_follow));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void saveComment(ViewHolder viewHolder, Video video, String comment) {
        HashMap<String, String> map = new HashMap<>(3);
        map.put("body", comment);
        map.put("resourceId", video.resourceId);
        map.put("token", AuthUtils.getInstance(activity).getApiToken());
        ApiServer.basePostRequest((BaseActivity) activity, Constants.COMMENT, map,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        try {

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void getFavourite(ViewHolder viewHolder, Video video, boolean flag) {
        HashMap<String, String> map = new HashMap<>(2);
        map.put("resourceId", video.resourceId);
        map.put("token", AuthUtils.getInstance(activity).getApiToken());
        ApiServer.basePostRequest((BaseActivity) activity, flag ? Constants.FAVOURITE : Constants.FAVOURITECANEL, map,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        try {
                            if (video.isMyFavourite) {
                                video.isMyFavourite = false;
                                video.favouriteCount -= 1;
                                viewHolder.tv_like.setText(video.favouriteCount + "");
                                viewHolder.iv_like.setBackgroundResource(R.mipmap.star);
                            } else {
                                video.isMyFavourite = true;
                                video.favouriteCount += 1;
                                viewHolder.tv_like.setText(video.favouriteCount + "");
                                viewHolder.iv_like.setBackgroundResource(R.mipmap.star_fill);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void getLike(ViewHolder viewHolder, Video video, boolean flag) {
        HashMap<String, String> map = new HashMap<>(2);
        map.put("resourceId", video.resourceId);
        map.put("token", AuthUtils.getInstance(activity).getApiToken());
        ApiServer.basePostRequest((BaseActivity) activity, flag ? Constants.LIKE : Constants.UNLIKE, map,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        try {
                            if (video.isMyLike) {
                                video.isMyLike = false;
                                video.likeCount -= 1;
                                viewHolder.tv_zan.setText(video.likeCount + "");
                                viewHolder.iv_zan.setBackgroundResource(R.mipmap.ic_heart_line);
                                viewHolder.iv_zan.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                            } else {
                                video.isMyLike = true;
                                video.likeCount += 1;
                                viewHolder.tv_zan.setText(video.likeCount + "");
                                viewHolder.iv_zan.setBackgroundResource(R.mipmap.ic_heart);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private OnItemClickListener mOnItemClickListener = null;

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

}
