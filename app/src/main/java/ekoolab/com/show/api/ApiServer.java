package ekoolab.com.show.api;

import com.androidnetworking.common.Priority;
import com.google.gson.reflect.TypeToken;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.uber.autodispose.FlowableSubscribeProxy;

import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.beans.Video;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.RxUtils;
import io.reactivex.Flowable;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/22
 * @description
 */
public class ApiServer {

    public static <T> FlowableSubscribeProxy<T> basePostRequest(BaseActivity activity,
                                                                String url,
                                                                HashMap<String, String> map,
                                                                TypeToken<T> typeToken) {
        return Rx2AndroidNetworking
                .post(url)
                .addBodyParameter(map)
                .setPriority(Priority.MEDIUM)
                .build()
                .getParseFlowable(typeToken)
                .compose(RxUtils.rxSchedulerHelper())
                .as(activity.autoDisposable());
    }

    public static <T> FlowableSubscribeProxy<T> basePostRequest(BaseFragment fragment,
                                                                String url,
                                                                HashMap<String, String> map,
                                                                TypeToken<T> typeToken) {
        return Rx2AndroidNetworking
                .post(url)
                .addBodyParameter(map)
                .setPriority(Priority.MEDIUM)
                .build()
                .getParseFlowable(typeToken)
                .compose(RxUtils.rxSchedulerHelper())
                .as(fragment.autoDisposable());
    }
}
