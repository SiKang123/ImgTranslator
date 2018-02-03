package main.ocr.com.imagetranslator;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.ocr.com.imagetranslator.translator.Translator;

/**
 * Created by Sikang on 2017/7/12.
 */

public class ImageTranslator {
    //字体库路径
    private String language_path = "";

    private static ImageTranslator mImageTranslator = null;


    private ImageTranslator() {

    }

    public static ImageTranslator getInstance() {
        if (mImageTranslator == null)
            synchronized (ImageTranslator.class) {
                if (mImageTranslator == null)
                    mImageTranslator = new ImageTranslator();
            }
        return mImageTranslator;
    }

    public ImageTranslator init(String path) {
        File file = new File(path + "/tessdata/");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            language_path = path;
        } else {
            throw new RuntimeException("初始化失败");
        }
        return this;
    }


    public interface TesseractCallback {
        void onResult(String result);
    }


    /**
     * 开始单行识别
     *
     * @param bmp      需要识别的图片
     * @param callBack 结果回调（携带一个String 参数即可）
     */

    public void translate(final Translator translator, final Bitmap bmp, final TesseractCallback callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (checkLanguage(translator)) {
                    TessBaseAPI baseApi = new TessBaseAPI();
                    //初始化OCR的字体数据，TESSBASE_PATH为路径，ENGLISH_LANGUAGE指明要用的字体库（不用加后缀）
                    if (baseApi.init(language_path, translator.initLanguage())) {
                        //设置识别模式
                        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
                        //内容捕捉
                        Bitmap textBmp = translator.catchText(bmp);
                        if (textBmp != null) {
                            //设置要识别的图片
                            baseApi.setImage(textBmp);
                            baseApi.setVariable(TessBaseAPI.VAR_SAVE_BLOB_CHOICES, TessBaseAPI.VAR_TRUE);
                            //开始识别
                            String result = baseApi.getUTF8Text();
                            baseApi.clear();
                            baseApi.end();
                            bmp.recycle();
                            callBack.onResult(filter(translator, result));
                        } else {
                            callBack.onResult("");
                        }
                    } else {
                        callBack.onResult("");
                    }
                } else {
                    callBack.onResult("");
                }
            }
        }).start();
    }

    /**
     * 获取字符串中的手机号
     */
    public String filter(Translator translator, String str) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(translator.filterRule()))
            return str;
        Pattern pattern = Pattern.compile(translator.filterRule());
        Matcher matcher = pattern.matcher(str);
        StringBuffer bf = new StringBuffer();
        while (matcher.find()) {
            bf.append(matcher.group()).append(",");
        }
        int len = bf.length();
        if (len > 0) {
            bf.deleteCharAt(len - 1);
        }
        return bf.toString();
    }

    /**
     * 检查语言包
     */
    public boolean checkLanguage(Translator translator) {
        if (TextUtils.isEmpty(language_path + "/tessdata")) {
            throw new RuntimeException("tessdata not found! you need init ImageTranslator first! 未找到字库目录，请先初始化ImageTranslator");
        }
        File file = new File(language_path + "/tessdata");
        if (!file.exists())
            throw new RuntimeException("tessdata not found! you need init ImageTranslator first! 未找到字库目录，请先初始化ImageTranslator");


        String tessdata = language_path + translator.initLanguage() + ".traineddata";
        file = new File(tessdata);
        if (file.exists())
            throw new RuntimeException("没有找到正确的字库文件 : " + translator.initLanguage() + ".traineddata  not found!");

        return true;
    }


}
