package main.ocr.com.imgtranslator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.ocr.com.imagetranslator.ImageTranslator;
import main.ocr.com.imagetranslator.translator.PhoneNumberTranslator;
import main.ocr.com.imagetranslator.translator.Translator;

/**
 * Created by Sikang on 2017/4/21.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private final String TAG = "CameraView";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean isPreviewOn;
    private Translator translator;
    //默认预览尺寸
    private int imageWidth = 1920;
    private int imageHeight = 1080;
    //帧率
    private int frameRate = 30;
    private ImageView hintImage;

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHolder = getHolder();
        //设置SurfaceView 的SurfaceHolder的回调函数
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Surface创建时开启Camera
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //设置Camera基本参数
        if (mCamera != null)
            initCameraParams();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            release();
        } catch (Exception e) {
        }
    }

    public boolean isScanning = false;
    private long starTime, endTime;

    /**
     * Camera帧数据回调用
     */
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        //识别中不处理其他帧数据
        if (!isScanning) {
            isScanning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("scantest", "-------------Start------------------");
                        starTime = System.currentTimeMillis();
                        //获取Camera预览尺寸
                        Camera.Size size = camera.getParameters().getPreviewSize();
                        int left = (int) (size.width / 2 - getResources().getDimension(R.dimen.x20));
                        int top = (int) (size.height / 2 - getResources().getDimension(R.dimen.x80));
                        int right = (int) (left + getResources().getDimension(R.dimen.x40));
                        int bottom = (int) (top + getResources().getDimension(R.dimen.x160));
                        //将帧数据转为bitmap
                        final YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if (image != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(left, top, right, bottom), getQuality(size.height), stream);
                            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            if (bmp == null) {
                                isScanning = false;
                                return;
                            }
                            if (translator == null) {
                                if (getTag() != null) {
                                    if (getTag() instanceof ImageView)
                                        hintImage = (ImageView) getTag();
                                }
                                translator = new PhoneNumberTranslator(hintImage);
                            }

                            //开始识别
                            ImageTranslator.getInstance().translate(translator, rotateToDegrees(bmp, 90), new ImageTranslator.TesseractCallback() {
                                @Override
                                public void onResult(String result) {
                                    endTime = System.currentTimeMillis();
                                    Log.d("scantest", "扫描结果：  " + result);
                                    if (!TextUtils.isEmpty(result)) {
                                        //检索结果中是否包含手机号
                                        Log.d("scantest", "手机号码：  " + result);
                                    }
                                    isScanning = false;
                                    Log.d("scantest", "-------------End------------------");
                                }
                            });
                        } else {
                            isScanning = false;
                        }
                    } catch (Exception ex) {
                        Log.d("scantest", ex.getMessage());
                        isScanning = false;
                    }

                }
            }).start();
        }

    }

    //压缩比例
    private int getQuality(int width) {
        int quality = 100;
        if (width > 480) {
            float w = 480 / (float) width;
            quality = (int) (w * 100);
        }
        return quality;
    }


    /**
     * 图片旋转
     *
     * @param tmpBitmap
     * @param degrees
     * @return
     */
    public static Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degrees);
        return Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix,
                true);
    }


    /**
     * 摄像头配置
     */
    public void initCameraParams() {
        stopPreview();
        Camera.Parameters camParams = mCamera.getParameters();
        List<Camera.Size> sizes = camParams.getSupportedPreviewSizes();
        for (int i = 0; i < sizes.size(); i++) {
            if ((sizes.get(i).width >= imageWidth && sizes.get(i).height >= imageHeight) || i == sizes.size() - 1) {
                imageWidth = sizes.get(i).width;
                imageHeight = sizes.get(i).height;
//                Log.v(TAG, "Changed to supported resolution: " + imageWidth + "x" + imageHeight);
                break;
            }
        }
        camParams.setPreviewSize(imageWidth, imageHeight);
        camParams.setPictureSize(imageWidth, imageHeight);
//        Log.v(TAG, "Setting imageWidth: " + imageWidth + " imageHeight: " + imageHeight + " frameRate: " + frameRate);

        camParams.setPreviewFrameRate(frameRate);
//        Log.v(TAG, "Preview Framerate: " + camParams.getPreviewFrameRate());

        mCamera.setParameters(camParams);
        //取到的图像默认是横向的，这里旋转90度，保持和预览画面相同
        mCamera.setDisplayOrientation(90);
        // Set the holder (which might have changed) again
        startPreview();
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(mHolder);//set the surface to be used for live preview
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCB);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }

    /**
     * 打开指定摄像头
     */
    public void openCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    mCamera = Camera.open(cameraId);
                } catch (Exception e) {
                    if (mCamera != null) {
                        mCamera.release();
                        mCamera = null;
                    }
                }
                break;
            }
        }
    }

    /**
     * 摄像头自动聚焦
     */
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            postDelayed(doAutoFocus, 1000);
        }
    };
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null) {
                try {
                    mCamera.autoFocus(autoFocusCB);
                } catch (Exception e) {
                }
            }
        }
    };

    /**
     * 释放
     */
    public void release() {
        if (isPreviewOn && mCamera != null) {
            isPreviewOn = false;
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

}
