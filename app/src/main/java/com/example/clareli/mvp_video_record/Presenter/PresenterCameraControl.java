package com.example.clareli.mvp_video_record.Presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import android.view.Surface;
import android.view.TextureView;

import com.example.clareli.mvp_video_record.MainActivity;
import com.example.clareli.mvp_video_record.Model.CameraCodec;
import com.example.clareli.mvp_video_record.Model.ICamera;
import com.example.clareli.mvp_video_record.Model.CameraClass;
import com.example.clareli.mvp_video_record.Model.ICameraCodec;
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
    private ICamera _iCamera = null;
    private PresenterCameraCallback _cameraCallback = null;
    private Object _systemService = null;
    private String[] cameraPermission = {WRITE_EXTERNAL_STORAGE, CAMERA, RECORD_AUDIO};
    private ViewErrorCallback _viewErrorCallback = null;
    private CameraDevice _cameraDevice = null;
    private AutoFitTextureView _textureView = null;
    private ICameraCodec _cameraCodec = null;
//    private CaptureRequest.Builder _cameraBuilder = null;
    private SurfaceTexture previewSurTexture;
    private Surface recordSurface = null;
    private Surface previewSurface = null;


    public PresenterCameraControl(Activity activity, ViewErrorCallback viewErrorCallback) {
        _messageViewReference = new WeakReference<>(activity);
        _cameraCallback = new PresenterCameraCallback(this);
        _iCamera = new CameraClass(_messageViewReference.get(), _cameraCallback);

        _viewErrorCallback = viewErrorCallback;
        _cameraCodec = new CameraCodec(_cameraCallback);

    }

    @Override
    public void videoRecordStart(String filePath) {
//        List<Surface> surfacesList = new ArrayList<>();
        if (_cameraDevice != null) {
            _iCamera.closePreviewSession();
            previewSurTexture = _textureView.getSurfaceTexture();
            assert previewSurTexture != null;
            previewSurTexture.setDefaultBufferSize(_textureView.getWidth(), _textureView.getHeight());
            recordSurface = _cameraCodec.initCodec();
            previewSurface = new Surface(previewSurTexture);
            _iCamera.createCaptureSession(previewSurface, recordSurface);


        }
    }

    @Override
    public void videoRecordStop() {
        _cameraCodec.stopRecord();
        _iCamera.startPreview(previewSurface);
    }


    @Override
    public void videoPreviewStart(AutoFitTextureView textureView, IViewErrorCallback iViewErrorCallback) {
        _textureView = textureView;
        if (textureView.isAvailable()) {
            if (hasPermissionsGranted(_messageViewReference.get(), cameraPermission)) {
                String cameraId = selectCamera();
                CameraManager manager = (CameraManager) _systemService;
                _iCamera.openCamera(textureView.getWidth(), textureView.getHeight(), cameraId, manager, textureView.getSurfaceTexture());
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
            if(_iCamera.tryToGetAcquire()) {
                cameraId = manager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return cameraId;
    }

    @Override
    public void closeCamera() {
        _iCamera.closeCamera();
        _iCamera.stopBackgroundThread();
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
                _iCamera.openCamera(width, height, cameraId, manager, surface);
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
            _cameraCodec.stopRecord();
//            return false;
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void startBackground() {
        _iCamera.startBackgroundThread();
    }


    @Override
    public void errorCameraCallback() {
        _viewErrorCallback.viewShowErrorDialog("open Camera error");
    }

    @Override
    public void errorCameraRecordCallback() {
        _viewErrorCallback.viewShowErrorDialog("Camera record error");


    }

    @Override
    public void completedCameraCallback() {

    }

    @Override
    public void completedCameraRecordCallback() {

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

    @Override
    public void getCameraDevice(CameraDevice mCameraDevice) {
        _cameraDevice = mCameraDevice;
    }

}
