package ekoolab.com.show.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.facebook.login.Login;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.ProfileAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.TextPicture;
import ekoolab.com.show.beans.UserInfo;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.fragments.DatePickerFragment;
import ekoolab.com.show.fragments.submyvideos.MyCollectsFragment;
import ekoolab.com.show.fragments.submyvideos.MyVideoFragment;
import ekoolab.com.show.fragments.submyvideos.MymomentsFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;

import static com.facebook.FacebookSdk.getApplicationContext;

public class PersonActivity extends BaseActivity implements View.OnClickListener  {

//    private TextView tv_name,tv_cancel,tv_save;
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private ProfileAdapter pagerAdapter;
    private List<BaseFragment> fragments;


    private BaseActivity activity;
    private RelativeLayout name_rl,nickname_rl,gender_rl,birthday_rl,whatsup_rl,region_rl,title_rl,header_rl;
    private TextView tv_name,tv_nickname,tv_gender,tv_birthday,tv_whatsup,tv_region;
    private Button btn_edit_cover, btn_logout;
    private ImageView avatar, cover_image;
    private UserInfo userInfo;
    protected String birthday;


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
        Bundle info = getIntent().getExtras();
        userInfo = info.getParcelable("userInfo");
        loadData();
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
        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(this);
        tv_name = findViewById(R.id.tv_name);
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_gender = findViewById(R.id.tv_gender);
        tv_birthday = findViewById(R.id.tv_birthday);
        tv_whatsup = findViewById(R.id.tv_whatsup);
        tv_region = findViewById(R.id.tv_region);
        avatar = findViewById(R.id.avatar);
        cover_image = findViewById(R.id.cover_image);
//        tv_name = findViewById(R.id.tv_name);
//        tv_nickname = findViewById(R.id.tv_nickname);
//        tv_gender = findViewById(R.id.tv_gender);
//        tv_birthday = findViewById(R.id.tv_birthday);
//        tv_whatsup = findViewById(R.id.tv_whatsup);
//        tv_region = findViewById(R.id.tv_region);
    }

    private void loadData(){
        tv_name.setText(userInfo.name);
        tv_nickname.setText(userInfo.nickname);
        tv_gender.setText((userInfo.gender == 0)? "Male":"Female");
        tv_birthday.setText(TimeUtils.getDateByTimeStamp(userInfo.birthday, "yy-MM-dd"));
        tv_whatsup.setText(userInfo.whatsup);
        tv_region.setText(userInfo.region);
        String avatarSmall = AuthUtils.getInstance(getApplicationContext()).getAvator(1);
        Glide.with(this).load(avatarSmall).into(avatar);
        Glide.with(this).load(avatarSmall).into(cover_image);
        if(userInfo.roleType == 2){
            btn_edit_cover = findViewById(R.id.btn_edit_cover);
            btn_edit_cover.setOnClickListener(this);
            btn_edit_cover.setVisibility(View.VISIBLE);
        }
//        String coverMedium = AuthUtils.getInstance(getApplicationContext()).getAvator(2);
//        Glide.with(this).load(userInfo.coverImage.medium).into();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()){
            case R.id.name_rl:
                name_rl.setClickable(false);
                intent = new Intent(this, NameActivity.class);
//                intent.putExtra("name", userInfo.name);
                this.startActivityForResult(intent, UserInfo.REQUEST_NAME);
                break;
            case R.id.nickname_rl:
                nickname_rl.setClickable(false);
                intent = new Intent(this, NicknameActivity.class);
//                intent.putExtra("nickName", userInfo.nickName);
                this.startActivityForResult(intent, UserInfo.REQUEST_NICKNAME);
                break;
            case R.id.gender_rl:
                gender_rl.setClickable(false);
                intent = new Intent(this, GenderActivity.class);
                intent.putExtra("gender", userInfo.gender);
                this.startActivityForResult(intent, UserInfo.REQUEST_GENDER);
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
                whatsup_rl.setClickable(false);
                intent = new Intent(this, WhatsupActivity.class);
//                intent.putExtra("whatsup", userInfo.whatsup);
                this.startActivityForResult(intent, UserInfo.REQUEST_WHATSUP);
                break;
            case R.id.region_rl:
                region_rl.setClickable(false);
                intent = new Intent(this, RegionActivity.class);
                intent.putExtra("region", userInfo.region);
                this.startActivityForResult(intent, UserInfo.REQUEST_REGION);
                break;
            case R.id.btn_edit_cover:
                btn_edit_cover.setClickable(false);
                intent = new Intent(this, ChooseCoverActivity.class);
                this.startActivity(intent);
                break;
            case R.id.btn_logout:
                AuthUtils.getInstance(getApplicationContext()).logout();
                btn_logout.setClickable(false);
                onBackPressed();
                break;
            case R.id.title_rl:
                title_rl.setClickable(false);
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("Enter onActivityResult funcion. RequestCode: " + requestCode + ". ResultCode: "+ resultCode + ". data: ");
        if(data == null){ return; }
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case UserInfo.REQUEST_NAME:
                    userInfo.name = data.getStringExtra("name");
                    tv_name.setText(userInfo.name);
                    break;
                case UserInfo.REQUEST_NICKNAME:
                    userInfo.nickname = data.getStringExtra("nickname");
                    tv_nickname.setText(userInfo.nickname);
                    break;
                case UserInfo.REQUEST_GENDER:
                    userInfo.gender = data.getIntExtra("gender",userInfo.gender);
                    tv_gender.setText((userInfo.gender == 0)? "Male":"Female");
                    break;
//            case REQUEST_BIRTHDAY:
//                break;
                case UserInfo.REQUEST_REGION:
                    userInfo.region = data.getStringExtra("region");
                    tv_region.setText(userInfo.region);
                    break;
                case UserInfo.REQUEST_WHATSUP:
                    userInfo.whatsup = data.getStringExtra("whatsup");
                    tv_whatsup.setText(userInfo.whatsup);
                    break;
            }
        }
    }

//    private void login(){
//        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//        getApplicationContext().startActivity(intent);
//    }

    @Subscribe
    public void onEventMessage(EventBusMsg event){
        birthday = event.getMsg();
        tv_birthday.setText(birthday);
        System.out.println("Enter event bus, birthday: " + birthday);
        birthday = String.valueOf(TimeUtils.getTimeStampByDate(birthday, TimeUtils.YYYYMMDD));
        HashMap<String, String> map = new HashMap<>(2);
        map.put("birthday", birthday);
        map.put("token", AuthUtils.getInstance(PersonActivity.this).getApiToken());
        ApiServer.basePostRequest(this, Constants.UPDATE_USERPROFILE, map,
                new TypeToken<ResponseData<TextPicture>>() {
                })
                .subscribe(new NetworkSubscriber<TextPicture>() {
                    @Override
                    protected void onSuccess(TextPicture textPicture) {
                        ToastUtils.showToast("Saved");
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        System.out.println("===errorMsg==="+errorMsg);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }
}
