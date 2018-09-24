package ekoolab.com.show.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/24
 * @description
 */
public class MyHorizontalScrollView extends HorizontalScrollView {

    private OnScrollChangeListener listener;

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (listener != null) {
            listener.onScrollChange(l, oldl);
        }
    }

    public void setListener(OnScrollChangeListener listener) {
        this.listener = listener;
    }

    public interface OnScrollChangeListener {
        void onScrollChange(int scrollX, int oldScrollX);
    }
}
