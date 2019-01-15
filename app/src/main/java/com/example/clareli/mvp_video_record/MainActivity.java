package com.example.clareli.mvp_video_record;

import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.example.clareli.mvp_video_record.Presenter.IPresenterControl;
import com.example.clareli.mvp_video_record.Presenter.LUPresenterControl;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewErrorCallback;
import com.example.clareli.mvp_video_record.View.LUViewErrorCallback;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.clareli.mvp_video_record.Util.IConstant.REQUEST_PERMISSION_CODE;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_PERMISSIONS;
import static com.example.clareli.mvp_video_record.Util.LUPermissionCheck.hasPermissionsGranted;
import static com.example.clareli.mvp_video_record.Util.LUPermissionCheck.requestPermission;

public class MainActivity extends AppCompatActivity implements IViewErrorCallback {
    private AutoFitTextureView _textureView;
    private String TAG = "MainActivity";
    private Button _recordStartBtn;
    private IPresenterControl _iPresenterControl = null;
    private static String _fileName = "mvp_mediacodec.mp4";//"mvp_video.mjpeg";

    private String _filePath = null;
    private File _fileRecord = null;
    private boolean _isRecordingVideo = false;
    private LUViewErrorCallback _LU_viewErrorCallback;
    private TextureView.SurfaceTextureListener _surfaceTextureListener;
    private View.OnClickListener recordClickListener;

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
        if (hasPermissionsGranted(this, VIDEO_PERMISSIONS)) {

            if (_textureView.isAvailable()) {
                _iPresenterControl.openCamera(_textureView.getSurfaceTexture(), _textureView.getWidth(), _textureView.getHeight());
            } else {
                _textureView.setSurfaceTextureListener(_surfaceTextureListener);
            }
        } else {
            requestPermission();
        }

    }

    @Override
    protected void onPause() {
        if (_iPresenterControl != null)
            _iPresenterControl.closeCamera();
        super.onPause();

    }

    public void findViews() {
        _textureView = findViewById(R.id.texture);
        _recordStartBtn = findViewById(R.id.record_btn);
        setupButtonClickListener();
        _recordStartBtn.setOnClickListener(recordClickListener);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            //save to internal storage D
            _filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            _fileRecord = new File(_filePath, _fileName);
        }
    }

    public void initPresenter() {
        _LU_viewErrorCallback = new LUViewErrorCallback(this);
        _iPresenterControl = new LUPresenterControl(this, _LU_viewErrorCallback);
        //it needs presenter object, so after initialize _iPresenterControl
        setupSurfaceTextureListener();
    }

    public void setupButtonClickListener(){
        recordClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.record_btn:
                        if (_isRecordingVideo == false) {
                            _isRecordingVideo = true;
                            _recordStartBtn.setText("Stop");
                            _iPresenterControl.videoRecordStart(_fileRecord.getAbsolutePath(), _textureView.getSurfaceTexture(), _textureView.getWidth(), _textureView.getHeight());

                        } else {
                            _isRecordingVideo = false;
                            _recordStartBtn.setText("Record");
                            _iPresenterControl.videoRecordStop();
                        }
                        break;
                }
            }
        };
    }

    public void setupSurfaceTextureListener() {
        _surfaceTextureListener = new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (hasPermissionsGranted(MainActivity.this, VIDEO_PERMISSIONS)) {
                    _iPresenterControl.openCamera(surface, width, height);

                } else {
                    requestPermission();
                    return;
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                _iPresenterControl.stopEncode();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };

    }

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
                showErrorMsg("You have to enable the permissions!");
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
