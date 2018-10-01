package ekoolab.com.show.views.ninegridview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.Photo;


/**
 * @author wxq
 * @version V_5.0.0
 * @modify Neil
 * @date 2017/2/17 0017
 * @modifydate 2017/11/1
 * @description
 */
public class NewNineGridlayout extends LinearLayout {

    private Context mcontext;
    private NineGridlayout classPic;
    private static final int NUMBER_2 = 2;
    private static final int NUMBER_10 = 10;

    public static final int PHOTO_QUALITY_SMALL = 0;
    public static final int PHOTO_QUALITY_MEDIUM = 1;
    public static final int PHOTO_QUALITY_ORIGIN = 2;

    public NewNineGridlayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public NewNineGridlayout(Context context) {
        this(context, null, 0);
    }

    public NewNineGridlayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    /**
     * 初始化控件
     *
     * @param context
     */
    private void init(Context context) {
        ViewGroup view = (ViewGroup) View.inflate(context, R.layout.common_new_nine_grid, this);
        classPic = (NineGridlayout) view.getChildAt(0);
        mcontext = context;
    }

    /**
     * 设置图片宽度以及监听等
     */
    public void showPic(int nineGridViewWidth, List<Photo> photos,
                        NineGridlayout.onNineGirdItemClickListener listener,
                        int quality) {
        classPic.setTotalWidth(nineGridViewWidth);
        classPic.setImagesData(photos, 0, 0, quality);
        classPic.setonNineGirdItemClickListener(listener);
    }
}
