package ekoolab.com.show.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.List;

import ekoolab.com.show.utils.ViewHolder;

public abstract class BaseFragment extends Fragment {
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    protected Context mContext;
    private ViewHolder mViewHolder;
    private View mRoot;
    public RxPermissions rxPermissions;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
        rxPermissions = new RxPermissions(this);
        //初始化广播
        initBroadcastAction();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @LayoutRes
    protected abstract int getLayoutId();

    public ViewHolder getViewHolder() {
        return mViewHolder;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
            }
        } else {
            mViewHolder = new ViewHolder(inflater, container, getLayoutId());
            mRoot = mViewHolder.getRootView();
            initViews(mViewHolder, mViewHolder.getRootView());
        }
        return mViewHolder.getRootView();
    }

    /**
     * 先走initViews方法，再走initData方法
     * @param holder
     * @param root
     */
    protected abstract void initViews(ViewHolder holder, View root);

    protected void initData() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    protected void toast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mViewHolder.getRootView() != null) {
            ((ViewGroup) mViewHolder.getRootView().getParent()).removeView(mViewHolder.getRootView());
        }
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null && mContext != null) {
            //取消注册广播
            mContext.unregisterReceiver(broadcastReceiver);
        }
        if (localBroadcastReceiver != null && mContext != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(localBroadcastReceiver);
        }
        super.onDestroy();
    }

    public <T> AutoDisposeConverter<T> autoDisposable() {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this));
    }

    /**
     * 子类添加的action
     */
    public List<String> getBroadcastAction() {
        return null;
    }

    public List<String> getLocalBroadcastAction() {  //钩子函数
        return null;
    }

    public void dealWithBroadcastAction(Context context, Intent intent) {
    }


    // 处理系统发出的广播
    private BroadcastReceiver broadcastReceiver = null, localBroadcastReceiver = null;

    //注册广播
    private void initBroadcastAction() {
        List<String> broadcastAction = getBroadcastAction();
        if (broadcastAction != null && broadcastAction.size() > 0) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    dealWithBroadcastAction(context, intent);
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            for (String action : broadcastAction) {
                intentFilter.addAction(action);
            }
            mContext.registerReceiver(broadcastReceiver, intentFilter);
        }

        List<String> localBroadcastAction = getLocalBroadcastAction();
        if (localBroadcastAction != null && !localBroadcastAction.isEmpty()) {
            localBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    dealWithBroadcastAction(context, intent);
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            for (String action : localBroadcastAction) {
                intentFilter.addAction(action);
            }
            LocalBroadcastManager.getInstance(mContext).registerReceiver(localBroadcastReceiver, intentFilter);
        }
    }
}
