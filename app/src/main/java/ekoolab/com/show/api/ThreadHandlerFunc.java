package ekoolab.com.show.api;


import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public abstract class ThreadHandlerFunc<T, R> implements Function<ResponseData<T>, Flowable<ResponseData<R>>> {

    @Override
    public Flowable<ResponseData<R>> apply(@NonNull ResponseData<T> tResponseData) throws Exception {
        //response中code码不为1 出现错误
        if (NetworkSubscriber.STATUS_SUCCESS == tResponseData.errorCode) {
            return onSuccess(tResponseData.data);
        } else {
            throw new HttpException(tResponseData.errorCode, tResponseData.message);
        }
    }

    public abstract Flowable<ResponseData<R>> onSuccess(T t);
}