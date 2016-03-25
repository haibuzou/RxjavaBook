package haibuzou.rxjavabook.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import haibuzou.rxjavabook.bean.AppInfo;
import haibuzou.rxjavabook.view.RxView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.joins.Pattern2;
import rx.joins.Plan0;
import rx.observables.GroupedObservable;
import rx.observables.JoinObservable;
import rx.schedulers.Schedulers;


public class RxPresenter {

    Context context;
    RxView rxView;
    Intent mainIntent;
    PackageManager packageManager;

    public RxPresenter(Context context, RxView rxView) {
        this.context = context;
        this.rxView = rxView;
        packageManager = context.getPackageManager();
        mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    }

    /**
     * 检索设备内所有的app 信息
     */
    public List<ResolveInfo> getAllApp(){
        return context.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    public void findApp() {
        rxView.setListItem(findApps());
    }


    public void findAppSenior(){
        rxView.setListItem(getAppInfo().toList().toBlocking().single());
    }

    public void findTimerApp(){
        rxView.setListItem(getTimerAppInfo().toList().toBlocking().single());
    }

    public void findIntervalApp(){
        rxView.setListItem(getIntervalAppInfo().toList().toBlocking().single());
    }



    /**
     * --------------------------------------------------------------------------- 第一章分割线
     * 创建Observable 以及一些 常用操作符
     */


    /**
     * 这是最蠢，最复杂的写法 但是通过这个例子 可以了解 Observable 执行过程
     * 使用.create 手动创建observable 在call方法中选择发送的数据 .onNext
     */
    private List<AppInfo> findApps() {
        rxView.showLoading();
        final List<AppInfo> appInfoList = new ArrayList<>();
        Observable.create(new Observable.OnSubscribe<AppInfo>() {
            @Override
            public void call(Subscriber<? super AppInfo> subscriber) {

                List<ResolveInfo> infos = getAllApp();

                for (ResolveInfo info : infos) {
                    AppInfo appinfo = new AppInfo(info.loadLabel(packageManager).toString(), info.loadIcon(packageManager));
                    subscriber.onNext(appinfo);
                }

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }

            }
        })
                //这里使用toSortedList() 转换成集合的时候，必须要让javabean实现 Comparable<Object>
                .toSortedList()
                .subscribe(new Observer<List<AppInfo>>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(context, "查询结束", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "查询出错", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<AppInfo> appInfos) {
                        appInfoList.addAll(appInfos);
                    }
                });
        rxView.hideLoading();
        return appInfoList;
    }


    /**
     * 进化版写法
     * 使用from 操作符来发射获取的app信息 List<ResolveInfo>
     * from 专门用来发送集合
     * 通过map操作符用来转换成需要的AppInfo 发射出去
     * map用来转换发送的数据
     */
    private Observable<AppInfo> getAppInfo() {
        return Observable.from(getAllApp())
                .map(new Func1<ResolveInfo, AppInfo>() {
                    @Override
                    public AppInfo call(ResolveInfo resolveInfo) {
                        AppInfo appinfo = new AppInfo(resolveInfo.loadLabel(packageManager).toString(),
                                resolveInfo.loadIcon(packageManager));
                        return appinfo;
                    }
                });
    }


    /**
     * 将Observable<AppInfo> 转换成 Observable<List<AppInfo>>  toList()
     * Observable<List<AppInfo>> 转换成 List<AppInfo> toBlocking.single 读取单个
     */
    private List<AppInfo> getList() {
        return getAppInfo().toList().toBlocking().single();
    }


    /**
     * timer() 定时发送数据
     * 但是只发送一次
     */
    public Observable<AppInfo> getTimerAppInfo(){
        return Observable.timer(3,TimeUnit.SECONDS)
                .from(getAllApp())
                .map(new Func1<ResolveInfo, AppInfo>() {
                    @Override
                    public AppInfo call(ResolveInfo resolveInfo) {
                        AppInfo appInfo = new AppInfo(resolveInfo.loadLabel(packageManager).toString(),
                                resolveInfo.loadIcon(packageManager));
                        return appInfo;
                    }
                });
    }


    /**
     * interval() 轮询操作符
     * 以下代表 每3秒执行一次
     */
    public Observable<AppInfo> getIntervalAppInfo(){
        return Observable.interval(3, TimeUnit.SECONDS)
                .from(getAllApp())
                .map(new Func1<ResolveInfo, AppInfo>() {
                    @Override
                    public AppInfo call(ResolveInfo resolveInfo) {
                        AppInfo appInfo = new AppInfo(resolveInfo.loadLabel(packageManager).toString(),
                                resolveInfo.loadIcon(packageManager));
                        return appInfo;
                    }
                });
    }


    /**
     * rang()操作符 可以选择发送的范围
     * 从一个指定的数字X开始发射N个数字
     * 但是要在发送数据前使用
     */
    private Observable<AppInfo> getRangAppInfo(){
        return Observable.range(0,10)
                .from(getAllApp())
                .map(new Func1<ResolveInfo, AppInfo>() {
                    @Override
                    public AppInfo call(ResolveInfo resolveInfo) {
                        AppInfo appInfo = new AppInfo(resolveInfo.loadLabel(packageManager).toString(),
                                resolveInfo.loadIcon(packageManager));
                        return appInfo;
                    }
                });
    }


    /**
     * 模拟的加载更多
     * just 操作符用来发送单个对象
     * repeate 操作符可以 重复发送3次
     */
    public void loadMore(){
        AppInfo appinfo1 = new AppInfo("additem1",null);
        AppInfo appinfo2 = new AppInfo("additem2",null);
        AppInfo appinfo3 = new AppInfo("additem3",null);
        Observable.just(appinfo1,appinfo2,appinfo3)
                .repeat(3)
                .subscribe(new Observer<AppInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(AppInfo appInfo) {

                    }
                });
    }


    public void findAppUserFrom() {
        Observable.from(getList())
                .subscribe(new Observer<AppInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(AppInfo appInfo) {

                    }
                });
    }


    /**
     * --------------------------------------------------------------------------- 第二章分割线
     * 过滤Observable
     */

    /**
     * filter 操作符
     * 过滤 getAppInfo() 获取的App 数据
     * 判断的方式boolean 这里是 是否以字母A开头
     * 最常用的场景是过滤 appInfo 为null 的情况
     */
    public Observable<AppInfo> getFilterAppInfo(){
        return  getAppInfo().filter(new Func1<AppInfo, Boolean>() {
            @Override
            public Boolean call(AppInfo appInfo) {
                return appInfo.mName.startsWith("A");
            }
        });
    }


    /**
     * take 操作符
     * 获取我们想获取的部分 比如前3个 take(3)
     */
    public Observable<AppInfo> getTakeAppInfo(){
        return getAppInfo().take(3);
    }


    /**
     * takeLast 操作符
     * 获取队列最后的部分 比如最后3个 takeLast(3)
     */
    public Observable<AppInfo> getTakeLastAppInfo(){
        return getAppInfo().takeLast(3);
    }


    /**
     * Distinct 操作符
     * 与数据库查询一致 过滤重复数据
     * 这里的例子 获取前3个数据 然后故意repeate了3次
     * 制造重复数据 最后用distinct过滤
     */
    public Observable<AppInfo> getDistinctAppInfo(){
        return getAppInfo().take(3).repeat(3).distinct();
    }


    /**
     * DistinctUntilsChanged 操作符
     * 并不是单纯的整体过滤重复的数据
     *
     * A...B...A...A..........B...
     *     |   |              |
     *     |   |             |
     * A...B...A............B
     *
     * 忽略掉重复的值并且在温度确实改变时才想得到通知
     *
     */
    public Observable<AppInfo> getDistinctUntilsChangedAppInfo(){
        return getAppInfo().take(0).take(1).take(0).repeat(2).take(1).distinctUntilChanged();
    }


    /**
     * first 和 last 操作符
     * 没有参数的时候 发送第一个数据/发送最后一个数据
     * 也可以传入参数 达到限制条件的第一个数据/最后一个数据
     */
    public Observable<AppInfo> getFirstAppInfo(){
        return getAppInfo().first(new Func1<AppInfo, Boolean>() {
            @Override
            public Boolean call(AppInfo appInfo) {
                return appInfo.mName.startsWith("Z");
            }
        });
    }

    public Observable<AppInfo> getLastAppInfo(){
        return getAppInfo().last(new Func1<AppInfo, Boolean>() {
            @Override
            public Boolean call(AppInfo appInfo) {
                return appInfo.mName.startsWith("A");
            }
        });
    }

    /**
     * firstOrDefault/lastOrDefault 操作符
     * 功能与first/last 一样
     * 区别是不发送任何值的时候 会发送一个默认值
     */
    public Observable<AppInfo> getFirstorDefaultAppInfo(){
        AppInfo appInfo = new AppInfo("dafault app",null);
        return getAppInfo().firstOrDefault(appInfo, new Func1<AppInfo, Boolean>() {
            @Override
            public Boolean call(AppInfo appInfo) {
                return appInfo.mName.startsWith("Q");
            }
        });
    }


    /**
     * skip / skipLast 操作符
     * 与take / takeLast 相似
     * 跳过N个 / 跳过最后的N个
     */
    public Observable<AppInfo> getSkipAppInfo(){
        return getAppInfo().skip(2);
    }

    public Observable<AppInfo> getSkipLastAppInfo(){
        return getAppInfo().skipLast(2);
    }


    /**
     * elementAt 操作符
     * 用来发送指定位置的数据
     * 当然如果指定位置没有数据 可以用elementAtOrDefault 发送一个默认数据
     */
    public Observable<AppInfo> getElementAtAppInfo(){
        AppInfo appInfo = new AppInfo("dafault app",null);
        return getAppInfo().elementAt(2).elementAtOrDefault(100,appInfo);
    }


    /**
     * Sample 操作符
     * 假如现在有一个不断发出数据的情况
     * 但是数据并不会一直变化 所以我想取一段区间内的数据
     * 就可以用 sample 来实现  创建想要的Observable
     *
     * 使用轮询来模拟 不断发送数据的情况
     */
    public Observable<AppInfo> Sample(){
        return Observable.interval(3,TimeUnit.SECONDS)
                .sample(30,TimeUnit.SECONDS)
                .from(getAllApp())
                .map(new Func1<ResolveInfo, AppInfo>() {
                    @Override
                    public AppInfo call(ResolveInfo resolveInfo) {
                        AppInfo appInfo = new AppInfo(resolveInfo.loadLabel(packageManager).toString(),
                                resolveInfo.loadIcon(packageManager));
                        return appInfo;
                    }
                });
    }


    /**
     * TimeOut 操作符
     * 与字面意思超时一样 希望每一段时间就要发送数据
     * 超过指定时间 如果不发送数据 就会走 onError()
     * 时效性的意思
     */
    public Observable<AppInfo> getTimeOutAppInfo(){
        return getAppInfo().timeout(2,TimeUnit.SECONDS);
    }


    /**
     * debounce 操作符
     * 过滤掉由Observable发射的速率过快的数据；
     * 如果在一个指定的时间间隔过去了仍旧没有发射一个，那么它将发射最后的那个。
     * 比如指定2秒间隔，2秒间隔内的不会发送 , 但是超出2秒如果没有数据会发送最后一个
     *
     *  A....B..C....D
     *
     *    debounce ....
     *
     *  A..........C....D
     *
     */
    public Observable<AppInfo> getDebounceAppInfo(){
        return getAppInfo().debounce(2,TimeUnit.SECONDS);
    }


    /**
     * -------------------------------------------------------------------------- 第三章分割线
     *  转换Observables map  groupby buffer window cast
     */

    /**
     * map 操作符
     * 转换发送的数据类型
     * 下面的例子就是为了只获取文件名 将appinfo 转换成了 String
     */
    public Observable<String> getAppNameWithMap(){
        return getAppInfo()
                .map(new Func1<AppInfo, String>() {
                    @Override
                    public String call(AppInfo appInfo) {
                        return appInfo.mName;
                    }
                });
    }


    /**
     * flatmap 操作符
     * 同样是转换 但是flatmap可以将数据转换Observable，flatmap 可以处理更加复杂的数据情况
     * 下面的例子比较简单。只是为了展示 faltmap的写法
     * 想象一下 如果这个getAppInfo()返回的是 Observable<List<AppInfo>> flatMap就可以把它改成 Observable<AppInfo>
     *
     *     A......B..D
     *
     *      flatMap --> A --> A....C
     *
     *    A....C..B..D..C..C
     *  最重要的一点 它允许交叉。正如上图所示，这意味着flatMap()不能够保证在最终生成的Observable中源Observables确切的发射顺序。
     */
    public Observable<Drawable> getAppIconWithFlatMap(){
        return getAppInfo()
                .flatMap(new Func1<AppInfo, Observable<Drawable>>() {
                    @Override
                    public Observable<Drawable> call(AppInfo appInfo) {
                        return Observable.just(appInfo.mIcon);
                    }
                });
    }


    /**
     * concatMap 操作符
     * 写法功能与flatMap相同 它主要解决了交叉的问题
     *
     *     A......B..D
     *
     *      flatMap --> A --> A....C
     *
     *    A....C..B....C..D....C
     */
    public Observable<Drawable> getAppIconWithConcatMap(){
        return getAppInfo()
                .concatMap(new Func1<AppInfo, Observable<? extends Drawable>>() {
                    @Override
                    public Observable<? extends Drawable> call(AppInfo appInfo) {
                        return Observable.just(appInfo.mIcon);
                    }
                });
    }


    /**
     * flatMapIterable 操作符
     * 与flatMap功能一样 但是flatMapIterable是将数据转换成集合 而不是Observable
     * 同样flatMapIterable也会产生交叉
     */
    public Observable<Drawable> getAppIconWithFlatMapIterable(){
        return getAppInfo()
                .flatMapIterable(new Func1<AppInfo, List<Drawable>>() {
                    @Override
                    public List<Drawable> call(AppInfo appInfo) {
                        List<Drawable> list = new ArrayList<>();
                        list.add(appInfo.mIcon);
                        return list;
                    }
                });
    }


    /**
     * switchMap 操作符
     * 与flatMap功能一样,但是switchMap再有新数据进来时会放弃监听之前数据产生的Observable 转而监视当前发射的数据
     *
     *     A......B.D
     *
     *      switchMap --> A --> A....C
     *
     *    A....C..B.D....C
     */
    public Observable<Drawable> getAppIconWithSwitchMap(){
        return getAppInfo()
                .switchMap(new Func1<AppInfo, Observable<Drawable>>() {
                    @Override
                    public Observable<Drawable> call(AppInfo appInfo) {
                        return Observable.just(appInfo.mIcon);
                    }
                });
    }


    /**
     * scan 操作符
     * scan()函数对原始Observable发射的每一项数据都应用一个函数，
     * 计算出函数的结果值，并将该值填充回可观测序列，等待和下一次发射的数据一起使用。
     * 典型的可以做累加器 下面的例子是根据app的name的长度进行排序
     */
    public Observable<AppInfo> getScanAppInfo(){
        return getAppInfo()
                .scan(new Func2<AppInfo, AppInfo, AppInfo>() {
                    @Override
                    public AppInfo call(AppInfo appInfo, AppInfo appInfo2) {
                        if(appInfo.mName.length()>appInfo2.mName.length()){
                            return appInfo;
                        }else{
                            return appInfo2;
                        }
                    }
                });
    }

    /**
     * 使用 scan 完成的累加器的例子
     */
    public void sum(){
        Observable.just(1,2,3,4,5)
                .scan(new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer, Integer integer2) {
                        return integer+integer2;
                    }
                })
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.d("RxJava",""+"onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("RxJava",""+"onError");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d("RxJava",""+integer);
                    }
                });
    }


    /**
     * groupBy 操作符
     * sql语句中的groupby 功能类似，按照指定规则来分组元素
     * 下面的例子创建了一个新的Observable，groupedItems，它将会发射一个带有GroupedObservable的序列
     * GroupedObservable是一个特殊的Observable  GroupedObservable<K, T> key/value的结构
     * 这里的key就是哪来分组的规则
     */
    public Observable<AppInfo> getGroupByAppInfo(){
        Observable<GroupedObservable<String,AppInfo>> groupItem =
                getAppInfo()
                        .groupBy(new Func1<AppInfo, String>() {
                            @Override
                            public String call(AppInfo appInfo) {
                                return appInfo.mName;
                            }
                        });

        return Observable.concat(groupItem);
    }


    /**
     * Buffer 操作符
     * 让Observable每次发射一组值 而不是一个一个发射
     *
     * A....B....C....D
     *
     *   buffer(count = 2)
     *
     * AB....CD
     *
     * 注意使用buffer后返回的数据会被转成集合
     *
     */
    public Observable<List<AppInfo>> getBufferAppInfo(){
        return getAppInfo().buffer(2);
    }

    /**
     * buffer(count = 2,skip = 3)
     * 可以选择跳过不需要的部分
     */
    public Observable<List<AppInfo>> getBufferSkipInfo(){
        return getAppInfo().buffer(2,3);
    }

    /**
     * buffer(timeSpan = 4,TimeUnit.SECONDS,count = 3)
     * 以时间为间隔 每隔timespan时间段就会发射一个列表的Observable
     *
     * A.B.C....D.E.F
     *
     *  buffer(timeSpan = 4,TimeUnit.SECONDS,count = 2)
     *
     * A,B.C....D,E.F
     *
     *
     */
    public Observable<List<AppInfo>> getBufferTimeSpanInfo(){
        return getAppInfo().buffer(4,TimeUnit.SECONDS,2);
    }


    /**
     * window 操作符
     * 与buffer很像，区别是window 发射的是 Observable 而不是集合
     * 同样window 也有skip
     * window(Count = 3)
     * window(count = 3,skip = 3)
     */
    public Observable<Observable<AppInfo>> getWindowAooInfo(){
        return getAppInfo().window(3);
    }


    /**
     * cast 操作符
     * cast是map()操作符的特殊版本。它将源Observable中的每一项数据都转换为新的类型，把它变成了不同的Class
     */
    public Observable<String> getAppNameWithCast(){
        return getAppInfo().cast(String.class);
    }



    /**
     * -------------------------------------------------------------------------- 第四章分割线
     * 合并Observable
     */


    /**
     * merge 操作符
     * 合并多个Observable 并发射数据
     *
     * RxJava提供了mergeDelayError()，它能从一个Observable中继续发射数据即便是其中有一个抛出了错误。
     * 当所有的Observables都完成时，mergeDelayError()将会发射onError()
     */
    public Observable<AppInfo> getMergeAppInfo(){
        List<ResolveInfo> reverseApp = getAllApp();
        Collections.reverse(reverseApp);
        Observable<AppInfo> reverseAppObservable = Observable.from(reverseApp).map(new Func1<ResolveInfo, AppInfo>() {
            @Override
            public AppInfo call(ResolveInfo resolveInfo) {
                AppInfo appinfo = new AppInfo(resolveInfo.loadLabel(packageManager).toString(),
                        resolveInfo.loadIcon(packageManager));
                return appinfo;
            }
        });

        Observable<AppInfo> appObservable = getAppInfo();

//        Observable.mergeDelayError(reverseAppObservable,appObservable);
        return Observable.merge(reverseAppObservable,appObservable);
    }


    /**
     * zip 操作符
     *
     * 合并两个或者多个Observables发射出的数据项，根据指定的函数Func*变换它们，并发射一个新值
     *
     * 1...2....3...4
     * A...B....C...D
     *
     *  Zip
     *
     * 1A...2B....3C...4D
     *
     * 下面的例子 创建了每隔1秒发送一个数字的Observable tictoc  和 查询出来的 appObservable 进行zip 操作
     * 具体操作规则 是将app的Name后面 添加 tictoc 发射的数字，返回的值仍然是 AppInfo
     */
    public Observable<AppInfo> getZipAppInfo(){
        Observable<AppInfo> appObservable = getAppInfo();
        Observable<Long> tictoc  = Observable.interval(1,TimeUnit.SECONDS);

        return Observable.zip(tictoc , appObservable, new Func2<Long, AppInfo, AppInfo>() {
            @Override
            public AppInfo call(Long aLong, AppInfo appInfo) {
                AppInfo newApp = new AppInfo(appInfo.mName+aLong,appInfo.mIcon);
                return newApp;
            }
        });
    }


    /**
     * join 操作符
     * 基于时间窗口将2个Observable发射的数据结合到一起
     * join的4个参数
     * 第二个Observable和源Observable结合。
     Func1参数：在指定的由时间窗口定义时间间隔内，源Observable发射的数据和从第二个Observable发射的数据相互配合返回的Observable。
     Func1参数：在指定的由时间窗口定义时间间隔内，第二个Observable发射的数据和从源Observable发射的数据相互配合返回的Observable。
     Func2参数：定义已发射的数据如何与新发射的数据项相结合。
     */
    public Observable<AppInfo> getJoinAppInfo(){
        final List<AppInfo> apps = getList();
        Observable<Long> tictoc = Observable.interval(1,TimeUnit.SECONDS);
        final Observable<AppInfo> appsSequence = Observable.interval(1,TimeUnit.SECONDS).map(new Func1<Long, AppInfo>() {
            @Override
            public AppInfo call(Long aLong) {
                return apps.get(aLong.intValue());
            }
        });

        return appsSequence.join(tictoc, new Func1<AppInfo, Observable<Long>>() {
            @Override
            public Observable<Long> call(AppInfo appInfo) {
                return Observable.timer(2,TimeUnit.SECONDS);
            }
        }, new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                return Observable.timer(0, TimeUnit.SECONDS);
            }
        }, new Func2<AppInfo, Long, AppInfo>() {
            @Override
            public AppInfo call(AppInfo appInfo, Long aLong) {
                appInfo.mName = aLong+appInfo.mName;
                return appInfo;
            }
        });
    }

    /**
     * combineLatest 操作符
     * zip作用于2个Observable combineLatest则作用于最近发射的数据项
     * 比如：如果Observable1发射了A并且Observable2发射了B和C，combineLatest()将会分组处理AB和AC
     *
     * 1.....2.....................3...4......5
     *
     * ...A.....B.........C...D................
     *
     *    combineLatest((x,y)->(""+x+y))
     *
     * ...1A.....2A2B....2C...2D...3D...4D.....5D
     *
     */
    public Observable<AppInfo> getCombinLatestAppInfo(){
        final List<AppInfo> apps = getList();
        Observable<AppInfo> appsSequence = Observable.interval(1000,TimeUnit.MILLISECONDS).map(new Func1<Long, AppInfo>() {
            @Override
            public AppInfo call(Long aLong) {
                return apps.get(aLong.intValue());
            }
        });

        Observable<Long> tictoc = Observable.interval(1500,TimeUnit.MILLISECONDS);
        return Observable.combineLatest(appsSequence, tictoc, new Func2<AppInfo, Long, AppInfo>() {
            @Override
            public AppInfo call(AppInfo appInfo, Long aLong) {
                appInfo.mName = aLong+appInfo.mName;
                return appInfo;
            }
        });

    }


    /**
     * and/then/when 操作符
     * 对于某些复杂的架构zip无法解决的时候，可以用 and/then/when 使用Pattern和Plan作为中介，将发射的数据集合并到一起
     * 不过他们在RxJava的joins包下
     * compile 'io.reactivex:rxjava-joins:0.22.0'
     */
    public Observable<AppInfo> getAndThenWhenAppInfo(){
        Observable<AppInfo> observableApp = getAppInfo();
        Observable<Long> tictoc = Observable.interval(1,TimeUnit.SECONDS);
        Pattern2<AppInfo, Long> pattern = JoinObservable.from(observableApp).and(tictoc);
        Plan0<AppInfo> plan = pattern.then(new Func2<AppInfo, Long, AppInfo>() {
            @Override
            public AppInfo call(AppInfo appInfo, Long aLong) {
                appInfo.mName = aLong+appInfo.mName;
                return appInfo;
            }
        });

        return JoinObservable.when(plan).toObservable();
    }


    /**
     * switch 操作符
     * 将一个发射多个Observables的Observable转换成另一个单独的Observable，后者发射那些Observables最近发射的数据项
     * Switch订阅一个发射多个Observables的Observable。它每次观察那些Observables中的一个，Switch返回的这个Observable取消订阅前一个发射数据的Observable，
     * 开始发射最近的Observable发射的数据。注意：当原始Observable发射了一个新的Observable时（不是这个新的Observable发射了一条数据时），它将取消订阅之前的那个Observable。
     * 这意味着，在后来那个Observable产生之后到它开始发射数据之前的这段时间里，前一个Observable发射的数据将被丢弃
     */
    public Observable<AppInfo> getSwitchAppInfo(){
       Observable<Observable<AppInfo>> switchObservable = Observable.from(getAllApp())
                .map(new Func1<ResolveInfo, Observable<AppInfo>>() {
                    @Override
                    public Observable<AppInfo> call(ResolveInfo resolveInfo) {
                        return null;
                    }
                });
        return Observable.switchOnNext(switchObservable);
    }


    /**
     * startWith 操作符
     * Observable开始发射他们的数据之前，startWith()通过传递一个参数来先发射一个数据序列
     * starWith()可以接收单个数据，也可以接受集合甚至是Observable
     *
     */
    public Observable<AppInfo> getStartWithAppInfo(){
        AppInfo appInfo = new AppInfo("我才是第一个",null);
        return getAppInfo().startWith(appInfo);
    }




    /**
     * -------------------------------------------------------------------------- 第五章分割线
     * 调度器
     * 调度器可以很容易的帮助我们创建主线程以及提高我们应用程序的性能
     * 处理处理多线程和并发编程的问题
     */

    /**
     * Schedulers
       调度器以一种最简单的方式将多线程用在你的Apps的中。它们时RxJava重要的一部分并能很好地与Observables协同工作。
       它们无需处理实现、同步、线程、平台限制、平台变化而可以提供一种灵活的方式来创建并发程序。
       RxJava提供了5种调度器：
       .io()
       .computation()
       .immediate()
       .newThread()
       .trampoline()

       让我们一个一个的来看下它们：
       Schedulers.io()、
       这个调度器时用于I/O操作。它基于根据需要，增长或缩减来自适应的线程池。我们将使用它来修复我们之前看到的StrictMode违规做法。
       由于它专用于I/O操作，所以并不是RxJava的默认方法；正确的使用它是由开发者决定的。

       重点需要注意的是线程池是无限制的，大量的I/O调度操作将创建许多个线程并占用内存。一如既往的是，我们需要在性能和简捷两者之间找到一个有效的平衡点。
       Schedulers.computation()

       这个是计算工作默认的调度器，它与I/O操作无关。它也是许多RxJava方法的默认调度器：buffer(),debounce(),delay(),interval(),sample(),skip()。
       Schedulers.immediate()

       这个调度器允许你立即在当前线程执行你指定的工作。它是timeout(),timeInterval(),以及timestamp()方法默认的调度器。
       Schedulers.newThread()

       这个调度器正如它所看起来的那样：它为指定任务启动一个新的线程。
       Schedulers.trampoline()

       当我们想在当前线程执行一个任务时，并不是立即，我们可以用.trampoline()将它入队。这个调度器将会处理它的队列并且按序运行队列中每一个任务。
       它是repeat()和retry()方法默认的调度器。
     */


    /**
     * SubscribeOn and ObserveOn
     * SubscribeOn 用于每个Observable对象 也就是事件源所执行的线程
     * ObserveOn 指定的是观察者的线程，也就是Observable出发数据后在哪个线程执行
     */
    public Observable<AppInfo> getAppInfoWithObserveOnAndSubscribeOn(){
          return getAppInfo().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 模拟了一个耗时操作
     * subscribeOn(Schedulers.computation()) 设置计算的线程
     * 如果不设置导致App卡死
     */
    public Observable<AppInfo> getAppInfoWithLongTask(){
        return Observable.from(getAllApp())
                .map(new Func1<ResolveInfo, AppInfo>() {
                    @Override
                    public AppInfo call(ResolveInfo resolveInfo) {
                        for (double i = 0; i < 1000000000; i++) {
                            double y = i * i;
                        }
                        AppInfo appinfo = new AppInfo(resolveInfo.loadLabel(packageManager).toString(),
                                resolveInfo.loadIcon(packageManager));
                        return appinfo;
                    }
                }).subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread());
    }

}
