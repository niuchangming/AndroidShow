package ekoolab.com.show.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import ekoolab.com.show.R;
import ekoolab.com.show.views.BlurTransformation;

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
            imageView.setImageResource(ERROR_PLACEHOLDER);
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

    public static void displayImageAsCircle(String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(ERROR_PLACEHOLDER);
            return;
        }
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(new DrawableImageViewTarget(imageView) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setBackgroundResource(ERROR_PLACEHOLDER);
                    }
                });
    }

    public static void displayImage(String imageUrl, ImageView imageView, int blur) {
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(ERROR_PLACEHOLDER);
            return;
        }
        GlideApp.with(imageView.getContext())
                .load(imageUrl)
                .transform(new BlurTransformation(imageView.getContext(),blur))
                .into(new DrawableImageViewTarget(imageView) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setBackgroundResource(ERROR_PLACEHOLDER);
                    }
                });
    }


}
