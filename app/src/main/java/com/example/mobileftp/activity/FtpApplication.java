package com.example.mobileftp.activity;

import android.app.Application;
import com.blankj.utilcode.util.Utils;

public class FtpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }

}
