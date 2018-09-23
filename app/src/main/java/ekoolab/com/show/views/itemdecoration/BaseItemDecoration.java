package ekoolab.com.show.views.itemdecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ekoolab.com.show.utils.Utils;

/**
 * Created by wxq on 2017/4/16.
 * 万能的分割线recycleview
 */
public abstract class BaseItemDecoration extends RecyclerView.ItemDecoration {


    private Paint mPaint;
    /**
     * px 分割线宽
     */
    private int lineWidth;

    /**
     * A single color value in the form 0xAARRGGBB.
     **/
    private int colorRGB;

    /**
     * 表示这几个位置的item不绘制分割线
     */
    private int[] positions = null;

    /**
     * 上下左右四个margin， left, top, right, bottom
     */
    private int[] margins = {0, 0, 0, 0};

    public BaseItemDecoration(Context context, int lineWidthDp, @ColorInt int colorRGB) {
        this(context, (float) lineWidthDp, colorRGB, 0);
    }

    public BaseItemDecoration(Context context, float lineWidthDp, @ColorInt int colorRGB, int bottomMarginLeft) {
        this.colorRGB = colorRGB;
        this.lineWidth = (int) lineWidthDp;
        margins[0] = bottomMarginLeft;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(colorRGB);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public BaseItemDecoration(float lineWidthDp, @ColorInt int colorRGB, int[] margins) {
        this.colorRGB = colorRGB;
        this.lineWidth = (int) lineWidthDp;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(colorRGB);
        mPaint.setStyle(Paint.Style.FILL);
        this.margins = margins;
    }

    public BaseItemDecoration(Context context, float lineWidthDp, @ColorInt int colorRGB, int bottomMarginLeft, int[] positions) {
        this.colorRGB = colorRGB;
        this.lineWidth = (int) lineWidthDp;
        margins[0] = bottomMarginLeft;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(colorRGB);
        mPaint.setStyle(Paint.Style.FILL);
        this.positions = positions;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        //left, top, right, bottom
        int childCount1 = parent.getChildCount();
        //最后一个不画
        for (int i = 0; i < childCount1 - 1; i++) {
            if (positions != null && Utils.containInt(positions, i)) {
                continue;
            }
            View child = parent.getChildAt(i);

            int itemPosition = ((RecyclerView.LayoutParams) child.getLayoutParams()).getViewLayoutPosition();

            boolean[] sideOffsetBooleans = getItemSidesIsHaveOffsets(itemPosition);
            if (sideOffsetBooleans[0]) {
                drawChildLeftVertical(child, c, parent);
            }
            if (sideOffsetBooleans[1]) {
                drawChildTopHorizontal(child, c, parent);
            }
            if (sideOffsetBooleans[2]) {
                drawChildRightVertical(child, c, parent);
            }
            if (sideOffsetBooleans[3]) {
                drawChildBottomHorizontal(child, c, parent);
            }
        }
    }

    private void drawChildBottomHorizontal(View child, Canvas c, RecyclerView parent) {

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
        int left = child.getLeft() - params.leftMargin - lineWidth;
        int right = child.getRight() + params.rightMargin + lineWidth;
        int top = child.getBottom() + params.bottomMargin;
        int bottom = top + lineWidth;

        mPaint.setColor(Color.WHITE);
        c.drawRect(left, top, margins[0], bottom, mPaint);
        mPaint.setColor(colorRGB);
        c.drawRect(margins[0], top, right - margins[2], bottom, mPaint);
        mPaint.setColor(Color.WHITE);
        c.drawRect(right - margins[2], top, right, bottom, mPaint);
    }

    private void drawChildTopHorizontal(View child, Canvas c, RecyclerView parent) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
        int left = child.getLeft() - params.leftMargin - lineWidth;
        int right = child.getRight() + params.rightMargin + lineWidth;
        int bottom = child.getTop() - params.topMargin;
        int top = bottom - lineWidth;

        mPaint.setColor(Color.WHITE);
        c.drawRect(left, top, margins[0], bottom, mPaint);
        mPaint.setColor(colorRGB);
        c.drawRect(margins[0], top, right - margins[2], bottom, mPaint);
        mPaint.setColor(Color.WHITE);
        c.drawRect(right - margins[2], top, right, bottom, mPaint);
    }

    private void drawChildLeftVertical(View child, Canvas c, RecyclerView parent) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
        int top = child.getTop() - params.topMargin - lineWidth;
        int bottom = child.getBottom() + params.bottomMargin + lineWidth;
        int right = child.getLeft() - params.leftMargin;
        int left = right - lineWidth;

        mPaint.setColor(colorRGB);
        c.drawRect(left, top, right, bottom, mPaint);
    }

    private void drawChildRightVertical(View child, Canvas c, RecyclerView parent) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
        int top = child.getTop() - params.topMargin - lineWidth;
        int bottom = child.getBottom() + params.bottomMargin + lineWidth;
        int left = child.getRight() + params.rightMargin;
        int right = left + lineWidth;

        mPaint.setColor(colorRGB);
        c.drawRect(left, top, right, bottom, mPaint);
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        //outRect 看源码可知这里只是把Rect类型的outRect作为一个封装了left,right,top,bottom的数据结构,
        //作为传递left,right,top,bottom的偏移值来用的

        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        if (itemPosition == parent.getAdapter().getItemCount() - 1) {
            return;
        }
        if (positions != null && Utils.containInt(positions, itemPosition)) {
            return;
        }
        //
        boolean[] sideOffsetBooleans = getItemSidesIsHaveOffsets(itemPosition);

        int left = sideOffsetBooleans[0] ? lineWidth : 0;
        int top = sideOffsetBooleans[1] ? lineWidth : 0;
        int right = sideOffsetBooleans[2] ? lineWidth : 0;
        int bottom = sideOffsetBooleans[3] ? lineWidth : 0;

        outRect.set(left, top, right, bottom);
    }

    /**
     * 顺序:left, top, right, bottom
     *
     * @return boolean[4]
     */
    public abstract boolean[] getItemSidesIsHaveOffsets(int itemPosition);


}
