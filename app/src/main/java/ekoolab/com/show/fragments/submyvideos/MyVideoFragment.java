package ekoolab.com.show.fragments.submyvideos;

import android.util.Log;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.santalu.emptyview.EmptyView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ekoolab.com.show.R;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ViewHolder;

public class MyVideoFragment extends BaseFragment {
    private final String TAG = "VideoFragment";
    private int pageIndex;
    private EmptyView emptyView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_video;
    }


    @Override
    protected void initData() {
        super.initData();

        pageIndex = 0;
        loadVideoData();
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        emptyView = holder.get(R.id.empty_view);
    }

    private void loadVideoData(){
        emptyView.showLoading();
        AndroidNetworking.post(Constants.VIDEO_LIST)
                .addBodyParameter("timestamp", new Date().getTime() + "")
                .addBodyParameter("pageSize", Constants.PAGE_SIZE + "")
                .addBodyParameter("pageIndex", pageIndex + "")
                .addBodyParameter("token", AuthUtils.getInstance(getContext()).getApiToken())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            int errorCode = response.getInt("errorCode");
                            String message = response.getString("message");
                            if (errorCode == 1) {
                                emptyView.content().show();
                            } else {
                                emptyView.error().setErrorText(message).show();
                            }
                        }catch (JSONException e){
                            emptyView.error(e).show();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        emptyView.error(error).show();
                    }
                });
    }
}
