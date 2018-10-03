package ekoolab.com.show.activities;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.army.gifdemo.GifHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.CameraActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.LinkedList;

import ekoolab.com.show.R;
import ekoolab.com.show.dialogs.DialogViewHolder;
import ekoolab.com.show.dialogs.XXDialog;
import ekoolab.com.show.fragments.TabFragment;
import ekoolab.com.show.fragments.subhomes.MomentFragment;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.views.TabButton;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends BaseActivity implements TabFragment.OnTabBarSelectedListener,
        MomentFragment.OnInteractivePlayGifListener, GifHandler.OnGifPlayFinishListener, AnimationListener {

    private TabFragment tabFragment;
    private XXDialog xxDialog = null;
    private GifImageView ivPlayGif;
    private LinkedList<String> animImages = new LinkedList<>();
//    private GifHandler gifHandler = null;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onReselect(TabButton navigationButton) {
        Fragment fragment = navigationButton.getFragment();

    }

    @Override
    public void onCenterCameraClick() {
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
                    holder.setOnClick(R.id.tv_pictures,view -> {
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
        Intent intent = new Intent(this,PostTextActivity.class);
        startActivity(intent);

    }

    private void gotoTakePicture() {
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        CameraActivity.navToCameraOnlyVideoThenJump(MainActivity.this,
                                Constants.VIDEO_PATH, Constants.IMAGE_PATH, PostVideoActivity.class);
//                        startActivity(new Intent(MainActivity.this, PostVideoActivity.class));
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
//                        startActivity(new Intent(MainActivity.this, PostVideoActivity.class));
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(EventBusMsg eventBusMsg) {
        showOrHideNavAnim(eventBusMsg.getFlag());
    }


    private void showOrHideNavAnim(int flag) {
//        if (flag == RefreshRecyclerFragment.SCROLL_STATE_UP) {
//            hideBottomNav(mNavBar.getView());
//        } else if (flag == RefreshRecyclerFragment.SCROLL_STATE_DOWN) {
//            showBottomNav(mNavBar.getView());
//        }

    }

    private void showBottomNav(final View mTarget) {
        ValueAnimator va = ValueAnimator.ofFloat(mTarget.getY(), 0);
        va.setDuration(200);
        va.addUpdateListener(valueAnimator -> mTarget.setY((Float) valueAnimator.getAnimatedValue()));
        va.start();
    }

    private void hideBottomNav(final View mTarget) {
        ValueAnimator va = ValueAnimator.ofFloat(mTarget.getY(), mTarget.getHeight());
        va.setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTarget.setY((Float) valueAnimator.getAnimatedValue());
            }
        });

        va.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
//                if (gifHandler == null) {
//                    gifHandler = new GifHandler(resource.getAbsolutePath(), ivPlayGif);
//                    gifHandler.setFinishListener(MainActivity.this);
//                } else {
//                    gifHandler.resetGif(resource.getAbsolutePath());
//                }
//                gifHandler.startPlayGifOnce();
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
//        if (gifHandler != null) {
//            gifHandler.stop();
//        }
        super.onDestroy();
    }

    @Override
    public void onAnimationCompleted(int loopNumber) {
        if (ListUtils.isNotEmpty(animImages)) {
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
}
