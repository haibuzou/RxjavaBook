package haibuzou.rxjavabook.bean;

import android.graphics.drawable.Drawable;


public class AppInfo implements Comparable<Object> {

//    long mLastUpdateTime;
    public String mName;
    public Drawable mIcon;

    public AppInfo(String nName, Drawable icon) {
        mName = nName;
        mIcon = icon;
    }

    @Override
    public int compareTo(Object another) {
        AppInfo f = (AppInfo)another;
        return this.mName.compareTo(f.mName);
    }
}
