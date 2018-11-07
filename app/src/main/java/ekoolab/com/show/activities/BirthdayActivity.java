package ekoolab.com.show.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.TextPicture;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

//public class BirthdayActivity extends BaseActivity implements View.OnClickListener{
public class BirthdayActivity extends BaseActivity{
//    private DatePicker datePicker;
//    private Calendar calendar;
//
//    private TextView tv_name,tv_cancel,tv_save;
//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_birthday;
//    }
//
//    @Override
//    protected void initViews() {
//        super.initViews();
//        tv_name = findViewById(R.id.tv_name);
//        tv_name.setText(getResources().getString(R.string.birthday));
//        tv_cancel = findViewById(R.id.tv_cancel);
//        tv_cancel.setOnClickListener(this);
//        tv_save = findViewById(R.id.tv_save);
//        tv_save.setOnClickListener(this);
//    }
//    @Override
//    protected void initData() {
//        super.initData();
//
//    }
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_birthday);
//        datePicker = (DatePicker) findViewById(R.id.dpPicker);
//        datePicker.init(2013, 8, 20, new DatePicker.OnDateChangedListener() {
//            @Override
//            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                Calendar calendar = Calendar.getInstance();
//                calendar.set(year, monthOfYear, dayOfMonth);
//                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日  HH:mm");
////                Toast.makeText(this, format.format(calendar.getTime()), Toast.LENGTH_SHORT)
////                        .show();
////                saveBirthday(calendar);
//            }
//        });
//    }
//
////    private void saveBirthday(Calendar calendar){
////        this.calendar = calendar;
////    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.tv_save:
//                onDateSet();
//                break;
//            case R.id.tv_cancel:
//                finish();
//                break;
//        }
//    }
//
//    public void onDateSet() {
//        setViewClickable(false);
////        Calendar selectedDate = Calendar.getInstance();
////        selectedDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),0,0,0);
//        String birthday = datePicker.getYear() + "-" + datePicker.getMonth() + "-" + datePicker.getDayOfMonth();
//        Long timeStamp = TimeUtils.getTimeStampByDate(birthday + " 00:00", TimeUtils.YYYYmmDDHHMM);
//        System.out.println("see birthday and timestamp: " + birthday + "; " + timeStamp);
//        HashMap<String, String> map = new HashMap<>(2);
//        map.put("birthday", Long.toString(timeStamp));
//        map.put("token", AuthUtils.getInstance(this).getApiToken());
//        ApiServer.basePostRequest(this, Constants.UPDATE_USERPROFILE, map,
//                new TypeToken<ResponseData<TextPicture>>() {
//                })
//                .subscribe(new NetworkSubscriber<TextPicture>() {
//                    @Override
//                    protected void onSuccess(TextPicture textPicture) {
//                        ToastUtils.showToast("Saved");
//                        Intent intent = new Intent();
//                        intent.putExtra("birthday", birthday);
//                        intent.putExtra("timeStamp", timeStamp);
//                        setResult(RESULT_OK, intent);
//                        finish();
//                    }
//
//                    @Override
//                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
//                        System.out.println("===errorMsg==="+errorMsg);
//                        tv_save.setVisibility(View.VISIBLE);
//                        setViewClickable(true);
//                        return super.dealHttpException(code, errorMsg, e);
//                    }
//                });
//    }
//
//    private void setViewClickable(Boolean clickable){
//        tv_save.setClickable(clickable);
//        tv_cancel.setClickable(clickable);
//    }

    private TextView timeText;

    @Override
    protected int getLayoutId(){
        return R.layout.activity_birthday;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_birthday);
//        timeText = (TextView) findViewById(R.id.time_text);
        //为TextView设置点击事件
//        timeText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //将timeText传入用于显示所选择的时间
//                showDialogPick();
//            }
//        });
    }

    @Override
    protected void initViews() {
        super.initViews();
        showDialogPick();
//        back_ll = findViewById(R.id.back_ll);
//        back_ll.setOnClickListener(this);
//
//        mEmptyView = findViewById(R.id.empty_view);
//        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.addItemDecoration(new LinearItemDecoration(this,
//                1, R.color.colorLightGray, 0));
//        refreshLayout = findViewById(R.id.refreshLayout);
//        initRefreshLayout();
//        initAdapter();
//        recyclerView.setAdapter(mAdapter);
    }


    //将两个选择时间的dialog放在该函数中
    private void showDialogPick() {
        final StringBuffer time = new StringBuffer();
        //获取Calendar对象，用于获取当前时间
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            //选择完日期后会调用该回调函数
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //因为monthOfYear会比实际月份少一月所以这边要加1
                time.append(year + "-" + (monthOfYear+1) + "-" + dayOfMonth);
            }
        }, year, month, day);
        //弹出选择日期对话框
        datePickerDialog.show();
    }
}
