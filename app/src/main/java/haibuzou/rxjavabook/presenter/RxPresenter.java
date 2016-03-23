package haibuzou.rxjavabook.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import haibuzou.rxjavabook.bean.AppInfo;
import haibuzou.rxjavabook.view.RxView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;


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
     * 21°...22°...21°...21°...22°...
     *     |        |         |
     *     |        |         |
     * 21°...22°....21°........22°
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
     * 就可以用 sample 来实现
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
     * 与字面意思超时一样
     * 超过指定时间 如果不发送数据 就会走 onError()
     * 时效性的意思
     */
    public Observable<AppInfo> getTimeOutAppInfo(){
        return getAppInfo().timeout(2,TimeUnit.SECONDS);
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
     * 使用from 操作符来发射获取的app信息 ResolveInfo
     * from 专门用来发送集合
     * 通过map操作符来转换成需要的AppInfo 发射出去
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


}
