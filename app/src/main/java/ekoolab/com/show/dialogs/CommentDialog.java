package ekoolab.com.show.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import ekoolab.com.show.R;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.Utils;


public class CommentDialog extends Dialog {


    private OnClickListener onClickListener;

    private EditText etComment;
    private String textHint;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public CommentDialog(Context activity) {
        super(activity, R.style.common_dialog_bg_transparent);
        init();
    }

    public CommentDialog(Context activity, String hint) {
        this(activity);
        textHint = hint;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(textHint != null){
            etComment.setHint(textHint);
        } else {
            etComment.setHint(R.string.comment_video);
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //设置宽度
        lp.width = DisplayUtils.getScreenWidth();
        //设置高度
        lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);
        getWindow().getAttributes().gravity = Gravity.BOTTOM;
        getWindow().setBackgroundDrawable(new ColorDrawable());

    }

    private void init() {
        setContentView(R.layout.dialog_moment_comment);
        etComment = findViewById(R.id.et_comment);
        findViewById(R.id.tv_send).setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.getString(etComment.getText().toString());
            }
            Utils.hideInput(etComment);
        });
    }

    public void showKeyboard() {
        if (etComment != null) {
            Utils.showInput(etComment);
        }
    }

    public void clearText(){
        etComment.setText("");
    }

    @Override
    public void dismiss() {
        textHint = null;
        Utils.hideInput(etComment);
        super.dismiss();
    }

    public interface OnClickListener {
        void getString(String str);
    }
}
