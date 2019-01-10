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
    private CameraDevice _cameraDevice = null;
    private CameraCaptureSession _previewSession = null;
    private MediaRecorder _mediaRecorder = null;
    private CaptureRequest.Builder _previewBuilder = null;

    private HandlerThread _backgroundThread = null;
    private Handler _backgroundHandler = null;
    MediaCodec _mediaCodec = null;

    private Semaphore _cameraOpenCloseLock = new Semaphore(1);
    private final WeakReference<Activity> _messageViewReference;

    private int _textureViewWidth = 1920;
    private int _textureViewHeight = 1080;
    private PresenterCameraCallback _cameraCallback = null;
    private SurfaceTexture _previewSurfaceTexture;


    public CameraClass(Activity activity, PresenterCameraCallback cameraCallback) {
        _messageViewReference = new WeakReference<>(activity);
        _cameraCallback = cameraCallback;

    }


    @Override
    public void setSurfaceTextureSize(int width, int height) {
        _textureViewWidth = width;
        _textureViewHeight = height;
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
    public void closePreviewSession() {
        if (_previewSession != null) {
            _previewSession.close();
            _previewSession = null;
        }
    }

    @Override
    public void createCaptureSession(final Surface previewSurface, Surface recorderSurface) {
        try {
            try {
                _mediaCodec = MediaCodec.createEncoderByType("video/avc");
            } catch (IOException e) {
                e.printStackTrace();
            }

            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
            int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
            int videoBitrate = 90000;
            int videoFramePerSecond = 25;
            int iframeInterval = 2;
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);

            _mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            List<Surface> surfaces = new ArrayList<>();
            final Surface mEncodeSurface = _mediaCodec.createInputSurface();
            surfaces.add(mEncodeSurface);
            surfaces.add(previewSurface);

            _cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    _previewSession = session;
                    try {
                        CaptureRequest.Builder builder = _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        builder.addTarget(previewSurface);
                        builder.addTarget(mEncodeSurface);
                        _previewSession.setRepeatingRequest(builder.build(), null, _backgroundHandler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // todo error
                }
            }, _backgroundHandler);

            _mediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(MediaCodec codec, int index) {
//                    Log.d("samson", "input");
                }

                @Override
                public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
//                    Log.d("samson", "output" + String.valueOf(info.size));
                    ByteBuffer buffer = codec.getOutputBuffer(index);
//                    Log.d("samson", "size = " + String.valueOf(info.size));
                    codec.releaseOutputBuffer(index, false);
                }

                @Override
                public void onError(MediaCodec codec, MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {

                }
            });
            _mediaCodec.start();

        } catch (CameraAccessException e) {
            _cameraCallback.errorPreview();
            e.printStackTrace();
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

}
