package main.ocr.com.imgtranslator;

import android.app.Application;
import android.os.Environment;

import java.io.File;

import main.ocr.com.imagetranslator.ImageTranslator;

/**
 * Created by Administrator on 2018/2/3.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        String path = Environment.getExternalStorageDirectory() + "/Download";
        ImageTranslator.getInstance().init(path);
    }
}
