package ekoolab.com.show.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.CameraActivity;

import ekoolab.com.show.R;
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
    private TextView tvPermission;
    private View toolbarTitle;
    private EasyPopup easyPopup;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                onBackPressed();
                break;
            case R.id.iv_right:
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
                            startActivity(new Intent(this, ChooseAddressActivity.class));
                        });
                break;
            default:
                break;
        }
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
        etContent = findViewById(R.id.et_content);
        tvAtFriend = findViewById(R.id.tv_at_friend);
        ivVideoImage = findViewById(R.id.iv_video_image);
        tvLocation = findViewById(R.id.tv_location);
        tvLocation.setOnClickListener(this);
        findViewById(R.id.tv_location_label).setOnClickListener(this);
        tvPermission = findViewById(R.id.tv_permission);
        tvPermission.setOnClickListener(this);

        String videoPath = getIntent().getStringExtra(CameraActivity.EXTRA_PATH);
        Glide.with(this).load(videoPath).into(ivVideoImage);


    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_post_video;
    }

}
