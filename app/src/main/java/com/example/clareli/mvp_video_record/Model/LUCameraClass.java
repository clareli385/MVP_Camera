package com.example.clareli.mvp_video_record.Model;


import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.support.annotation.NonNull;

import com.example.clareli.mvp_video_record.Presenter.LUPresenterCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class LUCameraClass implements LUICamera {
    private CameraDevice _cameraDevice;
    private CameraCaptureSession _previewSession;
    private CaptureRequest.Builder _previewBuilder;

    private HandlerThread _backgroundThread;
    private Handler _backgroundHandler;
    private Semaphore _cameraOpenCloseLock = new Semaphore(1);

    private int _textureViewWidth = 0;
    private int _textureViewHeight = 0;
    private LUPresenterCallback _presenterCallback;
    private SurfaceTexture _previewSurfaceTexture;
    private CameraDevice.StateCallback _stateCallback;

    public LUCameraClass(LUPresenterCallback cameraCallback) {
        _presenterCallback = cameraCallback;
    }


    @Override
    public void startPreview(Surface previewSurface) {
        if (null == _cameraDevice) {
            _presenterCallback.errorPreview("Start Preview camera Device error!");
            return;
        }

        closePreviewSession();
        if (_previewSurfaceTexture == null) {
            _presenterCallback.errorPreview("Start Preview Surface Texture error!");
            return;
        }

        if (_textureViewWidth <= 0 || _textureViewHeight <= 0) {
            _presenterCallback.errorPreview("Start Preview Width Height error!");
        }
        _previewSurfaceTexture.setDefaultBufferSize(_textureViewWidth, _textureViewHeight);
        List<Surface> surfaces = new ArrayList<>();
        surfaces.add(previewSurface);
        createPreviewAndRecordCaptureSession(surfaces, false);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void openCamera(int width, int height, String cameraId, CameraManager manager, SurfaceTexture surfaceTexture) {
        _textureViewWidth = width;
        _textureViewHeight = height;

        if (cameraId == null) {
            _presenterCallback.errorPreview("Open Camera ID error!");
            return;
        }
        startBackgroundThread();
        setCameraDeviceStateCallback();
        try {
            _previewSurfaceTexture = surfaceTexture;
            manager.openCamera(cameraId, _stateCallback, _backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            _presenterCallback.errorPreview("Open Camera error!");
        }


    }


    @Override
    public void closeCamera() {
        if (_cameraDevice == null) {
            _presenterCallback.errorPreview("Close Camera error!");
            return;
        }
        try {
            _cameraOpenCloseLock.acquire();
            if (_previewSession != null) {
                _previewSession.close();
                _previewSession = null;
            }
            if (null != _cameraDevice) {
                _cameraDevice.close();
                _cameraDevice = null;
            }

        } catch (InterruptedException e) {
            _presenterCallback.errorPreview("Close Camera Semaphore acquire error!");
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            _cameraOpenCloseLock.release();
            stopBackgroundThread();
        }

    }

    private void startBackgroundThread() {
        _backgroundThread = new HandlerThread("CameraBackground");
        _backgroundThread.start();
        _backgroundHandler = new Handler(_backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (_backgroundThread != null) {
            _backgroundThread.quitSafely();
            try {
                _backgroundThread.join();
                _backgroundThread = null;
                _backgroundHandler = null;
            } catch (InterruptedException e) {
                _presenterCallback.errorPreview("Stop Background Thread error!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void createCaptureSession(final Surface previewSurface, final Surface recordSurface) {
        List<Surface> surfaces = new ArrayList<>();
        surfaces.add(recordSurface);
        surfaces.add(previewSurface);
        createPreviewAndRecordCaptureSession(surfaces, true);

    }

    @Override
    public void closePreviewSession() {
        if (_previewSession != null) {
            _previewSession.close();
            _previewSession = null;
        } else {
            _presenterCallback.errorPreview("Close Preview Session error!");

        }
    }

    @Override
    public boolean tryToGetAcquire(long timeout) {
        try {
            if (!_cameraOpenCloseLock.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                return true;
            }
        } catch (InterruptedException e) {
            _presenterCallback.errorPreview("Get Acquire error!");
            e.printStackTrace();
            return false;
        } finally {
            return true;
        }
    }

    public void setCameraDeviceStateCallback() {
        _stateCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                _cameraDevice = camera;
                _presenterCallback.getCameraDevice(_cameraDevice);
                Surface previewSurface = new Surface(_previewSurfaceTexture);
                startPreview(previewSurface);
                _cameraOpenCloseLock.release();

            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                _cameraOpenCloseLock.release();
                camera.close();
                _cameraDevice = null;
                //TODO add callback to presenter to notify camera closed
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                _cameraOpenCloseLock.release();
                camera.close();
                _cameraDevice = null;
                _presenterCallback.errorPreview("Set CameraDevice StateCallback error !");

            }
        };
    }

    private void createPreviewAndRecordCaptureSession(final List<Surface> surfacesList, final boolean isRecord){
        try {
            _cameraDevice.createCaptureSession(surfacesList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    _previewSession = session;
                    try {
                        //for only preview
                        if(isRecord == false) {
                            // Auto focus should be continuous for camera preview.
                            _previewBuilder = _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            //TODO modify to keep settings
//                            _previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                        } else {
                            //for video record and preview
                            _previewBuilder = _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        }
                        for(int index = 0; index < surfacesList.size(); index++) {
                            _previewBuilder.addTarget(surfacesList.get(index));
                        }
                        //displaying the camera preview.
                        _previewSession.setRepeatingRequest(_previewBuilder.build(),null, _backgroundHandler);

                    } catch (CameraAccessException e) {
                        _presenterCallback.errorPreview("Set Repeating Request error!");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    _presenterCallback.errorPreview("Create Capture Session Configure Failed!");

                }
            }, _backgroundHandler);
        } catch (CameraAccessException e) {
            _presenterCallback.errorPreview("Create Capture Session error!");
            e.printStackTrace();
        }
    }
}
