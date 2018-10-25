package ekoolab.com.show.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewDebug;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.TextPicture;
import ekoolab.com.show.beans.UserInfo;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
//import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;

public class GenderActivity extends BaseActivity implements View.OnClickListener{


    private TextView tv_name,tv_cancel,tv_save;
    private RelativeLayout male_rl,female_rl;
    private ImageView male_right_icon, female_right_icon;
    private int gender;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_gender;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_name = findViewById(R.id.tv_name);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(this);
        male_rl = findViewById(R.id.male_rl);
        male_rl.setOnClickListener(this);
        female_rl = findViewById(R.id.female_rl);
        female_rl.setOnClickListener(this);
        tv_save = findViewById(R.id.tv_save);
        tv_save.setOnClickListener(this);
        tv_name.setText(getResources().getString(R.string.gender));
        male_right_icon = findViewById(R.id.male_right_icon);
        female_right_icon = findViewById(R.id.female_right_icon);
    }
    @Override
    protected void initData() {
        super.initData();

    }
    @Override
    protected void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
        gender = getIntent().getIntExtra("gender", 1);
        System.out.println("gender is " + gender);
        setSelection(gender);
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onResultEvent(EventBusMsg eventBusMsg) {
//        showOrHideNavAnim(eventBusMsg.getFlag());
//    }


    private void showOrHideNavAnim(int flag) {

    }

    @Override
    protected void onStop() {
        super.onStop();
//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view){
        Intent intent = new Intent();
        switch (view.getId()){
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.male_rl:
                gender = 0;
                setSelection(gender);
                break;
            case R.id.female_rl:
                gender = 1;
                setSelection(gender);
                break;
            case R.id.tv_save:
                setGender();
                break;
        }
    }

    private void setSelection(int num){
        switch (num){
            case 0:
                male_right_icon.setVisibility(View.VISIBLE);
                female_right_icon.setVisibility(View.INVISIBLE);
                break;
            case 1:
                male_right_icon.setVisibility(View.INVISIBLE);
                female_right_icon.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setGender(){
        tv_save.setVisibility(View.INVISIBLE);
//        progressView.setVisibility(View.VISIBLE);
//        progressView.start();
        setViewClickable(false);
        HashMap<String, String> map = new HashMap<>(2);
        map.put("gender", Integer.toString(gender));
        map.put("token", AuthUtils.getInstance(GenderActivity.this).getApiToken());
        ApiServer.baseUploadRequest(this, Constants.UPDATE_USERPROFILE, map, null,
                new TypeToken<ResponseData<TextPicture>>() {
                })
                .subscribe(new NetworkSubscriber<TextPicture>() {
                    @Override
                    protected void onSuccess(TextPicture textPicture) {
                        ToastUtils.showToast("Saved");
                        Intent intent = new Intent();
                        intent.putExtra("gender", gender);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        System.out.println("===errorMsg==="+errorMsg);
                        tv_save.setVisibility(View.VISIBLE);
                        setViewClickable(true);
//                        progressView.setVisibility(View.GONE);
//                        progressView.stop();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void setViewClickable(boolean clickable) {
        tv_cancel.setClickable(clickable);
        tv_save.setClickable(clickable);
        male_rl.setClickable(clickable);
        female_rl.setClickable(clickable);
    }
}
