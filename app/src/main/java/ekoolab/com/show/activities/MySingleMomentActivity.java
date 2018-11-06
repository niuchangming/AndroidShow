package ekoolab.com.show.activities;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.nestlistview.NestFullListView;
import ekoolab.com.show.views.nestlistview.NestFullListViewAdapter;
import ekoolab.com.show.views.nestlistview.NestFullViewHolder;
import ekoolab.com.show.views.ninegridview.NewNineGridlayout;

public class MySingleMomentActivity extends BaseActivity implements View.OnClickListener {

    private TextView tv_name, tv_content, tv_time, tv_zan_num, delete_moment;
    private ImageView iv_icon;
    private NestFullListView comments;
    private Moment moment;
    private int nineTotalWidth = DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(60 * 2);
    @Override
    protected int getLayoutId() {
        return R.layout.item_moment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_name = findViewById(R.id.tv_name);
        tv_content = findViewById(R.id.tv_content);
        tv_time = findViewById(R.id.tv_time);
        tv_zan_num = findViewById(R.id.tv_zan_num);
        iv_icon = findViewById(R.id.iv_icon);
        comments = findViewById(R.id.nest_full_listview);
        delete_moment = findViewById(R.id.delete_moment);
        delete_moment.setOnClickListener(this);
    }
    @Override
    protected void initData() {
        super.initData();
    }
    @Override
    public void onStart() {
        super.onStart();
        Bundle info  = getIntent().getExtras();
        moment = info.getParcelable("moment");
        showMoment(moment);
    }

    private void showMoment(Moment item){
        tv_name.setText(item.creator.nickName);
        tv_content.setText(item.body);
        tv_time.setText(TimeUtils.stringDateToStringDate(TimeUtils.getDateStringByTimeStamp(item.uploadTime), TimeUtils.MMMM_dd, TimeUtils.YYYYMMDDHHMMSSZero));
        tv_zan_num.setText(Integer.toString(item.likeCount));
        ImageLoader.displayImageAsCircle(item.creator.avatar.small, iv_icon);
        NewNineGridlayout newNineGridlayout = findViewById(R.id.nine_grid_layout);
        newNineGridlayout.showPic(nineTotalWidth, item.photoArray,
                null,
                NewNineGridlayout.PHOTO_QUALITY_SMALL);
        boolean notEmpty = Utils.isNotEmpty(item.comments);
        if (notEmpty) {
            comments.setAdapter(new NestFullListViewAdapter<Moment.CommentsBean>(R.layout.item_moment_comment, item.comments) {
                @Override
                public void onBind(int position, Moment.CommentsBean bean, NestFullViewHolder holder) {
                    if (!bean.ishasParentComment) {
                        holder.setText(R.id.tv_comment, Html.fromHtml(getString(R.string.moment_reply1,
                                bean.creator.name, bean.body)));
                    } else {
                        holder.setText(R.id.tv_comment, Html.fromHtml(getString(R.string.moment_reply2,
                                bean.creator.name, bean.replyToName, bean.body)));
                    }
                }
            });
        } else { comments.setVisibility(View.GONE); }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.delete_moment:
                delete("moment", moment.resourceId);
                break;
        }
    }

    private void delete(String type, String id){
        String url;
        HashMap<String, String> map = new HashMap<>(2);
        map.put("token", AuthUtils.getInstance(this).getApiToken());
        map.put("resourceId", id);
        url = (type=="comment")?Constants.COMMENTDEL:Constants.MOMENTDEL;
        ApiServer.basePostRequest(this, url, map, new TypeToken<ResponseData<String>>() {
        }).subscribe(new NetworkSubscriber<String>(){
            @Override
            protected void onSuccess(String s){
                ToastUtils.showToast("Deleted");
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
