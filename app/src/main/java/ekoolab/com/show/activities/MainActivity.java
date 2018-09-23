package ekoolab.com.show.activities;

import android.Manifest;
import android.animation.ValueAnimator;
import android.support.v4.app.Fragment;
import android.view.View;

import com.luck.picture.lib.CameraActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ekoolab.com.show.R;
import ekoolab.com.show.dialogs.DialogViewHolder;
import ekoolab.com.show.dialogs.XXDialog;
import ekoolab.com.show.fragments.TabFragment;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.views.TabButton;

public class MainActivity extends BaseActivity implements TabFragment.OnTabBarSelectedListener {

    private TabFragment tabFragment;
    private XXDialog xxDialog = null;

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
                    holder.setOnClick(R.id.tv_cancel, view -> cancelDialog());
                }
            }.fromBottom().fullWidth();
        }
        xxDialog.showDialog();
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
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTarget.setY((Float) valueAnimator.getAnimatedValue());
            }
        });
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
}
