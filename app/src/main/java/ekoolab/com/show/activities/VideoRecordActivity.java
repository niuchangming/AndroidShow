package ekoolab.com.show.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.faceunity.FURenderer;
import com.faceunity.encoder.MediaAudioEncoder;
import com.faceunity.encoder.MediaEncoder;
import com.faceunity.encoder.MediaMuxerWrapper;
import com.faceunity.encoder.MediaVideoEncoder;
import com.faceunity.utils.Constant;
import com.faceunity.utils.MiscUtil;
import com.luck.picture.lib.cameralibrary.util.AngleUtil;
import com.luck.picture.lib.cameralibrary.util.FileUtil;
import com.luck.picture.lib.utils.ThreadExecutorManager;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ekoolab.com.show.R;
import ekoolab.com.show.beauty.renderer.CameraRenderer;
import ekoolab.com.show.beauty.ui.BeautyControlView;
import ekoolab.com.show.beauty.ui.RecordBtn;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.FileUtils;

import static android.graphics.Bitmap.createBitmap;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

public class VideoRecordActivity extends BaseActivity implements View.OnClickListener,
        CameraRenderer.OnRendererStatusListener, SensorEventListener {

    protected GLSurfaceView mGLSurfaceView;
    protected CameraRenderer mCameraRenderer;
    protected RecordBtn recordBtn;
    protected ImageButton flipBtn;
    protected ImageButton beautyBtn;
    protected Button recordCancelBtn;
    protected Button recordOkBtn;
    protected BeautyControlView beautyBar;

    private FURenderer mFURenderer;
    private ArrayList<String> framePaths;

    private int angle = 0;
    private long mStartTime = 0;
    private int frameRate = 3;
    private int frameCalledIndex = 0;
    private boolean isRecording = false;
    private File mOutFile;
    private MediaVideoEncoder mVideoEncoder;
    private MediaMuxerWrapper mMuxer;

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder) {
                final MediaVideoEncoder videoEncoder = (MediaVideoEncoder) encoder;
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        videoEncoder.setEglContext(EGL14.eglGetCurrentContext());
                        mVideoEncoder = videoEncoder;
                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordBtn.setSecond(0);
                    }
                });
            }

        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            mVideoEncoder = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordBtn.setSecond(mStartTime = 0);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mOutFile)));
                }
            });
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_record;
    }

    @Override
    protected void initData() {
        super.initData();
        framePaths = new ArrayList<>();
        mFURenderer = new FURenderer
                .Builder(this)
                .maxFaces(4)
                .inputTextureType(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
                .createEGLContext(false)
                .needReadBackImage(false)
                .defaultEffect(null)
                .build();
    }



    @Override
    protected void initViews() {
        super.initViews();

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setOnClickListener(this);
        mCameraRenderer = new CameraRenderer(this, mGLSurfaceView, this);
        mGLSurfaceView.setRenderer(mCameraRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        beautyBar = findViewById(R.id.beauty_bar);

        flipBtn = findViewById(R.id.camera_flip_btn);
        flipBtn.setOnClickListener(this);

        beautyBtn = findViewById(R.id.beauty_btn);
        beautyBtn.setOnClickListener(this);

        recordCancelBtn = findViewById(R.id.record_cancel_btn);
        recordCancelBtn.setOnClickListener(this);

        recordOkBtn = findViewById(R.id.record_ok_btn);
        recordOkBtn.setOnClickListener(this);

        recordBtn = findViewById(R.id.record_video_btn);
        recordBtn.setOnRecordListener(new RecordBtn.OnRecordListener() {
            @Override
            public void takePic() { }

            @Override
            public void startRecord() {
                isRecording = true;
                startRecording();
            }

            @Override
            public void stopRecord() {
                isRecording = false;
                stopRecording();

            }
        });

        beautyBar.setOnFUControlListener(mFURenderer);
        beautyBar.setOnBottomAnimatorChangeListener(new BeautyControlView.OnBottomAnimatorChangeListener() {
            @Override
            public void onBottomAnimatorChangeListener(float showRate) {
                recordBtn.setDrawWidth((int) (getResources().getDimensionPixelSize(R.dimen.x166) * (1 - showRate * 0.265)));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraRenderer.onCreate();
        mCameraRenderer.onResume();
        if (beautyBar != null) {
            beautyBar.onResume();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.camera_flip_btn:
                mCameraRenderer.changeCamera();
                break;
            case R.id.beauty_btn:
                if (beautyBar.getHeight() == 0){
                    beautyBar.show();
                }else{
                    beautyBar.hide();
                }
                break;
            case R.id.gl_surface_view:
                if (beautyBar.getHeight() > 0){
                    beautyBar.hide();
                }
                break;
            case R.id.record_cancel_btn:
                reset();

                recordCancelBtn.setVisibility(View.GONE);
                recordOkBtn.setVisibility(View.GONE);
                recordBtn.setVisibility(View.VISIBLE);
                break;
            case R.id.record_ok_btn:
                recordCancelBtn.setVisibility(View.GONE);
                recordOkBtn.setVisibility(View.GONE);
                recordBtn.setVisibility(View.VISIBLE);

                goNext();
                break;
        }
    }

    protected void sendRecordingData(int texId, final float[] tex_matrix, final long timeStamp) {
        if (mVideoEncoder != null) {
            mVideoEncoder.frameAvailableSoon(texId, tex_matrix);
            if (mStartTime == 0) mStartTime = timeStamp;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordBtn.setSecond(timeStamp - mStartTime);
                }
            });
        }
    }

    private void generateFrameBitmap(final byte[] frameData, String framePath) {
        ThreadExecutorManager.getInstance().runInThreadPool(() -> {
            try {
                Camera.Parameters parameters = mCameraRenderer.getmCamera().getParameters();
                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;
                YuvImage yuv = new YuvImage(frameData, parameters.getPreviewFormat(), width, height, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
                byte[] bytes = out.toByteArray();
                Bitmap frame = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Matrix matrix = new Matrix();
                if (mCameraRenderer.getmCurrentCameraType() == CAMERA_FACING_FRONT) {
                    if(angle == 0){
                        matrix.setRotate(270);
                        matrix.postScale(-1, 1, frame.getWidth() / 2, frame.getHeight() / 2);
                    }
                }else{
                    if(angle == 0){
                        matrix.setRotate(90);
                    }
                }
                frame = createBitmap(frame, 0, 0, frame.getWidth(), frame
                        .getHeight(), matrix, true);
                File file = new File(framePath);
                FileUtil.createDir(file.getParent());
                frame.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFURenderer.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public int onDrawFrame(byte[] cameraNV21Byte, int cameraTextureId, int cameraWidth, int cameraHeight, float[] mtx, long timeStamp) {
        int fuTextureId = mFURenderer.onDrawFrame(cameraNV21Byte, cameraTextureId, cameraWidth, cameraHeight);
        sendRecordingData(fuTextureId, mtx, timeStamp / Constant.NANO_IN_ONE_MILLI_SECOND);
        if (isRecording) {
            if(frameCalledIndex%frameRate == 0){
                String name = "frame_" + System.currentTimeMillis() + ".jpg";
                generateFrameBitmap(cameraNV21Byte, Constants.IMAGE_PATH + name);
                framePaths.add(Constants.IMAGE_PATH + name);
            }
        }
        frameCalledIndex++;
        return fuTextureId;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
            return;
        }
        float[] values = event.values;
        angle = AngleUtil.getSensorAngle(values[0], values[1]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSurfaceDestroy() {
        mFURenderer.onSurfaceDestroyed();
    }

    @Override
    public void onCameraChange(int currentCameraType, int cameraOrientation) {
        mFURenderer.onCameraChange(currentCameraType, cameraOrientation);
    }

    private void startRecording() {
        reset();
        try {
            String videoFileName = MiscUtil.getCurrentDate() + ".mp4";
            mOutFile = new File(Constants.VIDEO_PATH, videoFileName);
            mMuxer = new MediaMuxerWrapper(mOutFile.getAbsolutePath());

            // for video capturing
            new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mCameraRenderer.getCameraHeight(), mCameraRenderer.getCameraWidth());
            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);

            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            Logger.e(e.getLocalizedMessage());
        }
    }

    private void stopRecording() {
        if (mMuxer != null) {
            mMuxer.stopRecording();
        }

        recordCancelBtn.setVisibility(View.VISIBLE);
        recordOkBtn.setVisibility(View.VISIBLE);
        recordBtn.setVisibility(View.INVISIBLE);

        frameCalledIndex = 0;
        System.gc();
    }

    private void reset(){
        if(framePaths != null && framePaths.size() > 0){
            framePaths.clear();
            FileUtils.deleteFiles(framePaths);
        }

        if(mOutFile != null && mOutFile.exists()){
            FileUtils.deleteFile(mOutFile);
        }

        mMuxer = null;
    }

    private void goNext(){
        Intent intent = new Intent(VideoRecordActivity.this, ChooseCoverActivity.class);
        intent.putExtra(ChooseCoverActivity.VIDEO_PATH, mOutFile.getPath());
        if (framePaths.size() > 0){
            intent.putExtra(ChooseCoverActivity.FIRST_FRAME_PATH, framePaths.get(0));
        }
        if(framePaths.size() > 1){
            framePaths.remove(0);
            intent.putStringArrayListExtra(ChooseCoverActivity.VIDEO_FRAME_PATHS, framePaths);
        }
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraRenderer.onPause();
        mCameraRenderer.onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
