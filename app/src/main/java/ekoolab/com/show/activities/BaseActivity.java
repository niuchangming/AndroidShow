package ekoolab.com.show.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.luck.picture.lib.utils.AppManager;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Utils;

import static ekoolab.com.show.utils.AuthUtils.AuthType.LOGGED;

public abstract class BaseActivity extends AppCompatActivity {
    public static final String IS_FULL_SCREEN = "is_full_screen";
    private boolean isFullScreen;
    private Toast mToast;
    private Fragment mFragment;
    private long mBackPressedTime;
    protected FrameLayout activityContainer;
    private List<TurnBackListener> mTurnBackListeners = new ArrayList<>();
    public RxPermissions rxPermissions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_base);
        rxPermissions = new RxPermissions(this);

        isFullScreen = getIntent().getBooleanExtra(IS_FULL_SCREEN, false);
        if (isFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData();
        initViews();
    }

    @LayoutRes
    protected abstract int getLayoutId();

    /**
     * 初始化数据，调用位置在 initViews 之前
     */
    protected void initData() {}

    protected void initViews() {
        if (getLayoutId() == 0) return;
        activityContainer = findViewById(R.id.activity_container);
        activityContainer.addView(LayoutInflater.from(this).inflate(getLayoutId(), activityContainer, false));
    }

    public void toastLong(String text) {
        toast(text, Toast.LENGTH_LONG);
    }

    private void toast(final String text, final int duration) {
        if (!TextUtils.isEmpty(text)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mToast == null) {
                        mToast = Toast.makeText(getApplicationContext(), text, duration);
                    } else {
                        mToast.setText(text);
                        mToast.setDuration(duration);
                    }
                    mToast.show();
                }
            });
        }
    }

    public Boolean authorized(boolean showLogin){
        AuthUtils.AuthType authType = AuthUtils.getInstance(this).loginState();
        String apiToken = AuthUtils.getInstance(this).getApiToken();
        if (authType != LOGGED || Utils.isBlank(apiToken)) {
            if (showLogin){
                Intent intent = new Intent(this, SMSLoginActivity.class);
                startActivity(intent);
            }
            return false;
        }
        return true;
    }

    protected void addFragment(int frameLayoutId, Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (fragment.isAdded()) {
                if (mFragment != null) {
                    transaction.hide(mFragment).show(fragment);
                } else {
                    transaction.show(fragment);
                }
            } else {
                if (mFragment != null) {
                    transaction.hide(mFragment).add(frameLayoutId, fragment);
                } else {
                    transaction.add(frameLayoutId, fragment);
                }
            }
            mFragment = fragment;
            transaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    public void addOnTurnBackListener(TurnBackListener l) {
        this.mTurnBackListeners.add(l);
    }

    @Override
    public void onBackPressed() {
        for (TurnBackListener l : mTurnBackListeners) {
            if (l.onTurnBack()) return;
        }

        if (this instanceof MainActivity) {
            long curTime = SystemClock.uptimeMillis();
            if ((curTime - mBackPressedTime) < (3 * 1000)) {
                finish();
            } else {
                mBackPressedTime = curTime;
                toastLong(this.getString(R.string.tip_double_click_exit));
            }
        } else {
            super.onBackPressed();
        }
    }

    public interface TurnBackListener {
        boolean onTurnBack();
    }

    public <T> AutoDisposeConverter<T> autoDisposable() {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getInstance().killActivity(this);
    }
}
