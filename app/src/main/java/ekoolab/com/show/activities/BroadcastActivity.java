package ekoolab.com.show.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.aware.PublishConfig;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
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

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.ShowVideoCapturer;

public class BroadcastActivity extends BaseActivity implements View.OnClickListener, Session.SessionListener, PublisherKit.PublisherListener{

    private EmptyView emptyView;
    private FrameLayout publisherContainer;
    private boolean isPermissionAllowed = false;

    private String sessionId;
    private String token;
    private Session mSession;
    private Publisher mPublisher;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broadcast;
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

        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .subscribe(granted -> {
                    isPermissionAllowed = granted;
                    if (!granted) {
                        emptyView.builder().setErrorText(getString(R.string.permission_video_audio));
                        emptyView.showError();
                    } else {
                        requestBroadcastInfo();
                    }
                });
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

        publisherContainer.addView(mPublisher.getView());
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
                        Logger.i("--------> " + response.toString());
                    }
                    @Override
                    public void onError(ANError error) {
                        Logger.e("--------> " + error.getLocalizedMessage());
                    }
                });

//        ApiServer.basePostRequest(this, url, headers, params, new TypeToken<ResponseData<HashMap<String, String>>>(){
//        }).subscribe(new NetworkSubscriber<HashMap<String, String>>() {
//            @Override
//            protected void onSuccess(HashMap<String, String> mapResponseData) {
//                Logger.i("Broadcast Info: " + mapResponseData);
//                toastLong(getString(R.string.broadcasting));
//            }
//
//            @Override
//            protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
//
//                return super.dealHttpException(code, errorMsg, e);
//            }
//        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.empty_button:
                if(isPermissionAllowed){
//                    requestBroadcastInfo();
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
            publisherContainer.removeView(mPublisher.getView());
            mSession.unpublish(mPublisher);
            mPublisher.destroy();
            mPublisher = null;
        }
        mSession.disconnect();
    }
}



























