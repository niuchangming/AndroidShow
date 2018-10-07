package ekoolab.com.show.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.Photo;
import ekoolab.com.show.utils.ImageLoader;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/29
 * @description 查看大图的页面
 */
public class WatchImageActivity extends BaseActivity {
    public static final String BUNDLE_PHOTOS = "bundle_photos";
    public static final String BUNDLE_POSITION = "bundle_position";

    private ViewPager mViewPager;
    private List<Photo> photos;
    private int position;

    @Override
    protected void initData() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        photos = getIntent().getParcelableArrayListExtra(BUNDLE_PHOTOS);
        position = getIntent().getIntExtra(BUNDLE_POSITION, 0);
    }

    @Override
    protected void initViews() {
        super.initViews();
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ImageAdapter());
        position = Integer.MAX_VALUE / 2 / photos.size() * photos.size() + position;
        mViewPager.setCurrentItem(position);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_watch_image;
    }

    public static void navToWatchImage(Context context, List<Photo> photos, int position) {
        Intent intent = new Intent(context, WatchImageActivity.class);
        intent.putParcelableArrayListExtra(BUNDLE_PHOTOS, (ArrayList<Photo>) photos);
        intent.putExtra(BUNDLE_POSITION, position);
        context.startActivity(intent);
    }

    private class ImageAdapter extends PagerAdapter {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(container.getContext());
            Glide.with(container)
                    .download(photos.get(position % photos.size()).origin)
                    .into(new CustomViewTarget<SubsamplingScaleImageView, File>(imageView) {
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            view.setImage(ImageSource.resource(ImageLoader.ERROR_PLACEHOLDER));
                        }

                        @Override
                        public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                            Uri mediaUri = Uri.fromFile(resource);
                            view.setOrientation(getOrientation(mediaUri, getApplication()));
                            view.setImage(ImageSource.uri(mediaUri));
                        }

                        @Override
                        protected void onResourceCleared(@Nullable Drawable placeholder) {

                        }
                    });
            imageView.setOnClickListener(view -> onBackPressed());
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    public static int getOrientation(Uri uri, Context ctx) {
        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return 0;
            }
            ExifInterface exif = new ExifInterface(in);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return SubsamplingScaleImageView.ORIENTATION_180;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return SubsamplingScaleImageView.ORIENTATION_90;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return SubsamplingScaleImageView.ORIENTATION_270;
                default:
                    return SubsamplingScaleImageView.ORIENTATION_0;
            }
        } catch (IOException e) {
            return 0;
        }
    }
}
