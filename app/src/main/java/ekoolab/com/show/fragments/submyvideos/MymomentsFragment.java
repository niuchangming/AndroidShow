package ekoolab.com.show.fragments.submyvideos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.santalu.emptyview.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.MySingleMomentActivity;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.activities.MySingleMomentActivity;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
//import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;
import ekoolab.com.show.views.ninegridview.NewNineGridlayout;

public class MymomentsFragment extends BaseFragment implements OnRefreshLoadMoreListener {
    private final String TAG = "VideoFragment";
    public static final String ACTION_REFRESH_DATA = "ekoolab.com.show.fragments.submyvideos.MymomentsFragment.refresh_data";
    private int pageIndex;
    private EmptyView mEmptyView;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout llTipsContainer;
    private BaseQuickAdapter<Moment, BaseViewHolder> mAdapter = null;
    private List<Moment> moments = new ArrayList<>(20);
    private String userCode = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_moment;
    }


    @Override
    protected void initData() {
        mEmptyView.showLoading();
        Bundle bundle = this.getArguments();
        if(bundle != null){
            userCode = bundle.getString("userCode");
        }
        loadMyMoment(true);
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        mEmptyView = holder.get(R.id.empty_view);
        recyclerView = holder.get(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new LinearItemDecoration(mContext,
                1, R.color.common_color_gray_lighted, DisplayUtils.dip2px(15)));
        refreshLayout = holder.get(R.id.refreshLayout);
        initRefreshLayout();
        initAdapter();
        mAdapter.setOnItemClickListener((parent, view, position) -> {
            String apiToken = AuthUtils.getInstance(getContext()).getApiToken();
            if (TextUtils.isEmpty(apiToken)) {
                ToastUtils.showToast(R.string.login_first);
                return;
            }
            Moment moment = moments.get(position);
            System.out.println("number of likes: " + moment.likeCount);
            Intent intent = new Intent(getContext(), MySingleMomentActivity.class);
            intent.putExtra("moment", moment);
            getContext().startActivity(intent);
        });
        recyclerView.setAdapter(mAdapter);
        llTipsContainer = holder.get(R.id.ll_tips_container);

    }

    private void initRefreshLayout() {
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshLoadMoreListener(this);
    }

    private void initAdapter() {
        mAdapter = new BaseQuickAdapter<Moment, BaseViewHolder>(R.layout.item_my_moment_list, moments) {

            private int nineTotalWidth = DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(120 * 2);

            @Override
            protected void convert(BaseViewHolder helper, Moment item) {
                helper.setText(R.id.tv_time, TimeUtils.getDateStringByTimeStamp(item.uploadTime));
                helper.setGone(R.id.tv_content, !TextUtils.isEmpty(item.body));
                helper.setText(R.id.tv_content, item.body);
                helper.setGone(R.id.nine_grid_layout, Utils.isNotEmpty(item.photoArray));
                NewNineGridlayout newNineGridlayout = helper.getView(R.id.nine_grid_layout);
                newNineGridlayout.showPic(nineTotalWidth, item.photoArray,
                       null, NewNineGridlayout.PHOTO_QUALITY_SMALL);
            }
        };
        mAdapter.setHasStableIds(false);
    }

    private void loadMyMoment(boolean isRefresh){
        if (isRefresh) {
            pageIndex = 0;
        }
        HashMap<String, String> map = new HashMap<>(4);
        if(userCode!=null){
            map.put("userCode", userCode);
        }
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageIndex", pageIndex + "");
        map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
        ApiServer.basePostRequest(this, Constants.MYMOMENTLIST, map,
                new TypeToken<ResponseData<List<Moment>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Moment>>() {
                    @Override
                    protected void onSuccess(List<Moment> momentList) {
                        System.out.println("===momentList==="+momentList.size()+";pageIndex==="+pageIndex);
                        if (Utils.isNotEmpty(momentList)) {
                            if (isRefresh) {
                                moments.clear();
                            }
                            moments.addAll(momentList);
                            if (isRefresh) {
                                mAdapter.notifyDataSetChanged();
                            } else {
                                mAdapter.notifyItemRangeInserted(moments.size() - momentList.size(),
                                        momentList.size());
                            }
                            mEmptyView.showContent();
                            if (momentList.size() < Constants.PAGE_SIZE) {
                                refreshLayout.setEnableLoadMore(false);
                            } else {
                                pageIndex++;
                            }
                        } else if(moments.size()!=0){
                            refreshLayout.setEnableLoadMore(false);
                        } else{
                            mEmptyView.showEmpty();
                        }
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        if (isRefresh) {
                            mEmptyView.error(e).show();
                        }
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        loadMyMoment(false);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        loadMyMoment(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void dealWithBroadcastAction(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_REFRESH_DATA.equals(action)) {
            loadMyMoment(true);
        }
    }

    @Override
    public List<String> getLocalBroadcastAction() {
        return Arrays.asList(ACTION_REFRESH_DATA);
    }
}
