package com.example.clareli.mvp_video_record;

import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.clareli.mvp_video_record.Presenter.IPresenterControl;
import com.example.clareli.mvp_video_record.Presenter.LUPresenterControl;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewCallback;
import com.example.clareli.mvp_video_record.View.LUViewCallback;
import com.example.clareli.mvp_video_record.View.ProgressbarDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.clareli.mvp_video_record.Util.IConstant.AUDIO_AAC;
import static com.example.clareli.mvp_video_record.Util.IConstant.REQUEST_PERMISSION_CODE;
import static com.example.clareli.mvp_video_record.Util.IConstant.RECORD_PERMISSIONS;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_AVC;
import static com.example.clareli.mvp_video_record.Util.LUPermissionCheck.hasPermissionsGranted;

public class MainActivity extends AppCompatActivity implements IViewCallback, ProgressbarDialog.OnFragmentInteractionListener {
    private AutoFitTextureView _textureView;
    private String TAG = "MainActivity";
    private Button _recordStartBtn;
    private IPresenterControl _presenterControl = null;
    private static String _fileName = "_mediacodec.mp4";

    private String _filePath = null;
    private File _fileRecord = null;
    private boolean _isRecordingVideo = false;
    private LUViewCallback _viewErrorCallback;
    private TextureView.SurfaceTextureListener _surfaceTextureListener;
    private View.OnClickListener _recordClickListener;
    private boolean isGetVideoCodecInfo = false;
    private boolean isGetAudioCodecInfo = false;
    private ProgressbarDialog _progressbarDialog;

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
        if (hasPermissionsGranted(this, RECORD_PERMISSIONS)) {

            if (_textureView.isAvailable()) {
                _presenterControl.openCamera(_textureView.getSurfaceTexture(), _textureView.getWidth(), _textureView.getHeight());
            } else {
                _textureView.setSurfaceTextureListener(_surfaceTextureListener);
            }
        } else {
            requestPermission();
        }

    }

    @Override
    protected void onPause() {
        if (_presenterControl != null)
            _presenterControl.closeCamera();
        super.onPause();

    }

    public void findViews() {
        _textureView = findViewById(R.id.texture);
        _recordStartBtn = findViewById(R.id.record_btn);
        _progressbarDialog = ProgressbarDialog.newInstance("wait....","");
        setupButtonClickListener();
        _recordStartBtn.setOnClickListener(_recordClickListener);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            //save to internal storage D
            _filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        }
    }

    public void initPresenter() {
        _viewErrorCallback = new LUViewCallback(this);
        _presenterControl = new LUPresenterControl(this, _viewErrorCallback);
        _progressbarDialog.setMessage("please wait.");
        _progressbarDialog.show(getSupportFragmentManager().beginTransaction(), ProgressbarDialog.class.getSimpleName());
        //2019-01-20,Clare
        // it needs presenter object, so after initialize _presenterControl
        setupSurfaceTextureListener();
        //user will decide which codec type
        _presenterControl.findAllSupportedCodecs();
        _presenterControl.separateCodecs(VIDEO_AVC);
        _presenterControl.separateCodecs(AUDIO_AAC);
    }

    public void setupButtonClickListener(){
        _recordClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.record_btn:
                        if (_isRecordingVideo == false) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd--hh-mm-ss");
                            String format = simpleDateFormat.format(new Date());
                            String finalName = format+_fileName;
                            _fileRecord = new File(_filePath, finalName);

                            _isRecordingVideo = true;
                            _recordStartBtn.setText("Stop");
                            _presenterControl.startRecord(_fileRecord.getAbsolutePath(), _textureView.getSurfaceTexture(), _textureView.getWidth(), _textureView.getHeight());
                        } else {
                            _isRecordingVideo = false;
                            _recordStartBtn.setText("Record");
                            _presenterControl.stopRecord();
                            _presenterControl.stopMuxer();
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
                if (hasPermissionsGranted(MainActivity.this, RECORD_PERMISSIONS)) {
                    _presenterControl.openCamera(surface, width, height);

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
               _presenterControl.stopVideoEncode();
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
            if (grantResults.length == RECORD_PERMISSIONS.length) {
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
        Log.i(TAG, "!!!error:" + msg);
    }

    //video codec can be selected
    @Override
    public void isVideoCodecSettingAllowed(boolean result) {
        isGetVideoCodecInfo = result;
        if(result == true){
            Toast.makeText(this, "video Codec ok", Toast.LENGTH_SHORT).show();
            isRecordReady();
        }
    }

    //audio codec can be selected
    @Override
    public void isAudioCodecSettingAllowed(boolean result) {
        isGetAudioCodecInfo = result;
        if(result == true){
            Toast.makeText(this, "audio Codec ok", Toast.LENGTH_SHORT).show();
            isRecordReady();
        }
    }

    public void isRecordReady(){
        if(isGetAudioCodecInfo && isGetVideoCodecInfo) {
            _recordStartBtn.setEnabled(true);
            _progressbarDialog.dismiss();

        } else {
            _recordStartBtn.setEnabled(false);
        }
    }

}
