package ekoolab.com.show.activities;



import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.santalu.emptyview.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.HomeAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.beans.UserInfo;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.itemdecoration.GridSpacingItemDecoration;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;
import ekoolab.com.show.views.nestlistview.NestFullListView;
import ekoolab.com.show.views.ninegridview.NewNineGridlayout;
import me.shihao.library.RecyclerAdapter;
import me.shihao.library.XRecyclerView;

import static com.facebook.FacebookSdk.getApplicationContext;

public class FollowersActivity extends BaseActivity implements View.OnClickListener,
                                                                    OnRefreshLoadMoreListener {
    private LinearLayout back_ll;
    public static final String ACTION_REFRESH_DATA = "ekoolab.com.show.activity.FollowersActivity.refresh_data";
    private int pageIndex;
    private EmptyView mEmptyView;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private BaseQuickAdapter mAdapter;
//    private BaseQuickAdapter<UserInfo, BaseViewHolder> mAdapter = null;
    private List<UserInfo> followers = new ArrayList<>(20);

    @Override
    protected int getLayoutId(){
        return R.layout.activity_followers;
    }



    @Override
    public void onStart() {
        super.onStart();
        mEmptyView.showLoading();
        getFollowers(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initViews() {
        super.initViews();
        back_ll = findViewById(R.id.back_ll);
        back_ll.setOnClickListener(this);

        mEmptyView = findViewById(R.id.empty_view);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LinearItemDecoration(this,
                1, R.color.colorLightGray, 0));
        refreshLayout = findViewById(R.id.refreshLayout);
        initRefreshLayout();
        initAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view){
        Intent intent;
        switch(view.getId()){
            case R.id.back_ll:
                onBackPressed();
                break;
        }
    }

    private void initAdapter() {
        System.out.println("Enter initAdapter");
        mAdapter = new BaseQuickAdapter<UserInfo, BaseViewHolder>(R.layout.item_followers_following, followers) {

            @Override
            protected void convert(BaseViewHolder helper, UserInfo item) {
                ImageLoader.displayImage(item.avatar.small, helper.getView(R.id.iv_avatar));
                if(item.nickname!=null){
                    helper.setText(R.id.tv_name, item.nickname);
                } else {
                    helper.setText(R.id.tv_name, item.name);
                }
            }
        };
        mAdapter.setHasStableIds(false);
    }

    private void initRefreshLayout() {
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshLoadMoreListener(this);
    }


    private void getFollowers(int flag){
        String apiToken = AuthUtils.getInstance(this).getApiToken();
        HashMap<String, String> map = new HashMap<>(1);
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageIndex", pageIndex + "");
        map.put("token", apiToken);
        ApiServer.basePostRequest(this, Constants.MY_FOllOWER, map, new TypeToken<ResponseData<List<UserInfo>>>(){})
                .subscribe(new NetworkSubscriber<List<UserInfo>>() {
                    @Override
                    protected void onSuccess(List<UserInfo> followerList) {
                        try {
                            if (Utils.isNotEmpty(followerList)) {
                                if (flag == 2) {
                                    followers.addAll(followerList);
                                    mAdapter.notifyItemRangeChanged(followers.size() - followerList.size(), followers.size());
                                } else if (flag == 1) {
                                    mAdapter.notifyItemRangeRemoved(followerList.size(), followers.size());
                                    followers.clear();
                                    followers.addAll(followerList);
                                    mAdapter.notifyItemRangeChanged(0, followers.size());
                                } else {
                                    followers.clear();
                                    followers.addAll(followerList);
                                    mAdapter.notifyDataSetChanged();
                                }
                                if (followerList.size() == 20) {
                                    pageIndex++;
                                } else {
                                    refreshLayout.setEnableLoadMore(false);
                                }
                                mEmptyView.content().show();
                            } else if(followers.size()!=0){
                                refreshLayout.setEnableLoadMore(false);
                            } else{
                                mEmptyView.showEmpty();
                            }
                            refreshLayout.finishRefresh();
                            refreshLayout.finishLoadMore();
                        } catch (Exception e) {
                            refreshLayout.finishRefresh();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        mEmptyView.error(e).show();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });

    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        getFollowers(2);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getFollowers(1);
    }
}
