package com.example.clareli.mvp_video_record.Presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import com.example.clareli.mvp_video_record.MainActivity;
import com.example.clareli.mvp_video_record.Model.IVideoRecord;
import com.example.clareli.mvp_video_record.Model.VideoRecordClass;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewVideoRecordCallback;

import java.lang.ref.WeakReference;

public class PresenterVideoPreviewRecord implements IPresenterVideoPreviewRecord {
    private final WeakReference<Activity> _messageViewReference;
    private IVideoRecord iVideoRecord;
    private IViewVideoRecordCallback _iViewVideoRecordCallback;
    private int _rotation = Surface.ROTATION_0;
    private Object _systemService = null;
    private int _orientation = -5;

    public PresenterVideoPreviewRecord(Activity activity) {
        _messageViewReference = new WeakReference<>(activity);
        iVideoRecord = new VideoRecordClass(_messageViewReference.get(), this);


    }

    @Override
    public void videoRecordStart(String filePath) {
        iVideoRecord.startRecordingVideo(filePath);
    }

    @Override
    public void videoRecordStop() {
        iVideoRecord.stopRecordingVideo();
    }


    @Override
    public void videoPreviewStart(AutoFitTextureView textureView, IViewVideoRecordCallback iViewVideoRecordCallback) {
        _iViewVideoRecordCallback = iViewVideoRecordCallback;
        getInitData();
        if (textureView.isAvailable()) {
            if (((MainActivity) (_messageViewReference.get())).isPermissionGranted()) {
                iVideoRecord.openCamera(textureView.getWidth(), textureView.getHeight());
            } else {
                ((MainActivity) (_messageViewReference.get())).requestPermission();
                return;
            }
        } else {
            iVideoRecord.sendTextureView(textureView);
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }


    }

    @Override
    public void closeCamera() {
        iVideoRecord.closeCamera();
        iVideoRecord.stopBackgroundThread();
    }

    @Override
    public void cameraOpenError() {
        _messageViewReference.get().finish();
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (((MainActivity) (_messageViewReference.get())).isPermissionGranted()) {
                iVideoRecord.openCamera(width, height);
            } else {
                ((MainActivity) (_messageViewReference.get())).requestPermission();
                return;
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            iVideoRecord.configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void getInitData() {
        if ((_systemService == null) && (_orientation == -5)) {
            _rotation = _messageViewReference.get().getWindowManager().getDefaultDisplay().getRotation();
            _systemService = _messageViewReference.get().getSystemService(Context.CAMERA_SERVICE);
            _orientation = _messageViewReference.get().getResources().getConfiguration().orientation;
            iVideoRecord.setInitSetting(_systemService, _rotation, _orientation);
        } else
            return;

    }

    @Override
    public void startBackground() {
        iVideoRecord.startBackgroundThread();
    }

    @Override
    public void viewShowMsg(String msg) {
        _iViewVideoRecordCallback.showRecordStatus(msg);

    }

}
