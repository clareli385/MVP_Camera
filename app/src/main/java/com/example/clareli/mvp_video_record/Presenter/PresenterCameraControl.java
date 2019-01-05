package com.example.clareli.mvp_video_record.Presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.TextureView;

import com.example.clareli.mvp_video_record.MainActivity;
import com.example.clareli.mvp_video_record.Model.ICamera;
import com.example.clareli.mvp_video_record.Model.CameraClass;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewErrorCallback;
import com.example.clareli.mvp_video_record.View.ViewErrorCallback;

import java.lang.ref.WeakReference;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.clareli.mvp_video_record.Util.IConstant.REQUEST_PERMISSION_CODE;
import static com.example.clareli.mvp_video_record.Util.PermissionCheck.hasPermissionsGranted;
import static com.example.clareli.mvp_video_record.Util.PermissionCheck.requestPermission;

public class PresenterCameraControl implements IPresenterCameraControl, IInterfaceCameraCallback {
    private final WeakReference<Activity> _messageViewReference;
    private ICamera iCamera;
    private PresenterCameraCallback _cameraCallback;
    private Object _systemService = null;
    private String[] cameraPermission = {WRITE_EXTERNAL_STORAGE, CAMERA, RECORD_AUDIO};
    private ViewErrorCallback _viewErrorCallback;

    public PresenterCameraControl(Activity activity, ViewErrorCallback viewErrorCallback) {
        _messageViewReference = new WeakReference<>(activity);
        _cameraCallback = new PresenterCameraCallback(this);
        iCamera = new CameraClass(_messageViewReference.get(), _cameraCallback);
        _viewErrorCallback = viewErrorCallback;
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
    public void videoPreviewStart(AutoFitTextureView textureView, IViewErrorCallback iViewErrorCallback) {
        if (textureView.isAvailable()) {
            if (hasPermissionsGranted(_messageViewReference.get(), cameraPermission)) {
                String cameraId = selectCamera();
                CameraManager manager = (CameraManager) _systemService;
                iCamera.openCamera(textureView.getWidth(), textureView.getHeight(), cameraId, manager, textureView.getSurfaceTexture());
            } else {
                ((MainActivity) (_messageViewReference.get())).requestPermission();
                return;
            }
        } else {
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    public String selectCamera() {
        String cameraId = null;
        if (_systemService == null)
            _systemService = _messageViewReference.get().getSystemService(Context.CAMERA_SERVICE);

        CameraManager manager = (CameraManager) _systemService;
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return cameraId;
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
            if (hasPermissionsGranted(_messageViewReference.get(), cameraPermission)) {
                String cameraId = selectCamera();
                CameraManager manager = (CameraManager) _systemService;
                iCamera.openCamera(width, height, cameraId, manager, surface);
            } else {
                requestPermission((MainActivity) (_messageViewReference.get()), cameraPermission, REQUEST_PERMISSION_CODE);
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
    public void startBackground() {
        iCamera.startBackgroundThread();
    }


    @Override
    public void errorCameraCallback() {
        Log.i("CLE", "errorCameraCallback....");
        _viewErrorCallback.viewShowErrorDialog("open Camera error");
    }

    @Override
    public void errorCameraRecordCallback() {
        Log.i("CLE", "errorCameraRecordCallback....");
        _viewErrorCallback.viewShowErrorDialog("Camera record error");


    }

    @Override
    public void completedCameraCallback() {
        Log.i("CLE", "completedCameraCallback....");

    }

    @Override
    public void completedCameraRecordCallback() {
        Log.i("CLE", "completedCameraRecordCallback....");

    }

    @Override
    public void errorEncoderCallback() {

    }

    @Override
    public void completedEncoderCallback() {

    }

    @Override
    public void errorDecoderCallback() {

    }

    @Override
    public void completedDecoderCallback() {

    }
}
