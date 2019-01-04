package com.example.clareli.mvp_video_record.Model;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.support.annotation.NonNull;

import com.example.clareli.mvp_video_record.Presenter.IPresenterVideoPreviewRecord;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CameraClass implements ICamera {
    private CameraDevice mCameraDevice  = null;
    private CameraCaptureSession mPreviewSession = null;
//    private Size mPreviewSize = null;
//    private Size mVideoSize = null;
    private MediaRecorder mMediaRecorder = null;
    private CaptureRequest.Builder mPreviewBuilder = null;

    private HandlerThread mBackgroundThread = null;
    private Handler mBackgroundHandler = null;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private final WeakReference<Activity> _messageViewReference;
    private Integer mSensorOrientation = 0;

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private int textureViewWidth = 1920;
    private int textureViewHeight = 1080;
    private AutoFitTextureView _textureView = null;
    private String _videoFilePath = null;
    private IPresenterVideoPreviewRecord _presenterVideo = null;
    private int _rotation;
    private Object _systemService;
    private int _orientation;

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    public CameraClass(Activity activity, IPresenterVideoPreviewRecord presenterVideoPreviewRecord){
        _messageViewReference = new WeakReference<>(activity);
        _presenterVideo = presenterVideoPreviewRecord;

    }



    public void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    @Override
    public void setAudioSource(int micSource) {
        mMediaRecorder.setAudioSource(micSource);
    }

    @Override
    public void setVideoSource(int surface) {
        mMediaRecorder.setVideoSource(surface);

    }

    @Override
    public void setVideoOutputFormat(int outputFormat) {
        mMediaRecorder.setOutputFormat(outputFormat);
    }

    @Override
    public void setVideoOutputFile(String outputPath) {
        mMediaRecorder.setOutputFile(outputPath);
    }

    @Override
    public void setVideoEncodingBitRate(int rate) {
        mMediaRecorder.setVideoEncodingBitRate(rate);
    }

    @Override
    public void setVideoFrameRate(int rate) {
        mMediaRecorder.setVideoFrameRate(rate);

    }

    @Override
    public void setVideoSize(int width, int height) {
        mMediaRecorder.setVideoSize(width, height);
    }

    @Override
    public void setSurfaceTextureSize(int width, int height) {
        textureViewWidth = width;
        textureViewHeight = height;
    }

    @Override
    public void setVideoEncoder(int videoEncoder) {
        mMediaRecorder.setVideoEncoder(videoEncoder);

    }

    @Override
    public void setAudioEncoder(int audioEncoder) {
        mMediaRecorder.setAudioEncoder(audioEncoder);

    }

    //setup Video settings before recording
    @Override
    public void setupVideoRecord() {
        if(_messageViewReference.get() == null)
            return;

        setAudioSource(MediaRecorder.AudioSource.MIC);
        setVideoSource(MediaRecorder.VideoSource.SURFACE);
        setVideoOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        setVideoOutputFile(_videoFilePath);
        setVideoEncodingBitRate(10000000);
        setVideoFrameRate(30);
        setVideoSize(textureViewWidth, textureViewHeight);
//        setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(_rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(_rotation));
                break;
        }
        try {
            mMediaRecorder.prepare();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void startPreview() {
        if (null == mCameraDevice || !_textureView.isAvailable() /*|| null == mPreviewSize*/) {
            return;
        }
        closePreviewSession();
        SurfaceTexture texture = _textureView.getSurfaceTexture();
        assert texture != null;
        texture.setDefaultBufferSize(textureViewWidth, textureViewHeight);
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            },mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startRecordingVideo(String filePath) {
        if (null == mCameraDevice || !_textureView.isAvailable()) {
            return;
        }
        _videoFilePath = filePath;
        closePreviewSession();
        setupVideoRecord();
        SurfaceTexture texture = _textureView.getSurfaceTexture();
        assert texture != null;
        texture.setDefaultBufferSize(textureViewWidth, textureViewHeight);
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                   Thread thread = new Thread(){
                       @Override
                       public void run() {
                           // Start recording
                           mMediaRecorder.start();

                       }
                   };
                    thread.start();

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void stopRecordingVideo() {

        if(mMediaRecorder != null ) {
            // Stop recording

            mMediaRecorder.stop();
            mMediaRecorder.reset();

            _videoFilePath = null;
        }

        startPreview();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void openCamera(int width, int height) {
        if (null == _messageViewReference.get()) {
            return;
        }

        CameraManager manager = (CameraManager) _systemService;
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {

                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[0];
            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }

            mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, mStateCallback, null);

        } catch (CameraAccessException e) {
            //return Open Error to presenter
            _presenterVideo.cameraOpenError();
            e.printStackTrace();

        }catch (NullPointerException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");

        }

    }

    @Override
    public void closeCamera() {
        if(mCameraDevice == null)
            return;

        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }

    }

    @Override
    public void sendTextureView(AutoFitTextureView textureView) {
        _textureView = textureView;
    }



    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback(){

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
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
                _presenterVideo.cameraOpenError();
            }
        }
    };


    public void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    @Override
    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    public void stopBackgroundThread() {
        if(mBackgroundThread != null) {
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
    public void setInitSetting(Object systemService, int rotation, int orientation) {
        _rotation = rotation;
        _systemService = systemService;
        _orientation = orientation;
    }


}
