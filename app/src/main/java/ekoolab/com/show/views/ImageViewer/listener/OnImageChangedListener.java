package ekoolab.com.show.views.ImageViewer.listener;

import ekoolab.com.show.views.ImageViewer.widget.ScaleImageView;

/**
 * 图片的切换监听事件
 */
public interface OnImageChangedListener {

    void onImageSelected(int position, ScaleImageView view);
}
