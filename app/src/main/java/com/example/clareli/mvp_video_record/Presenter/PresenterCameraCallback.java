package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;

import java.nio.ByteBuffer;

public class PresenterCameraCallback {
    private IInterfaceCameraCallback _cameraCallback;
    public PresenterCameraCallback(IInterfaceCameraCallback callback){
        _cameraCallback = callback;
    }
    public void completedPreview(){
        _cameraCallback.completedCameraCallback();
    }
    public void errorPreview(){
        _cameraCallback.errorCameraCallback();
    }
    public void completedRecord(){
        _cameraCallback.completedCameraRecordCallback();
    }
    public void errorRecord(){
        _cameraCallback.errorCameraRecordCallback();
    }
    public void completedFromEncoder(){
        _cameraCallback.completedEncoderCallback();
    }
    public void errorFromEncoder(){
        _cameraCallback.errorEncoderCallback();
    }
    public void completedFromDecoder(){
        _cameraCallback.completedDecoderCallback();
    }
    public void errorFromDecoder(){
        _cameraCallback.errorDecoderCallback();
    }
    public void getCameraDevice(CameraDevice cameraDevice){
        _cameraCallback.getCameraDevice(cameraDevice);
    }

//    public void getOutputDataForCodec(){
//        _cameraCallback.startToGetOutputDataForCodec();
//    }
}
