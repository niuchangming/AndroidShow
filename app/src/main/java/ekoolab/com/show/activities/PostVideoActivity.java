package ekoolab.com.show.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
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
 * @author Changming Niu
 * @version V_1.0.0
 * @date 2018/9/22
 * @description 发布视频
 */
public class PostVideoActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar mTopToolbar;
    private EditText etContent;
    private Button atFriendBtn;
    private ImageButton uploadingBtn;
    private ProgressView uploadingBar;
    private ImageView ivVideoImage;
    private TextView tvLocation;
    private TextView tvLocationLabel;
    private TextView tvPermission;
    private EasyPopup easyPopup;
    public static final int REQUEST_PICKER_ADDRESS = 223;
    private double lat, lnt;
    private String videoPath, imagePath;

    @Override
    protected void initViews() {
        super.initViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryBlue));
        }

        uploadingBtn = findViewById(R.id.upload_btn);
        uploadingBtn.setOnClickListener(this);
        uploadingBar = findViewById(R.id.progress_pv);

        etContent = findViewById(R.id.et_content);
        atFriendBtn = findViewById(R.id.at_friend_btn);
        atFriendBtn.setOnClickListener(this);

        ivVideoImage = findViewById(R.id.iv_video_image);
        tvLocation = findViewById(R.id.tv_location);
        tvLocation.setOnClickListener(this);
        tvLocationLabel = findViewById(R.id.tv_location_label);
        tvLocationLabel.setOnClickListener(this);
        tvPermission = findViewById(R.id.tv_permission);
        tvPermission.setOnClickListener(this);

        videoPath = getIntent().getStringExtra(ChooseCoverActivity.VIDEO_PATH);
        imagePath = getIntent().getStringExtra(ChooseCoverActivity.FIRST_FRAME_PATH);
        Glide.with(this).load(imagePath).into(ivVideoImage);

        mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.post_video));
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                onBackPressed();
                break;
            case R.id.upload_btn:
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
                openPlacePicker();
                break;
            default:
                break;
        }
    }

    private void openPlacePicker(){
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)
                .subscribe(aBoolean -> {
                    try {
                        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build(this);
                        startActivityForResult(intent, REQUEST_PICKER_ADDRESS);

                    } catch (GooglePlayServicesRepairableException e) {
                        GooglePlayServicesUtil
                                .getErrorDialog(e.getConnectionStatusCode(), this, 0);
                    } catch (GooglePlayServicesNotAvailableException e) {
                        toastLong("Google Play Services is not available.");
                    }
                });
    }

    private void publishVideo() {
        uploadingBtn.setVisibility(View.GONE);
        uploadingBar.setVisibility(View.VISIBLE);
        uploadingBar.start();
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
                        uploadingBtn.setVisibility(View.VISIBLE);
                        uploadingBar.setVisibility(View.GONE);
                        uploadingBar.stop();
                        setViewClickable(true);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICKER_ADDRESS && resultCode == RESULT_OK) {
            Place selectedPlace = PlacePicker.getPlace(data, this);
            lat = selectedPlace.getLatLng().latitude;
            lnt = selectedPlace.getLatLng().longitude;
            tvLocation.setText(selectedPlace.getAddress());
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_post_video;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
