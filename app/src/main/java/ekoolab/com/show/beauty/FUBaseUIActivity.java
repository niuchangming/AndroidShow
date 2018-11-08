package ekoolab.com.show.beauty;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.faceunity.encoder.MediaVideoEncoder;
import com.santalu.emptyview.EmptyView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.beauty.renderer.CameraRenderer;

public abstract class FUBaseUIActivity extends BaseActivity implements View.OnClickListener,
        CameraRenderer.OnRendererStatusListener,
        SensorEventListener {

    protected EmptyView emptyView;
    protected FrameLayout publisherContainer;
    protected ImageButton flipBtn;
    protected ImageButton dismissBtn;
    protected ImageButton beautyBtn;
    protected ImageButton commentBtn;

//    protected GLSurfaceView mGLSurfaceView;
//    protected CameraRenderer mCameraRenderer;
//    private SensorManager mSensorManager;
//    private Sensor mSensor;

    private long mStartTime = 0;
    private MediaVideoEncoder mVideoEncoder;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_fu_base;
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initViews() {
        super.initViews();
        emptyView = findViewById(R.id.empty_view);
        publisherContainer = findViewById(R.id.publisher_container);

        flipBtn = findViewById(R.id.camera_flip_btn);
        flipBtn.setOnClickListener(this);

        dismissBtn = findViewById(R.id.dismiss_btn);
        dismissBtn.setOnClickListener(this);

        beautyBtn = findViewById(R.id.beauty_btn);
        beautyBtn.setOnClickListener(this);

        commentBtn = findViewById(R.id.comment_btn);
        commentBtn.setOnClickListener(this);

//        mGLSurfaceView = findViewById(R.id.fu_base_gl_surface);
//        mGLSurfaceView.setEGLContextClientVersion(2);
//        mCameraRenderer = new CameraRenderer(this, mGLSurfaceView, this);
//        mGLSurfaceView.setRenderer(mCameraRenderer);
//        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//
//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mCameraRenderer.onCreate();
//        mCameraRenderer.onResume();
//        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (Math.abs(x) > 3 || Math.abs(y) > 3) {
                if (Math.abs(x) > Math.abs(y)) {
                    onSensorChanged(x > 0 ? 0 : 180);
                } else {
                    onSensorChanged(y > 0 ? 90 : 270);
                }
            }
        }
    }

    protected abstract void onSensorChanged(int rotation);

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public int onDrawFrame(byte[] cameraNV21Byte, int cameraTextureId, int cameraWidth, int cameraHeight, float[] mtx, long timeStamp) {
        return 0;
    }

    @Override
    public void onSurfaceDestroy() {

    }

    @Override
    public void onCameraChange(int currentCameraType, int cameraOrientation) {

    }

    @Override
    protected void onPause() {
        super.onPause();
//        mSensorManager.unregisterListener(this);
//        mCameraRenderer.onPause();
//        mCameraRenderer.onDestroy();
    }
}
