package ekoolab.com.show.activities;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.PictureSelectorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.TextPicture;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.ImageSeclctUtils;
import ekoolab.com.show.views.EasyPopup;
import ekoolab.com.show.views.HorizontalGravity;

public class PostPictureActivity extends BaseActivity implements View.OnClickListener{

    private TextView tv_name,tv_cancel,tv_save,tv_permission;
    private EditText et_content;
    private EasyPopup easyPopup;
    private PictureSelectorView pictureSelectorView;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_post_picture;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_name = findViewById(R.id.tv_name);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_save = findViewById(R.id.tv_save);
        tv_permission = findViewById(R.id.tv_permission);
        et_content = findViewById(R.id.et_content);
        tv_name.setText(getResources().getString(R.string.moment));
        pictureSelectorView = findViewById(R.id.psv);
        tv_cancel.setOnClickListener(this);
        tv_save.setOnClickListener(this);
        tv_permission.setOnClickListener(this);
    }
    @Override
    protected void initData() {
        super.initData();
        ArrayList<String> arrayList = getIntent().getStringArrayListExtra("url");
        pictureSelectorView.setOutputCameraPath(Constants.IMAGE_PATH);
        pictureSelectorView.initData(this,3,arrayList.size(),200);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_cancel:
                PostPictureActivity.this.finish();
                break;
            case R.id.tv_save:
                postPicture();
                break;
            case R.id.tv_permission:
                if (easyPopup == null) {
                    View contentView = getLayoutInflater().inflate(R.layout.popup_post_permission, null);
                    TextView tvPublic = contentView.findViewById(R.id.tv_public);
                    TextView tvFriend = contentView.findViewById(R.id.tv_friend);
                    TextView tvPrivate = contentView.findViewById(R.id.tv_private);
                    tvPublic.setOnClickListener(this);
                    tvFriend.setOnClickListener(this);
                    tvPrivate.setOnClickListener(this);
                    easyPopup = new EasyPopup(this)
                            .setContentView(contentView)
                            .setFocusAndOutsideEnable(true)
                            .createPopup();
                }
                easyPopup.setHorizontalGravity(HorizontalGravity.RIGHT);
                easyPopup.showAsDropDown(tv_permission);
                break;
            case R.id.tv_public:
            case R.id.tv_friend:
            case R.id.tv_private:
                tv_permission.setText(((TextView) view).getText());
                tv_permission.setHint(((TextView) view).getHint());
                easyPopup.dismiss();
                break;
        }
    }

    private void postPicture() {
        HashMap<String, String> map = new HashMap<>(5);
        map.put("body", et_content.getText().toString());
        map.put("momentPhotos", "picture");
        map.put("type", "picture");
        map.put("permission", tv_permission.getText().toString());
        map.put("token", AuthUtils.getInstance(PostPictureActivity.this).getApiToken());
        ApiServer.baseUploadRequest(this, Constants.TextPost, map, null,
                new TypeToken<ResponseData<TextPicture>>() {
                })
                .subscribe(new NetworkSubscriber<TextPicture>() {
                    @Override
                    protected void onSuccess(TextPicture textPicture) {
                        try {
                            PostPictureActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }
}
