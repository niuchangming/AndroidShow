package ekoolab.com.show.utils;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/10/3
 * @description
 */
@GlideModule
public class ShowGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565).disallowHardwareConfig());
        //设置缓存目录
        try {
            File cacheDir = new File(Constants.IMAGE_CACHE_PATH);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            DiskCache cache = DiskLruCacheWrapper.create(cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
            builder.setDiskCache(() -> cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置memory和Bitmap池的大小
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context).build();
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        int customMemoryCacheSize = 2 * defaultMemoryCacheSize;
        int customBitmapPoolSize = 2* defaultBitmapPoolSize;
        builder.setMemoryCache(new LruResourceCache(customMemoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));
        builder.setLogLevel(Log.DEBUG);
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
