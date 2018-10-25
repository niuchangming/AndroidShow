package ekoolab.com.show.fragments.submyvideos;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.santalu.emptyview.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.WatchImageActivity;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
//import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;
import ekoolab.com.show.views.nestlistview.NestFullListView;
import ekoolab.com.show.views.nestlistview.NestFullListViewAdapter;
import ekoolab.com.show.views.nestlistview.NestFullViewHolder;
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
//        super.initData();
        mEmptyView.showLoading();
        loadMyMoment(true);
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        System.out.println("Enter initview");
        mEmptyView = holder.get(R.id.empty_view);
        recyclerView = holder.get(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new LinearItemDecoration(mContext,
                1, R.color.common_color_gray_lighted, DisplayUtils.dip2px(15)));
        refreshLayout = holder.get(R.id.refreshLayout);
        initRefreshLayout();
        initAdapter();
        recyclerView.setAdapter(mAdapter);
        System.out.println("Action 14");
        llTipsContainer = holder.get(R.id.ll_tips_container);

    }

    private void initRefreshLayout() {
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshLoadMoreListener(this);
    }

    private void initAdapter() {
        System.out.println("Enter initAdapter");
        mAdapter = new BaseQuickAdapter<Moment, BaseViewHolder>(R.layout.item_moment, moments) {

            private int nineTotalWidth = DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(60 * 2);

            @Override
            protected void convert(BaseViewHolder helper, Moment item) {
                System.out.println("Enter mymoment converter");
//                ImageLoader.displayImage(item.creator.avatar.small, helper.getView(R.id.iv_icon));
//                helper.setText(R.id.tv_name, item.creator.name);
                helper.setText(R.id.tv_time, TimeUtils.getDateStringByTimeStamp(item.uploadTime));
                helper.setGone(R.id.tv_content, !TextUtils.isEmpty(item.body));
                helper.setText(R.id.tv_content, item.body);
                helper.setGone(R.id.nine_grid_layout, Utils.isNotEmpty(item.photoArray));
                NewNineGridlayout newNineGridlayout = helper.getView(R.id.nine_grid_layout);
                newNineGridlayout.showPic(nineTotalWidth, item.photoArray,
                        position -> WatchImageActivity.navToWatchImage(mContext, item.photoArray, position),
                        NewNineGridlayout.PHOTO_QUALITY_SMALL);
//                ImageView ivHeart = helper.getView(R.id.iv_heart);
//                ivHeart.setSelected(item.isMyLike);
//                ivHeart.setOnClickListener(view -> {
//                    if (zanMap.containsKey(item.resourceId)) {
//                        return;
//                    }
//                    zanOrCancelMoment(item);
//                });
//                helper.setText(R.id.tv_zan_num, String.valueOf(item.likeCount));
//                helper.getView(R.id.iv_comment).setOnClickListener(view -> {
//                    curMoment = item;
//                    curCommentBean = null;
//                    showCommentDialog();
//                });
//                helper.getView(R.id.iv_reward).setOnClickListener(view -> {
//                    curMoment = item;
//                    showGiftDialog();
//                });
//                boolean notEmpty = ListUtils.isNotEmpty(item.comments);
//                helper.setGone(R.id.nest_full_listview, notEmpty);
//                if (notEmpty) {
//                    NestFullListView listView = helper.getView(R.id.nest_full_listview);
//                    listView.setAdapter(new NestFullListViewAdapter<Moment.CommentsBean>(R.layout.item_moent_comment, item.comments) {
//
//                        @Override
//                        public void onBind(int position, Moment.CommentsBean bean, NestFullViewHolder holder) {
//                            if (!bean.ishasParentComment) {
//                                holder.setText(R.id.tv_comment, Html.fromHtml(getString(R.string.moment_reply1,
//                                        bean.creator.name, bean.body)));
//                            } else {
//                                holder.setText(R.id.tv_comment, Html.fromHtml(getString(R.string.moment_reply2,
//                                        bean.creator.name, bean.replyToName, bean.body)));
//                            }
//                        }
//                    });
//                    listView.setOnItemClickListener((parent, view, position) -> {
//                        String apiToken = AuthUtils.getInstance(getContext()).getApiToken();
//                        if (TextUtils.isEmpty(apiToken)) {
//                            ToastUtils.showToast(R.string.login_first);
//                            return;
//                        }
//                        Moment.CommentsBean bean = item.comments.get(position);
//                        if (Utils.equals(bean.creator.userCode, AuthUtils.getInstance(mContext).getUserCode())) {
//                            return;
//                        }
//                        curCommentBean = bean;
//                        curMoment = item;
//                        showCommentDialog();
//                    });
//                }
            }
        };
        mAdapter.setHasStableIds(false);
    }

    private void loadMyMoment(boolean isRefresh){
        if (isRefresh) {
            pageIndex = 0;
        }
        HashMap<String, String> map = new HashMap<>(4);
//        map.put("timestamp", System.currentTimeMillis() + "");
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageIndex", pageIndex + "");
        map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
        ApiServer.basePostRequest(this, Constants.MYMOMENTLIST, map,
                new TypeToken<ResponseData<List<Moment>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Moment>>() {
                    @Override
                    protected void onSuccess(List<Moment> momentList) {
                        System.out.println("after loading data");
                        System.out.println("===momentList==="+momentList.size()+";pageIndex==="+pageIndex);
                        if (Utils.isNotEmpty(momentList)) {
                            if (isRefresh) {
                                moments.clear();
                                System.out.println("Action 1");
                            }
//                            convertData(momentList);
//                            moments.addAll(momentList);
//                            if (isRefresh) {
//                                mAdapter.notifyDataSetChanged();
//                            } else {
//                                mAdapter.notifyItemRangeInserted(moments.size() - momentList.size(),
//                                        momentList.size());
//                            }
                            mEmptyView.showContent();
                            if (momentList.size() < Constants.PAGE_SIZE) {
                                refreshLayout.setEnableLoadMore(false);
                                System.out.println("Action 2");
                            } else {
                                pageIndex++;
                                System.out.println("Action 3");
                            }
                        } else if(moments.size()!=0){
                            refreshLayout.setEnableLoadMore(false);
                            System.out.println("Action 4");
                        } else{
                            mEmptyView.showEmpty();
                            System.out.println("Action 5");
                        }
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                        System.out.println("Action 6");
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
