package ekoolab.com.show.activities;

import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ekoolab.com.show.R;
import ekoolab.com.show.utils.EventBusMsg;

public class GenderActivity extends BaseActivity {


    private TextView tv_name,tv_cancel,tv_save;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_gender;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_name = findViewById(R.id.tv_name);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_save = findViewById(R.id.tv_save);
        tv_name.setText(getResources().getString(R.string.gender));
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
}
