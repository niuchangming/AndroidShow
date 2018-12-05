package ekoolab.com.show.views.ImageViewer.listener;

import ekoolab.com.show.views.ImageViewer.ImageViewerState;
import ekoolab.com.show.views.ImageViewer.widget.ScaleImageView;

/**
 * 监听图片浏览器的状态
 */
public interface OnPreviewStatusListener {

    /**
     * 监听图片预览器的当前状态
     *
     * @param state      图片预览器的当前状态
     * @param imagePager 当前的 itemView
     */
    void onPreviewStatus(@ImageViewerState int state, ScaleImageView imagePager);
}
