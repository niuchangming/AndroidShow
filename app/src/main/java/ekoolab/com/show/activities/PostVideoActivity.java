package ekoolab.com.show.activities;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.CameraActivity;

import ekoolab.com.show.R;

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                onBackPressed();
                break;
            case R.id.iv_right:
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
        tvPermission = findViewById(R.id.tv_permission);

        String videoPath = getIntent().getStringExtra(CameraActivity.EXTRA_PATH);
        Glide.with(this).load(videoPath).into(ivVideoImage);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_post_video;
    }

}
