package ekoolab.com.show.beauty.renderer;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.View;

import com.faceunity.FURenderer;
import com.faceunity.gles.ProgramTexture2d;
import com.faceunity.gles.ProgramTextureOES;
import com.faceunity.gles.core.GlUtil;
import com.opentok.android.BaseVideoRenderer;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LiveRenderer extends BaseVideoRenderer implements GLSurfaceView.Renderer, LiveCapture.LiveCaptureListener{
    private GLSurfaceView mGLSurfaceView;
    private FURenderer mFURenderer;

    private int mViewWidth = 1280;
    private int mViewHeight = 720;
    private int mCameraWidth = 1280;
    private int mCameraHeight = 720;

    private byte[] mCameraNV21Byte;
    private SurfaceTexture mSurfaceTexture;
    private int mCameraTextureId;

    private int mFuTextureId;
    private final float[] mtx = new float[16];
    private float[] mvp = new float[16];
    private ProgramTexture2d mFullFrameRectTexture2D;
    private ProgramTextureOES mTextureOES;

    public LiveRenderer(GLSurfaceView glSurfaceView, FURenderer fuRenderer) {
        this.mGLSurfaceView = glSurfaceView;
        this.mGLSurfaceView.setEGLContextClientVersion(2);
        this.mGLSurfaceView.setRenderer(this);
        this.mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        this.mFURenderer = fuRenderer;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFullFrameRectTexture2D = new ProgramTexture2d();
        mTextureOES = new ProgramTextureOES();
        mCameraTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

        mFURenderer.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, mViewWidth = width, mViewHeight = height);
        mvp = GlUtil.changeMVPMatrix(GlUtil.IDENTITY_MATRIX, mViewWidth, mViewHeight, mCameraHeight, mCameraWidth);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mtx);
        } catch (Exception e) {
            return;
        }
        if (mCameraNV21Byte == null) {
            mFullFrameRectTexture2D.drawFrame(mFuTextureId, mtx, mvp);
            return;
        }
        mFuTextureId = mFURenderer.onDrawFrame(mCameraNV21Byte, mCameraTextureId, mCameraWidth, mCameraHeight);
        //用于屏蔽切换调用SDK处理数据方法导致的绿屏（切换SDK处理数据方法是用于展示，实际使用中无需切换，故无需调用做这个判断,直接使用else分支绘制即可）
        if (mFuTextureId <= 0) {
            mTextureOES.drawFrame(mCameraTextureId, mtx, mvp);
        } else {
            mFullFrameRectTexture2D.drawFrame(mFuTextureId, mtx, mvp);
        }
    }

    public void onDestroy() {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.release();
                    mSurfaceTexture = null;
                }

                if (mCameraTextureId != 0) {
                    int[] textures = new int[]{mCameraTextureId};
                    GLES20.glDeleteTextures(1, textures, 0);
                    mCameraTextureId = 0;
                }

                if (mFullFrameRectTexture2D != null) {
                    mFullFrameRectTexture2D.release();
                    mFullFrameRectTexture2D = null;
                }
            }
        });

        mGLSurfaceView.onPause();
        mFURenderer.onSurfaceDestroyed();
    }

    @Override
    public void cameraStart(Camera camera) {
        try {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }

            mSurfaceTexture = new SurfaceTexture(1);
            camera.setPreviewTexture(mSurfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cameraStop() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    @Override
    public void onDrawFrame(byte[] data) {
        mCameraNV21Byte = data;
        mGLSurfaceView.requestRender();
    }

    // Interface BaseVideoRenderer Start
    @Override
    public void onFrame(Frame frame) {

    }

    @Override
    public void setStyle(String s, String s1) {

    }

    @Override
    public void onVideoPropertiesChanged(boolean b) {

    }

    @Override
    public View getView() {
        return mGLSurfaceView;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {
        this.mGLSurfaceView.onResume();
    }

    // Interface BaseVideoRenderer End

}
