package ekoolab.com.show.fragments.subhomes;

import android.support.v7.widget.SimpleItemAnimator;
import android.text.Html;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.santalu.emptyview.EmptyView;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.WatchImageActivity;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.beans.User;
import ekoolab.com.show.dialogs.CommentDialog;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.RxUtils;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.UIHandler;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;
import ekoolab.com.show.views.nestlistview.NestFullListView;
import ekoolab.com.show.views.nestlistview.NestFullListViewAdapter;
import ekoolab.com.show.views.nestlistview.NestFullViewHolder;
import ekoolab.com.show.views.ninegridview.NewNineGridlayout;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.shihao.library.XRecyclerView;

public class MomentFragment extends BaseFragment {
    private int pageIndex;
    private EmptyView mEmptyView;
    private XRecyclerView mRecyclerView;
    private BaseQuickAdapter<Moment, BaseViewHolder> mAdapter = null;
    private List<Moment> moments = new ArrayList<>(20);
    private ArrayMap<String, String> zanMap = new ArrayMap<>(10);
    /**
     * 对那条评论进行评论，null表示对图文评论
     */
    private Moment.CommentsBean curCommentBean = null;
    private Moment curMoment = null;
    private CommentDialog commentDialog = null;
    private long delay = 150L;

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
                newNineGridlayout.showPic(nineTotalWidth, item.photoArray,
                        position -> WatchImageActivity.navToWatchImage(mContext, item.photoArray, position),
                        NewNineGridlayout.PHOTO_QUALITY_SMALL);
                ImageView ivHeart = helper.getView(R.id.iv_heart);
                ivHeart.setSelected(item.isMyLike);
                ivHeart.setOnClickListener(view -> {
                    if (zanMap.containsKey(item.resourceId)) {
                        return;
                    }
                    zanOrCancelMoment(item);
                });
                helper.setText(R.id.tv_zan_num, String.valueOf(item.likeCount));
                helper.getView(R.id.iv_comment).setOnClickListener(view -> {
                    curMoment = item;
                    showCommentDialog();
                });
                boolean notEmpty = ListUtils.isNotEmpty(item.comments);
                helper.setGone(R.id.nest_full_listview, notEmpty);
                if (notEmpty) {
                    NestFullListView listView = helper.getView(R.id.nest_full_listview);
                    listView.setAdapter(new NestFullListViewAdapter<Moment.CommentsBean>(R.layout.item_moent_comment, item.comments) {
                        @Override
                        public void onBind(int position, Moment.CommentsBean bean, NestFullViewHolder holder) {
                            if (Utils.equals(bean.replyTo, item.creator.userCode)) {
                                holder.setText(R.id.tv_comment, Html.fromHtml(getString(R.string.moment_reply1,
                                        bean.creator.name, bean.body)));
                            } else {
                                holder.setText(R.id.tv_comment, Html.fromHtml(getString(R.string.moment_reply2,
                                        bean.creator.name, bean.replyToName, bean.body)));
                            }
                        }
                    });
                }
            }
        };
        mAdapter.setHasStableIds(false);
    }

    private void showCommentDialog() {
        if (commentDialog == null) {
            commentDialog = new CommentDialog(mContext);
            commentDialog.setOnClickListener(content -> {
                sendComment(content);
                commentDialog.dismiss();
            });
        }
        commentDialog.show();
        UIHandler.getInstance().postDelayed(() -> commentDialog.showKeyboard(), delay);
        delay = 50L;
    }

    private void sendComment(String content) {
        if (TextUtils.isEmpty(content)) {
            ToastUtils.showToast("please input comment");
            return;
        }
        if (curMoment == null) {
            return;
        }
        Flowable.create((FlowableOnSubscribe<Moment.CommentsBean>) emitter -> {
            String userCode = AuthUtils.getInstance(mContext).getUserCode();
            String nickName = AuthUtils.getInstance(mContext).getName();
            Moment.CommentsBean bean = new Moment.CommentsBean();
            bean.body = content;
            bean.creator = new User();
            bean.creator.name = nickName;
            bean.creator.userCode = userCode;
            if (curCommentBean == null) {
                bean.replyTo = curMoment.creator.userCode;
                bean.replyToName = curMoment.creator.name;
            } else {
                bean.replyTo = curCommentBean.creator.userCode;
                bean.replyToName = curCommentBean.creator.name;
            }
            emitter.onNext(bean);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER)
                .compose(RxUtils.rxSchedulerHelper())
                .map(commentsBean -> {
                    if (curMoment.comments == null) {
                        curMoment.comments = new ArrayList<>(10);
                    }
                    curMoment.comments.add(commentsBean);
                    mAdapter.notifyItemChanged(moments.indexOf(curMoment));
                    if (commentDialog != null) {
                        commentDialog.clearText();
                    }
                    return commentsBean;
                })
                .observeOn(Schedulers.io())
                .flatMap((Function<Moment.CommentsBean, Publisher<ResponseData<Moment.CommentsBean>>>) bean -> {
                    HashMap<String, String> map = new HashMap<>(4);
                    map.put("resourceId", curMoment.resourceId);
                    map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
                    map.put("body", bean.body);
                    if (curCommentBean != null) {
                        map.put("commentId", curCommentBean.commentId);
                    }
                    return ApiServer.basePostRequestNoDisposable(MomentFragment.this, Constants.COMMENT,
                            map, new TypeToken<ResponseData<Moment.CommentsBean>>() {
                            });
                })
                .as(autoDisposable())
                .subscribe(new NetworkSubscriber<Moment.CommentsBean>() {
                    @Override
                    protected void onSuccess(Moment.CommentsBean s) {
                    }
                });
    }

    private void zanOrCancelMoment(Moment item) {
        HashMap<String, String> map = new HashMap<>(2);
        map.put("resourceId", item.resourceId);
        map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
        ApiServer.basePostRequest(this, item.isMyLike ? Constants.UNLIKE : Constants.LIKE,
                map, new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        item.isMyLike = !item.isMyLike;
                        mAdapter.notifyItemChanged(moments.indexOf(item));
                        zanMap.remove(item.resourceId);
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        zanMap.remove(item.resourceId);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
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
