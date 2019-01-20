package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface IPresenterCallback {
    void getCameraDevice(CameraDevice mCameraDevice);
    void errorCameraCallback(String msg);
    void onVideoOutputFormatChanged(MediaFormat format);
    void onVideoOutputBufferAvailable(MediaCodec.BufferInfo info, ByteBuffer encodedData);
    void muxerErrorCallback(String msg);
    void encodedVideoErrorCallback(String msg);
    void encodedAudioErrorCallback(String msg);
    void recordedAudioErrorCallback(String msg);
    void notifyToAccessBuffer(byte[] rowData, int readBytes);
    void onAudioOutputFormatChanged(MediaFormat format);
    void onAudioOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info, ByteBuffer encodedData);
    void onAudioInputBufferAvailable(ByteBuffer inputBuffer, int index);

}
