package ekoolab.com.show.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;

import ekoolab.com.show.R;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/10/3
 * @description
 */
public class StrokeTextView extends android.support.v7.widget.AppCompatTextView {
    private float mStrokeWidth;
    private int mStrokeColor;

    public StrokeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
            mStrokeColor = a.getColor(R.styleable.StrokeTextView_strokeColor, Color.WHITE);
            mStrokeWidth = a.getDimension(R.styleable.StrokeTextView_strokeWidth, 0f);
            a.recycle();
        }
        setTextColor(mStrokeColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 只有描边
        TextPaint paint = getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        super.onDraw(canvas);
    }
}
