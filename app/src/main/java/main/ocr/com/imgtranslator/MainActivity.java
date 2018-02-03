package main.ocr.com.imgtranslator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    CameraView mCameraView;
    RecycledImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = (CameraView) findViewById(R.id.main_camera);
        mImageView = (RecycledImageView) findViewById(R.id.main_image);

        mCameraView.setTag(mImageView);

    }


}
