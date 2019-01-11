package com.example.clareli.mvp_video_record.Model;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.support.annotation.NonNull;

import com.example.clareli.mvp_video_record.Presenter.PresenterCameraCallback;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CameraClass implements ICamera {
    private CameraDevice _cameraDevice;
    private CameraCaptureSession _previewSession;
    private MediaRecorder _mediaRecorder;
    private CaptureRequest.Builder _previewBuilder;

    private HandlerThread _backgroundThread;
    private Handler _backgroundHandler;
    private Semaphore _cameraOpenCloseLock = new Semaphore(1);
    private final WeakReference<Activity> _messageViewReference;

    private int _textureViewWidth = 1920;
    private int _textureViewHeight = 1080;
    private PresenterCameraCallback _cameraCallback;
    private SurfaceTexture _previewSurfaceTexture;


    public CameraClass(Activity activity, PresenterCameraCallback cameraCallback) {
        _messageViewReference = new WeakReference<>(activity);
        _cameraCallback = cameraCallback;

    }


    @Override
    public void startPreview(Surface previewSurface) {
        if (null == _cameraDevice) {
            return;
        }

        try {
            closePreviewSession();
            assert _previewSurfaceTexture != null;
            _previewSurfaceTexture.setDefaultBufferSize(_textureViewWidth, _textureViewHeight);
            _previewBuilder = _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            _previewBuilder.addTarget(previewSurface);

            _cameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    _previewSession = session;
                    try {
                        // Auto focus should be continuous for camera preview.
                        _previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                        // Finally, we start displaying the camera preview.
                        _previewSession.setRepeatingRequest(_previewBuilder.build(),
                                null, _backgroundHandler);
                    } catch (CameraAccessException e) {
                        _cameraCallback.errorPreview();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, _backgroundHandler);

        } catch (CameraAccessException e) {
            _cameraCallback.errorPreview();
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void openCamera(int width, int height, String cameraId, CameraManager manager, SurfaceTexture surfaceTexture) {
        if ((null == _messageViewReference.get()) || (cameraId == null)) {
            return;
        }

        try {
            _previewSurfaceTexture = surfaceTexture;
            manager.openCamera(cameraId, mStateCallback, null);
        }  catch (CameraAccessException e) {
            e.printStackTrace();
            _cameraCallback.errorPreview();
        }


    }



    @Override
    public void closeCamera() {
        if (_cameraDevice == null)
            return;

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
            if (null != _mediaRecorder) {
                _mediaRecorder.release();
                _mediaRecorder = null;
            }
        } catch (InterruptedException e) {
            _cameraCallback.errorPreview();
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            _cameraOpenCloseLock.release();
        }

    }

    @Override
    public void startBackgroundThread() {
        _backgroundThread = new HandlerThread("CameraBackground");
        _backgroundThread.start();
        _backgroundHandler = new Handler(_backgroundThread.getLooper());
    }

    @Override
    public void stopBackgroundThread() {
        if (_backgroundThread != null) {
            _backgroundThread.quitSafely();
            try {
                _backgroundThread.join();
                _backgroundThread = null;
                _backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void createCaptureSession(final Surface previewSurface, final Surface recordSurface, String filePath) {
        try {

            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(recordSurface);
            surfaces.add(previewSurface);

            _cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    _previewSession = session;
                    try {
                        CaptureRequest.Builder builder = _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        builder.addTarget(previewSurface);
                        builder.addTarget(recordSurface);
                        _previewSession.setRepeatingRequest(builder.build(), null, _backgroundHandler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    _cameraCallback.errorPreview();
                }
            }, _backgroundHandler);

        } catch (CameraAccessException e) {
            _cameraCallback.errorPreview();
            e.printStackTrace();
        }
    }

    @Override
    public void closePreviewSession() {
        if (_previewSession != null) {
            _previewSession.close();
            _previewSession = null;
        }
    }

    @Override
    public boolean tryToGetAcquire() {
        try {
            if (!_cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            return true;
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            _cameraDevice = camera;
            _cameraCallback.getCameraDevice(_cameraDevice);
            Surface previewSurface = new Surface(_previewSurfaceTexture);
            startPreview(previewSurface);
            _cameraOpenCloseLock.release();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            _cameraOpenCloseLock.release();
            camera.close();
            _cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            _cameraOpenCloseLock.release();
            camera.close();
            _cameraDevice = null;

            Activity activity = _messageViewReference.get();
            if (null != activity) {
                _cameraCallback.errorPreview();
            }
        }
    };


}
