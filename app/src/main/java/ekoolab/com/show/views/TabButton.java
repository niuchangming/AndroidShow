package ekoolab.com.show.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ekoolab.com.show.R;

public class TabButton extends FrameLayout {
    private Fragment mFragment = null;
    private Class<?> mClx;
    private ImageView mIconView;
    private TextView mTitleView;
    private String mTag;
    private View mRootView;
    private AnimatorSet mAnimator = new AnimatorSet();//组合动画
    public TabButton(Context context) {
        super(context);
        init();
    }

    public TabButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mRootView = inflater.inflate(R.layout.tab_button_layout, this, true);

        mIconView = findViewById(R.id.nav_iv_icon);
        mTitleView = findViewById(R.id.nav_tv_title);
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mIconView.setSelected(selected);
        mTitleView.setSelected(selected);
        if (selected) {
            scaleAnimator(mRootView, 1f, 1.1f);
        } else {
            scaleAnimator(mRootView, 1.1f, 1f);
        }
    }

    public void scaleAnimator(View view, float orignal, float dest) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", orignal, dest);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", orignal, dest);
        mAnimator.setDuration(500);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.play(scaleX).with(scaleY);//两个动画同时开始
        mAnimator.start();
    }


    public void init(@DrawableRes int resId, String strId, Class<?> clx) {
        mIconView.setImageResource(resId);
        mTitleView.setText(strId);
        mClx = clx;
        mTag = mClx.getName();
    }

    public Class<?> getClx() {
        return mClx;
    }

    public Fragment getFragment() {
        return mFragment;
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }

    public String getTag() {
        return mTag;
    }
}
