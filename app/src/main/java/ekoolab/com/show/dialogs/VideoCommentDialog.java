package ekoolab.com.show.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
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
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.UIHandler;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

public class VideoCommentDialog extends Dialog implements OnRefreshLoadMoreListener {

    private CommentDialog.OnClickListener onClickListener;

    private EmptyView mEmptyView;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private BaseQuickAdapter<Moment.CommentsBean, BaseViewHolder> mAdapter;
    private List<Moment.CommentsBean> commentsBeans = new ArrayList<>(20);
    private CommentDialog commentDialog = null;
    private long delay = 150L;
    private int pageIndex;
    private Context mContext;
    private String resourceId;
    private EditText etComment;
    private TextView tv_comment;
    private int previousPos = -2;

    public void setOnClickListener(CommentDialog.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public VideoCommentDialog(Context context, String resourceId) {
        super(context, R.style.common_dialog_bg_transparent);
        mContext = context;
        this.resourceId = resourceId;
//        init();
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity=Gravity.BOTTOM;
        layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height= WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);

        getWindow().setAttributes(layoutParams);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_video_comment);
        etComment = findViewById(R.id.et_comment);
        tv_comment = findViewById(R.id.tv_comment);

        mEmptyView = findViewById(R.id.empty_view);
        ViewGroup.LayoutParams params = mEmptyView.getLayoutParams();
        params.height = DisplayUtils.getScreenHeight()*2/3;
        mEmptyView.setLayoutParams(params);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new LinearItemDecoration(mContext,
                1, R.color.common_color_gray_lighted, DisplayUtils.dip2px(15)));
        refreshLayout = findViewById(R.id.refreshLayout);
        initRefreshLayout();
        initAdapter();
        tv_comment.setOnClickListener(view -> {
            showCommentDialog(-1);
        });
        recyclerView.setAdapter(mAdapter);
        mEmptyView.showLoading();
        getComments(true);

    }

    private void initRefreshLayout() {
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshLoadMoreListener(this);
    }

    private void initAdapter() {
        mAdapter = new BaseQuickAdapter<Moment.CommentsBean, BaseViewHolder>(R.layout.item_video_comment, commentsBeans) {

            //TODO change function after comment.creator is not null
            @Override
            protected void convert(BaseViewHolder helper, Moment.CommentsBean item) {
                helper.setText(R.id.tv_time, TimeUtils.formatTime(item.postTime));
                helper.setText(R.id.tv_name, item.creator.nickName);
                if (!item.ishasParentComment) {
                    helper.setText(R.id.tv_body, item.body);
                } else {
                    helper.setText(R.id.tv_body, Html.fromHtml(mContext.getString(R.string.video_reply1,
                            item.replyToName, item.body)));
                }
                ImageLoader.displayImageAsCircle(item.creator.avatar.small, helper.getView(R.id.avatar_iv));
                TextView tv_content = helper.getView(R.id.tv_body);
                tv_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String apiToken = AuthUtils.getInstance(mContext).getApiToken();
                        if (TextUtils.isEmpty(apiToken)) {
                            ToastUtils.showToast(R.string.login_first);
                            return;
                        }
                        if (Utils.equals(item.creator.userCode, AuthUtils.getInstance(mContext).getUserCode())) {
//                            ToastUtils.showToast("comment yourself is not allowed");
                            return;
                        }
                        showCommentDialog(commentsBeans.indexOf(item));
                    }
                });
            }
        };
        mAdapter.setHasStableIds(false);
    }

    private void getComments(boolean isRefresh){
        if(isRefresh){
            pageIndex = 0;
        }
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageIndex", pageIndex + "");
        map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
        map.put("resourceId", this.resourceId);
        ApiServer.basePostRequestNoDisposable(Constants.COMMENTLIST, map,
                new TypeToken<ResponseData<List<Moment.CommentsBean>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Moment.CommentsBean>>() {
                    @Override
                    protected void onSuccess(List<Moment.CommentsBean> commentsBeanList) {
                        if (Utils.isNotEmpty(commentsBeanList)) {
                            if (isRefresh) {
                                commentsBeans.clear();
                            }
                            List<Moment.CommentsBean> commentsList = new ArrayList<>();
                            convertData(commentsBeanList, commentsList, false);
                            commentsBeans.addAll(commentsList);
                            if (isRefresh) {
                                mAdapter.notifyDataSetChanged();
                            } else {
                                mAdapter.notifyItemRangeInserted(commentsBeans.size() - commentsList.size(),
                                        commentsList.size());
                            }
                            mEmptyView.showContent();
                            if (commentsBeanList.size() < Constants.PAGE_SIZE) {
                                refreshLayout.setEnableLoadMore(false);
                            } else {
                                pageIndex++;
                            }
                        } else if(commentsBeans.size()!=0 && !isRefresh){
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

    private void convertData(List<Moment.CommentsBean> commentsBeanList, List<Moment.CommentsBean> commentsList, boolean hasParentComment){
        for(Moment.CommentsBean comment: commentsBeanList){
            comment.ishasParentComment = hasParentComment;
            commentsList.add(comment);
            if(Utils.isNotEmpty(comment.comments)){
                convertData(comment.comments, commentsList, true);
            }
        }
    }

    private void sendComment(String content, int position ) {
        if (TextUtils.isEmpty(content)) {
            ToastUtils.showToast("please input comment");
            return;
        }
        String token = AuthUtils.getInstance(mContext).getApiToken();
        HashMap<String, Object> map = new HashMap<>(3);
        map.put("body", content);
        map.put("token", token);
        if(position >= 0) {
            map.put("commentId", commentsBeans.get(position).commentId);
        } else {
            map.put("resourceId", this.resourceId);
        }
        ApiServer.basePostRequestNoDisposable(Constants.COMMENT, map,
                new TypeToken<ResponseData<Moment.CommentsBean>>() {
                })
                .subscribe(new NetworkSubscriber<Moment.CommentsBean>() {
                    @Override
                    protected void onSuccess(Moment.CommentsBean comment) {
                        if(position >= 0){
                            comment.ishasParentComment = true;
                            commentsBeans.add(position+1, comment);
                            mAdapter.notifyItemRangeInserted(position+1, 1);
                        } else {
                            commentsBeans.add(comment);
                            mAdapter.notifyItemInserted(commentsBeans.size());
                        }
                        mEmptyView.showContent();
                        if (commentDialog != null) {
                            commentDialog.clearText();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void showCommentDialog(int position) {
        String apiToken = AuthUtils.getInstance(getContext()).getApiToken();
        if (TextUtils.isEmpty(apiToken)) {
            ToastUtils.showToast(R.string.login_first);
            return;
        }
        if(previousPos != position){
            if(position>=0){
                commentDialog = new CommentDialog(mContext, mContext.getString(R.string.reply_to) + " " + commentsBeans.get(position).creator.nickName + ":");
            } else {
                commentDialog = new CommentDialog(mContext);
            }
            previousPos = position;
            commentDialog.setOnClickListener(content -> {
                sendComment(content, position);
                commentDialog.dismiss();
            });
        }
        commentDialog.show();
        UIHandler.getInstance().postDelayed(() -> commentDialog.showKeyboard(), delay);
        delay = 50L;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        getComments(false);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getComments(true);
    }

}
