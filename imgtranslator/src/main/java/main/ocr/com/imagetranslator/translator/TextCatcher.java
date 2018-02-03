package main.ocr.com.imagetranslator.translator;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2018/2/3.
 */

public interface TextCatcher {

    String initLanguage();

    Bitmap catchText(Bitmap bitmap);

}
