package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public class LUPresenterCameraCallback {
    private IPresenterCallback _cameraCallback;
    public LUPresenterCameraCallback(IPresenterCallback callback){
        _cameraCallback = callback;
    }
    public void getCameraDevice(CameraDevice cameraDevice){
        _cameraCallback.getCameraDevice(cameraDevice);
    }

    public void errorPreview(String msg){
        _cameraCallback.errorCameraCallback(msg);
    }


    public void errorFromDecoder(){
        _cameraCallback.errorDecoderCallback();
    }


    public void getVideoOutputFormatChanged(MediaFormat format){
        _cameraCallback.onVideoOutputFormatChanged(format);
    }

    public void getVideoOutputBufferAvailable(MediaCodec.BufferInfo info, ByteBuffer encodedData){
        _cameraCallback.onVideoOutputBufferAvailable(info, encodedData);
    }

    public void getMuxerErrorMsg(String msg){
        _cameraCallback.muxerErrorCallback(msg);
    }

    public void getEncodedErrorMsg(String msg){
        _cameraCallback.encodedErrorCallback(msg);
    }

}
