package ekoolab.com.show.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import com.google.gson.reflect.TypeToken;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.ProfileAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.UserInfo;
import ekoolab.com.show.beans.Video;
import ekoolab.com.show.beauty.utils.ToastUtil;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.fragments.submyvideos.MyCollectsFragment;
import ekoolab.com.show.fragments.submyvideos.MyVideoFragment;
import ekoolab.com.show.fragments.submyvideos.MymomentsFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.ToastUtils;

import static com.facebook.FacebookSdk.getApplicationContext;

public class OthersInfoActivity extends BaseActivity implements View.OnClickListener{
    private LinearLayout report_ll;
    private Button btn_block, btn_follow;
    private ImageView avatar;
    private TextView tv_name, tv_followers, tv_following, toolbar_title;
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private ProfileAdapter pagerAdapter;
    private List<BaseFragment> fragments;
    private String userCode;
    private UserInfo userInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_others_info;
    }

    @Override
    protected void initData() {
        super.initData();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            userCode = bundle.getString("userCode");
        }
        System.out.println("step 2");
    }

    @Override
    protected void initViews() {
        super.initViews();
        toolbar_title = findViewById(R.id.toolbar_title);
        report_ll = findViewById(R.id.report_ll);
        btn_block = findViewById(R.id.btn_block);
        btn_follow = findViewById(R.id.btn_follow);
        btn_block.setBackgroundColor(getResources().getColor(R.color.colorGray));
        btn_follow.setBackgroundColor(getResources().getColor(R.color.colorRed));
        avatar = findViewById(R.id.avatar);
        tv_name = findViewById(R.id.tv_name);
        tv_followers = findViewById(R.id.tv_followers);
        tv_following = findViewById(R.id.tv_following);

        indicatorTabLayout = findViewById(R.id.indicator_tab);
        viewPager = findViewById(R.id.viewpager);
        Bundle bundle = new Bundle();
        bundle.putString("userCode", userCode);
        MyVideoFragment myVideoFragment = new MyVideoFragment();
        myVideoFragment.setArguments(bundle);
        MymomentsFragment mymomentsFragment = new MymomentsFragment();
        mymomentsFragment.setArguments(bundle);
        fragments = new ArrayList<>();
        fragments.add(myVideoFragment);
        fragments.add(mymomentsFragment);
        pagerAdapter =  new ProfileAdapter(this, getSupportFragmentManager(), fragments, true);
        viewPager.setAdapter(pagerAdapter);
        indicatorTabLayout.setupWithViewPager(viewPager);
        indicatorTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                LinearLayout tabLayout = (LinearLayout) ((ViewGroup) indicatorTabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                TextView tabTextView = (TextView) tabLayout.getChildAt(1);
                tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.BOLD);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                LinearLayout tabLayout = (LinearLayout)((ViewGroup) indicatorTabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                TextView tabTextView = (TextView) tabLayout.getChildAt(1);
                tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.NORMAL);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        getUserInfo(userCode);
    }

    private void getUserInfo(String userCode){
        String apiToken = AuthUtils.getInstance(this).getApiToken();
        HashMap<String, String> map = new HashMap<>(2);
        System.out.println("see data sent, token: " + apiToken + ", userCode: " + userCode);
        map.put("token", apiToken);
        map.put("userCode", userCode);
        ApiServer.basePostRequest(this, Constants.GET_USERPROFILE, map, new TypeToken<ResponseData<UserInfo>>(){})
                .subscribe(new NetworkSubscriber<UserInfo>() {
                    @Override
                    protected void onSuccess(UserInfo userInfo) {
                        saveUserInfo(userInfo);
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void saveUserInfo(UserInfo userInfo){
        this.userInfo = userInfo;
        tv_name.setText(userInfo.name);
        toolbar_title.setText(userInfo.name);
        report_ll.setOnClickListener(this);
        btn_block.setOnClickListener(this);
        btn_follow.setOnClickListener(this);
        tv_followers.setText(userInfo.followerCount + getResources().getString(R.string.followers));
        tv_following.setText(userInfo.followingCount + getResources().getString(R.string.following));
        btn_follow.setText(((userInfo.followship==1)||(userInfo.followship==3))? getResources().getString(R.string.un_follow): getResources().getString(R.string.follow));
        ImageLoader.displayImageAsCircle(userInfo.avatar.small, avatar);
//        tv_name.setText(video.creator.name);
//        tv_followers.setText(Integer.toString(video.creator.followerCount) + "followers");
//        tv_following.setText(Integer.toString(video.creator.followingCount) + "following");
//        ImageLoader.displayImageAsCircle(userInfo.avatar.small, avatar);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.report_ll:
                break;
            case R.id.btn_block:
                if (!this.authorized(true)) {
                    ToastUtils.showToast("Please login first");
                }
                block();
                break;
            case R.id.btn_follow:
                if (!this.authorized(true)) {
                    ToastUtils.showToast("Please login first");
                }
                setFollow();
                break;
        }

    }

    private void setFollow() {
        HashMap<String, String> map = new HashMap<>(2);
        map.put("userCode", userInfo.userCode);
        map.put("token", AuthUtils.getInstance(this).getApiToken());
        ApiServer.basePostRequest(this, ((userInfo.followship%2==1)? Constants.UN_FOLLOW: Constants.FOLLOW), map,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        try {
                            if(userInfo.followship%2==1){
                                userInfo.followship -= 1;
                                btn_follow.setText(getResources().getString(R.string.follow));
                            } else {
                                userInfo.followship += 1;
                                btn_follow.setText(getResources().getString(R.string.un_follow));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void block(){
        HashMap<String, String> map = new HashMap<>(2);
        map.put("userCode", userInfo.userCode);
        map.put("token", AuthUtils.getInstance(this).getApiToken());
        String url = (userInfo.blocking)? Constants.UNBLOCK: Constants.BLOCK;
        ApiServer.basePostRequest(this, url, map,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        try {
                            if(!userInfo.blocking){
                                userInfo.blocking = true;
                                btn_block.setText(getResources().getString(R.string.unblock));
                            } else {
                                userInfo.blocking = false;
                                btn_block.setText(getResources().getString(R.string.block));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }
}
