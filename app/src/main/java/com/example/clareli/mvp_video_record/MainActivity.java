package com.example.clareli.mvp_video_record;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.clareli.mvp_video_record.Presenter.IPresenterCameraControl;
import com.example.clareli.mvp_video_record.Presenter.PresenterCameraControl;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewErrorCallback;
import com.example.clareli.mvp_video_record.View.ViewErrorCallback;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.clareli.mvp_video_record.Util.IConstant.REQUEST_PERMISSION_CODE;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_PERMISSIONS;

public class MainActivity extends AppCompatActivity implements IViewErrorCallback {
    private AutoFitTextureView _textureView;
    private String TAG = "MainActivity";
    private Button _recordStartBtn;
    private IPresenterCameraControl _iPresenterCameraControl = null;
    private static String _fileName = "mvp_mediacodec.mp4";

    private String _filePath = null;
    private File _fileRecord = null;
    private boolean _isRecordingVideo = false;
    private ViewErrorCallback _viewErrorCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

        initPresenter();


    }


    @Override
    protected void onResume() {
        super.onResume();
        _iPresenterCameraControl.startBackground();
        _iPresenterCameraControl.videoPreviewStart(_textureView, this);

    }

    @Override
    protected void onPause() {
        if (_iPresenterCameraControl != null)
            _iPresenterCameraControl.closeCamera();
        super.onPause();

    }

    public void findViews() {
        _textureView = findViewById(R.id.texture);
        _recordStartBtn = findViewById(R.id.take_photo);
        _recordStartBtn.setOnClickListener(recordClickListener);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            //save to internal storage D
            _filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            _fileRecord = new File(_filePath, _fileName);
        }
    }

    public void initPresenter() {
        _viewErrorCallback = new ViewErrorCallback(this);
        _iPresenterCameraControl = new PresenterCameraControl(this, _viewErrorCallback);

    }

    View.OnClickListener recordClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_photo:
                    if (_isRecordingVideo == false) {
                        _isRecordingVideo = true;
                        _recordStartBtn.setText("Stop");
                        _iPresenterCameraControl.videoRecordStart(_fileRecord.getAbsolutePath());

                    } else {
                        _isRecordingVideo = false;
                        _recordStartBtn.setText("Record");
                        _iPresenterCameraControl.videoRecordStop();
                    }
                    break;
            }
        }
    };


    public void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, CAMERA}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        break;
                    }
                }


            } else {
                // permission denied
            }
        } else {
            onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void showErrorMsg(String msg) {
        Log.i(TAG, "error:" + msg);
    }
}
