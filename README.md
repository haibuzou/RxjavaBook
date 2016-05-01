# RxjavaBook
Rxjava 有丰富的操作符，RxJava Essential 这本书对所有的操作符常用方法以及调度器
都有全面的介绍，可惜的是下载不到源码。
所谓自己动手丰衣足食，这个工程就是来自与RxJava Essential, 顺带学习了

##MVP
采用MVP+Rxjava的架构的RxjavaBook，所有关于操作符的关键类 RxPresenter。
添加了一点个人理解的注释

```java
 /**
     * DistinctUntilsChanged 操作符
     * 并不是单纯的整体过滤重复的数据
     * <p/>
     * A...B...A...A..........B...
     * |   |              |
     * |   |             |
     * A...B...A............B
     * <p/>
     * 忽略掉重复的值并且在温度确实改变时才想得到通知
     */
    public Observable<AppInfo> getDistinctUntilsChangedAppInfo() {
        return getAppInfo().take(3).repeat(3).distinctUntilChanged();
    }
    
    
    /**
     * flatmap 操作符
     * 同样是转换 但是flatmap可以将数据转换Observable，flatmap 可以处理更加复杂的数据情况
     * 下面的例子比较简单。只是为了展示 faltmap的写法
     * 想象一下 如果这个getAppInfo()返回的是 Observable<List<AppInfo>> flatMap就可以把它改成 Observable<AppInfo>
     * <p/>
     * A......B..D
     * <p/>
     * flatMap --> A --> A....C
     * <p/>
     * A....C..B..D..C..C
     * 最重要的一点 它允许交叉。正如上图所示，这意味着flatMap()不能够保证在最终生成的Observable中源Observables确切的发射顺序。
     */
    public void getAppIconWithFlatMap() {
        final List<AppInfo> appInfoList = new ArrayList<>();
        getAppInfo()
                .flatMap(new Func1<AppInfo, Observable<Drawable>>() {
                    @Override
                    public Observable<Drawable> call(AppInfo appInfo) {
                        return Observable.just(appInfo.mIcon);
                    }
                })
                .subscribe(new Observer<Drawable>() {
                    @Override
                    public void onCompleted() {
                        rxView.setListItem(appInfoList);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Drawable drawable) {
                        AppInfo appInfo = new AppInfo("flatmap app", drawable);
                        appInfoList.add(appInfo);
                    }
                });
    }

   .....
```
