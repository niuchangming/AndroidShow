package ekoolab.com.show.api;


import com.orhanobut.logger.Logger;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import ekoolab.com.show.R;
import ekoolab.com.show.application.ShowApplication;
import ekoolab.com.show.utils.NetworkUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;


/**
 * @author Army
 * @version V_1.0.0
 * @date 2017/4/30
 * @description 网络请求基本订阅者
 */
public abstract class NetworkSubscriber<T> implements Subscriber<ResponseData<T>> {
    public static final int NO_NETWORK = -1000;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILURE = -1;
    public static final int ERROR_MSG_MAX_LENGTH = 50;


    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable e) {
        Logger.e(Utils.outputError(e));
        try {
            if (!NetworkUtils.isNetworkAvailable(ShowApplication.application)) {
                if (!dealHttpException(NO_NETWORK, "", e)) {
                    ToastUtils.showToast(R.string.useless_network);
                }
            } else {
                int code;
                String errorMsg;
                if (e instanceof HttpException) {
                    code = ((HttpException) e).getStatus();
                    errorMsg = e.getMessage();
                } else {
                    code = STATUS_FAILURE;
                    errorMsg = "";
                }
                if (!dealHttpException(code, errorMsg, e)) {
                    if (e instanceof SocketTimeoutException || e instanceof ConnectException) {
                        ToastUtils.showToast(R.string.weak_network);
                    } else if (e != null && !android.text.TextUtils.isEmpty(e.getMessage())
                            && e.getMessage().length() <= ERROR_MSG_MAX_LENGTH) {
                        ToastUtils.showToast(e.getMessage());
                    } else {
                        ToastUtils.showToast(R.string.request_fail);
                    }
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onNext(ResponseData<T> tResponseData) {
        try {
            if (STATUS_SUCCESS == tResponseData.errorCode) {
                onSuccess(tResponseData.data);
            } else {
                if (!dealHttpException(tResponseData.errorCode, tResponseData.message, new Exception(tResponseData.message))) {
                    if (Utils.isBlank(tResponseData.message) || tResponseData.message.length() > ERROR_MSG_MAX_LENGTH) {
                        ToastUtils.showToast(R.string.request_fail);
                    } else {
                        ToastUtils.showToast(tResponseData.message);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(e);
        }
    }

    protected abstract void onSuccess(T t);


    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
        return false;
    }
}
