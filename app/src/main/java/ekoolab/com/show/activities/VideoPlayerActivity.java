package ekoolab.com.show.activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dingmouren.layoutmanagergroup.viewpager.OnViewPagerListener;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;

import java.util.ArrayList;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.VideoPlayerAdapter;
import ekoolab.com.show.beans.Video;
import ekoolab.com.show.views.FixedTextureVideoView;

public class VideoPlayerActivity extends BaseActivity {
    private final String TAG = "VideoPlayerActivity";
    private ArrayList<Video> videos;
    private int currentIndex;
    private RecyclerView recyclerView;
    private ViewPagerLayoutManager layoutManager;
    private VideoPlayerAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_player;
    }

    @Override
    protected void initData() {
        super.initData();
        videos = getIntent().getParcelableArrayListExtra("videos");
        currentIndex = getIntent().getIntExtra("current_index", currentIndex);
    }

    @Override
    protected void initViews() {
        super.initViews();
        recyclerView = findViewById(R.id.recycler_view);

        layoutManager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new VideoPlayerAdapter(this, videos);
        adapter.setOnItemClickListener(new VideoPlayerAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                onBackPressed();
            }
        });
        recyclerView.setAdapter(adapter);
        int size = videos.size();
        int centerPos = Integer.MAX_VALUE / 2 / size * size;

        initListener();
        layoutManager.scrollToPosition(centerPos + currentIndex);
    }

    private void initListener() {
        layoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {
                playVideo(0);
            }

            @Override
            public void onPageRelease(boolean isNext, int position) {
                Log.e(TAG, "释放位置:" + position + " 下一页:" + isNext);
                int index;
                if (isNext) {
                    index = 0;
                } else {
                    index = 1;
                }

                releaseVideo(index);
            }

            @Override
            public void onPageSelected(int position, boolean isBottom) {
                playVideo(0);
            }

        });
    }

    private void playVideo(int position) {
        View itemView = recyclerView.getChildAt(position);
        final FixedTextureVideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgThumb = itemView.findViewById(R.id.preview_iv);
        final MediaPlayer[] mediaPlayer = new MediaPlayer[1];
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mediaPlayer[0] = mp;
                mp.setLooping(true);
                imgThumb.animate().alpha(0).setDuration(1000).start();
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                try {
                    Log.v(TAG, "onPrepared");
                    WindowManager wm = (WindowManager) VideoPlayerActivity.this.getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics metrics = new DisplayMetrics();
                    wm.getDefaultDisplay().getRealMetrics(metrics);
                    int width = metrics.widthPixels;  //以要素为单位
                    int height = metrics.heightPixels;
                    int mpWidth = mp.getVideoWidth();
                    int mpHeight = mp.getVideoHeight();
                    if(mpWidth>mpHeight){
                        videoView.setFixedSize(width, width*mpHeight/mpWidth);
                    }else{
                        videoView.setFixedSize(width, height);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        videoView.requestFocus();
        videoView.start();
    }


    private void releaseVideo(int index) {
        View itemView = recyclerView.getChildAt(index);
        final FixedTextureVideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgPlay = itemView.findViewById(R.id.preview_iv);
        videoView.stopPlayback();
        imgPlay.animate().alpha(0f).start();
    }

    @Override
    public void onBackPressed() {
        try {
            releaseVideo(0);
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("videos", videos);
            setResult(2, intent);
            VideoPlayerActivity.this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
