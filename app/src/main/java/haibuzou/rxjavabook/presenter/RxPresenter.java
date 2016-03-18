package haibuzou.rxjavabook.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import haibuzou.rxjavabook.bean.AppInfo;
import haibuzou.rxjavabook.view.RxView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;


public class RxPresenter {

    Context context;
    RxView rxView;

    public RxPresenter(Context context,RxView rxView) {
        this.context = context;
        this.rxView = rxView;
    }

    public void helloWorld(){
        rxView.setListItem(findApps());
    }


    private List<AppInfo> findApps(){
        final List<AppInfo> appInfoList = new ArrayList<>();
         Observable.create(new Observable.OnSubscribe<AppInfo>() {
            @Override
            public void call(Subscriber<? super AppInfo> subscriber) {
                PackageManager packageManager = context.getPackageManager();
                final Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(mainIntent,0);

                for(ResolveInfo info : infos){
                    AppInfo appinfo = new AppInfo(info.loadLabel(packageManager).toString(),info.loadIcon(packageManager));
                    subscriber.onNext(appinfo);
                }

                if(!subscriber.isUnsubscribed()){
                    subscriber.onCompleted();
                }

            }
        }).toSortedList().subscribe(new Observer<List<AppInfo>>() {
            @Override
            public void onCompleted() {
                Toast.makeText(context,"查询结束",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(context,"查询出错",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(List<AppInfo> appInfos) {
               appInfoList.addAll(appInfos);
            }
        });

        return appInfoList;
    }
}
