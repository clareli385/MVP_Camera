package com.example.clareli.mvp_video_record.Model;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.support.annotation.NonNull;

import com.example.clareli.mvp_video_record.Presenter.PresenterCameraCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CameraClass implements ICamera {
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mPreviewSession = null;
    private MediaRecorder mMediaRecorder = null;
    private CaptureRequest.Builder _previewBuilder = null;

    private HandlerThread mBackgroundThread = null;
    private Handler mBackgroundHandler = null;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private final WeakReference<Activity> _messageViewReference;

    private int textureViewWidth = 1920;
    private int textureViewHeight = 1080;
    private PresenterCameraCallback _cameraCallback = null;
    private SurfaceTexture _previewSurfaceTexture;


    public CameraClass(Activity activity, PresenterCameraCallback cameraCallback) {
        _messageViewReference = new WeakReference<>(activity);
        _cameraCallback= cameraCallback;

    }

    //for record and preview
    public void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(_previewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(_previewBuilder.build(), null, mBackgroundHandler);
            _cameraCallback.getCameraDevice(mCameraDevice);
        } catch (CameraAccessException e) {
            _cameraCallback.errorPreview();
            e.printStackTrace();
        }

    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
    }

    @Override
    public void setSurfaceTextureSize(int width, int height) {
        textureViewWidth = width;
        textureViewHeight = height;
    }


    @Override
    public void startPreview(Surface previewSurface) {
        if (null == mCameraDevice) {
            return;
        }

        try {
            closePreviewSession();
            assert _previewSurfaceTexture != null;
            _previewSurfaceTexture.setDefaultBufferSize(textureViewWidth, textureViewHeight);
            _previewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            _previewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;
                    try {
                        // Auto focus should be continuous for camera preview.
                        _previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        // Finally, we start displaying the camera preview.
                        mPreviewSession.setRepeatingRequest(_previewBuilder.build(),
                                null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        _cameraCallback.errorPreview();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mBackgroundHandler);

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
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {

                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            _previewSurfaceTexture = surfaceTexture;
            manager.openCamera(cameraId, mStateCallback, null);

        } catch (InterruptedException e) {
            e.printStackTrace();
            _cameraCallback.errorPreview();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            _cameraCallback.errorPreview();
        }


    }

    @Override
    public void closeCamera() {
        if (mCameraDevice == null)
            return;

        try {
            mCameraOpenCloseLock.acquire();
            if (mPreviewSession != null) {
                mPreviewSession.close();
                mPreviewSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            _cameraCallback.errorPreview();
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }

    }


    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            _cameraCallback.getCameraDevice(mCameraDevice);
            Surface previewSurface = new Surface(_previewSurfaceTexture);
            startPreview(previewSurface);
            mCameraOpenCloseLock.release();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;

            Activity activity = _messageViewReference.get();
            if (null != activity) {
                _cameraCallback.errorPreview();
            }
        }
    };

    @Override
    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    public void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    @Override
    public void createCaptureSession(Surface previewSurface , Surface recorderSurface) {
        List<Surface> surfacesList = new ArrayList<>();
        surfacesList.add(previewSurface);
        surfacesList.add(recorderSurface);
        try {
            _previewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            _previewBuilder.addTarget(previewSurface);
            _previewBuilder.addTarget(recorderSurface);
            mCameraDevice.createCaptureSession(surfacesList, new CameraCaptureSession.StateCallback(){

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            _cameraCallback.errorPreview();
            e.printStackTrace();
        }
    }

}
