package ekoolab.com.show.views.itemdecoration;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

/**
 * Created by Administrator on 2017/4/16.
 */
public class LinearItemDecoration extends BaseItemDecoration {
    public LinearItemDecoration(Context context, int lineWidthDp, @ColorRes int colorIds) {
        super(context, lineWidthDp, ContextCompat.getColor(context, colorIds), 0);
    }

    public LinearItemDecoration(Context context, int lineWidthDp, @ColorRes int colorIds, int bottomMarginLeft) {
        super(context, lineWidthDp, ContextCompat.getColor(context, colorIds), bottomMarginLeft);
    }

    public LinearItemDecoration(Context context, float lineWidthDp, @ColorRes int colorIds, int bottomMarginLeft, int[] positions) {
        super(context, lineWidthDp, ContextCompat.getColor(context, colorIds), bottomMarginLeft, positions);
    }

    public LinearItemDecoration(Context context, float lineWidthDp, @ColorRes int colorIds, int[] margins) {
        super(lineWidthDp, ContextCompat.getColor(context, colorIds), margins);
    }

    @Override
    public boolean[] getItemSidesIsHaveOffsets(int itemPosition) {
        //顺时针顺序:left, top, right, bottom
        //默认只有bottom显示分割线
        return new boolean[]{false, false, false, true};
    }
}
