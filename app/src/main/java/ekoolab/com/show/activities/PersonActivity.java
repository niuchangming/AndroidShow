package ekoolab.com.show.activities;

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

import com.facebook.login.Login;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.ProfileAdapter;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.fragments.DatePickerFragment;
import ekoolab.com.show.fragments.submyvideos.MyCollectsFragment;
import ekoolab.com.show.fragments.submyvideos.MyVideoFragment;
import ekoolab.com.show.fragments.submyvideos.MymomentsFragment;
import ekoolab.com.show.utils.EventBusMsg;

public class PersonActivity extends BaseActivity implements View.OnClickListener  {

//    private TextView tv_name,tv_cancel,tv_save;
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private ProfileAdapter pagerAdapter;
    private List<BaseFragment> fragments;


    private BaseActivity activity;
    private RelativeLayout name_rl,nickname_rl,gender_rl,birthday_rl,whatsup_rl,region_rl,title_rl;
    private TextView tv_name,tv_nickname,tv_gender,tv_birthday,tv_whatsup,tv_region;
    private Button btn_editCover, btn_logout;

//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_person;
//    }

//    @Override
//    protected void initViews() {
//        super.initViews();
//        indicatorTabLayout = findViewById(R.id.indicator_tab);
//        viewPager = findViewById(R.id.viewpager);
//
//        fragments = new ArrayList<>();
//        fragments.add(new MyVideoFragment());
//        fragments.add(new MyCollectsFragment());
//        fragments.add(new MymomentsFragment());
//        pagerAdapter =  new ProfileAdapter(getSupportFragmentManager(), fragments);
//        viewPager.setAdapter(pagerAdapter);
//        indicatorTabLayout.setupWithViewPager(viewPager);
//        indicatorTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                LinearLayout tabLayout = (LinearLayout) ((ViewGroup) indicatorTabLayout.getChildAt(0)).getChildAt(tab.getPosition());
//                TextView tabTextView = (TextView) tabLayout.getChildAt(1);
//                tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.BOLD);
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                LinearLayout tabLayout = (LinearLayout)((ViewGroup) indicatorTabLayout.getChildAt(0)).getChildAt(tab.getPosition());
//                TextView tabTextView = (TextView) tabLayout.getChildAt(1);
//                tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.NORMAL);
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
//
//
//    }
//    @Override
//    protected void initData() {
//        super.initData();
//
//    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        EventBus.getDefault().register(this);
//    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onResultEvent(EventBusMsg eventBusMsg) {
//        showOrHideNavAnim(eventBusMsg.getFlag());
//    }

//
//    private void showOrHideNavAnim(int flag) {
//
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        EventBus.getDefault().unregister(this);
//    }

    //NEW


    @Override
    protected int getLayoutId() {
        return R.layout.activity_person;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(EventBusMsg eventBusMsg) {
        if (eventBusMsg.getFlag() == 0 || eventBusMsg.getFlag() == 1) {

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initViews() {
        super.initViews();
        name_rl = findViewById(R.id.name_rl);
        name_rl.setOnClickListener(this);
        nickname_rl = findViewById(R.id.nickname_rl);
        nickname_rl.setOnClickListener(this);
        gender_rl = findViewById(R.id.gender_rl);
        gender_rl.setOnClickListener(this);
        birthday_rl = findViewById(R.id.birthday_rl);
        birthday_rl.setOnClickListener(this);
        whatsup_rl = findViewById(R.id.whatsup_rl);
        whatsup_rl.setOnClickListener(this);
        region_rl = findViewById(R.id.region_rl);
        region_rl.setOnClickListener(this);
        title_rl = findViewById(R.id.title_rl);
        title_rl.setOnClickListener(this);
        btn_editCover = findViewById(R.id.btn_edit_cover);
        btn_editCover.setOnClickListener(this);
        btn_logout = findViewById(R.id.btn_login);
        btn_logout.setOnClickListener(this);
//        tv_name = findViewById(R.id.tv_name);
//        tv_nickname = findViewById(R.id.tv_nickname);
//        tv_gender = findViewById(R.id.tv_gender);
//        tv_birthday = findViewById(R.id.tv_birthday);
//        tv_whatsup = findViewById(R.id.tv_whatsup);
//        tv_region = findViewById(R.id.tv_region);


    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()){
            case R.id.name_rl:
                intent = new Intent(this, NameActivity.class);
                this.startActivity(intent);
                break;
            case R.id.nickname_rl:
                intent = new Intent(this, NicknameActivity.class);
                this.startActivity(intent);
                break;
            case R.id.gender_rl:
                intent = new Intent(this, GenderActivity.class);
                this.startActivity(intent);
                break;
//            case R.id.birthday_rl:
//                intent = new Intent(getApplicationContext(), BirthdayActivity.class);
//                this.startActivity(intent);
//                break;
            case R.id.birthday_rl:
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getFragmentManager(), "datepicker");
                break;
            case R.id.whatsup_rl:
                intent = new Intent(this, WhatsupActivity.class);
                this.startActivity(intent);
                break;
            case R.id.region_rl:
                intent = new Intent(this, RegionActivity.class);
                this.startActivity(intent);
                break;
            case R.id.btn_edit_cover:
                intent = new Intent(this, ChooseCoverActivity.class);
                this.startActivity(intent);
                break;
            case R.id.btn_login:
                onBackPressed();
                break;
            case R.id.title_rl:
                onBackPressed();
                break;
        }
    }
//
//    private void login(){
//        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//        getApplicationContext().startActivity(intent);
//    }

}
