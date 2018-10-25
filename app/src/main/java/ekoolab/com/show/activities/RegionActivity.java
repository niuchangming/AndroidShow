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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ekoolab.com.show.R;

public class RegionActivity extends BaseActivity implements View.OnClickListener {


    private TextView tv_name,tv_cancel,tv_save;
    private RelativeLayout china_rl, singapore_rl, malaysia_rl, last_layout;
    private ImageView china_right_icon, singapore_right_icon, malaysia_right_icon;
    private int color_highlight, color_default;
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
//        EventBus.getDefault().register(this);
    }


    private void showOrHideNavAnim(int flag) {

    }

    @Override
    protected void onStop() {
        super.onStop();
//        EventBus.getDefault().unregister(this);
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
                break;
            case R.id.singapore_rl:
                highlight(singapore_rl);
                china_right_icon.setVisibility(View.INVISIBLE);
                singapore_right_icon.setVisibility(View.VISIBLE);
                malaysia_right_icon.setVisibility(View.INVISIBLE);
                break;
            case R.id.malaysia_rl:
                highlight(malaysia_rl);
                china_right_icon.setVisibility(View.INVISIBLE);
                singapore_right_icon.setVisibility(View.INVISIBLE);
                malaysia_right_icon.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_save:
                saveRegionInfo();
                onBackPressed();
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

    private void saveRegionInfo(){

    }
}
