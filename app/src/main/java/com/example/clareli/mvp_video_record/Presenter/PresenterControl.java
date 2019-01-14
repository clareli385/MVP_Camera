package com.example.clareli.mvp_video_record.Presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.example.clareli.mvp_video_record.MainActivity;
import com.example.clareli.mvp_video_record.Model.IVideoEncoder;
import com.example.clareli.mvp_video_record.Model.VideoEncoder;
import com.example.clareli.mvp_video_record.Model.ICamera;
import com.example.clareli.mvp_video_record.Model.CameraClass;
import com.example.clareli.mvp_video_record.Model.IMuxerOutput;
import com.example.clareli.mvp_video_record.Model.MuxerOutput;
import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewErrorCallback;
import com.example.clareli.mvp_video_record.View.ViewErrorCallback;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;


import static com.example.clareli.mvp_video_record.Util.IConstant.REQUEST_PERMISSION_CODE;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_PERMISSIONS;
import static com.example.clareli.mvp_video_record.Util.PermissionCheck.hasPermissionsGranted;
import static com.example.clareli.mvp_video_record.Util.PermissionCheck.requestPermission;

public class PresenterControl implements IPresenterControl, IPresenterCallback {
    private final WeakReference<Activity> _messageViewReference;
    private String TAG = "PresenterControl";
    private ICamera _iCamera;
    private PresenterCameraCallback _presenterCallback;
    private Object _systemService;
    private ViewErrorCallback _viewErrorCallback;
    private CameraDevice _cameraDevice;
    private AutoFitTextureView _textureView;
    private IVideoEncoder _cameraCodec;
    private IMuxerOutput _muxerOutput;
    private SurfaceTexture _previewSurTexture;
    private Surface _previewSurface;
    private String _dstFilePath;


    //constructor
    public PresenterControl(Activity activity, ViewErrorCallback viewErrorCallback) {
        _messageViewReference = new WeakReference<>(activity);
        _presenterCallback = new PresenterCameraCallback(this);
        _iCamera = new CameraClass(_messageViewReference.get(), _presenterCallback);
        _viewErrorCallback = viewErrorCallback;
        _cameraCodec = new VideoEncoder(_presenterCallback);

    }

    /*
   textrure view will be started after mSurfaceTextureListener ->onSurfaceTextureAvailable
   So finally we need to open camera for preview
    */
    @Override
    public void videoPreviewStart(AutoFitTextureView textureView, IViewErrorCallback iViewErrorCallback) {
        _textureView = textureView;
        if (textureView.isAvailable()) {
            if (hasPermissionsGranted(_messageViewReference.get(), VIDEO_PERMISSIONS)) {
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

    /*
    default select index 0 camera
     */
    public String selectCamera() {
        String cameraId = null;
        if (_systemService == null)
            _systemService = _messageViewReference.get().getSystemService(Context.CAMERA_SERVICE);

        CameraManager manager = (CameraManager) _systemService;
        try {
            if (_iCamera.tryToGetAcquire()) {
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

    /*
    start a background to do preview
     */
    @Override
    public void startBackground() {
        _iCamera.startBackgroundThread();
    }


    /*  start to do video record
        1._iCamera.createCaptureSession(...) will pass preview and record surface to builder.addTarget()
        after 2, _cameraCodec setCodecCallback will be called by android system
        2.setup _cameraCodec.setCodecCallback() will feedback result to onOutputFormatChanged() and onOutputBufferAvailable()
     */
    @Override
    public void videoRecordStart(String filePath) {
        if (_cameraDevice != null) {
            _iCamera.closePreviewSession();
            _previewSurTexture = _textureView.getSurfaceTexture();
            assert _previewSurTexture != null;
            _previewSurTexture.setDefaultBufferSize(_textureView.getWidth(), _textureView.getHeight());
            _dstFilePath = filePath;
            _cameraCodec.initCodec();
            _cameraCodec.setCodecCallback();
            _previewSurface = new Surface(_previewSurTexture);
            _iCamera.createCaptureSession(_previewSurface, _cameraCodec.getSurface(), filePath);

        }
    }

    /*
    press stop record button will stop codec, muxer and camera preview.
     */
    @Override
    public void videoRecordStop() {
        _cameraCodec.stopRecord();
        _muxerOutput.stopMuxer();
        _iCamera.startPreview(_previewSurface);
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (hasPermissionsGranted(_messageViewReference.get(), VIDEO_PERMISSIONS)) {
                String cameraId = selectCamera();
                CameraManager manager = (CameraManager) _systemService;
                _iCamera.openCamera(width, height, cameraId, manager, surface);
            } else {
                requestPermission((MainActivity) (_messageViewReference.get()), VIDEO_PERMISSIONS, REQUEST_PERMISSION_CODE);
                return;
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            _cameraCodec.stopRecord();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };



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


    /*from VideoEncoder.java
    prepare for muxer to write data to file
     */
    @Override
    public void onOutputFormatChanged(MediaFormat format) {
        //the format include "csd-0" and "csd-1" byte buffers
        _muxerOutput = new MuxerOutput(_dstFilePath, format);
    }

    /*from VideoEncoder.java
    prepare for muxer to write data to file
     */
    @Override
    public void onOutputBufferAvailable(MediaCodec.BufferInfo info, ByteBuffer encodedData) {
        Log.i(TAG,"info_presentationTimeUs:"+info.presentationTimeUs+", offset:"+info.offset);
        _muxerOutput.writeSampleData(encodedData, info);
    }

}
