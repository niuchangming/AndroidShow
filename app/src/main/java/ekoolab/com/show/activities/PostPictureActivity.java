package ekoolab.com.show.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.PictureSelectorView;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.TextPicture;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.ImageSeclctUtils;
import ekoolab.com.show.views.EasyPopup;
import ekoolab.com.show.views.HorizontalGravity;

public class PostPictureActivity extends BaseActivity implements View.OnClickListener{

    private TextView tv_name,tv_cancel,tv_save,tv_permission;
    private EditText et_content;
    private EasyPopup easyPopup;
    private PictureSelectorView pictureSelectorView;
    List<LocalMedia> arrayList = new ArrayList<>();
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
        pictureSelectorView.setOutputCameraPath(Constants.IMAGE_PATH);
        pictureSelectorView.initData(this, 3, 9, DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(30),() -> pictureSelectorView.getOnAddPicClickListener().onAddPicClick());
        pictureSelectorView.setDataForPicSelectView(arrayList);
    }
    @Override
    protected void initData() {
        super.initData();
        arrayList = getIntent().getParcelableArrayListExtra("url");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pictureSelectorView.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    arrayList = pictureSelectorView.getSelectList();
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                default:
                    break;
            }
        }
    }
}
