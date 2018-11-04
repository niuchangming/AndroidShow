package ekoolab.com.show.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.health.SystemHealthManager;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
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
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;

public class RegionActivity extends BaseActivity implements View.OnClickListener {


    private TextView tv_name,tv_cancel,tv_save;
    private RelativeLayout china_rl, singapore_rl, malaysia_rl, last_layout;
    private ImageView china_right_icon, singapore_right_icon, malaysia_right_icon;
    private int color_highlight, color_default;
    private String region;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_region;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_name = findViewById(R.id.tv_name);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(this);
        china_rl = findViewById(R.id.china_rl);
        china_rl.setOnClickListener(this);
        singapore_rl = findViewById(R.id.singapore_rl);
        singapore_rl.setOnClickListener(this);
        malaysia_rl = findViewById(R.id.malaysia_rl);
        malaysia_rl.setOnClickListener(this);
        china_right_icon = findViewById(R.id.china_right_icon);
        singapore_right_icon = findViewById(R.id.singapore_right_icon);
        malaysia_right_icon = findViewById(R.id.malaysia_right_icon);
        color_default = ContextCompat.getColor(this, R.color.colorWhite);
        color_highlight = ContextCompat.getColor(this, R.color.appBackground);
//        color_default = R.color.colorWhite;
//        color_highlight = R.color.gray;
        tv_save = findViewById(R.id.tv_save);
        tv_save.setOnClickListener(this);
        tv_name.setText(getResources().getString(R.string.region));
    }
    @Override
    protected void initData() {
        super.initData();

    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view){
        Intent intent;
        switch (view.getId()){
            case R.id.tv_cancel:
                onBackPressed();
                break;
            case R.id.china_rl:
                highlight(china_rl);
                china_right_icon.setVisibility(View.VISIBLE);
                singapore_right_icon.setVisibility(View.INVISIBLE);
                malaysia_right_icon.setVisibility(View.INVISIBLE);
                region = "China";
                break;
            case R.id.singapore_rl:
                highlight(singapore_rl);
                china_right_icon.setVisibility(View.INVISIBLE);
                singapore_right_icon.setVisibility(View.VISIBLE);
                malaysia_right_icon.setVisibility(View.INVISIBLE);
                region = "Singapore";
                break;
            case R.id.malaysia_rl:
                highlight(malaysia_rl);
                china_right_icon.setVisibility(View.INVISIBLE);
                singapore_right_icon.setVisibility(View.INVISIBLE);
                malaysia_right_icon.setVisibility(View.VISIBLE);
                region = "Malaysia";
                break;
            case R.id.tv_save:
                setRegion();
        }
    }

    private void highlight(RelativeLayout current_layout){
        if(last_layout != null) {
            color_default = ContextCompat.getColor(this, R.color.colorWhite);
            last_layout.setBackgroundColor(color_default);
        }
        current_layout.setBackgroundColor(color_highlight);
        last_layout = current_layout;
    }

    private void setRegion(){
        tv_save.setVisibility(View.INVISIBLE);
        setViewClickable(false);
        HashMap<String, String> map = new HashMap<>(2);
        map.put("region", region);
        map.put("token", AuthUtils.getInstance(this).getApiToken());
        ApiServer.basePostRequest(this, Constants.UPDATE_USERPROFILE, map,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String success) {
                        ToastUtils.showToast("Saved");
                        Intent intent = new Intent();
                        intent.putExtra("region", region);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        System.out.println("===errorMsg==="+errorMsg);
                        tv_save.setVisibility(View.VISIBLE);
                        setViewClickable(true);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void setViewClickable(boolean clickable) {
        tv_cancel.setClickable(clickable);
        tv_save.setClickable(clickable);
        malaysia_rl.setClickable(clickable);
        malaysia_rl.setEnabled(clickable);
        china_rl.setClickable(clickable);
        china_rl.setEnabled(clickable);
        singapore_rl.setClickable(clickable);
        singapore_rl.setEnabled(clickable);
    }
}
