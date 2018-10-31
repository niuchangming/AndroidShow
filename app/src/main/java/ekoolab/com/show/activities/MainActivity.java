package ekoolab.com.show.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.army.gifdemo.GifHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.CameraActivity;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.orhanobut.logger.Logger;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.LoggingMXBean;

import ekoolab.com.show.R;
import ekoolab.com.show.Services.FriendService;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.dialogs.DialogViewHolder;
import ekoolab.com.show.dialogs.XXDialog;
import ekoolab.com.show.fragments.ChatListFragment;
import ekoolab.com.show.fragments.TabFragment;
import ekoolab.com.show.fragments.subhomes.MomentFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Chat.ChatManager;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageSeclctUtils;
import ekoolab.com.show.utils.LocalBinder;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.TabButton;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends BaseActivity implements TabFragment.OnTabBarSelectedListener,
        MomentFragment.OnInteractivePlayGifListener, GifHandler.OnGifPlayFinishListener, AnimationListener {

    public static final String BUNDLE_ERROR_MSG = "bundle_error_msg";
    private TabFragment tabFragment;
    private XXDialog xxDialog = null;
    private GifImageView ivPlayGif;
    private LinkedList<String> animImages = new LinkedList<>();
    private ArrayList<LocalMedia> localMedias = new ArrayList<>();
    public FriendService friendService;

    private final ServiceConnection friendServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            friendService = ((LocalBinder<FriendService>) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            friendService = null;
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AuthUtils.LOGGED_IN)) {
                LoginData loginData = intent.getParcelableExtra(LoginActivity.LOGIN_DATA);
                loginSBirdChat(loginData);
            }else if (intent.getAction().equals(FriendService.CONTACT_UPLOADED)) {
                ChatListFragment chatFragment = (ChatListFragment) tabFragment.getTabChat().getFragment();
                if (chatFragment != null) {
                    chatFragment.friendSyncCompleted();
                }
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tabFragment = new TabFragment();
        addFragment(R.id.bottom_bar, tabFragment);
        tabFragment.setup(R.id.main_container, this);
        ivPlayGif = findViewById(R.id.iv_play_gif);
        ivPlayGif.getLayoutParams().height = DisplayUtils.getScreenWidth();
        if (getIntent().hasExtra(BUNDLE_ERROR_MSG)) {
            Logger.e(getIntent().getStringExtra(BUNDLE_ERROR_MSG));
        }
    }

    @Override
    protected void initData() {
        super.initData();

        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (!aBoolean) {
                        ToastUtils.showToast(getString(R.string.permission_storage));
                    }
                });

        loginSBirdChat(null);
    }

    private void loginSBirdChat(LoginData loginData){
        ChatManager.getInstance(this).login(new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (user != null && loginData != null){
                    String displayName = Utils.getDisplayName(loginData.name, loginData.nickName);
                    ChatManager.getInstance(MainActivity.this).updateCurrentUserInfo(displayName, loginData.avatar.small);
                    ChatManager.getInstance(MainActivity.this).registerDeviceTokenWithSBird();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AuthUtils.LOGGED_IN);
        filter.addAction(FriendService.CONTACT_UPLOADED);
        this.registerReceiver(broadcastReceiver, filter);

        if (authorized(false)) {
            startFriendService();
        }
    }

    private void startFriendService(){
        rxPermissions.request(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE)
                .subscribe(granted -> {
                    if (!granted) {
                        ToastUtils.showToast(getString(R.string.permission_contact));
                    } else {
                        bindService(new Intent(MainActivity.this, FriendService.class), friendServiceConn, Context.BIND_AUTO_CREATE);
                        FriendService.isBinded = true;
                    }
                });
    }

    @Override
    public void onReselect(TabButton navigationButton) {
        // get each tab fragment instance by navigationButton.getFragment()
    }

    @Override
    public void onCenterCameraClick() {
        if (!authorized(true)){ return; }

        if (xxDialog == null) {
            xxDialog = new XXDialog(this, R.layout.dialog_choose_content) {
                @Override
                public void convert(DialogViewHolder holder) {
                    holder.setOnClick(R.id.tv_video, view -> {
                        cancelDialog();
                        gotoTakeVideo();
                    });
                    holder.setOnClick(R.id.tv_text, view -> {
                        cancelDialog();
                        gotoTakeText();
                    });
                    holder.setOnClick(R.id.tv_pictures, view -> {
                        cancelDialog();
                        gotoTakePicture();
                    });
                    holder.setOnClick(R.id.tv_cancel, view -> cancelDialog());
                }
            }.fromBottom().fullWidth();
        }
        xxDialog.showDialog();
    }

    private void gotoTakeText() {
        Intent intent = new Intent(this, PostTextActivity.class);
        startActivity(intent);

    }

    private void gotoTakePicture() {
        localMedias.clear();
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        ImageSeclctUtils.openBulm(MainActivity.this, localMedias);
                    }
                });
    }


    private void gotoTakeVideo() {
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        CameraActivity.navToCameraOnlyVideoThenJump(MainActivity.this,
                                Constants.VIDEO_PATH, Constants.IMAGE_PATH, PostVideoActivity.class);
                    }
                });
    }

    @Override
    public void playGif(String imageUrl) {
        if (ivPlayGif.getVisibility() == View.VISIBLE) {
            animImages.offer(imageUrl);
        } else {
            ivPlayGif.setVisibility(View.VISIBLE);
            loadAndPlayGif(imageUrl);
        }
    }

    private void loadAndPlayGif(String imageUrl) {
        Glide.with(this).download(imageUrl).into(new CustomViewTarget<ImageView, File>(ivPlayGif) {
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {

            }

            @Override
            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                ivPlayGif.setImageURI(Uri.fromFile(resource));
                ((GifDrawable) ivPlayGif.getDrawable()).addAnimationListener(MainActivity.this);
            }

            @Override
            protected void onResourceCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    @Override
    public void onFinish() {

    }

    @Override
    protected void onDestroy() {
        if (friendServiceConn != null && FriendService.isBinded) {
            unbindService(friendServiceConn);
            FriendService.isBinded = false;
        }
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onAnimationCompleted(int loopNumber) {
        if (Utils.isNotEmpty(animImages)) {
            String imageUrl = animImages.pollFirst();
            if (!TextUtils.isEmpty(imageUrl)) {
                loadAndPlayGif(imageUrl);
            } else {
                onAnimationCompleted(1);
            }
        } else {
            ivPlayGif.setImageResource(0);
            ivPlayGif.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PictureConfig.CHOOSE_REQUEST && resultCode == RESULT_OK) {
            localMedias.addAll(PictureSelector.obtainMultipleResult(data));
            if (!localMedias.isEmpty()) {
                Intent intent = new Intent(this, PostPictureActivity.class);
                intent.putParcelableArrayListExtra("url", localMedias);
                startActivity(intent);
            }
        }
    }
}
