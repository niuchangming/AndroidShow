package ekoolab.com.show.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.activities.BirthdayActivity;
import ekoolab.com.show.activities.FollowersActivity;
import ekoolab.com.show.activities.FollowingsActivity;
import ekoolab.com.show.activities.GenderActivity;
import ekoolab.com.show.activities.LoginActivity;
import ekoolab.com.show.activities.NameActivity;
import ekoolab.com.show.activities.NicknameActivity;
import ekoolab.com.show.activities.PersonActivity;
import ekoolab.com.show.activities.RegionActivity;
import ekoolab.com.show.activities.WhatsupActivity;
import ekoolab.com.show.adapters.ProfileAdapter;
import ekoolab.com.show.fragments.submyvideos.MyCollectsFragment;
import ekoolab.com.show.fragments.submyvideos.MyVideoFragment;
import ekoolab.com.show.fragments.submyvideos.MymomentsFragment;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.ViewHolder;

public class ProfileFragment extends BaseFragment implements View.OnClickListener{
//public class ProfileFragment extends BaseFragment{

    private BaseActivity activity;
    private RelativeLayout  name_rl,nickname_rl,gender_rl,birthday_rl,whatsup_rl,region_rl,header_rl;
//    private TextView tv_name,tv_nickname,tv_gender,tv_birthday,tv_whatsup,tv_region;

    // NEW

    private TextView tv_name,tv_cancel,tv_save;
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private ProfileAdapter pagerAdapter;
    private List<BaseFragment> fragments;
    private Button btn_edit;
    private LinearLayout edit_ll, followers_ll, followings_ll;

//    @Override
//    public void onAttach(Context context) {
//        activity = (BaseActivity) context;
//        super.onAttach(context);
//    }
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.fragment_profile;
//    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onResultEvent(EventBusMsg eventBusMsg) {
//        if (eventBusMsg.getFlag() == 0 || eventBusMsg.getFlag() == 1) {
//
//        }
//    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        EventBus.getDefault().unregister(this);
//    }
//
//    @Override
//    protected void initViews(ViewHolder holder, View root) {
//        name_rl = holder.get(R.id.name_rl);
//        name_rl.setOnClickListener(this);
//        nickname_rl = holder.get(R.id.nickname_rl);
//        nickname_rl.setOnClickListener(this);
//        gender_rl = holder.get(R.id.gender_rl);
//        gender_rl.setOnClickListener(this);
//        birthday_rl = holder.get(R.id.birthday_rl);
//        birthday_rl.setOnClickListener(this);
//        whatsup_rl = holder.get(R.id.whatsup_rl);
//        whatsup_rl.setOnClickListener(this);
//        region_rl = holder.get(R.id.region_rl);
//        region_rl.setOnClickListener(this);
//        header_rl = holder.get(R.id.header_rl);
//        header_rl.setOnClickListener(this);
//        tv_name = holder.get(R.id.tv_name);
//        tv_nickname = holder.get(R.id.tv_nickname);
//        tv_gender = holder.get(R.id.tv_gender);
//        tv_birthday = holder.get(R.id.tv_birthday);
//        tv_whatsup = holder.get(R.id.tv_whatsup);
//        tv_region = holder.get(R.id.tv_region);
//
//
//    }


//    @Override
//    protected void initData() {
//        super.initData();
//    }
//
//    @Override
//    public void onClick(View view) {
//        Intent intent;
//        switch(view.getId()){
//            case R.id.header_rl:
//                intent = new Intent(getContext(), PersonActivity.class);
//                getContext().startActivity(intent);
//                break;
//
//            case R.id.name_rl:
//                intent = new Intent(getContext(), NameActivity.class);
//                getContext().startActivity(intent);
//                break;
//            case R.id.nickname_rl:
//                intent = new Intent(getContext(), NicknameActivity.class);
//                getContext().startActivity(intent);
//                break;
//            case R.id.gender_rl:
//                intent = new Intent(getContext(), GenderActivity.class);
//                getContext().startActivity(intent);
//                break;
//            case R.id.birthday_rl:
//                intent = new Intent(getContext(), BirthdayActivity.class);
//                getContext().startActivity(intent);
//                break;
//            case R.id.whatsup_rl:
//                intent = new Intent(getContext(), WhatsupActivity.class);
//                getContext().startActivity(intent);
//                break;
//            case R.id.region_rl:
//                intent = new Intent(getContext(), RegionActivity.class);
//                getContext().startActivity(intent);
//                break;
//        }
//    }
//
//    private void login(){
//        Intent intent = new Intent(getContext(), LoginActivity.class);
//        getContext().startActivity(intent);
//    }


    // NEW


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
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
        followings_ll = holder.get(R.id.followings_ll);
        followings_ll.setOnClickListener(this);

        indicatorTabLayout = holder.get(R.id.indicator_tab);
        viewPager = holder.get(R.id.viewpager);

        fragments = new ArrayList<>();
        fragments.add(new MyVideoFragment());
        fragments.add(new MyCollectsFragment());
        fragments.add(new MymomentsFragment());
        pagerAdapter =  new ProfileAdapter(getChildFragmentManager(), fragments);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(EventBusMsg eventBusMsg) {
        showOrHideNavAnim(eventBusMsg.getFlag());
    }


    private void showOrHideNavAnim(int flag) {

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()){
            case R.id.edit_ll:
                intent = new Intent(getContext(), PersonActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.btn_edit:
                intent = new Intent(getContext(), PersonActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.followers_ll:
                intent = new Intent(getContext(), FollowersActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.followings_ll:
                intent = new Intent(getContext(), FollowingsActivity.class);
                getContext().startActivity(intent);
                break;
        }
    }
}
