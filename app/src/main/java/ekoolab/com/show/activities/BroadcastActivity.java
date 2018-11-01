package ekoolab.com.show.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.faceunity.FURenderer;
import com.faceunity.utils.Constant;
import com.google.gson.reflect.TypeToken;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.orhanobut.logger.Logger;
import com.santalu.emptyview.EmptyView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beauty.FUBaseUIActivity;
import ekoolab.com.show.beauty.ui.BeautyControlView;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.ShowVideoCapturer;

public class BroadcastActivity extends FUBaseUIActivity implements View.OnClickListener, Session.SessionListener, PublisherKit.PublisherListener, ShowVideoCapturer.ShowVideoCaptureListener {
    private BeautyControlView mBeautyControlView;
    private FURenderer mFURenderer;
    private boolean isPermissionAllowed = false;

    private String sessionId;
    private String token;
    private Session mSession;
    private Publisher mPublisher;

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initViews() {
        super.initViews();
        mFURenderer = new FURenderer
                .Builder(this)
                .maxFaces(4)
                .inputTextureType(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
                .createEGLContext(false)
                .needReadBackImage(false)
                .defaultEffect(null)
                .build();

        mBottomViewStub.setLayoutResource(R.layout.layout_fu_beauty);
        mBottomViewStub.inflate();

        mBeautyControlView = (BeautyControlView) findViewById(R.id.fu_beauty_control);
        mBeautyControlView.setOnFUControlListener(mFURenderer);
        mBeautyControlView.setOnBottomAnimatorChangeListener(new BeautyControlView.OnBottomAnimatorChangeListener() {
            @Override
            public void onBottomAnimatorChangeListener(float showRate) {

            }
        });
        mGLSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeautyControlView.hideBottomLayoutAnimator();
            }
        });

        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .subscribe(granted -> {
                    isPermissionAllowed = granted;
                    if (!granted) {
                        emptyView.builder().setErrorText(getString(R.string.permission_video_audio));
                        emptyView.showError();
                    } else {
                        emptyView.showContent();
//                        requestBroadcastInfo();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBeautyControlView != null)
            mBeautyControlView.onResume();
    }

    @Override
    protected void onSensorChanged(int rotation) {
        mFURenderer.setTrackOrientation(rotation);
    }

    @Override
    public void onCameraChange(int currentCameraType, int cameraOrientation) {
        mFURenderer.onCameraChange(currentCameraType, cameraOrientation);
    }

    @Override
    public int onDrawFrame(byte[] cameraNV21Byte, int cameraTextureId, int cameraWidth, int cameraHeight, float[] mtx, long timeStamp) {
        int fuTextureId = mFURenderer.onDrawFrame(cameraNV21Byte, cameraTextureId, cameraWidth, cameraHeight);;
        sendRecordingData(fuTextureId, mtx, timeStamp / Constant.NANO_IN_ONE_MILLI_SECOND);
        return fuTextureId;
    }

    @Override
    public void handleFrame(byte[] data, int cameraWidth, int cameraHeight) {
//        int textureId = mFURenderer.onDrawFrame(data, cameraWidth, cameraHeight);
//        Logger.i("Texture Id: " + textureId);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFURenderer.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy() {
        //通知FU销毁
        mFURenderer.onSurfaceDestroyed();
    }

    private void requestBroadcastInfo(){
        emptyView.showLoading();

        HashMap<String, String> map = new HashMap<>();
        map.put("token", AuthUtils.getInstance(this).getApiToken());
        ApiServer.basePostRequest(this, Constants.BROADCAST_INFO, map,
                new TypeToken<ResponseData<Map<String, String>>>(){
        }).subscribe(new NetworkSubscriber<Map<String, String>>() {
            @Override
            protected void onSuccess(Map<String, String> mapResponseData) {
                sessionId = mapResponseData.get("sessionId");
                token = mapResponseData.get("token");

                emptyView.builder().setLoadingText(getString(R.string.connecting));
                startConnectWithOpenTok();
            }

            @Override
            protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                emptyView.error(e);
                emptyView.showError();
                return super.dealHttpException(code, errorMsg, e);
            }
        });
    }

    private void startConnectWithOpenTok(){
        mSession = new Session.Builder(this, Constants.TOKBOX_APP_ID, sessionId).build();
        mSession.setSessionListener(this);
        mSession.connect(token);
    }

    private void doPublish(){
        mPublisher = new Publisher.Builder(this).build();
        mPublisher = new Publisher.Builder(this)
                .capturer(new ShowVideoCapturer(this, Publisher.CameraCaptureResolution.MEDIUM, Publisher.CameraCaptureFrameRate.FPS_30))
                .build();
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisher.setPublisherListener(this);

//        publisherContainer.addView(mPublisher.getView());
        mSession.publish(mPublisher);
    }

    private void startBroadcast() throws JSONException {
        String url = "https://api.opentok.com/v2/project/" + Constants.TOKBOX_APP_ID + "/broadcast";

        HashMap<String, String> headers = new HashMap<>();
        headers.put("X-OPENTOK-AUTH", Utils.getJWTString(20));
        headers.put("Content-Type", "application/json");

        HashMap<String, String> params = new HashMap<>();
        params.put("sessionId", sessionId);

        JSONObject object = new JSONObject();
        object.put("sessionId", sessionId);

        AndroidNetworking.post(url)
                .addHeaders(headers)
                .addJSONObjectBody(object)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Logger.i("Broadcast info:  " + response.toString());
                    }
                    @Override
                    public void onError(ANError error) {
                        Logger.e("Broadcast error: " + error.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.empty_button:
                if(isPermissionAllowed){
                    requestBroadcastInfo();
                }else{
                   goSettings();
                }
                break;
        }
    }

    private void goSettings(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onConnected(Session session) {
        emptyView.builder().setLoadingText(getString(R.string.connected));
        doPublish();
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        emptyView.showContent();
        toastLong(getString(R.string.publishing));

        try {
            startBroadcast();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    protected void onDestroy() {
        disconnectSession();
        super.onDestroy();
    }

    private void disconnectSession() {
        if (mSession == null) {
            return;
        }

        if (mPublisher != null) {
            mSession.unpublish(mPublisher);
            mPublisher.destroy();
            mPublisher = null;
        }
        mSession.disconnect();
    }

}



























