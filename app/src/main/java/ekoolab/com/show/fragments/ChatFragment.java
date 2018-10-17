package ekoolab.com.show.fragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.santalu.emptyview.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

public class ChatFragment extends BaseFragment implements OnRefreshLoadMoreListener, BaseQuickAdapter.OnItemClickListener{
    private EmptyView emptyView;
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private BaseQuickAdapter<Friend, BaseViewHolder> adapter;
    private List<Friend> friends = new ArrayList<>();

    private int pageIndex;
    private long curRequestTime;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        emptyView = holder.get(R.id.empty_view);
        recyclerView=  holder.get(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new LinearItemDecoration(mContext,
                1, R.color.gray_very_light, DisplayUtils.dip2px(15)));

        refreshLayout = holder.get(R.id.refresh_layout);
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshLoadMoreListener(this);

        bindAdapter(recyclerView);
    }

    @Override
    protected void initData() {
        super.initData();
        getFriendData(true);
    }

    private void bindAdapter(RecyclerView recyclerView) {
        this.adapter = new BaseQuickAdapter<Friend, BaseViewHolder>(R.layout.item_chat_list, friends) {
            @Override
            protected void convert(BaseViewHolder helper, Friend item) {
                ImageLoader.displayImageAsCircle(item.avatar.small, helper.getView(R.id.avatar_iv));
                helper.setText(R.id.name_tv, Utils.getDisplayName(item.name, item.nickName));
            }
        };

        this.adapter.setHasStableIds(false);
        this.adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void getFriendData(boolean isInitial){
        if (isInitial) {
            curRequestTime = System.currentTimeMillis();
            pageIndex = 0;

        }
        emptyView.showLoading();
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", curRequestTime + "");
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageIndex", pageIndex + "");
        map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
        ApiServer.basePostRequest(this, Constants.My_FOllOWING, map,
                new TypeToken<ResponseData<List<Friend>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Friend>>() {
                    @Override
                    protected void onSuccess(List<Friend> userList) {
                        Logger.i("User Data List: " + userList.size());
                        if (ListUtils.isNotEmpty(userList)) {
                            if (isInitial) {
                                friends.clear();
                            }
                            friends.addAll(userList);
                            if (isInitial) {
                                adapter.notifyDataSetChanged();
                            } else {
                                adapter.notifyItemRangeInserted(friends.size() - userList.size(),
                                        userList.size());
                            }
                            emptyView.showContent();
                            if (userList.size() < Constants.PAGE_SIZE) {
                                refreshLayout.setEnableLoadMore(false);
                            } else {
                                pageIndex++;
                            }
                        } else{
                            emptyView.showEmpty();
                        }
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        if (isInitial) {
                            emptyView.error(e).show();
                        }
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }
}
