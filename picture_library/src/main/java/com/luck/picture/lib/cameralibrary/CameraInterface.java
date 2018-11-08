package com.luck.picture.lib.cameralibrary;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import com.luck.picture.lib.cameralibrary.listener.ErrorListener;
import com.luck.picture.lib.cameralibrary.util.AngleUtil;
import com.luck.picture.lib.cameralibrary.util.CameraParamUtil;
import com.luck.picture.lib.cameralibrary.util.CheckPermission;
import com.luck.picture.lib.cameralibrary.util.DeviceUtil;
import com.luck.picture.lib.cameralibrary.util.FileUtil;
import com.luck.picture.lib.cameralibrary.util.LogUtil;
import com.luck.picture.lib.cameralibrary.util.ScreenUtils;
import com.luck.picture.lib.utils.ScheduledExecutorManager;
import com.luck.picture.lib.utils.ThreadExecutorManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.graphics.Bitmap.createBitmap;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/25
 * 描    述：camera操作单例
 * =====================================
 */
@SuppressWarnings("deprecation")
public class CameraInterface implements Camera.PreviewCallback {

    private static final String TAG = "CJT";
    private Context mContext;
    private int widthSize;
    private int heightSize;

    private volatile static CameraInterface mCameraInterface;

    public static void destroyCameraInterface() {
        if (mCameraInterface != null) {
            mCameraInterface = null;
        }
    }

    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;

    private int SELECTED_CAMERA = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int CAMERA_POST_POSITION = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int CAMERA_FRONT_POSITION = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private SurfaceHolder mHolder = null;
    private float screenProp = -1.0f;

    private boolean isRecorder = false;
    private MediaRecorder mediaRecorder;
    private String videoFileName;
    private String saveVideoPath;
    private String saveImagePath;
    private String videoFileAbsPath;
    private String videoFirstFramePath = null;

    private ErrorListener errorLisenter;

    private ImageView mSwitchView;

    private int preview_width;
    private int preview_height;

    private int angle = 0;
    private int cameraAngle = 90;//摄像头角度   默认为90度
    private int rotation = 0;
    private byte[] frameData;
    private int recordAngle = 0;

    public static final int TYPE_RECORDER = 0x090;
    public static final int TYPE_CAPTURE = 0x091;
    private int nowScaleRate = 0;
    private int recordScleRate = 0;

    //视频质量
    private int mediaQuality = JCameraView.MEDIA_QUALITY_MIDDLE;
    private SensorManager sm = null;
    private List<String> framePaths = new ArrayList<>(20);

    private Runnable captureRunnable = new Runnable() {
        @Override
        public void run() {
            mCamera.setOneShotPreviewCallback(CameraInterface.this);
        }
    };

    //获取CameraInterface单例
    public static synchronized CameraInterface getInstance() {
        if (mCameraInterface == null) {
            synchronized (CameraInterface.class) {
                if (mCameraInterface == null) {
                    mCameraInterface = new CameraInterface();
                }
            }
        }
        return mCameraInterface;
    }

    public void setSwitchView(ImageView mSwitchView) {
        this.mSwitchView = mSwitchView;
        if (mSwitchView != null) {
            cameraAngle = CameraParamUtil.getInstance().getCameraDisplayOrientation(mSwitchView.getContext(),
                    SELECTED_CAMERA);
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }
            float[] values = event.values;
            angle = AngleUtil.getSensorAngle(values[0], values[1]);
            rotationAnimation();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    //切换摄像头icon跟随手机角度进行旋转
    private void rotationAnimation() {
        if (mSwitchView == null) {
            return;
        }
        if (rotation != angle) {
            int start_rotaion = 0;
            int end_rotation = 0;
            switch (rotation) {
                case 0:
                    start_rotaion = 0;
                    if (angle == 90) {
                        end_rotation = -90;
                    } else if (angle == 270) {
                        end_rotation = 90;
                    }
                    break;
                case 90:
                    start_rotaion = -90;
                    if (angle == 0) {
                        end_rotation = 0;
                    } else if (angle == 180) {
                        end_rotation = -180;
                    }
                    break;
                case 180:
                    start_rotaion = 180;
                    if (angle == 90) {
                        end_rotation = 270;
                    } else if (angle == 270) {
                        end_rotation = 90;
                    }
                    break;
                case 270:
                    start_rotaion = 90;
                    if (angle == 0) {
                        end_rotation = 0;
                    } else if (angle == 180) {
                        end_rotation = 180;
                    }
                    break;
                default:
                    break;
            }
            ObjectAnimator anim = ObjectAnimator.ofFloat(mSwitchView, "rotation", start_rotaion, end_rotation);
            anim.setDuration(500);
            anim.start();
            rotation = angle;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void setSaveVideoPath(String saveVideoPath) {
        this.saveVideoPath = saveVideoPath;
        File file = new File(saveVideoPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void setSaveImagePath(String saveImagePath) {
        this.saveImagePath = saveImagePath;
        File file = new File(saveImagePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public void setZoom(float zoom, int type) {
        if (mCamera == null) {
            return;
        }
        if (mParams == null) {
            mParams = mCamera.getParameters();
        }
        if (!mParams.isZoomSupported() || !mParams.isSmoothZoomSupported()) {
            return;
        }
        switch (type) {
            case TYPE_RECORDER:
                //如果不是录制视频中，上滑不会缩放
                if (!isRecorder) {
                    return;
                }
                if (zoom >= 0) {
                    //每移动50个像素缩放一个级别
                    int scaleRate = (int) (zoom / 40);
                    if (scaleRate <= mParams.getMaxZoom() && scaleRate >= nowScaleRate && recordScleRate != scaleRate) {
                        mParams.setZoom(scaleRate);
                        mCamera.setParameters(mParams);
                        recordScleRate = scaleRate;
                    }
                }
                break;
            case TYPE_CAPTURE:
                if (isRecorder) {
                    return;
                }
                //每移动50个像素缩放一个级别
                int scaleRate = (int) (zoom / 50);
                if (scaleRate < mParams.getMaxZoom()) {
                    nowScaleRate += scaleRate;
                    if (nowScaleRate < 0) {
                        nowScaleRate = 0;
                    } else if (nowScaleRate > mParams.getMaxZoom()) {
                        nowScaleRate = mParams.getMaxZoom();
                    }
                    mParams.setZoom(nowScaleRate);
                    mCamera.setParameters(mParams);
                }
                break;
        }

    }

    void setMediaQuality(int quality) {
        this.mediaQuality = quality;
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        frameData = data;
        if (isRecorder) {
            String path = String.format(Locale.getDefault(),"%sframe_%d.jpeg", saveImagePath,
                    System.currentTimeMillis());
            framePaths.add(path);
            generateFrameBitmap(recordAngle, frameData, path);
        }
    }


    public interface CameraOpenOverCallback {
        void cameraHasOpened();
    }

    private CameraInterface() {
        findAvailableCameras();
        SELECTED_CAMERA = CAMERA_FRONT_POSITION;
        saveVideoPath = "";
    }


    /**
     * open Camera
     */
    void doOpenCamera(CameraOpenOverCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!CheckPermission.isCameraUseable(SELECTED_CAMERA) && this.errorLisenter != null) {
                this.errorLisenter.onError();
                return;
            }
        }
        if (mCamera == null) {
            openCamera(SELECTED_CAMERA);
        }
        callback.cameraHasOpened();
    }

    private synchronized void openCamera(int id) {
        try {
            this.mCamera = Camera.open(id);
        } catch (Exception var3) {
            var3.printStackTrace();
            if (this.errorLisenter != null) {
                this.errorLisenter.onError();
            }
        }

        if (Build.VERSION.SDK_INT > 17 && this.mCamera != null) {
            try {
                this.mCamera.enableShutterSound(false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("CJT", "enable shutter sound faild");
            }
        }
    }

    public synchronized void switchCamera(SurfaceHolder holder, float screenProp) {
        if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
            SELECTED_CAMERA = CAMERA_FRONT_POSITION;
        } else {
            SELECTED_CAMERA = CAMERA_POST_POSITION;
        }
        doDestroyCamera();
        LogUtil.i("open start");
        openCamera(SELECTED_CAMERA);
//        mCamera = Camera.open();
        if (Build.VERSION.SDK_INT > 17 && this.mCamera != null) {
            try {
                this.mCamera.enableShutterSound(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogUtil.i("open end");
        doStartPreview(holder, screenProp);
    }

    /**
     * doStartPreview
     */
    public void doStartPreview(SurfaceHolder holder, float screenProp) {
        if (isPreviewing) {
            LogUtil.i("doStartPreview isPreviewing");
        }
        if (this.screenProp < 0) {
            this.screenProp = screenProp;
        }
        if (holder == null) {
            return;
        }
        this.mHolder = holder;
        if (mCamera != null) {
            try {
                mParams = mCamera.getParameters();
//                Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(mParams
//                        .getSupportedPreviewSizes(), 1200, screenProp);
                Camera.Size previewSize = CameraParamUtil.getInstance().getCloselyPreSize(true, widthSize, heightSize, mParams.getSupportedPreviewSizes());

                Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(mParams
                        .getSupportedPictureSizes(), 1200, screenProp);
                mParams.setPreviewSize(previewSize.width, previewSize.height);

                preview_width = previewSize.width;
                preview_height = previewSize.height;

                mParams.setPictureSize(pictureSize.width, pictureSize.height);

                if (CameraParamUtil.getInstance().isSupportedFocusMode(
                        mParams.getSupportedFocusModes(),
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                if (CameraParamUtil.getInstance().isSupportedPictureFormats(mParams.getSupportedPictureFormats(),
                        ImageFormat.JPEG)) {
                    mParams.setPictureFormat(ImageFormat.JPEG);
                    mParams.setJpegQuality(100);
                }
                mCamera.setParameters(mParams);
                mParams = mCamera.getParameters();
                mCamera.setPreviewDisplay(holder);  //SurfaceView
                mCamera.setDisplayOrientation(cameraAngle);//浏览角度
                mCamera.setPreviewCallback(this); //每一帧回调
                mCamera.startPreview();//启动浏览
                isPreviewing = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止预览
     */
    public void doStopPreview() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.setPreviewDisplay(null);
                isPreviewing = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 销毁Camera
     */
    void doDestroyCamera() {
        errorLisenter = null;
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mSwitchView = null;
                mCamera.stopPreview();
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.setPreviewDisplay(null);
                mHolder = null;
                isPreviewing = false;
                mCamera.release();
                mCamera = null;
//                destroyCameraInterface();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    /**
     * 拍照
     */
    private int nowAngle;

    public void takePicture(final TakePictureCallback callback) {
        if (mCamera == null) {
            return;
        }
        switch (cameraAngle) {
            case 90:
                nowAngle = Math.abs(angle + cameraAngle) % 360;
                break;
            case 270:
                nowAngle = Math.abs(cameraAngle - angle);
                break;
        }
//
        Log.i("CJT", angle + " = " + cameraAngle + " = " + nowAngle);
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                    matrix.setRotate(nowAngle);
                } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                    matrix.setRotate(360 - nowAngle);
                    matrix.postScale(-1, 1);
                }

                bitmap = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (callback != null) {
                    if (nowAngle == 90 || nowAngle == 270) {
                        callback.captureResult(bitmap, true);
                    } else {
                        callback.captureResult(bitmap, false);
                    }
                }
            }
        });
    }

    //启动录像
    public void startRecord(Surface surface, float screenProp, ErrorCallback callback) {
        recordAngle  = (angle + 90) % 360;
        //获取第一帧图片
        videoFirstFramePath = String.format(Locale.getDefault(), "%sfirstframe_%d.jpeg",
                saveImagePath, System.currentTimeMillis());
        generateFrameBitmap(recordAngle, frameData, videoFirstFramePath);

        if (isRecorder) {
            return;
        }
        if (mCamera == null) {
            openCamera(SELECTED_CAMERA);
        }
        mCamera.setPreviewCallback(null);
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        if (mParams == null) {
            mParams = mCamera.getParameters();
        }
        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(mParams);
        mCamera.unlock();
        mediaRecorder.reset();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);


        Camera.Size videoSize;
        if (mParams.getSupportedVideoSizes() == null) {
            videoSize = CameraParamUtil.getInstance().getPreviewSize(mParams.getSupportedPreviewSizes(), 1200,
                    screenProp);
        } else {
            videoSize = CameraParamUtil.getInstance().getPreviewSize(mParams.getSupportedVideoSizes(), 1200,
                    screenProp);
        }
        Log.i(TAG, "setVideoSize    width = " + videoSize.width + "\theight = " + videoSize.height);
        if (videoSize.width == videoSize.height) {
            mediaRecorder.setVideoSize(preview_width, preview_height);
        } else {
            mediaRecorder.setVideoSize(videoSize.width, videoSize.height);
        }

        if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
            //手机预览倒立的处理
            if (cameraAngle == 270) {
                //横屏
                if (recordAngle == 0) {
                    mediaRecorder.setOrientationHint(180);
                } else if (recordAngle == 270) {
                    mediaRecorder.setOrientationHint(270);
                } else {
                    mediaRecorder.setOrientationHint(90);
                }
            } else {
                if (recordAngle == 90) {
                    mediaRecorder.setOrientationHint(270);
                } else if (recordAngle == 270) {
                    mediaRecorder.setOrientationHint(90);
                } else {
                    mediaRecorder.setOrientationHint(recordAngle);
                }
            }
        } else {
            mediaRecorder.setOrientationHint(recordAngle);
        }


        if (DeviceUtil.isHuaWeiRongyao()) {
            mediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
        } else {
            mediaRecorder.setVideoEncodingBitRate(mediaQuality);
        }
        mediaRecorder.setPreviewDisplay(surface);

        videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        if (saveVideoPath.equals("")) {
            saveVideoPath = Environment.getExternalStorageDirectory().getPath();
        }
        videoFileAbsPath = saveVideoPath + videoFileName;
        FileUtil.createDir(saveVideoPath);
        mediaRecorder.setOutputFile(videoFileAbsPath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecorder = true;
            framePaths.clear();
            ScheduledExecutorManager.getInstance().schedule(captureRunnable, 200, 1000);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            if (this.errorLisenter != null) {
                this.errorLisenter.onError();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (this.errorLisenter != null) {
                this.errorLisenter.onError();
            }
        }
    }

    private void generateFrameBitmap(int nowAngle, final byte[] frameData, String framePath) {
        ThreadExecutorManager.getInstance().runInThreadPool(() -> {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                int width = parameters.getPreviewSize().width;
                int height = parameters.getPreviewSize().height;
                YuvImage yuv = new YuvImage(frameData, parameters.getPreviewFormat(), width, height, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
                byte[] bytes = out.toByteArray();
                Bitmap frame = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Matrix matrix = new Matrix();
                if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                    matrix.setRotate(nowAngle);
                } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                    matrix.setRotate(270);
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

    //停止录像
    public void stopRecord(boolean isShort, StopRecordCallback callback) {
        if (!isRecorder) {
            return;
        }
        ScheduledExecutorManager.getInstance().cancelSchedule(captureRunnable);
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
                mediaRecorder = null;
                mediaRecorder = new MediaRecorder();
            } finally {
                if (mediaRecorder != null) {
                    mediaRecorder.release();
                }
                mediaRecorder = null;
                isRecorder = false;
            }
            if (isShort) {
                if (FileUtil.deleteFile(videoFileAbsPath)) {
                    callback.recordResult(null, null, framePaths);
                }
                return;
            }
            doStopPreview();
            String fileName = saveVideoPath + videoFileName;
            callback.recordResult(fileName, videoFirstFramePath, framePaths);
        }
    }

    private void findAvailableCameras() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraNum = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.getCameraInfo(i, info);
            switch (info.facing) {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    CAMERA_FRONT_POSITION = info.facing;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    CAMERA_POST_POSITION = info.facing;
                    break;
            }
        }
    }

    int handlerTime = 0;

    public void handleFocus(final Context context, final float x, final float y, final FocusCallback callback) {
        if (mCamera == null) {
            return;
        }
        final Camera.Parameters params = mCamera.getParameters();
        Rect focusRect = calculateTapArea(x, y, 1f, context);
        mCamera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
            callback.focusSuccess();
            return;
        }
        final String currentFocusMode = params.getFocusMode();
        try {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success || handlerTime > 10) {
                        Camera.Parameters params = camera.getParameters();
                        params.setFocusMode(currentFocusMode);
                        camera.setParameters(params);
                        handlerTime = 0;
                        callback.focusSuccess();
                    } else {
                        handlerTime++;
                        handleFocus(context, x, y, callback);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "autoFocus failer");
        }
    }


    private static Rect calculateTapArea(float x, float y, float coefficient, Context context) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / ScreenUtils.getScreenHeight(context) * 2000 - 1000);
        int centerY = (int) (y / ScreenUtils.getScreenWidth(context) * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF
                .bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    void setErrorLinsenter(ErrorListener errorLisenter) {
        this.errorLisenter = errorLisenter;
    }


    public interface StopRecordCallback {
        void recordResult(String url, String firstFramePath, List<String> framePaths);
    }

    interface ErrorCallback {
        void onError();
    }

    public interface TakePictureCallback {
        void captureResult(Bitmap bitmap, boolean isVertical);
    }

    public interface FocusCallback {
        void focusSuccess();

    }


    void registerSensorManager(Context context) {
        if (sm == null) {
            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sm.registerListener(sensorEventListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
                .SENSOR_DELAY_NORMAL);
    }

    void unregisterSensorManager(Context context) {
        if (sm == null) {
            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sm.unregisterListener(sensorEventListener);
    }

    void isPreview(boolean res) {
        this.isPreviewing = res;
    }

    void setW(float widthSize) {
        this.widthSize = (int) widthSize;
    }

    void setH(float heightSize) {
        this.heightSize = (int) heightSize;
    }
}
