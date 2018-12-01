package ekoolab.com.show.fragments.subhomes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.utils.ThreadExecutorManager;
import com.orhanobut.logger.Logger;
import com.santalu.emptyview.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.blogc.android.views.ExpandableTextView;
import ekoolab.com.show.R;
import ekoolab.com.show.adapters.DialogGiftPagerAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.beans.Gift;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.dialogs.CommentDialog;
import ekoolab.com.show.dialogs.DialogViewHolder;
import ekoolab.com.show.dialogs.XXDialog;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.RxUtils;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.UIHandler;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.ImageViewer.IyImageLoader;
import ekoolab.com.show.views.ImageViewer.ViewData;
import ekoolab.com.show.views.ImageViewer.dragger.ImageDraggerType;
import ekoolab.com.show.views.ImageViewer.widget.ImageViewer;
import ekoolab.com.show.views.ImageViewer.widget.ScaleImageView;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;
import ekoolab.com.show.views.nestlistview.NestFullListView;
import ekoolab.com.show.views.nestlistview.NestFullListViewAdapter;
import ekoolab.com.show.views.nestlistview.NestFullViewHolder;
import ekoolab.com.show.views.ninegridview.NewNineGridlayout;
import ekoolab.com.show.views.ninegridview.NineGridlayout;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.WINDOW_SERVICE;

public class MomentFragment extends BaseFragment implements OnRefreshLoadMoreListener {
    public static final long TIPS_LIVE_TIME = 3000;
    public static final String ACTION_REFRESH_DATA = "ekoolab.com.show.fragments.subhomes.MomentFragment.refresh_data";
    private int pageIndex;
    private EmptyView mEmptyView;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout llTipsContainer;
    private ImageViewer imagePreview;
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

        recyclerView = holder.get(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new LinearItemDecoration(mContext,
                1, R.color.colorLightGray, 0));
        refreshLayout = holder.get(R.id.refreshLayout);
        initRefreshLayout();
        initAdapter();
        recyclerView.setAdapter(mAdapter);
        llTipsContainer = holder.get(R.id.ll_tips_container);

        imagePreview = new ImageViewer(getActivity());
        imagePreview.doDrag(true);
        imagePreview.setDragType(ImageDraggerType.DRAG_TYPE_WX);
        imagePreview.setBackgroundColor(Color.BLACK);
        imagePreview.setVisibility(View.GONE);
        imagePreview.setImageLoader(new IyImageLoader<String>() {

            @Override
            public void displayImage(final int position, String src, final ImageView imageView) {
                final ScaleImageView scaleImageView= (ScaleImageView) imageView.getParent();
                ImageLoader.loadImage(getActivity(), src, new SimpleTarget<Drawable>() {

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        scaleImageView.showProgess();
                        imageView.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        scaleImageView.removeProgressView();
                        imageView.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        scaleImageView.removeProgressView();
                        imageView.setImageDrawable(resource);
                    }
                });
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.RIGHT | Gravity.TOP;
        WindowManager wm = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
        wm.addView(imagePreview, params);

    }

    private void initRefreshLayout() {
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshLoadMoreListener(this);
    }

    @Override
    protected void initData() {
        mEmptyView.showLoading();
        getMomentData(true);
        getGifts();
    }

    private void initAdapter() {
        mAdapter = new BaseQuickAdapter<Moment, BaseViewHolder>(R.layout.item_moment_list, moments) {
            private int nineTotalWidth = DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(15 * 2 + 32 + 16);

            @Override
            protected void convert(BaseViewHolder helper, Moment item) {
                ImageLoader.displayImage(item.creator.avatar.small, helper.getView(R.id.iv_icon));
                helper.setText(R.id.tv_name, item.creator.name);
                helper.setText(R.id.tv_time, TimeUtils.formatTime(item.uploadTime));
                helper.setGone(R.id.tv_content, !TextUtils.isEmpty(item.body));

                ExpandableTextView expandableTextView = helper.getView(R.id.tv_content);
                expandableTextView.setText(item.body);
                expandableTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        expandableTextView.setMaxLines(Integer.MAX_VALUE);
                        expandableTextView.measure
                                (
                                        View.MeasureSpec.makeMeasureSpec(expandableTextView.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                                );

                        if(expandableTextView.getLineCount() > 5){
                            expandableTextView.setMaxLines(5);
                            helper.setGone(R.id.expand_btn, true);
                        }else{
                            helper.setGone(R.id.expand_btn, false);
                        }
                    }
                });

                Button expandBtn = helper.getView(R.id.expand_btn);
                expandBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expandableTextView.toggle();
                    }
                });

                helper.setGone(R.id.nine_grid_layout, Utils.isNotEmpty(item.photoArray));
                NewNineGridlayout newNineGridlayout = helper.getView(R.id.nine_grid_layout);
                newNineGridlayout.showPic(nineTotalWidth, item.photoArray,
                        new NineGridlayout.onNineGirdItemClickListener() {

                            protected List<ViewData> mViewList;
                            protected List<String> mImageList;

                            @Override
                            public void onItemClick(int position) {
                                mViewList = new ArrayList<>();
                                mImageList = new ArrayList<>();

                                ViewGroup viewGroup = (ViewGroup) newNineGridlayout.getChildAt(0);
                                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                                    int[] location = new int[2];
                                    viewGroup.getChildAt(i).getLocationOnScreen(location);
                                    ViewData viewData = new ViewData();
                                    viewData.setTargetX(location[0]);
                                    viewData.setTargetY(location[1]);
                                    viewData.setTargetWidth(viewGroup.getChildAt(i).getMeasuredWidth());
                                    viewData.setTargetHeight(viewGroup.getChildAt(i).getMeasuredHeight());
                                    mViewList.add(i, viewData);
                                    mImageList.add(i, item.photoArray.get(i).origin);
                                }
                                imagePreview.setImageData(mImageList);
                                imagePreview.setStartPosition(position);
                                imagePreview.setViewData(mViewList);
                                imagePreview.watch();
                            }
                        }, NewNineGridlayout.PHOTO_QUALITY_SMALL);

                ImageButton ivHeart = helper.getView(R.id.iv_heart);
                ivHeart.setSelected(item.isMyLike);
                ivHeart.setOnClickListener(view -> {
                    if (zanMap.containsKey(item.resourceId)) {
                        return;
                    }
                    zanOrCancelMoment(item);
                });
                helper.setText(R.id.tv_like_count, String.valueOf(item.likeCount));

                ImageButton commentBtn =  helper.getView(R.id.iv_comment);
                commentBtn.setOnClickListener(view -> {
                    curMoment = item;
                    curCommentBean = null;
                    showCommentDialog();
                });

                ImageButton awardIv = helper.getView(R.id.iv_reward);
                awardIv.setOnClickListener(view -> {
                    curMoment = item;
                    showGiftDialog();
                });

                boolean notEmpty = Utils.isNotEmpty(item.comments);
                helper.setGone(R.id.nest_full_listview, notEmpty);
                if (notEmpty) {
                    NestFullListView listView = helper.getView(R.id.nest_full_listview);
                    listView.setAdapter(new NestFullListViewAdapter<Moment.CommentsBean>(R.layout.item_moment_comment, item.comments) {

                        @Override
                        public void onBind(int position, Moment.CommentsBean bean, NestFullViewHolder holder) {
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
                        Moment.CommentsBean bean = item.comments.get(position);
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
        if (!Utils.isNotEmpty(gifts)) {
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
        Flowable.create((FlowableOnSubscribe<HashMap<String, Object>>) emitter -> {
            HashMap<String, Object> map = new HashMap<>(3);
            map.put("momentId", curMoment.resourceId);
            map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
            map.put("giftid", gift.giftid);
            emitter.onNext(map);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER)
                .compose(RxUtils.rxThreadHelper())
                .flatMap((Function<HashMap<String, Object>, Publisher<ResponseData<String>>>)
                        map -> ApiServer.basePostRequestNoDisposable(Constants.MOMENT_SENDGIFT, map, new TypeToken<ResponseData<String>>() {
                                }))
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        Logger.i("Send Gift Result: " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Logger.i("Send Gift Result: " + e.getLocalizedMessage());
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
        Flowable.create((FlowableOnSubscribe<Moment.CommentsBean>) emitter -> {
            String userCode = AuthUtils.getInstance(mContext).getUserCode();
            String nickName = AuthUtils.getInstance(mContext).getName();
            Moment.CommentsBean bean = new Moment.CommentsBean();
            bean.body = content;
            bean.creator = new Friend();
            bean.creator.name = nickName;
            bean.creator.userCode = userCode;
            if (curCommentBean == null) {
                bean.replyTo = curMoment.creator.userCode;
                bean.replyToName = curMoment.creator.name;
            } else {
                bean.replyTo = curCommentBean.creator.userCode;
                bean.replyToName = curCommentBean.creator.name;
                bean.ishasParentComment = true;
            }
            emitter.onNext(bean);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER)
                .compose(RxUtils.rxSchedulerHelper())
                .map(commentsBean -> {
                    if (curMoment.comments == null) {
                        curMoment.comments = new ArrayList<>(10);
                    }
                    if (curCommentBean != null) {
                        int index;
                        if (Utils.isNotEmpty(curCommentBean.comments)) {
                            Moment.CommentsBean lastBean = curCommentBean.comments.get(curCommentBean.comments.size() - 1);
                            index = curMoment.comments.indexOf(lastBean);
                        } else {
                            index = curMoment.comments.indexOf(curCommentBean);
                        }
                        curMoment.comments.add(index + 1, commentsBean);
                    } else {
                        curMoment.comments.add(commentsBean);
                    }
                    mAdapter.notifyItemChanged(moments.indexOf(curMoment));
                    if (commentDialog != null) {
                        commentDialog.clearText();
                    }
                    return commentsBean;
                })
                .observeOn(Schedulers.io())
                .flatMap((Function<Moment.CommentsBean, Publisher<ResponseData<String>>>) bean -> {
                    HashMap<String, Object> map = new HashMap<>(4);
                    map.put("resourceId", curMoment.resourceId);
                    map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
                    map.put("body", bean.body);
                    if (curCommentBean != null) {
                        map.put("commentId", curCommentBean.commentId);
                    }
                    return ApiServer.basePostRequestNoDisposable(Constants.COMMENT,
                            map, new TypeToken<ResponseData<String>>() {
                            });
                })
                .as(autoDisposable())
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
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
        HashMap<String, String> map = new HashMap<>(3);
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageIndex", pageIndex + "");
        map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
        ApiServer.basePostRequest(this, Constants.MOMENTLIST, map,
                    new TypeToken<ResponseData<List<Moment>>>() {
                    })
                .subscribe(new NetworkSubscriber<List<Moment>>() {
                    @Override
                    protected void onSuccess(List<Moment> momentList) {
                        if (Utils.isNotEmpty(momentList)) {
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

    /**
     * 把所有评论和评论的子评论都放到一个集合去
     */
    private void convertData(List<Moment> momentList) {
        for (Moment moment : momentList) {
            if (!Utils.isNotEmpty(moment.comments)) {
                continue;
            }
            //一条图文的评论
            List<Moment.CommentsBean> newComments = new ArrayList<>(10);
            for (Moment.CommentsBean beanX : moment.comments) {
                newComments.add(beanX);
                //评论的评论
                addToNewComments(newComments, beanX.comments);
            }
            moment.comments = newComments;
        }
    }

    private void addToNewComments(List<Moment.CommentsBean> newComments,
                                  List<Moment.CommentsBean> comments) {
        if (Utils.isNotEmpty(comments)) {
            for (Moment.CommentsBean beans : comments) {
                beans.ishasParentComment = true;
                newComments.add(beans);
                addToNewComments(newComments, beans.comments);
            }
        }
    }

    private void getGifts() {
        ApiServer.basePostRequest(this, Constants.GIFTLIST, null,
                new TypeToken<ResponseData<List<Gift>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Gift>>() {
                    @Override
                    protected void onSuccess(List<Gift> giftList) {
                        if (Utils.isNotEmpty(giftList)) {
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

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        getMomentData(false);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getMomentData(true);
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
            curUserSmallAvator = "https://pic.qqtn.com/up/2018-7/2018071708152519847.jpg";
        }
        ImageView ivHeader = view.findViewById(R.id.iv_header);
        ImageView ivGiftImage = view.findViewById(R.id.iv_gift_image);
        TextView tvGiftName = view.findViewById(R.id.tv_gift_name);
        TextView tvGiftNameFlag = view.findViewById(R.id.tv_gift_name_flag);
        flGiftNum = view.findViewById(R.id.fl_gift_num);
        ImageLoader.displayImageAsCircle(curUserSmallAvator, ivHeader);
        ImageLoader.displayImageAsCircle(gift.image, ivGiftImage);
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
                if (Utils.isNotEmpty(integers)) {
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

    @Override
    public void dealWithBroadcastAction(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_REFRESH_DATA.equals(action)) {
            getMomentData(true);
        }
    }

    @Override
    public List<String> getLocalBroadcastAction() {
        return Arrays.asList(ACTION_REFRESH_DATA);
    }
}
