package ekoolab.com.show.activities;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.juziwl.ijkplayerlib.media.IjkVideoView;
import com.luck.picture.lib.CameraActivity;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import ekoolab.com.show.R;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.FileUtils;
import ekoolab.com.show.utils.UIHandler;
import ekoolab.com.show.views.MyHorizontalScrollView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/24
 * @description 选择封面
 */
public class ChooseCoverActivity extends BaseActivity implements View.OnClickListener, MyHorizontalScrollView.OnScrollChangeListener {
    public static final int IMAGE_WIDTH = DisplayUtils.dip2px(100);
    public static final int HALF_SCREEN_WIDTH = DisplayUtils.getScreenWidth() / 2;
    private IjkVideoView videoView;
    private LinearLayout llScroll;
    private ImageView ivPlay, ivPlayVideo, ivFirstFrame;
    private long videoDuration = 0;
    private MyHorizontalScrollView horizontal;

    private String firstFramePath = "", videoPath = "";
    private List<String> framePaths = null;
    private int totalFramesWidth = 0;
    private boolean isTouchHorizontalScrollView = false;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private TextView currentTime, tvNext;

    @Override
    protected void initData() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        firstFramePath = getIntent().getStringExtra(CameraActivity.EXTRA_IMAGE_PATH);
        videoPath = getIntent().getStringExtra(CameraActivity.EXTRA_VIDEO_PATH);
        framePaths = getIntent().getStringArrayListExtra(CameraActivity.EXTRA_VIDEO_FRAME_PATHS);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String fileLength = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (!TextUtils.isEmpty(fileLength)) {
            videoDuration = Long.parseLong(fileLength);
        }
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    @Override
    protected void initViews() {
        super.initViews();
        videoView = findViewById(R.id.video_view);
        videoView.setOnPreparedListener(iMediaPlayer -> iMediaPlayer.setLooping(true));
        horizontal = findViewById(R.id.horizontal);
        llScroll = findViewById(R.id.ll_scroll);
        ivFirstFrame = findViewById(R.id.iv_first_frame);
        ivPlay = findViewById(R.id.iv_play);
        ivPlay.setOnClickListener(this);
        ivPlayVideo = findViewById(R.id.iv_play_video);
        ivPlayVideo.setOnClickListener(this);
        currentTime = findViewById(R.id.current_time);
        tvNext = findViewById(R.id.tv_next);
        tvNext.setOnClickListener(this);
        Glide.with(this).load(firstFramePath).into(ivFirstFrame);
        videoView.setVideoPath(videoPath);
        initScroll();
    }

    private void initScroll() {
        horizontal.setListener(this);
        horizontal.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isTouchHorizontalScrollView = true;
                videoView.pause();
                ivPlay.setSelected(false);
                UIHandler.getInstance().removeCallbacks(mShowProgress);
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                isTouchHorizontalScrollView = false;
            }
            return false;
        });
        Space space = new Space(this);
        llScroll.addView(space, new LinearLayout.LayoutParams(HALF_SCREEN_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT));
        for (String framePath : framePaths) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            llScroll.addView(imageView, new LinearLayout.LayoutParams(IMAGE_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT));
            Glide.with(this).load(framePath).into(imageView);
        }
        Space space2 = new Space(this);
        llScroll.addView(space2, new LinearLayout.LayoutParams(HALF_SCREEN_WIDTH, ViewGroup.LayoutParams.MATCH_PARENT));
        totalFramesWidth = framePaths.size() * IMAGE_WIDTH;
    }


    @Override
    public void onScrollChange(int scrollX, int oldScrollX) {
        if (isTouchHorizontalScrollView) {
            ivPlayVideo.setVisibility(View.GONE);
            ivFirstFrame.setVisibility(View.GONE);
            int curPosition = (int) ((scrollX * 1f / totalFramesWidth) * videoDuration);
            videoView.seekTo(curPosition);
            currentTime.setText(stringForTime(curPosition));
        }
    }

    private Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (videoView != null && videoView.isPlaying()) {
                UIHandler.getInstance().postDelayed(mShowProgress, 100 - (pos % 100));
            }
        }
    };

    private int setProgress() {
        if (videoView == null || !videoView.isPlaying()) {
            return 0;
        }
        int position = videoView.getCurrentPosition();
        if (horizontal != null) {
            if (videoDuration > 0) {
                float percent = position * 1f / videoDuration;
                horizontal.scrollTo((int) (totalFramesWidth * percent), 0);
            }
        }
        currentTime.setText(stringForTime(position));
        return position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                if (ivPlay.isSelected()) {
                    videoView.pause();
                    UIHandler.getInstance().removeCallbacks(mShowProgress);
                } else {
                    videoView.start();
                    UIHandler.getInstance().post(mShowProgress);
                }
                ivPlay.setSelected(!ivPlay.isSelected());
                break;
            case R.id.iv_play_video:
                videoView.start();
                UIHandler.getInstance().postDelayed(() -> {
                    ivFirstFrame.setVisibility(View.GONE);
                }, 100);
                ivPlayVideo.setVisibility(View.GONE);
                ivPlay.setSelected(true);
                UIHandler.getInstance().post(mShowProgress);
                break;
            case R.id.tv_next:
                Intent intent = new Intent(this, PostVideoActivity.class);
                int currentPosition = videoView.getCurrentPosition();
                if (currentPosition == 0) {
                    intent.putExtra(CameraActivity.EXTRA_IMAGE_PATH, firstFramePath);
                } else if (currentPosition >= videoDuration) {
                    String imagePath = framePaths.remove(framePaths.size() - 1);
                    framePaths.add(firstFramePath);
                    intent.putExtra(CameraActivity.EXTRA_IMAGE_PATH, imagePath);
                } else {
                    int position = (int) (framePaths.size() * currentPosition * 1f / videoDuration);
                    String imagePath = framePaths.remove(position);
                    framePaths.add(firstFramePath);
                    intent.putExtra(CameraActivity.EXTRA_IMAGE_PATH, imagePath);
                }
                intent.putExtra(CameraActivity.EXTRA_VIDEO_PATH, videoPath);
                startActivity(intent);
                FileUtils.deleteFiles(framePaths);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FileUtils.deleteFiles(framePaths);
        super.onBackPressed();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_choose_cover;
    }

    @Override
    protected void onDestroy() {
        UIHandler.getInstance().removeCallbacks(mShowProgress);
        videoView.stopPlayback();
        videoView.release(true);
        super.onDestroy();
    }
}
