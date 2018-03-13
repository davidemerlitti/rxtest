package com.informaticaumanistica.rxtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
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
                observableSleep(1,LONG_SLEEPING).subscribeOn(Schedulers.newThread()),
                observableSleep(2,MEDIUM_SLEEPING).subscribeOn(Schedulers.newThread()),
                observableSleep(3,SHORT_SLEEPING).subscribeOn(Schedulers.newThread()),
                (o1, o2, o3) -> ((long)o1 + (long)o2 + (long)o3))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(1))
                .doOnTerminate(() -> onTerminate(1))
                .subscribe();
    }

    @OnClick(R.id.btnTest2)
    public void doTest2() {
        Observable.zip(
                observableSleep(1,LONG_SLEEPING),
                observableSleep(2,MEDIUM_SLEEPING),
                observableSleep(3,SHORT_SLEEPING),
                (o1, o2, o3) -> ((long)o1 + (long)o2 + (long)o3))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(2))
                .doOnTerminate(() -> onTerminate(2))
                .subscribe();
    }

    @OnClick(R.id.btnTest3)
    public void doTest3() {
        Observable.zip(
                observableSleep(1,LONG_SLEEPING),
                observableSleep(2,MEDIUM_SLEEPING),
                observableSleep(3,SHORT_SLEEPING),
                (o1, o2, o3) -> ((long)o1 + (long)o2 + (long)o3))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(3))
                .doOnTerminate(() -> onTerminate(3))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @OnClick(R.id.btnTest4)
    public void doTest4() {
        Observable.just(
                observableSleep(1,LONG_SLEEPING),
                observableSleep(2, MEDIUM_SLEEPING),
                observableSleep(3,SHORT_SLEEPING)).flatMap(objectObservable -> objectObservable.subscribeOn(Schedulers.io()))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(4))
                .doOnTerminate(() -> onTerminate(4))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @OnClick(R.id.btnTest5)
    public void doTest5() {
        Observable.merge(
                observableSleep(1, LONG_SLEEPING).subscribeOn(Schedulers.newThread()),
                observableSleep(2, MEDIUM_SLEEPING).subscribeOn(Schedulers.newThread()),
                observableSleep(3, SHORT_SLEEPING).subscribeOn(Schedulers.newThread()))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(5))
                .doOnTerminate(() -> onTerminate(5))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @OnClick(R.id.btnTest6)
    public void doTest6() {
        Completable.concatArray(
                completableSleep(1, 5000),
                singleRandomSleep(2, 2000)
                        .repeat()
                        .takeUntil(o -> ((long)o > 1500))
                        .filter(o -> ((long)o > 1500))
                        .flatMapCompletable(o -> {
                            Log.d("TEST", "Exiting with " + o);
                            return Completable.complete();
                        }),
                completableRandomSleep(3, 6000))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> onSubscribe(6))
                .doOnTerminate(() -> onTerminate(6))
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
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static long sleep(int id, long duration) throws InterruptedException {
        long threadId = Thread.currentThread().getId();
        Log.d("TEST" + id, "Thread id " + threadId + " sleeping " + duration + "ms");
        Thread.sleep(duration);
        return duration;
    }

    private static long randomSleep(int id, long maxDuration) throws InterruptedException {
        long threadId = Thread.currentThread().getId();
        Random random = new Random();
        long duration = random.nextInt((int)maxDuration);
        Log.d("TEST" + id, "Thread id " + threadId + " sleeping " + duration + "ms");
        Thread.sleep(duration);
        return duration;
    }

    private static Observable<Object> observableSleep(int id, final long duration) {
        return Observable.fromCallable(() -> sleep(id, duration));
    }

    private static Single<Object> singleSleep(int id, final long duration) {
        return Single.fromCallable(() -> sleep(id, duration));
    }

    private static Completable completableSleep(int id, final long duration) {
        return Completable.fromCallable(() -> sleep(id, duration));
    }

    private static Single<Object> singleRandomSleep(int id, final long maxDuration) {
        return Single.fromCallable(() -> randomSleep(id, maxDuration));
    }

    private static Completable completableRandomSleep(int id, final long maxDuration) {
        return Completable.fromCallable(() -> randomSleep(id, maxDuration));
    }
}
