package ekoolab.com.show.activities;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ekoolab.com.show.R;
import ekoolab.com.show.utils.EventBusMsg;

public class GenderActivity extends BaseActivity implements View.OnClickListener{


    private TextView tv_name,tv_cancel,tv_save;
    private RelativeLayout male_rl,female_rl;
    private ImageView male_right_icon, female_right_icon;
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
        EventBus.getDefault().register(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(EventBusMsg eventBusMsg) {
        showOrHideNavAnim(eventBusMsg.getFlag());
    }


    private void showOrHideNavAnim(int flag) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view){
        Intent intent;
        switch (view.getId()){
            case R.id.tv_cancel:
                onBackPressed();
                break;
            case R.id.male_rl:
                male_right_icon.setVisibility(View.VISIBLE);
                female_right_icon.setVisibility(View.INVISIBLE);
                break;
            case R.id.female_rl:
                male_right_icon.setVisibility(View.INVISIBLE);
                female_right_icon.setVisibility(View.VISIBLE);
                break;
        }

    }
}
