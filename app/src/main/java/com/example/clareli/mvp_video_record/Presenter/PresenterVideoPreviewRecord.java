package com.example.clareli.mvp_video_record.Presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.example.clareli.mvp_video_record.MainActivity;
import com.example.clareli.mvp_video_record.Model.ICamera;
import com.example.clareli.mvp_video_record.Model.CameraClass;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewVideoRecordCallback;

import java.lang.ref.WeakReference;

public class PresenterVideoPreviewRecord implements IPresenterVideoPreviewRecord {
    private final WeakReference<Activity> _messageViewReference;
    private ICamera iCamera;
    private IViewVideoRecordCallback _iViewVideoRecordCallback;
    private int _rotation = Surface.ROTATION_0;
    private Object _systemService = null;
    private int _orientation = -5;

    public PresenterVideoPreviewRecord(Activity activity) {
        _messageViewReference = new WeakReference<>(activity);
        iCamera = new CameraClass(_messageViewReference.get(), this);


    }

    @Override
    public void videoRecordStart(String filePath) {
        iCamera.startRecordingVideo(filePath);
    }

    @Override
    public void videoRecordStop() {
        iCamera.stopRecordingVideo();
    }


    @Override
    public void videoPreviewStart(AutoFitTextureView textureView, IViewVideoRecordCallback iViewVideoRecordCallback) {
        _iViewVideoRecordCallback = iViewVideoRecordCallback;
        getInitData();
        if (textureView.isAvailable()) {
            if (((MainActivity) (_messageViewReference.get())).isPermissionGranted()) {
                iCamera.openCamera(textureView.getWidth(), textureView.getHeight());
            } else {
                ((MainActivity) (_messageViewReference.get())).requestPermission();
                return;
            }
        } else {
            iCamera.sendTextureView(textureView);
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }


    }

    @Override
    public void closeCamera() {
        iCamera.closeCamera();
        iCamera.stopBackgroundThread();
    }

    @Override
    public void cameraOpenError() {
        _messageViewReference.get().finish();
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (((MainActivity) (_messageViewReference.get())).isPermissionGranted()) {
                iCamera.openCamera(width, height);
            } else {
                ((MainActivity) (_messageViewReference.get())).requestPermission();
                return;
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
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
            iCamera.setInitSetting(_systemService, _rotation, _orientation);
        } else
            return;

    }

    @Override
    public void startBackground() {
        iCamera.startBackgroundThread();
    }

    @Override
    public void viewShowMsg(String msg) {
        _iViewVideoRecordCallback.showRecordStatus(msg);

    }

}
