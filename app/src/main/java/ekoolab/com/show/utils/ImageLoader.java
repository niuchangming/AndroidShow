package ekoolab.com.show.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import ekoolab.com.show.R;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/10/4
 * @description
 */
public class ImageLoader {

    public static final int ERROR_PLACEHOLDER = R.mipmap.ic_launcher;

    public static void displayImage(String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .into(new DrawableImageViewTarget(imageView) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setBackgroundResource(ERROR_PLACEHOLDER);
                    }
                });
    }
}
