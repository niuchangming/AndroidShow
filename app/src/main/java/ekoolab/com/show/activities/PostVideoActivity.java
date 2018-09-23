package ekoolab.com.show.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.CameraActivity;
import com.luck.picture.lib.utils.AppManager;
import com.rey.material.widget.ProgressView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.views.EasyPopup;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/22
 * @description 发布视频
 */
public class PostVideoActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvCancel;
    private ImageView ivLeft;
    private TextView tvName;
    private TextView tvSave;
    private ImageView ivRight;
    private EditText etContent;
    private TextView tvAtFriend;
    private ImageView ivVideoImage;
    private TextView tvLocation;
    private TextView tvLocationLabel;
    private TextView tvPermission;
    private View toolbarTitle;
    private EasyPopup easyPopup;
    public static final int REQUEST_CHOOSE_ADDRESS = 223;
    private double lat, lnt;
    private String videoPath, imagePath;
    private ProgressView progressView;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                onBackPressed();
                break;
            case R.id.iv_right:
                publishVideo();
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
                easyPopup.showAsDropDown(tvPermission);
                break;
            case R.id.tv_public:
            case R.id.tv_friend:
            case R.id.tv_private:
                tvPermission.setText(((TextView) v).getText());
                tvPermission.setHint(((TextView) v).getHint());
                easyPopup.dismiss();
                break;
            case R.id.tv_location:
            case R.id.tv_location_label:
                rxPermissions.request(Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)
                        .subscribe(aBoolean -> {
                            startActivityForResult(new Intent(this, ChooseAddressActivity.class), REQUEST_CHOOSE_ADDRESS);
                        });
                break;
            default:
                break;
        }
    }

    private void publishVideo() {
        ivRight.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);
        progressView.start();
        setViewClickable(false);
        Map<String, File> fileMap = new HashMap<>(2);
        fileMap.put("preview", new File(imagePath));
        fileMap.put("videofile", new File(videoPath));
        Map<String, String> valueMap = new HashMap<>(4);
        valueMap.put("token", AuthUtils.getInstance(getApplicationContext()).getApiToken());
        valueMap.put("title", etContent.getText().toString());
        valueMap.put("permission", tvPermission.getHint().toString());
        valueMap.put("lat", lat + "");
        valueMap.put("lnt", lnt + "");
        ApiServer.baseUploadRequest(this, Constants.UPLOAD_VIDEO, valueMap, fileMap,
                new TypeToken<ResponseData<String>>() {
                })
                .subscribe(new NetworkSubscriber<String>() {
                    @Override
                    protected void onSuccess(String s) {
                        ToastUtils.showToast("Post Success");
                        AppManager.getInstance().killActivity(CameraActivity.class);
                        finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        ivRight.setVisibility(View.VISIBLE);
                        setViewClickable(true);
                        progressView.setVisibility(View.GONE);
                        progressView.stop();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void setViewClickable(boolean clickable) {
        etContent.setClickable(clickable);
        etContent.setEnabled(clickable);
        tvLocation.setClickable(clickable);
        tvLocationLabel.setClickable(clickable);
        tvPermission.setClickable(clickable);
    }

    @Override
    protected void initViews() {
        super.initViews();
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setBackgroundResource(R.color.colorBlack);
        tvCancel = findViewById(R.id.tv_cancel);
        tvCancel.setVisibility(View.GONE);
        ivLeft = findViewById(R.id.iv_left);
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setOnClickListener(this);
        tvName = findViewById(R.id.tv_name);
        tvName.setText(R.string.post);
        tvName.setTextColor(Color.WHITE);
        tvSave = findViewById(R.id.tv_save);
        tvSave.setVisibility(View.GONE);
        ivRight = findViewById(R.id.iv_right);
        ivRight.setVisibility(View.VISIBLE);
        ivRight.setOnClickListener(this);
        progressView = findViewById(R.id.progress_bar);
        etContent = findViewById(R.id.et_content);
        tvAtFriend = findViewById(R.id.tv_at_friend);
        ivVideoImage = findViewById(R.id.iv_video_image);
        tvLocation = findViewById(R.id.tv_location);
        tvLocation.setOnClickListener(this);
        tvLocationLabel = findViewById(R.id.tv_location_label);
        tvLocationLabel.setOnClickListener(this);
        tvPermission = findViewById(R.id.tv_permission);
        tvPermission.setOnClickListener(this);

        videoPath = getIntent().getStringExtra(CameraActivity.EXTRA_VIDEO_PATH);
        imagePath = getIntent().getStringExtra(CameraActivity.EXTRA_IMAGE_PATH);
        Glide.with(this).load(imagePath).into(ivVideoImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_ADDRESS && resultCode == RESULT_OK) {
            lat = data.getDoubleExtra(ChooseAddressActivity.EXTRA_LATITUDE, 0);
            lnt = data.getDoubleExtra(ChooseAddressActivity.EXTRA_LONGITUDE, 0);
            tvLocation.setText(data.getStringExtra(ChooseAddressActivity.EXTRA_ADDRESS));
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_post_video;
    }

}
