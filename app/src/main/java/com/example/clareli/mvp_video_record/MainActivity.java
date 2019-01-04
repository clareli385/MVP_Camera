package com.example.clareli.mvp_video_record;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.clareli.mvp_video_record.Presenter.IPresenterVideoPreviewRecord;
import com.example.clareli.mvp_video_record.Presenter.PresenterVideoPreviewRecord;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewVideoRecordCallback;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.clareli.mvp_video_record.Util.IConstant.REQUEST_PERMISSION_CODE;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_PERMISSIONS;

public class MainActivity extends AppCompatActivity implements IViewVideoRecordCallback {
    private AutoFitTextureView mTextureView;
    private Button recordStartBtn;
    private IPresenterVideoPreviewRecord iPresenterVideoPreviewRecord = null;
//    private static String _fileName = "my_video_record.mp4";
private static String _fileName = "video_record.3gp";

    private String _filePath = null;
    private File _fileAudio = null;
    private boolean mIsRecordingVideo = false;

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
        iPresenterVideoPreviewRecord.startBackground();
        iPresenterVideoPreviewRecord.videoPreviewStart(mTextureView, this);

    }

    @Override
    protected void onPause() {
        if(iPresenterVideoPreviewRecord != null)
            iPresenterVideoPreviewRecord.closeCamera();
        super.onPause();

    }

    public void findViews(){
        mTextureView = findViewById(R.id.texture);
        recordStartBtn = findViewById(R.id.video_record_start);
        recordStartBtn.setOnClickListener(recordClickListener);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            //save to internal storage D
            _filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            _fileAudio = new File(_filePath, _fileName);
        }
    }

    public void initPresenter(){
        iPresenterVideoPreviewRecord = new PresenterVideoPreviewRecord(this);

    }

    View.OnClickListener recordClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.video_record_start:
                    if(mIsRecordingVideo == false) {
                        mIsRecordingVideo = true;
                        recordStartBtn.setText("Stop");
                        iPresenterVideoPreviewRecord.videoRecordStart(_fileAudio.getAbsolutePath());

                    } else {
                        mIsRecordingVideo = false;
                        recordStartBtn.setText("Record");
                        iPresenterVideoPreviewRecord.videoRecordStop();
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

        if(requestCode == REQUEST_PERMISSION_CODE){
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        break;
                    }
                }


            } else {
                // permission denied
            }
        } else{
            onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void showRecordStatus(String msg) {
    }
}
