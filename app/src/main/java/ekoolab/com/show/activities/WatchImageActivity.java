package ekoolab.com.show.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.Photo;

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
        photos = getIntent().getParcelableArrayListExtra(BUNDLE_PHOTOS);
        position = getIntent().getIntExtra(BUNDLE_POSITION, 0);
    }

    @Override
    protected void initViews() {
        super.initViews();
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ImageAdapter());
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
                    .download(photos.get(position).origin)
                    .into(new CustomViewTarget<SubsamplingScaleImageView, File>(imageView) {
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        }

                        @Override
                        public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                            view.setImage(ImageSource.uri(Uri.fromFile(resource)));
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
            return photos.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }
}