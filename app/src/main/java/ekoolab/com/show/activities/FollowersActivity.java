package ekoolab.com.show.activities;



import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ekoolab.com.show.R;
import ekoolab.com.show.utils.EventBusMsg;

public class FollowersActivity extends BaseActivity implements View.OnClickListener{
    private LinearLayout back_ll;

    @Override
    protected int getLayoutId(){
        return R.layout.activity_followers;
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
        back_ll = findViewById(R.id.back_ll);
        back_ll.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        Intent intent;
        switch(view.getId()){
            case R.id.back_ll:
                onBackPressed();
                break;
        }
    }

}
