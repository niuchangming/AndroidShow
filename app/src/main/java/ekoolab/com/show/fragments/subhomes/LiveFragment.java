package ekoolab.com.show.fragments.subhomes;

import android.content.Context;
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
import ekoolab.com.show.beans.Live;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

public class LiveFragment extends BaseFragment implements OnRefreshLoadMoreListener {
    private EmptyView emptyView;
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private BaseQuickAdapter<Live, BaseViewHolder> adapter;
    private int pageIndex;
    private long curRequestTime;
    private List<Live> lives = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live;
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        emptyView = holder.get(R.id.empty_view);

        recyclerView = holder.get(R.id.recycler_view);
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

        getLiveData(true);
    }

    private void bindAdapter(RecyclerView recyclerView) {
        this.adapter = new BaseQuickAdapter<Live, BaseViewHolder>(R.layout.item_live_list, lives) {
            @Override
            protected void convert(BaseViewHolder helper, Live item) {
                ImageLoader.displayImage(item.avatar.small, helper.getView(R.id.cover_iv));
                helper.setText(R.id.live_type_tv, getContext().getString(R.string.live));
                helper.setImageResource(R.id.live_type_iv, R.mipmap.camera_red);
                helper.setText(R.id.name_tv, item.nickname);
                helper.setText(R.id.audience_amount_tv, item.audienceCount + "");
            }
        };
        this.adapter.setHasStableIds(false);
    }

    private void getLiveData(boolean isInitial) {
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
        ApiServer.basePostRequest(this, Constants.LIVE_LIST, map,
                new TypeToken<ResponseData<List<Live>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Live>>() {
                    @Override
                    protected void onSuccess(List<Live> liveList) {
                        Logger.i("Live Data List: " + liveList.size());
                        if (ListUtils.isNotEmpty(liveList)) {
                            if (isInitial) {
                                lives.clear();
                            }
                            lives.addAll(liveList);
                            if (isInitial) {
                                adapter.notifyDataSetChanged();
                            } else {
                                adapter.notifyItemRangeInserted(lives.size() - liveList.size(),
                                        liveList.size());
                            }
                            emptyView.showContent();
                            if (liveList.size() < Constants.PAGE_SIZE) {
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
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        getLiveData(false);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getLiveData(true);
    }
}
