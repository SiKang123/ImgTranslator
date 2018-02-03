package main.ocr.com.imagetranslator.translator;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2018/2/3.
 */

public abstract class Translator {
    /**
     * 需要使用的字库的，这里需要返回字库名称
     */
    public abstract String initLanguage();

    /**
     * 内容捕捉，在这里实现捕捉内容的算法，并将最终需要识别的部分以Bitmap的方式返回
     */
    public abstract Bitmap catchText(Bitmap bitmap);

    /**
     * 过滤规则，把识别结果的内容赛选出来，这里需要返回一个正则表达式
     * 如果不需要过滤  返回 "" 即可
     */
    public abstract String filterRule();
}
