package haibuzou.rxjavabook.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by lenovo on 2016/3/30.
 */
public class Utils {
    public static Bitmap drawableToBitmap(Drawable drawable){
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
    }

    public static void storeBitmap(){

    }
}
