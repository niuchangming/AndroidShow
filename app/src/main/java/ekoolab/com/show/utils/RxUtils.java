package ekoolab.com.show.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;


import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * @author Army
 * @version V_1.0.0
 * @date 2017/4/29
 * @description
 */
public class RxUtils {
    private static final int DISPLAYONCE = 1;

    /**
     * 统一线程处理
     */
    public static <T> FlowableTransformer<T, T> rxSchedulerHelper() {    //compose简化线程
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 全在子线程
     */
    public static <T> FlowableTransformer<T, T> rxThreadHelper() {
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }


    public static Flowable<Integer> countDown(int count) {
        return Flowable.interval(1, TimeUnit.SECONDS)
                .take(count + 1).map(aLong -> count - aLong.intValue());
    }


    public static void click(View v, Consumer<Object> onNext) {
        RxView.clicks(v).throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(onNext);
    }

    public static void longClick(View v, Consumer<Object> onNext) {
        RxView.longClicks(v).subscribe(onNext);
    }

    public static Observable<Boolean> meetMultiConditionDo(Function<Object[], Boolean> combiner, TextView... tvs) {
        if (tvs != null && tvs.length > 0) {
            List<Observable<CharSequence>> observableList = new ArrayList<>();
            for (int i = 0; i < tvs.length; i++) {
                observableList.add(RxTextView.textChanges(tvs[i]).skip(1));
            }
            return Observable.combineLatest(observableList, combiner);
        }
        return null;
    }


    //定时任务以及循环任务
    private static Disposable mDisposable;

    /**
     * milliseconds毫秒后执行next操作
     *
     * @param milliseconds
     * @param next
     */
    public static void timer(long milliseconds, final IRxNext next) {
        Observable.timer(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if (next != null) {
                            next.doNext(number);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        //取消订阅
                        cancel();
                    }

                    @Override
                    public void onComplete() {
                        //取消订阅
                        cancel();
                    }
                });
    }

    /**
     * 每隔milliseconds毫秒后执行next操作
     *
     * @param milliseconds
     * @param next
     */
    public static void interval(long milliseconds, final IRxNext next) {
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if (next != null) {
                            next.doNext(number);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 取消订阅
     */
    public static void cancel() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    public interface IRxNext {
        void doNext(long number);
    }

//    @Override
//    protected void onDestroy(){
//        //取消定时器
//        RxTimerUtil.cancel();
//        LogUtil.e("====cancel====="+ DateUtil.getNowTime());
//        super.onDestroy();
//    }


}
