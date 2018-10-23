package ekoolab.com.show.views;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import ekoolab.com.show.R;

public class FakeActionBar extends RelativeLayout{

    public FakeActionBar(Context context) {
        super(context);
        initViews();
    }

    public FakeActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    private void initViews(){
        ShapeDrawable lineDrawable = new ShapeDrawable(new BorderShape(new RectF(0, 0, 0, 1)));
        lineDrawable.getPaint().setColor(getResources().getColor(R.color.colorLightGray));
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{
                new ColorDrawable(getResources().getColor(R.color.colorWhite)),
                lineDrawable
        });
        this.setBackground(layerDrawable);
    }

}
