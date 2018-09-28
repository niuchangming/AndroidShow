package ekoolab.com.show.fragments.subhomes;

import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.santalu.emptyview.EmptyView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;
import ekoolab.com.show.views.ninegridview.NewNineGridlayout;
import me.shihao.library.XRecyclerView;

public class MomentFragment extends BaseFragment {
    private int pageIndex;
    private EmptyView mEmptyView;
    private XRecyclerView mRecyclerView;
    private BaseQuickAdapter<Moment, BaseViewHolder> mAdapter = null;
    private List<Moment> moments = new ArrayList<>(20);

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_moment;
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        mEmptyView = holder.get(R.id.empty_view);
        mRecyclerView = holder.get(R.id.recycler_view);
        mRecyclerView.verticalLayoutManager();
        mRecyclerView.getRecyclerView().addItemDecoration(new LinearItemDecoration(mContext,
                1, R.color.gray_very_light, DisplayUtils.dip2px(15)));
        ((SimpleItemAnimator) mRecyclerView.getRecyclerView().getItemAnimator()).setSupportsChangeAnimations(false);
        initAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnRefreshListener(new XRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMomentData(true);
            }

            @Override
            public void onLoadMore() {
                getMomentData(false);
            }
        });
    }

    @Override
    protected void initData() {
        mEmptyView.showLoading();
        getMomentData(true);
    }

    private void initAdapter() {
        mAdapter = new BaseQuickAdapter<Moment, BaseViewHolder>(R.layout.item_moment_list, moments) {

            private int nineTotalWidth = DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(60 * 2);
            private RequestOptions requestOptions = null;

            @Override
            protected void convert(BaseViewHolder helper, Moment item) {
                if (requestOptions == null) {
                    requestOptions = new RequestOptions();
                    requestOptions.centerCrop();
                }
                Glide.with(MomentFragment.this)
                        .load(item.creator.avatar.small)
                        .apply(requestOptions)
                        .into((ImageView) helper.getView(R.id.iv_icon));
                helper.setText(R.id.tv_name, item.creator.name);
                helper.setText(R.id.tv_time, TimeUtils.formatTime(item.uploadTime));
                helper.setGone(R.id.tv_content, !TextUtils.isEmpty(item.body));
                helper.setText(R.id.tv_content, item.body);
                helper.setGone(R.id.nine_grid_layout, ListUtils.isNotEmpty(item.photoArray));
                NewNineGridlayout newNineGridlayout = helper.getView(R.id.nine_grid_layout);
                newNineGridlayout.showPic(nineTotalWidth, item.photoArray, position -> {

                }, NewNineGridlayout.PHOTO_QUALITY_SMALL);
                ImageView ivHeart = helper.getView(R.id.iv_heart);
                ivHeart.setSelected(item.isMyLike);
                helper.setText(R.id.tv_zan_num, String.valueOf(item.likeCount));
            }
        };
        mAdapter.setHasStableIds(false);
    }

    private void getMomentData(boolean isRefresh) {
        if (isRefresh) {
            pageIndex = 0;
        }
        HashMap<String, String> map = new HashMap<>(4);
        map.put("timestamp", System.currentTimeMillis() + "");
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageIndex", pageIndex + "");
        map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
        ApiServer.basePostRequest(this, Constants.MOMENTLIST, map,
                new TypeToken<ResponseData<List<Moment>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Moment>>() {
                    @Override
                    protected void onSuccess(List<Moment> momentList) {
                        if (ListUtils.isNotEmpty(momentList)) {
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
                                mRecyclerView.loadMoreNoData();
                            } else {
                                pageIndex++;
                            }
                        } else {
                            mEmptyView.showEmpty();
                        }
                        mRecyclerView.refreshComlete();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        if (isRefresh) {
                            mEmptyView.error(e).show();
                        }
                        mRecyclerView.refreshComlete();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }
}
