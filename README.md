# rxtest
RxJava Android Test

Questa app Android serve a fare qualche esperimento con [RxJava](https://github.com/ReactiveX/RxJava) e il multithreading.

## Il metodo perditempo Observable

```java
    private static Observable<Object> observableSleep(int id, final long duration) {
        return Observable.fromCallable(() -> sleep(id, duration));
    }
```

## La variante Single

```java
    private static Single<Object> singleSleep(int id, final long duration) {
        return Single.fromCallable(() -> sleep(id, duration));
    }
```

## La variante Completable

```java
    private static Completable completableSleep(int id, final long duration) {
        return Completable.fromCallable(() -> sleep(id, duration));
    }
```



## Test 1 (multithread)

Operatore `Observable.zip()`, nessuna `subscribeOn()` principale, ogni `observableSleep()` sottoscritta su un nuovo thread.
Vengono creati tre thread distinti, uno per ogni argomento dello `Observable.zip()`.

### Codice

```java
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
```

### Esempio di log del metodo doTest1()

<pre>
D/TEST1: Test started at 1519922810993ms thread 1
D/TEST1: Thread id 2350 sleeping 10000ms
D/TEST2: Thread id 2351 sleeping 5000ms
D/TEST3: Thread id 2352 sleeping 1000ms
D/TEST1: Test completed in 10008ms thread 1
</pre>

## Test 2 (non multithread)

Operatore `Observable.zip()`, nessuna `subscribeOn()` principale, nessuna `subscribeOn()` sui singoli `observableSleep()`.
Viene eseguito tutto in modo consecutivo sul thread principale di chiamata (thread 1).

### Codice

```java
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
```

### Esempio di log del metodo doTest2()

<pre>
D/TEST2: Test started at 1519923978159ms thread 1
D/TEST1: Thread id 1 sleeping 10000ms
D/TEST2: Thread id 1 sleeping 5000ms
D/TEST3: Thread id 1 sleeping 1000ms
D/TEST2: Test completed in 16041ms thread 1
</pre>


## Test 3 (non multithread)

Operatore `Observable.zip()`, `subscribeOn(Schedulers.io())` principale, nessuna `subscribeOn()` sui singoli `observableSleep()`.
Viene eseguito tutto in modo consecutivo su un nuovo thread diverso dal thread principale di chiamata.

### Codice 

```java
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
```

### Esempio di log del metodo doTest3()

<pre>
D/TEST3: Test started at 1519924104220ms thread 2354
D/TEST1: Thread id 2354 sleeping 10000ms
D/TEST2: Thread id 2354 sleeping 5000ms
D/TEST3: Thread id 2354 sleeping 1000ms
D/TEST3: Test completed in 16010ms thread 1
</pre>

## Test 4 (multithread)

Operatore `Observable.just()` con `flatMap()` che esegue una `subscribeOn()` sui singoli observable, `subscribeOn(Schedulers.io())` principale, nessuna `subscribeOn()` sui singoli `observableSleep()`.
Viene creato un thread per ogni `observableSleep()` e un thread nuovo anche per la chiamata principale.

### Codice

```java
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
```

### Esempio di log del metodo doTest4()

<pre>
D/TEST4: Test started at 1519924207252ms thread 2355
D/TEST1: Thread id 2356 sleeping 10000ms
D/TEST3: Thread id 2358 sleeping 1000ms
D/TEST2: Thread id 2357 sleeping 5000ms
D/TEST4: Test completed in 10017ms thread 1
</pre>

## Test 5  (multithread)

Operatore `Observable.merge()`, `subscribeOn(Schedulers.io())` principale, `subscribeOn()` sui singoli `observableSleep()`.
Viene creato un thread per ogni `observableSleep()` e un thread nuovo anche per la chiamata principale.

### Codice

```java
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
```

### Esempio di log del metodo doTest5()

<pre>
D/TEST5: Test started at 1519924286580ms thread 2355
D/TEST1: Thread id 2359 sleeping 10000ms
D/TEST2: Thread id 2360 sleeping 5000ms
D/TEST3: Thread id 2361 sleeping 1000ms
D/TEST5: Test completed in 10019ms thread 1
</pre>

