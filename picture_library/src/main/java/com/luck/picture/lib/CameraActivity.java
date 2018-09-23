package com.luck.picture.lib;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.luck.picture.lib.cameralibrary.JCameraView;
import com.luck.picture.lib.cameralibrary.listener.ErrorListener;
import com.luck.picture.lib.cameralibrary.listener.JCameraListener;
import com.luck.picture.lib.cameralibrary.util.FileUtil;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.utils.AppManager;

import java.io.File;


public class CameraActivity extends AppCompatActivity {
    private JCameraView jCameraView;
    private String imgOutPath = "";
    public static final String EXTRA_VIDEO_DURATION = "extra_videoDuration";
    public static final String EXTRA_VIDEO_OUTPATH = "extra_videoOutPath";
    public static final String EXTRA_IMG_OUTPATH = "extra_imgOutPath";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_JUMP_CLASS = "extra_jump_class";
    public static final String EXTRA_VIDEO_PATH = "extra_video_path";
    public static final String EXTRA_IMAGE_PATH = "extra_image_path";
    public static final int REQUEST_CODE = 987;
    private Class<?> jumpClass = null;

    public static void navToCameraOnlyVideo(Activity context, String videoPath, String imagePath) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(EXTRA_VIDEO_OUTPATH, videoPath);
        intent.putExtra(EXTRA_IMG_OUTPATH, imagePath);
        intent.putExtra(EXTRA_TYPE, PictureConfig.TYPE_VIDEO);
        context.startActivityForResult(intent, REQUEST_CODE);
    }

    public static void navToCameraOnlyVideoThenJump(Activity context, String videoPath, String imagePath,
                                                    Class<?> jumpClass) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(EXTRA_VIDEO_OUTPATH, videoPath);
        intent.putExtra(EXTRA_IMG_OUTPATH, imagePath);
        intent.putExtra(EXTRA_TYPE, PictureConfig.TYPE_VIDEO);
        intent.putExtra(EXTRA_JUMP_CLASS, jumpClass);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_camera);
        jCameraView = (JCameraView) findViewById(R.id.jcameraview);
        //设置视频保存路径
        jCameraView.setSaveVideoPath(getIntent().getStringExtra(EXTRA_VIDEO_OUTPATH));
        imgOutPath = getIntent().getStringExtra(EXTRA_IMG_OUTPATH);
        long videoDuration = getIntent().getLongExtra(EXTRA_VIDEO_DURATION, PictureConfig.DAFAULT_VIDEO_DURATION);
        jCameraView.setVideoDuration(videoDuration);
        int type = getIntent().getIntExtra(EXTRA_TYPE, -1);
        if (type == PictureConfig.TYPE_ALL) {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);
            jCameraView.setTip("轻触拍照，长按摄像");
        } else if (type == PictureConfig.TYPE_IMAGE) {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_CAPTURE);
        } else if (type == PictureConfig.TYPE_VIDEO) {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_RECORDER);
            jCameraView.setTip("长按摄像");
        }
        jumpClass = (Class<?>) getIntent().getSerializableExtra(EXTRA_JUMP_CLASS);
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
//                Intent intent = new Intent();
//                intent.putExtra("path", ""); //路径为空说明出错了前面判断
//                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
            }
        });
        //JCameraView监听
        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
                String dir;
                if (TextUtils.isEmpty(imgOutPath)) {
                    dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PictureSelector/CameraImage/";
                } else {
                    dir = imgOutPath;
                }
                String path = FileUtil.saveBitmap(dir, bitmap);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_VIDEO_PATH, path);
                setResult(PictureSelectorActivity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
                Intent intent = new Intent();
                intent.putExtra(EXTRA_VIDEO_PATH, url);
                if (jumpClass != null) {
                    String imagePath = FileUtil.saveBitmap(imgOutPath, firstFrame);
                    intent.putExtra(EXTRA_IMAGE_PATH, imagePath);
                    intent.setClass(CameraActivity.this, jumpClass);
                    startActivity(intent);
                } else {
                    setResult(PictureSelectorActivity.RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void quit() {
                //退出按钮
                CameraActivity.this.finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        jCameraView.onDestory();
        super.onDestroy();
        AppManager.getInstance().killActivity(this);
    }
}
