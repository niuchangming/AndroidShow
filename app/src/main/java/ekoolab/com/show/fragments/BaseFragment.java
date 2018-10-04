package ekoolab.com.show.fragments;

import android.annotation.SuppressLint;
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
    public RxPermissions rxPermissions;
    protected boolean isVisible;
    private boolean isPrepared;
    protected boolean isFirstLoad = true;
    private boolean waitingShowToUser = false;

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
        mViewHolder = new ViewHolder(inflater, container, getLayoutId());
        initViews(mViewHolder, mViewHolder.getRootView());
        isPrepared = true;
        return mViewHolder.getRootView();
    }

    /**
     * 如果是与ViewPager一起使用，调用的是setUserVisibleHint
     *
     * @param isVisibleToUser 是否显示出来了
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
       /* if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }*/
        if (isVisibleToUser) {
            // 父Fragment还没显示，你着什么急
            Fragment parentFragment = getParentFragment();
            if (parentFragment != null && !parentFragment.getUserVisibleHint()) {
                waitingShowToUser = true;
                super.setUserVisibleHint(false);
//                isVisible = false;
//                onInvisible();
            } else {
                isVisible = true;
                onVisible();
            }
        }
        if (getActivity() != null) {
            @SuppressLint("RestrictedApi")
            List<Fragment> childFragmentList = getChildFragmentManager().getFragments();
            if (isVisibleToUser) {
                // 将所有正等待显示的子Fragment设置为显示状态，并取消等待显示标记
                if (childFragmentList != null && childFragmentList.size() > 0) {
                    for (Fragment childFragment : childFragmentList) {
                        if (childFragment instanceof BaseFragment) {
                            BaseFragment childBaseFragment = (BaseFragment) childFragment;
                            if (childBaseFragment.isWaitingShowToUser()) {
                                childBaseFragment.setWaitingShowToUser(false);
                                childFragment.setUserVisibleHint(true);
                            }
                        }
                    }
                }
            } else {
                // 将所有正在显示的子Fragment设置为隐藏状态，并设置一个等待显示标记
                if (childFragmentList != null && childFragmentList.size() > 0) {
                    for (Fragment childFragment : childFragmentList) {
                        if (childFragment instanceof BaseFragment) {
                            BaseFragment childBaseFragment = (BaseFragment) childFragment;
                            if (childFragment.getUserVisibleHint()) {
                                childBaseFragment.setWaitingShowToUser(true);
                                childFragment.setUserVisibleHint(false);
                            }
                        }
                    }
                } else {
                    isVisible = false;
                    onInvisible();
                }
            }
        }
    }

    /**
     * 如果是通过FragmentTransaction的show和hide的方法来控制显示，调用的是onHiddenChanged.
     * 若是初始就show的Fragment 为了触发该事件 需要先hide再show
     *
     * @param hidden hidden True if the fragment is now hidden, false if it is not
     *               visible.
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }


    public void onVisible() {
        if (mViewHolder != null) {
            lazyLoad(mViewHolder.getRootView());
        }
    }

    public void onInvisible() {
    }

    /**
     * 要实现延迟加载Fragment内容,需要在 onCreateView
     * isPrepared = true;
     */
    private void lazyLoad(View view) {
        if (!isPrepared || !isVisible || !isFirstLoad) {
            //if (!isAdded() || !isVisible || !isFirstLoad) {
            return;
        }
        isFirstLoad = false;
        lazyLoadData(view);
    }

    public void lazyLoadData(View view) {
    }

    /**
     * 先走initViews方法，再走initData方法
     *
     * @param holder
     * @param root
     */
    protected abstract void initViews(ViewHolder holder, View root);

    protected void initData() {

    }

    protected void lazyInitData() {

    }

    public boolean isWaitingShowToUser() {
        return waitingShowToUser;
    }

    public void setWaitingShowToUser(boolean waitingShowToUser) {
        this.waitingShowToUser = waitingShowToUser;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 如果自己是显示状态，但父Fragment却是隐藏状态，就把自己也改为隐藏状态，并且设置一个等待显示标记
        if (getUserVisibleHint()) {
            Fragment parentFragment = getParentFragment();
            if (parentFragment != null && !parentFragment.getUserVisibleHint()) {
                waitingShowToUser = true;
                super.setUserVisibleHint(false);
            }
        }
        initData();
        if (mViewHolder != null) {
            lazyLoad(mViewHolder.getRootView());
        }
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
