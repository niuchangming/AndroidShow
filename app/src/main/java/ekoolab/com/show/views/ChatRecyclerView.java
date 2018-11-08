package ekoolab.com.show.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class ChatRecyclerView extends RecyclerView{

    private OnScrollListener listener;

    public void setOnScrollListener(OnScrollListener listener) {
        this.listener = listener;
    }

    public ChatRecyclerView(Context context) {
        super(context);
    }

    public ChatRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public interface OnScrollListener{
        void onScroll(int scrollX, int scrollY, int oldX, int oldY);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(listener != null){
            listener.onScroll(l, t, oldl, oldt);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

}
