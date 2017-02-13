package cz.uhk.fim.soucera.geocatcher;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Radek Soucek on 26.01.2017.
 */

public class Geocatcher extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
