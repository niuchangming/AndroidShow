package ekoolab.com.show.activities;

import android.Manifest;
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
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.compress.Luban;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.sendbird.android.SendBird;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
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
import ekoolab.com.show.utils.Chat.ChatManager;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.ImageSeclctUtils;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;

import static com.facebook.FacebookSdk.getApplicationContext;

public class PersonActivity extends BaseActivity implements View.OnClickListener  {

    private List<LocalMedia> localMedias = new ArrayList<>();
    private BaseActivity activity;
    private RelativeLayout name_rl,nickname_rl,gender_rl,birthday_rl,whatsup_rl,region_rl,title_rl,header_rl;
    private TextView tv_name,tv_nickname,tv_gender,tv_birthday,tv_whatsup,tv_region;
    private Button btn_edit_cover, btn_logout;
    private ImageView avatar, cover_image;
    private UserInfo userInfo;
    private int screenWidth, screenHeight;
    protected String birthday;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_person;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Bundle info = getIntent().getExtras();
        if(userInfo == null){
            userInfo = info.getParcelable("userInfo");
            loadData();

        }
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
        header_rl = findViewById(R.id.header_rl);
        avatar = findViewById(R.id.avatar);
        avatar.setOnClickListener(this);
        cover_image = findViewById(R.id.cover_image);
        ViewGroup.LayoutParams params = header_rl.getLayoutParams();
        params.height = DisplayUtils.getScreenWidth()*9/16;
    }

    private void loadData(){
        tv_name.setText(userInfo.name);
        tv_nickname.setText(userInfo.nickname);
        tv_gender.setText((userInfo.gender == 0)? "Male":"Female");
        tv_birthday.setText(TimeUtils.getDateStringByTimeStamp(userInfo.birthday));
        tv_whatsup.setText(userInfo.whatsup);
        tv_region.setText(userInfo.region);

        ImageLoader.displayImageAsCircle(userInfo.avatar.small, avatar);
        if(userInfo.roleType == 2){
            ImageLoader.displayImage(userInfo.coverImage.small, cover_image, 20);
            btn_edit_cover = findViewById(R.id.btn_edit_cover);
            btn_edit_cover.setOnClickListener(this);
            btn_edit_cover.setVisibility(View.VISIBLE);
        } else {
            ImageLoader.displayImage(userInfo.avatar.small, cover_image, 20);
        }
//        String coverMedium = AuthUtils.getInstance(getApplicationContext()).getAvator(2);
//        Glide.with(this).load(userInfo.coverImage.medium).into();
    }

    @Override
    protected void onDestroy(){
        PictureFileUtils.deleteCacheDirFile(PersonActivity.this);
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()){
            case R.id.name_rl:
                intent = new Intent(this, NameActivity.class);
//                intent.putExtra("name", userInfo.name);
                this.startActivityForResult(intent, UserInfo.REQUEST_NAME);
                break;
            case R.id.nickname_rl:
                intent = new Intent(this, NicknameActivity.class);
//                intent.putExtra("nickName", userInfo.nickName);
                this.startActivityForResult(intent, UserInfo.REQUEST_NICKNAME);
                break;
            case R.id.gender_rl:
                intent = new Intent(this, GenderActivity.class);
                intent.putExtra("gender", userInfo.gender);
                this.startActivityForResult(intent, UserInfo.REQUEST_GENDER);
                break;
            case R.id.birthday_rl:
//                intent = new Intent(this, BirthdayActivity.class);
////                intent.putExtra("gender", userInfo.gender);
//                this.startActivityForResult(intent, UserInfo.REQUEST_BIRTHDAY);
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getFragmentManager(), "datepicker");
                break;
            case R.id.whatsup_rl:
                intent = new Intent(this, WhatsupActivity.class);
//                intent.putExtra("whatsup", userInfo.whatsup);
                this.startActivityForResult(intent, UserInfo.REQUEST_WHATSUP);
                break;
            case R.id.region_rl:
                intent = new Intent(this, RegionActivity.class);
                intent.putExtra("region", userInfo.region);
                this.startActivityForResult(intent, UserInfo.REQUEST_REGION);
                break;
            case R.id.btn_edit_cover:
                selectImage(UserInfo.REQUEST_COVER);
                break;
            case R.id.btn_logout:
                logout();
                break;
            case R.id.title_rl:
                onBackPressed();
                break;
            case R.id.avatar:
                selectImage(UserInfo.REQUEST_AVATAR);
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
                case UserInfo.REQUEST_BIRTHDAY:
                    userInfo.birthday = data.getLongExtra("timeStamp", 0);
                    tv_nickname.setText(data.getStringExtra("birthday"));
                    break;
                case UserInfo.REQUEST_REGION:
                    userInfo.region = data.getStringExtra("region");
                    tv_region.setText(userInfo.region);
                    break;
                case UserInfo.REQUEST_WHATSUP:
                    userInfo.whatsup = data.getStringExtra("whatsup");
                    tv_whatsup.setText(userInfo.whatsup);
                    break;
                case UserInfo.REQUEST_AVATAR:
                    localMedias = PictureSelector.obtainMultipleResult(data);
                    userInfo.avatar.small = localMedias.get(0).getCutPath();
                    ImageLoader.displayImageAsCircle(userInfo.avatar.small, avatar);
                    if(userInfo.roleType != 2) { ImageLoader.displayImage(userInfo.avatar.small, cover_image, 20); }
                    updateImage(userInfo.avatar.small, "avatar");
                    break;
                case UserInfo.REQUEST_COVER:
                    localMedias = PictureSelector.obtainMultipleResult(data);
                    userInfo.coverImage.small = localMedias.get(0).getCutPath();
                    ImageLoader.displayImage(userInfo.coverImage.small, cover_image, 20);
                    updateImage(userInfo.coverImage.small, "coverImage");
                    break;
            }
        }
    }

    @Subscribe
    public void onEventMessage(String date){
        Long birthday = TimeUtils.getTimeStampByDate(date + " 00:00", TimeUtils.YYYYmmDDHHMM);
        tv_birthday.setText(TimeUtils.getDateStringByTimeStamp(birthday));
        HashMap<String, String> map = new HashMap<>(2);
        map.put("birthday", String.valueOf(birthday));
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

    private void updateImage(String path, String field){
        HashMap<String, File> fileMap = new HashMap<>(1);
        File image = new File(path);
        System.out.println("parameter: " + field);
        fileMap.put(field, image);
        HashMap<String, String> valueMap = new HashMap<>(1);
        valueMap.put("token", AuthUtils.getInstance(PersonActivity.this).getApiToken());
        ApiServer.baseUploadRequest(this, Constants.UPDATE_BROADCASTPROFILE, valueMap, fileMap,
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

    private void selectImage(int requestNum) {
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        PictureSelector.create(PersonActivity.this)
                                .openGallery(PictureMimeType.ofImage())
                                .imageSpanCount(4)
                                .selectionMode(PictureConfig.SINGLE)
                                .setOutputCameraPath(Constants.IMAGE_PATH)
                                .previewImage(true)
                                .compressGrade(Luban.THIRD_GEAR)
                                .enableCrop(true)
                                .withAspectRatio(5,3)
                                .isCamera(true)
                                .compress(false)
                                .selectionMedia(localMedias)
                                .forResult(requestNum);
                    }
                });
    }

    private void logout(){
        HashMap<String, String> map = new HashMap<>(1);
        map.put("token", AuthUtils.getInstance(PersonActivity.this).getApiToken());
        ApiServer.basePostRequest(this, Constants.LOGOUT, map,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        AuthUtils.getInstance(getApplicationContext()).logout();
//                        ChatManager.getInstance(PersonActivity.this).logout(new SendBird.DisconnectHandler() {
//                            @Override
//                            public void onDisconnected() {
//                                onDisconnected();
//                            }
//                        });
                        Intent intent = new Intent();
                        intent.putExtra("logout", true);
                        setResult(RESULT_OK, intent);
                        onBackPressed();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        System.out.println("===errorMsg==="+errorMsg);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }
}
