package ekoolab.com.show.beauty.renderer;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;

import com.opentok.android.BaseVideoCapturer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import ekoolab.com.show.beauty.utils.CameraUtils;

public class LiveCapture extends BaseVideoCapturer implements Camera.PreviewCallback{
    private Activity mActivity;
    private Camera mCamera;
    private Camera.CameraInfo currentDeviceInfo = null;
    private LiveCaptureListener listener;

    private static final int PREVIEW_BUFFER_COUNT = 3;
    private boolean isCaptureStarted = false;
    private boolean isCapturePaused = false;

    private final Object mCameraLock = new Object();

    private int mCurrentCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int mCameraOrientation;
    private int mCameraWidth = 1280;
    private int mCameraHeight = 720;

    private byte[][] previewCallbackBuffer;
    private int[] captureFpsRange;

    public LiveCapture(Activity activity) {
        mActivity = activity;
    }

    public void setListener(LiveCaptureListener listener) {
        this.listener = listener;
    }

    @Override
    public synchronized void init() {
        currentDeviceInfo = new Camera.CameraInfo();
        int cameraId = 0;
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, currentDeviceInfo);
            if (currentDeviceInfo.facing == mCurrentCameraType) {
                cameraId = i;
                mCamera = Camera.open(i);
                break;
            }
        }

        mCameraOrientation = CameraUtils.getCameraOrientation(cameraId);
        CameraUtils.setCameraDisplayOrientation(mActivity, cameraId, mCamera);

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mCameraWidth, mCameraHeight);
        parameters.setPreviewFormat(ImageFormat.NV21);

        captureFpsRange = findClosestEnclosingFpsRange(30 * 1000,
                parameters.getSupportedPreviewFpsRange());

        parameters.setPreviewFpsRange(captureFpsRange[0], captureFpsRange[1]);
        CameraUtils.setFocusModes(parameters);

        mCamera.setParameters(parameters);
    }

    @Override
    public int startCapture() {
        if (isCaptureStarted || mCamera == null) {
            return -1;
        }

        synchronized (mCameraLock) {
            if (previewCallbackBuffer == null) {
                previewCallbackBuffer = new byte[PREVIEW_BUFFER_COUNT][mCameraWidth * mCameraHeight * 3 / 2];
            }
            for (int i = 0; i < PREVIEW_BUFFER_COUNT; i++) {
                mCamera.addCallbackBuffer(previewCallbackBuffer[i]);
            }
            mCamera.setPreviewCallbackWithBuffer(this);

            if(listener != null){
                listener.cameraStart(mCamera);
            }
        }

        isCaptureStarted = true;
        return 0;
    }

    @Override
    public int stopCapture() {
        try {
            if (listener != null) {
                listener.cameraStop();
            }

            synchronized (mCameraLock) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.setPreviewTexture(null);
                    mCamera.setPreviewCallbackWithBuffer(null);
                    mCamera.release();
                    mCamera = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isCaptureStarted = false;
        return 0;
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean isCaptureStarted() {
        return isCaptureStarted;
    }

    @Override
    public CaptureSettings getCaptureSettings() {
        CaptureSettings settings = new CaptureSettings();

        if (mCamera != null) {
            settings = new CaptureSettings();
            settings.fps = 30;
            settings.width = mCameraWidth;
            settings.height = mCameraHeight;
            settings.format = NV21;
            settings.expectedDelay = 0;
        } else {
            settings.fps = 30;
            settings.width = mCameraWidth;
            settings.height = mCameraHeight;
            settings.format = ARGB;
        }

        return settings;
    }

    @Override
    public void onPause() {
        if (isCaptureStarted) {
            isCapturePaused = true;
            stopCapture();
        }
    }

    @Override
    public void onResume() {
        if (isCapturePaused) {
            init();
            startCapture();
            isCapturePaused = false;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(listener != null){
            listener.onDrawFrame(data);
        }
        provideByteArrayFrame(data, NV21, mCameraWidth, mCameraHeight, mCameraOrientation, isFrontCamera());
        mCamera.addCallbackBuffer(data);
    }

    public void sendData(byte[] data){
//        provideByteArrayFrame(data, NV21, mCameraWidth, mCameraHeight, mCameraOrientation, isFrontCamera());
    }

    public boolean isFrontCamera() {
        if (currentDeviceInfo != null) {
            return currentDeviceInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        return false;
    }

    private int[] findClosestEnclosingFpsRange(int preferredFps, List<int[]> supportedFpsRanges) {
        if (supportedFpsRanges == null || supportedFpsRanges.size() == 0) {
            return new int[]{0, 0};
        }
        /* Because some versions of the Samsung S5 have luminescence issues with 30fps front
           faced cameras, lock to 24 */
        if (isFrontCamera()
                && "samsung-sm-g900a".equals(Build.MODEL.toLowerCase())
                && 30000 == preferredFps) {
            preferredFps = 24000;
        }

        final int fps = preferredFps;
        int[] closestRange = Collections.min(supportedFpsRanges, new Comparator<int[]>() {
            // Progressive penalty if the upper bound is further away than |MAX_FPS_DIFF_THRESHOLD|
            // from requested.
            private static final int MAX_FPS_DIFF_THRESHOLD = 5000;
            private static final int MAX_FPS_LOW_DIFF_WEIGHT = 1;
            private static final int MAX_FPS_HIGH_DIFF_WEIGHT = 3;
            // Progressive penalty if the lower bound is bigger than |MIN_FPS_THRESHOLD|.
            private static final int MIN_FPS_THRESHOLD = 8000;
            private static final int MIN_FPS_LOW_VALUE_WEIGHT = 1;
            private static final int MIN_FPS_HIGH_VALUE_WEIGHT = 4;
            // Use one weight for small |value| less than |threshold|, and another weight above.
            private int progressivePenalty(int value, int threshold, int lowWeight, int highWeight) {
                return (value < threshold)
                        ? value * lowWeight
                        : threshold * lowWeight + (value - threshold) * highWeight;
            }

            private int diff(int[] range) {
                final int minFpsError = progressivePenalty(range[0],
                        MIN_FPS_THRESHOLD, MIN_FPS_LOW_VALUE_WEIGHT, MIN_FPS_HIGH_VALUE_WEIGHT);
                final int maxFpsError = progressivePenalty(Math.abs(fps * 1000 - range[1]),
                        MAX_FPS_DIFF_THRESHOLD, MAX_FPS_LOW_DIFF_WEIGHT, MAX_FPS_HIGH_DIFF_WEIGHT);;
                return minFpsError + maxFpsError;
            }

            @Override
            public int compare(int[] lhs, int[] rhs) {
                return diff(lhs) - diff(rhs);
            }
        });

        return closestRange;
    }

    public interface LiveCaptureListener {
        void cameraStart(Camera camera);
        void cameraStop();
        void onDrawFrame(byte[] data);
    }

}















