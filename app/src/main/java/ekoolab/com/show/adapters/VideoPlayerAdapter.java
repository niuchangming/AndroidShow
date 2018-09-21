package ekoolab.com.show.adapters;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.Video;

public class VideoPlayerAdapter extends RecyclerView.Adapter<VideoPlayerAdapter.ViewHolder>{
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_videoplayer, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video video = videos.get(position % videos.size());
        holder.bind(video);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        VideoView videoView;
        ImageView previewIv;
        TextView descTv;
        public ViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.video_view);
            previewIv = itemView.findViewById(R.id.preview_iv);
            descTv = itemView.findViewById(R.id.desc_tv);
        }

        public void bind(final Video video) {
            Glide.with(activity).load(video.preview.origin).into(previewIv);
            videoView.setVideoURI(Uri.parse(video.videoUrl));
            descTv.setText(video.description);
        }
    }

}
