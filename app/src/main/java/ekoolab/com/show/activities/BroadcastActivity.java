package ekoolab.com.show.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.faceunity.FURenderer;
import com.google.gson.reflect.TypeToken;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.orhanobut.logger.Logger;
import com.santalu.emptyview.EmptyView;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.OpenMessageAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.ChatMessage;
import ekoolab.com.show.beans.enums.MessageType;
import ekoolab.com.show.beauty.renderer.CameraRenderer;
import ekoolab.com.show.beauty.renderer.LiveCapture;
import ekoolab.com.show.beauty.ui.BeautyControlView;
import ekoolab.com.show.dialogs.CommentDialog;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Chat.ChatManager;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.UIHandler;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.ShowVideoCapture;
import ekoolab.com.show.views.ShowVideoRender;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

import static com.sendbird.android.SendBird.ConnectionState.OPEN;

public class BroadcastActivity extends BaseActivity implements
        View.OnClickListener,
        Session.SessionListener,
        PublisherKit.PublisherListener,
        ShowVideoCapture.ShowVideoCaptureListener,
        ChatManager.ChatManagerListener {

    protected EmptyView emptyView;
    protected GLSurfaceView glSurfaceView;
    protected FrameLayout publisherContainer;
    protected ImageButton flipBtn;
    protected ImageButton dismissBtn;
    protected ImageButton beautyBtn;
    protected ImageButton commentBtn;

    private BeautyControlView mBeautyControlView;
    private FURenderer mFURenderer;
    private CameraRenderer cameraRenderer;
    private boolean isPermissionAllowed = false;

    private String sessionId;
    private String token;
    private String broadcastId;
    private Session mSession;
    private Publisher mPublisher;
    private LiveCapture liveCapture;
    private ShowVideoRender showVideoRender;

    private OpenChannel openChannel;
    private List<BaseMessage> openMessages = null;
    private RecyclerView chatRecyclerView;
    private CommentDialog commentDialog = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broadcast;
    }

    @Override
    protected void initData() {
        super.initData();
        openMessages = new ArrayList<>();

        ChatManager.getInstance(this).register(this);
        if(SendBird.getConnectionState() == OPEN){
            connectOpenChannel();
        }else{
            ChatManager.getInstance(this).login(new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    if (e == null) {
                        connectOpenChannel();
                    }
                }
            });
        }
    }

    private void connectOpenChannel(){
        String channelUrl = AuthUtils.getInstance(this).getChannelUrl();
        if(Utils.isBlank(channelUrl)){
            obtainChannelUrl();
        }else{
            getChannel(channelUrl);
        }
    }

    @Override
    protected void initViews() {
        super.initViews();
        mFURenderer = new FURenderer
                .Builder(this)
                .maxFaces(4)
                .inputTextureType(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
                .createEGLContext(true)
                .needReadBackImage(false)
                .defaultEffect(null)
                .build();

        emptyView = findViewById(R.id.empty_view);
        glSurfaceView = findViewById(R.id.gl_surface_view);
        publisherContainer = findViewById(R.id.publisher_container);

        flipBtn = findViewById(R.id.camera_flip_btn);
        flipBtn.setOnClickListener(this);

        dismissBtn = findViewById(R.id.dismiss_btn);
        dismissBtn.setOnClickListener(this);

        beautyBtn = findViewById(R.id.beauty_btn);
        beautyBtn.setOnClickListener(this);

        commentBtn = findViewById(R.id.comment_btn);
        commentBtn.setOnClickListener(this);

        mBeautyControlView = findViewById(R.id.fu_beauty_control);
        mBeautyControlView.setOnFUControlListener(mFURenderer);
        mBeautyControlView.setOnBottomAnimatorChangeListener(new BeautyControlView.OnBottomAnimatorChangeListener() {
            @Override
            public void onBottomAnimatorChangeListener(float showRate) {

            }
        });

        chatRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.addItemDecoration(new LinearItemDecoration(this,
                0, R.color.colorLightGray, 0));
        chatRecyclerView.setAdapter(new OpenMessageAdapter(this, openMessages));

        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .subscribe(granted -> {
                    isPermissionAllowed = granted;
                    if (!granted) {
                        emptyView.builder().setErrorText(getString(R.string.permission_video_audio));
                        emptyView.showError();
                    } else {
                        emptyView.showContent();
                        requestBroadcastInfo();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBeautyControlView != null) {
            mBeautyControlView.onResume();
        }
    }

    @Override
    public void handleFrame(byte[] data, int cameraWidth, int cameraHeight) {
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    // Start TokBox
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
        liveCapture = new LiveCapture(this, mFURenderer);
        showVideoRender = new ShowVideoRender(this, glSurfaceView);
        mPublisher = new Publisher.Builder(this)
                .capturer(liveCapture)
                .renderer(showVideoRender)
                .build();
        mPublisher.setPublisherListener(this);
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

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
                        try {
                            JSONObject broadcastObj = response.getJSONObject("broadcastUrls");
                            String hls = broadcastObj.getString("hls");
                            broadcastId = response.getString("id");
                            afterBroadcast(broadcastId, hls);
                            Logger.i("Hls------> " + hls);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
            case R.id.camera_flip_btn:;

                break;
            case R.id.dismiss_btn:
                finish();
                break;
            case R.id.comment_btn:
                showCommentDialog();
                break;
            case R.id.beauty_btn:
                mBeautyControlView.show();
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
        Logger.i("Publish destroy");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Logger.i("Publish error: " + opentokError.getMessage());
    }

    @Override
    protected void onDestroy() {
        disconnectSession();
        stopBroadcast();
        ChatManager.getInstance(this).unregister(this);
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

    private void showCommentDialog() {
        if (commentDialog == null) {
            commentDialog = new CommentDialog(this);
            commentDialog.setOnClickListener(content -> {
                sendChat(content);
                commentDialog.dismiss();
                commentDialog.clearText();
            });
        }
        commentDialog.show();
        UIHandler.getInstance().postDelayed(() -> commentDialog.showKeyboard(), 150L);
    }

    private void sendChat(String content){
        if(openChannel != null){
            List<String> targetLanguages = new ArrayList<>();
            targetLanguages.add("zh-CHS");
            openChannel.sendUserMessage(content, "", MessageType.TEXT.getName(), targetLanguages, new BaseChannel.SendUserMessageHandler() {
                @Override
                public void onSent(UserMessage userMessage, SendBirdException e) {
                    if (e == null) {
                        openMessages.add(userMessage);
                        chatRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                }
            });

        }
    }

    private void getChannel(String channelUrl){
        OpenChannel.getChannel(channelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
                if(e == null){
                    BroadcastActivity.this.openChannel = openChannel;
                    enterChannel();
                }
            }
        });
    }

    private void obtainChannelUrl(){
        OpenChannel.createChannel(new OpenChannel.OpenChannelCreateHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
                if(e == null){
                    BroadcastActivity.this.openChannel = openChannel;
                    AuthUtils.getInstance(BroadcastActivity.this).saveChannelUrl(openChannel.getUrl());
                    uploadChannelUrl(openChannel.getUrl());
                    enterChannel();
                }
            }
        });
    }

    private void enterChannel(){
        if (this.openChannel != null) {
            this.openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    Logger.i("Enter Open Channel Failed");
                }
            });
        }
    }

    private void uploadChannelUrl(String channelUrl){
        HashMap<String, String> map = new HashMap<>();
        map.put("token", AuthUtils.getInstance(this).getApiToken());
        map.put("channelId", channelUrl);

        ApiServer.basePostRequest(this, Constants.UPLOAD_CHANNEL_URL, map, new TypeToken<ResponseData<String>>(){})
                .subscribe(new NetworkSubscriber<String>(){
                    @Override
                    protected void onSuccess(String s) {
                        Logger.i("Upload ChannelUrl Success");
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void afterBroadcast(String broadcastId, String hls){
        HashMap<String, String> map = new HashMap<>();
        map.put("broadcastId", broadcastId);
        map.put("resourceUri", hls);
        map.put("token", AuthUtils.getInstance(this).getApiToken());

        ApiServer.basePostRequest(this, Constants.UPLOAD_BROADCAST_INFO, map, new TypeToken<ResponseData<String>>(){
        }).subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {

                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void stopBroadcast(){
        String url = "https://api.opentok.com/v2/project/" + Constants.TOKBOX_APP_ID + "/broadcast/" + broadcastId + "/stop";

        HashMap<String, String> headers = new HashMap<>();
        headers.put("X-OPENTOK-AUTH", Utils.getJWTString(20));
        headers.put("Content-Type", "application/json");

        HashMap<String, String> params = new HashMap<>();
        params.put("sessionId", sessionId);

        try {
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
                            Logger.i("Broadcast stopped");
                        }
                        @Override
                        public void onError(ANError error) {
                            Logger.e("Stop Broadcast error: " + error.getLocalizedMessage());
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didReceivedMessage(ChatMessage chatMessage) { }

    @Override
    public void didReceivedOpenMessage(BaseMessage baseMessage) {
        if (Utils.equals(openChannel.getUrl(), baseMessage.getChannelUrl())){
            openMessages.add(baseMessage);
            chatRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

}



























