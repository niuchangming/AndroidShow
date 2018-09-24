package ekoolab.com.show.activities;

import android.content.Intent;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dingmouren.layoutmanagergroup.viewpager.OnViewPagerListener;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.juziwl.ijkplayerlib.media.IjkVideoView;

import java.util.ArrayList;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.VideoPlayerAdapter;
import ekoolab.com.show.beans.Video;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoPlayerActivity extends BaseActivity {
    private final String TAG = "VideoPlayerActivity";
    private ArrayList<Video> videos;
    private int currentIndex;
    private RecyclerView recyclerView;
    private ViewPagerLayoutManager layoutManager;
    private VideoPlayerAdapter adapter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_palyer;
    }

    @Override
    protected void initData() {
        super.initData();
        videos = getIntent().getParcelableArrayListExtra("videos");
        currentIndex = getIntent().getIntExtra("current_index", currentIndex);
//        if(videos != null && videos.size() > 0){
//            List<Video> firstVideoList = new ArrayList<>(videos.subList(currentIndex, videos.size()));
//            List<Video> secondVideoList = new ArrayList<>(videos.subList(0, currentIndex));
//            videos = new ArrayList<>(firstVideoList);
//            videos.addAll(secondVideoList);
//        }
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    @Override
    protected void initViews() {
        super.initViews();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

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
        final IjkVideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgThumb = itemView.findViewById(R.id.preview_iv);
        final IMediaPlayer[] mediaPlayer = new IMediaPlayer[1];
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                mediaPlayer[0] = iMediaPlayer;
                iMediaPlayer.setLooping(true);
                imgThumb.animate().alpha(0).setDuration(200).start();
                return false;
            }
        });
//        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//            @Override
//            public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                mediaPlayer[0] = mp;
//                mp.setLooping(true);
//                imgThumb.animate().alpha(0).setDuration(200).start();
//                return false;
//            }
//        });
//        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                Log.v(TAG,"onPrepared");
//            }
//        });
        videoView.requestFocus();
        videoView.start();
    }

    private void releaseVideo(int index) {
        View itemView = recyclerView.getChildAt(index);
        final IjkVideoView videoView = itemView.findViewById(R.id.video_view);
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
