package ekoolab.com.show.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.activities.FollowersActivity;
import ekoolab.com.show.activities.FollowingActivity;
import ekoolab.com.show.activities.SMSLoginActivity;
import ekoolab.com.show.activities.PersonActivity;
import ekoolab.com.show.adapters.ProfileAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.UserInfo;
import ekoolab.com.show.fragments.submyvideos.MyCollectsFragment;
import ekoolab.com.show.fragments.submyvideos.MyVideoFragment;
import ekoolab.com.show.fragments.submyvideos.MymomentsFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
//import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.utils.TimeUtils;

import static com.facebook.FacebookSdk.getApplicationContext;
import static ekoolab.com.show.utils.AuthUtils.AuthType.LOGGED;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {
//public class ProfileFragment extends BaseFragment{

    private BaseActivity activity;
    private RelativeLayout  name_rl,nickname_rl,gender_rl,birthday_rl,whatsup_rl,region_rl,header_rl;
//    private TextView tv_name,tv_nickname,tv_gender,tv_birthday,tv_whatsup,tv_region;

    // NEW

    private TextView tv_name,tv_cancel,tv_save,tv_birthday,tv_followers,tv_following,tv_coins,tv_gender,tv_region;
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private ProfileAdapter pagerAdapter;
    private List<BaseFragment> fragments;
    private Button btn_edit;
    private LinearLayout edit_ll, followers_ll, following_ll;
    private Context context;

    //UserInfo
    private int countryCode,roleType, followers, following;
    private String mobile, name, nickName, userCode, region, whatsup, category, description;
    private List<String> hobby;
    public ImageView avatar, coverImage;
    private Long birthday;
    private UserInfo userInfo;

    @Override
    public void onAttach(Context context) {
        activity = (BaseActivity) context;
        super.onAttach(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile;
    }

    @Override
    public void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
        if(AuthUtils.getInstance(getContext()).loginState() == LOGGED){
            getUserInfo();;
        } else {
        }
//        getUserInfo();
    }

    @Override
    public void onStop() {
        super.onStop();
//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void initData(){
        super.initData();
    }

    @Override
    protected void initViews(ViewHolder holder, View root){
        btn_edit = holder.get(R.id.btn_edit);
        btn_edit.setOnClickListener(this);
        edit_ll = holder.get(R.id.edit_ll);
        edit_ll.setOnClickListener(this);
        followers_ll = holder.get(R.id.followers_ll);
        followers_ll.setOnClickListener(this);
        following_ll = holder.get(R.id.following_ll);
        following_ll.setOnClickListener(this);

        indicatorTabLayout = holder.get(R.id.indicator_tab);
        viewPager = holder.get(R.id.viewpager);

        //User Info
        tv_name = holder.get(R.id.tv_name);
        tv_followers = holder.get(R.id.tv_followers);
        tv_following = holder.get(R.id.tv_following);
        tv_coins = holder.get(R.id.tv_coins);
        tv_gender = holder.get(R.id.tv_gender);
        tv_birthday = holder.get(R.id.tv_birthday);
        tv_region = holder.get(R.id.tv_region);
        avatar = holder.get(R.id.avatar);

        fragments = new ArrayList<>();
        fragments.add(new MyVideoFragment());
        fragments.add(new MyCollectsFragment());
        fragments.add(new MymomentsFragment());
        pagerAdapter =  new ProfileAdapter(getContext(), getChildFragmentManager(), fragments);
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
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onResultEvent(EventBusMsg eventBusMsg) {
//        showOrHideNavAnim(eventBusMsg.getFlag());
//    }


    private void showOrHideNavAnim(int flag) {

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()){
            case R.id.edit_ll:
                if(AuthUtils.getInstance(getContext()).loginState() == LOGGED){
                    intent = new Intent(getContext(), PersonActivity.class);
                    intent.putExtra("userInfo",userInfo);
                    activity.startActivityForResult(intent, Constants.PersonActResult);
                } else {
                    login();
                }
                break;
            case R.id.btn_edit:
                if(AuthUtils.getInstance(getContext()).loginState() == LOGGED){
                    intent = new Intent(getContext(), PersonActivity.class);
                    intent.putExtra("userInfo",userInfo);
                    getContext().startActivity(intent);
                } else {
                    login();
                }
                break;
            case R.id.followers_ll:
                intent = new Intent(getContext(), FollowersActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.following_ll:
                intent = new Intent(getContext(), FollowingActivity.class);
                getContext().startActivity(intent);
                break;
        }
    }

    private void getUserInfo(){
        String apiToken = AuthUtils.getInstance(getContext()).getApiToken();
        HashMap<String, String> map = new HashMap<>(1);
        map.put("token", apiToken);
        ApiServer.basePostRequest(this, Constants.GET_USERPROFILE, map, new TypeToken<ResponseData<UserInfo>>(){})
                .subscribe(new NetworkSubscriber<UserInfo>() {
                    @Override
                    protected void onSuccess(UserInfo userInfo) {
                        saveUserInfo(userInfo);
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        AuthUtils.getInstance(getApplicationContext()).logout();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    public void saveUserInfo(UserInfo userInfo) {
//        SharedPreferences sp = context.getSharedPreferences(context.getPackageCodePath(), Context.MODE_PRIVATE);
//        spEditor.putString(Constants.Auth.NICKNAME, userInfo.nickName);
//        spEditor.putLong(Constants.Auth.BIRTHDAY, userInfo.birthday);
//        spEditor.putInt(Constants.Auth.FOLLOWERS, userInfo.followers);
//        spEditor.putInt(Constants.Auth.FOLLOWING, userInfo.following);
//        spEditor.putString(Constants.Auth.REGION, userInfo.region);
//        spEditor.putString(Constants.Auth.WHATSUP, userInfo.whatsup);
//        spEditor.putString(Constants.Auth.CATEGORY, userInfo.category);
//        spEditor.putString(Constants.Auth.DESCRIPTION, userInfo.description);
//        if (userInfo.avatar != null) {
//            spEditor.putString(Constants.Auth.AVATAR_MEDIUM, userInfo.avatar.medium);
//            spEditor.putString(Constants.Auth.AVATAR_ORIGIN, userInfo.avatar.origin);
//            spEditor.putString(Constants.Auth.AVATAR_SMALL, userInfo.avatar.small);
//        }
//        spEditor.apply();

        this.userInfo = userInfo;
        tv_name.setText(userInfo.nickname);
        tv_followers.setText(Integer.toString(userInfo.followerCount));
        tv_following.setText(Integer.toString(userInfo.followingCount));
        String gender = (userInfo.gender == 0)? "Male":"Female";
        tv_gender.setText(gender);
        tv_birthday.setText(TimeUtils.getDateStringByTimeStamp(userInfo.birthday));
        tv_region.setText(userInfo.region);
//        String avatarSmall = AuthUtils.getInstance(getApplicationContext()).getAvator(2);
//        Glide.with(this).load(avatarSmall).into(avatar);
        ImageLoader.displayImageAsCircle(userInfo.avatar.small, avatar);
    }

    private void login(){
        Intent intent = new Intent(getContext(), SMSLoginActivity.class);
        getContext().startActivity(intent);
    }

    private void resetView(){
        //User Info
        String emptyInfo = "--";
        tv_name.setText("User information");
        tv_followers.setText(emptyInfo);
        tv_following.setText(emptyInfo);
        tv_coins.setText(emptyInfo);
        tv_gender.setText(emptyInfo);
        tv_birthday.setText(emptyInfo);
        tv_region.setText(emptyInfo);
        Uri uri = Uri.parse("android.resource://AndroidShow/" + R.mipmap.default_avatar);
        Glide.with(this).load(uri).into(avatar);
        System.out.println("display reset.");
    }
}
