package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface IPresenterCallback {
    void getCameraDevice(CameraDevice mCameraDevice);
    void errorCameraCallback(String msg);
    void onVideoOutputFormatChanged(MediaFormat format);
    void onVideoOutputBufferAvailable(final MediaCodec.BufferInfo info, final ByteBuffer encodedData);
    void muxerErrorCallback(String msg);
    void encodedVideoErrorCallback(String msg);
    void encodedAudioErrorCallback(String msg);
    void recordedAudioErrorCallback(String msg);
    void notifyToAccessBuffer(byte[] rowData, long presentationTimeStamp, boolean eos);
    void onAudioOutputFormatChanged(MediaFormat format);
    void onAudioOutputBufferAvailable(final MediaCodec.BufferInfo info, final ByteBuffer encodedData);
    void onAudioInputBufferAvailable(ByteBuffer inputBuffer, int index);
    void getVideoEncodeResult(MediaCodecInfo[] infos);
    void getAudioEncodeResult(MediaCodecInfo[] infos);

}
