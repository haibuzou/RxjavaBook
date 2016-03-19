package haibuzou.rxjavabook.view;

import java.util.List;

import haibuzou.rxjavabook.bean.AppInfo;


public interface RxView {

    void setListItem(List<AppInfo> appList);
    void showLoading();
    void hideLoading();

}
