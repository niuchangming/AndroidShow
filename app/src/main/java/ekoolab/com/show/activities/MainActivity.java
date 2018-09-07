package ekoolab.com.show.activities;

import android.animation.ValueAnimator;
import android.support.v4.app.Fragment;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.R;
import ekoolab.com.show.views.TabButton;
import ekoolab.com.show.fragments.TabFragment;

import static ekoolab.com.show.utils.AuthUtils.AuthType.LOGGED;

public class MainActivity extends BaseActivity implements TabFragment.OnTabBarSelectedListener {

    private TabFragment tabFragment;

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
