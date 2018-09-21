package ekoolab.com.show.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.Video;

public class VideoAdapter extends RecyclerView.Adapter <VideoAdapter.VideoHolder> {
    private Activity activity;
    private List<Video> videos;
    private OnItemClickListener listener;

    public VideoAdapter(Activity activity, List<Video> videos) {
        this.activity = activity;
        this.videos = videos;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = activity.getLayoutInflater().inflate(R.layout.video_grid_cell, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoHolder holder, int position) {
        Video video = videos.get(position);
        holder.bind(video, listener);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    class VideoHolder extends RecyclerView.ViewHolder {
        private ImageView coverIv;
        private TextView locationTv;
        private TextView nameTv;
        private ImageView avatarIv;

        public VideoHolder(View itemView) {
            super(itemView);

            coverIv = itemView.findViewById(R.id.cover_iv);
            locationTv = itemView.findViewById(R.id.loc_tv);
            nameTv = itemView.findViewById(R.id.name_tv);
            avatarIv = itemView.findViewById(R.id.avatar_iv);
        }

        public void bind(final Video video, final OnItemClickListener listener) {
            nameTv.setText(video.creator.name);
            locationTv.setText("大连");
            Glide.with(activity).load(video.creator.avatar.origin).into(avatarIv);
            Glide.with(activity).load(video.preview.origin).into(coverIv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                if(listener != null) {
                    listener.onItemClick(video);
                }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Video video);
    }
}

