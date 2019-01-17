package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public class LUPresenterCallback {
    private IPresenterCallback _cameraCallback;
    public LUPresenterCallback(IPresenterCallback callback){
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

    public void getVideoEncodedErrorMsg(String msg){
        _cameraCallback.encodedVideoErrorCallback(msg);
    }

    public void getAudioEncodedErrorMsg(String msg){
        _cameraCallback.encodedAudioErrorCallback(msg);
    }

    public void accessAudioRecordBuffer(byte[] rowData){
        _cameraCallback.notifyToAccessBuffer(rowData);
    }

    public void getAudioOutputFormatChanged(MediaFormat format){
        _cameraCallback.onAudioOutputFormatChanged(format);
    }

    public void getAudioOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info, ByteBuffer encodedData){
        _cameraCallback.onAudioOutputBufferAvailable(codec, index, info, encodedData);
    }

    public void getAudioInputBufferAvailable(ByteBuffer inputBuffer, int index) {
        _cameraCallback.onAudioInputBufferAvailable(inputBuffer, index);
    }

    public void getAudioRecordErrorMsg(String msg){
        _cameraCallback.recordedAudioErrorCallback(msg);
    }

}
