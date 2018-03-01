package com.informaticaumanistica.rxtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {
    long startTime;
    static int LONG_SLEEPING = 10000;
    static int MEDIUM_SLEEPING = 5000;
    static int SHORT_SLEEPING = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnTest1)
    public void doTest1() {
        Observable.zip(
                sleep(1,LONG_SLEEPING).subscribeOn(Schedulers.newThread()),
                sleep(2,MEDIUM_SLEEPING).subscribeOn(Schedulers.newThread()),
                sleep(3,SHORT_SLEEPING).subscribeOn(Schedulers.newThread()),
                (o1, o2, o3) -> {
                    return (long)o1 + (long)o2 + (long)o3;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(1))
                .doOnTerminate(() -> onTerminate(1))
                .subscribe();
    }

    @OnClick(R.id.btnTest2)
    public void doTest2() {
        Observable.zip(
                sleep(1,LONG_SLEEPING),
                sleep(2,MEDIUM_SLEEPING),
                sleep(3,SHORT_SLEEPING),
                (o1, o2, o3) -> {
                    return (long)o1 + (long)o2 + (long)o3;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(2))
                .doOnTerminate(() -> onTerminate(2))
                .subscribe();
    }

    @OnClick(R.id.btnTest3)
    public void doTest3() {
        Observable.zip(
                sleep(1,LONG_SLEEPING),
                sleep(2,MEDIUM_SLEEPING),
                sleep(3,SHORT_SLEEPING),
                (o1, o2, o3) -> {
                    return (long)o1 + (long)o2 + (long)o3;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(3))
                .doOnTerminate(() -> onTerminate(3))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @OnClick(R.id.btnTest4)
    public void doTest4() {
        Observable.just(
                sleep(1,LONG_SLEEPING),
                sleep(2, MEDIUM_SLEEPING),
                sleep(3,SHORT_SLEEPING)).flatMap(objectObservable -> {
                    return objectObservable.subscribeOn(Schedulers.io());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(4))
                .doOnTerminate(() -> onTerminate(4))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @OnClick(R.id.btnTest5)
    public void doTest5() {
        Observable.merge(
                sleep(1, LONG_SLEEPING).subscribeOn(Schedulers.newThread()),
                sleep(2, MEDIUM_SLEEPING).subscribeOn(Schedulers.newThread()),
                sleep(3, SHORT_SLEEPING).subscribeOn(Schedulers.newThread()))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(5))
                .doOnTerminate(() -> onTerminate(5))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void onSubscribe(int id) {
        long threadId = Thread.currentThread().getId();
        startTime = System.currentTimeMillis();
        String msg = "Test started at " + startTime + "ms";
        Log.d("TEST" + id, msg + " thread " + threadId);
    }

    private void onTerminate(int id) {
        long threadId = Thread.currentThread().getId();
        long duration = System.currentTimeMillis() - startTime;
        String msg = "Test completed in " + duration + "ms";
        Log.d("TEST" + id, msg + " thread " + threadId);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static Observable<Object> sleep(int id, final long duration) {
        return Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                long threadId = Thread.currentThread().getId();
                Log.d("TEST" + id, "Thread id " + threadId + " sleeping " + duration + "ms");
                Thread.sleep(duration);
                return duration;
            }
        });
    }
}
