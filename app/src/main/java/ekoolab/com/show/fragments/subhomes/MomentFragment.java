package ekoolab.com.show.fragments.subhomes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Html;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.utils.ThreadExecutorManager;
import com.santalu.emptyview.EmptyView;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.WatchImageActivity;
import ekoolab.com.show.adapters.DialogGiftPagerAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Gift;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.beans.User;
import ekoolab.com.show.dialogs.CommentDialog;
import ekoolab.com.show.dialogs.DialogViewHolder;
import ekoolab.com.show.dialogs.XXDialog;
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
    public static final long TIPS_LIVE_TIME = 3000;
    private int pageIndex;
    private EmptyView mEmptyView;
    private XRecyclerView mRecyclerView;
    private LinearLayout llTipsContainer;
    private BaseQuickAdapter<Moment, BaseViewHolder> mAdapter = null;
    private List<Moment> moments = new ArrayList<>(20);
    private ArrayMap<String, String> zanMap = new ArrayMap<>(10);
    /**
     * 对那条评论进行评论，null表示对图文评论
     */
    private Moment.CommentsBeanX curCommentBean = null;
    private Moment curMoment = null;
    private CommentDialog commentDialog = null;
    private long delay = 150L;
    private List<Gift> gifts = new ArrayList<>(10);
    private OnInteractivePlayGifListener playGifListener;
    private String curUserSmallAvator = null;
    private final Map<String, View> showingGiftTipsViews = new HashMap<>(10);
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String giftId = (String) msg.obj;
            View view = showingGiftTipsViews.get(giftId);
            int width = getResources().getDimensionPixelSize(R.dimen.moment_gift_tips_width);
            ObjectAnimator animator = ObjectAnimator.ofObject(view, "translationX",
                    new FloatEvaluator(), 0, -width);
            animator.setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    llTipsContainer.removeView(view);
                    showingGiftTipsViews.remove(giftId);
                }
            });
            view.setTag(R.id.gift_remove_animator, animator);
            animator.start();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInteractivePlayGifListener) {
            playGifListener = (OnInteractivePlayGifListener) context;
        }
    }

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
        llTipsContainer = holder.get(R.id.ll_tips_container);
    }

    @Override
    protected void initData() {
        mEmptyView.showLoading();
        getMomentData(true);
        getGifts();
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
                    curCommentBean = null;
                    showCommentDialog();
                });
                helper.getView(R.id.iv_reward).setOnClickListener(view -> {
                    curMoment = item;
                    showGiftDialog();
                });
                boolean notEmpty = ListUtils.isNotEmpty(item.comments);
                helper.setGone(R.id.nest_full_listview, notEmpty);
                if (notEmpty) {
                    NestFullListView listView = helper.getView(R.id.nest_full_listview);
                    listView.setAdapter(new NestFullListViewAdapter<Moment.CommentsBeanX>(R.layout.item_moent_comment, item.comments) {
                        @Override
                        public void onBind(int position, Moment.CommentsBeanX bean, NestFullViewHolder holder) {
                            if (!bean.ishasParentComment) {
                                holder.setText(R.id.tv_comment, Html.fromHtml(getString(R.string.moment_reply1,
                                        bean.creator.name, bean.body)));
                            } else {
                                holder.setText(R.id.tv_comment, Html.fromHtml(getString(R.string.moment_reply2,
                                        bean.creator.name, bean.replyToName, bean.body)));
                            }
                        }
                    });
                    listView.setOnItemClickListener((parent, view, position) -> {
                        String apiToken = AuthUtils.getInstance(getContext()).getApiToken();
                        if (TextUtils.isEmpty(apiToken)) {
                            ToastUtils.showToast(R.string.login_first);
                            return;
                        }
                        Moment.CommentsBeanX bean = item.comments.get(position);
                        if (Utils.equals(bean.creator.userCode, AuthUtils.getInstance(mContext).getUserCode())) {
                            return;
                        }
                        curCommentBean = bean;
                        curMoment = item;
                        showCommentDialog();
                    });
                }
            }
        };
        mAdapter.setHasStableIds(false);
    }

    private void showGiftDialog() {
        if (!ListUtils.isNotEmpty(gifts)) {
            ToastUtils.showToast("gift is loading");
            return;
        }
        XXDialog xxDialog = new XXDialog(mContext, R.layout.dialog_moment_gift, true) {
            @Override
            public void convert(DialogViewHolder holder) {
                ViewPager viewPager = holder.getView(R.id.viewpager);
                viewPager.getLayoutParams().height = ((int) (DisplayUtils.getScreenWidth() / 4 * 1.2)) * 2;
                DialogGiftPagerAdapter adapter = new DialogGiftPagerAdapter(mContext, gifts);
                viewPager.setAdapter(adapter);
                LinearLayout llIndicator = holder.getView(R.id.ll_indicator);
                int size = DisplayUtils.dip2px(6);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(size, size, size, size);
                llIndicator.setTag(0);
                for (int i = 0, len = adapter.getCount(); i < len; i++) {
                    View view = new View(mContext);
                    llIndicator.addView(view, params);
                    if (i == 0) {
                        view.setBackgroundResource(R.drawable.bg_indicator_white);
                    } else {
                        view.setBackgroundResource(R.drawable.bg_indicator_grey);
                    }
                }
                viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        int prePos = (int) llIndicator.getTag();
                        llIndicator.getChildAt(prePos).setBackgroundResource(R.drawable.bg_indicator_grey);
                        llIndicator.getChildAt(position).setBackgroundResource(R.drawable.bg_indicator_white);
                        llIndicator.setTag(position);
                    }
                });
                holder.setOnClick(R.id.tv_send, view -> {
                    int curGiftPos = adapter.getCurGiftPos();
                    if (curGiftPos == -1) {
                        return;
                    }
                    Gift gift = gifts.get(curGiftPos);
                    if (playGifListener != null) {
                        playGifListener.playGif(gift.animImage);
                    }
                    generateGiftTips(gift);
                    sendGift(gift);
                });
            }
        };
        xxDialog.fullWidth().fromBottom().showDialog();
    }

    private void sendGift(Gift gift) {
        Flowable.create((FlowableOnSubscribe<HashMap<String, String>>) emitter -> {
            HashMap<String, String> map = new HashMap<>(3);
            map.put("momentId", curMoment.resourceId);
            map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
            map.put("giftid", gift.giftid);
            emitter.onNext(map);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER)
                .compose(RxUtils.rxThreadHelper())
                .flatMap((Function<HashMap<String, String>, Publisher<ResponseData<String>>>)
                        map -> ApiServer.basePostRequestNoDisposable(MomentFragment.this,
                        Constants.MOMENT_SENDGIFT, map, new TypeToken<ResponseData<String>>() {
                        }))
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {

                    }
                });
    }

    private void showCommentDialog() {
        String apiToken = AuthUtils.getInstance(getContext()).getApiToken();
        if (TextUtils.isEmpty(apiToken)) {
            ToastUtils.showToast(R.string.login_first);
            return;
        }
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
        Flowable.create((FlowableOnSubscribe<Moment.CommentsBeanX>) emitter -> {
            String userCode = AuthUtils.getInstance(mContext).getUserCode();
            String nickName = AuthUtils.getInstance(mContext).getName();
            Moment.CommentsBeanX bean = new Moment.CommentsBeanX();
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
                .flatMap((Function<Moment.CommentsBeanX, Publisher<ResponseData<Moment.CommentsBeanX>>>) bean -> {
                    HashMap<String, String> map = new HashMap<>(4);
                    map.put("resourceId", curMoment.resourceId);
                    map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
                    map.put("body", bean.body);
                    if (curCommentBean != null) {
                        map.put("commentId", curCommentBean.commentId);
                    }
                    return ApiServer.basePostRequestNoDisposable(MomentFragment.this, Constants.COMMENT,
                            map, new TypeToken<ResponseData<Moment.CommentsBeanX>>() {
                            });
                })
                .as(autoDisposable())
                .subscribe(new NetworkSubscriber<Moment.CommentsBeanX>() {
                    @Override
                    protected void onSuccess(Moment.CommentsBeanX s) {
                    }
                });
    }

    private void zanOrCancelMoment(Moment item) {
        String apiToken = AuthUtils.getInstance(getContext()).getApiToken();
        if (TextUtils.isEmpty(apiToken)) {
            ToastUtils.showToast(R.string.login_first);
            return;
        }
        HashMap<String, String> map = new HashMap<>(2);
        map.put("resourceId", item.resourceId);
        map.put("token", apiToken);
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
                            convertData(momentList);
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

    /**
     * 把所有评论和评论的子评论都放到一个集合去
     */
    private void convertData(List<Moment> momentList) {
        for (Moment moment : momentList) {
            if (!ListUtils.isNotEmpty(moment.comments)) {
                continue;
            }
            //一条图文的评论
            List<Moment.CommentsBeanX> newComments = new ArrayList<>(10);
            for (Moment.CommentsBeanX beanX : moment.comments) {
                newComments.add(beanX);
                //评论的评论
                addToNewComments(newComments, beanX.comments);
            }
            moment.comments = newComments;
        }
    }

    private void addToNewComments(List<Moment.CommentsBeanX> newComments,
                                  List<List<Moment.CommentsBeanX.CommentsBean>> comments) {
        if (ListUtils.isNotEmpty(comments)) {
            for (List<Moment.CommentsBeanX.CommentsBean> beans : comments) {
                newComments.add(convert(beans.get(0)));
                addToNewComments(newComments, beans.get(0).comments);
            }
        }
    }

    private Moment.CommentsBeanX convert(Moment.CommentsBeanX.CommentsBean bean) {
        Moment.CommentsBeanX beanX = new Moment.CommentsBeanX();
        beanX.ishasParentComment = true;
        beanX.body = bean.body;
        beanX.commentId = bean.commentId;
        beanX.creator = bean.creator;
        beanX.likeCount = bean.likeCount;
        beanX.postTime = bean.postTime;
        beanX.replyTo = bean.replyTo;
        beanX.replyToName = bean.replyToName;
        return beanX;
    }

    private void getGifts() {
        ApiServer.basePostRequest(this, Constants.GIFTLIST, null,
                new TypeToken<ResponseData<List<Gift>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Gift>>() {
                    @Override
                    protected void onSuccess(List<Gift> giftList) {
                        if (ListUtils.isNotEmpty(giftList)) {
                            gifts.clear();
                            gifts.addAll(giftList);
                            for (Gift gift : gifts) {
                                ThreadExecutorManager.getInstance().runInThreadPool(() -> {
                                    FutureTarget<File> target = Glide.with(MomentFragment.this)
                                            .download(gift.animImage).submit();
                                });
                            }
                        }
                    }
                });
    }

    public interface OnInteractivePlayGifListener {
        void playGif(String imageUrl);
    }

    private void generateGiftTips(Gift gift) {
        View view;
        FrameLayout flGiftNum;
        synchronized (showingGiftTipsViews) {
            if (showingGiftTipsViews.containsKey(gift.giftid)) {
                mHandler.removeMessages(gifts.indexOf(gift));
                view = showingGiftTipsViews.get(gift.giftid);
                ObjectAnimator animator = (ObjectAnimator) view.getTag(R.id.gift_remove_animator);
                if (animator != null) {
                    animator.cancel();
                }
                flGiftNum = view.findViewById(R.id.fl_gift_num);
                int curCount = (int) view.getTag(R.id.gift_cur_count);
                LinkedList<Integer> remianCount = (LinkedList<Integer>) view.getTag(R.id.gift_remain_count);
                remianCount.offer(1);
                view.setTag(R.id.gift_cur_count, ++curCount);
                if (animator != null) {
                    startViewAnimator(gift, view, flGiftNum, view.getTranslationX(), 0);
                } else {
                    Animation animation = flGiftNum.getAnimation();
                    if (animation == null || animation.hasEnded()) {
                        textViewScaleAnim(gift, view, flGiftNum);
                    }
                }
                return;
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.layout_moment_gift_tips, null);
                showingGiftTipsViews.put(gift.giftid, view);
                view.setTag(R.id.gift_cur_count, 1);
                LinkedList<Integer> integers = new LinkedList<>();
                integers.offer(1);
                view.setTag(R.id.gift_remain_count, integers);
            }
        }
        if (TextUtils.isEmpty(curUserSmallAvator)) {
            curUserSmallAvator = AuthUtils.getInstance(mContext.getApplicationContext()).getAvator(AuthUtils.SMALL);
        }
        ImageView ivHeader = view.findViewById(R.id.iv_header);
        ImageView ivGiftImage = view.findViewById(R.id.iv_gift_image);
        TextView tvGiftName = view.findViewById(R.id.tv_gift_name);
        TextView tvGiftNameFlag = view.findViewById(R.id.tv_gift_name_flag);
        flGiftNum = view.findViewById(R.id.fl_gift_num);
        Glide.with(this).load(curUserSmallAvator).into(ivHeader);
        Glide.with(this).load(gift.image).into(ivGiftImage);
        String giftName = String.format("送 %s", gift.name);
        tvGiftName.setText(giftName);
        tvGiftNameFlag.setText(giftName);
        llTipsContainer.addView(view);
        int width = getResources().getDimensionPixelSize(R.dimen.moment_gift_tips_width);
        startViewAnimator(gift, view, flGiftNum, -width, 0);
    }

    private void startViewAnimator(Gift gift, View view, FrameLayout flGiftNum, Object... values) {
        ObjectAnimator animator = ObjectAnimator.ofObject(view, "translationX", new FloatEvaluator(), values);
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textViewScaleAnim(gift, view, flGiftNum);
            }
        });
        animator.start();
    }

    private void textViewScaleAnim(Gift gift, View view, FrameLayout flGiftNum) {
        int curCount = (int) view.getTag(R.id.gift_cur_count);
        final LinkedList<Integer> integers = (LinkedList<Integer>) view.getTag(R.id.gift_remain_count);
        integers.pollFirst();
        String giftNum = String.format(Locale.getDefault(), "x %d", curCount);
        ((TextView) flGiftNum.getChildAt(0)).setText(giftNum);
        ((TextView) flGiftNum.getChildAt(1)).setText(giftNum);
        ScaleAnimation animation = new ScaleAnimation(1, 1.5f, 1, 1.5f, 0.5f, 0.5f);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (ListUtils.isNotEmpty(integers)) {
                    textViewScaleAnim(gift, view, flGiftNum);
                } else {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(gifts.indexOf(gift), gift.giftid), TIPS_LIVE_TIME);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        flGiftNum.startAnimation(animation);
    }

    @Override
    public void onDestroyView() {
        for (int i = 0, len = gifts.size(); i < len; i++) {
            mHandler.removeMessages(i);
        }
        super.onDestroyView();
    }
}
