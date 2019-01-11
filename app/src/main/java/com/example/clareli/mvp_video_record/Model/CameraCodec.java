package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.example.clareli.mvp_video_record.Presenter.PresenterCameraCallback;


import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraCodec implements ICameraCodec {
    private String TAG = "CameraCodec";
    private MediaCodec _mCodec;

    private PresenterCameraCallback _presenterCallback;

    private MediaFormat _mediaFormat;
    private MediaCodec _mediaCodec;
    private Surface recordSurface;

    public CameraCodec(PresenterCameraCallback cameraCallback) {
        _presenterCallback = cameraCallback;
    }

    @Override
    public MediaCodec initCodec() {

        try { // video/avc is H.264 encode
            _mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }

        _mediaFormat = MediaFormat.createVideoFormat("video/avc", 1920, 1080);


        int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        int videoBitrate = 8880000;//90000;
        int videoFramePerSecond = 30;   //FPS
        int iframeInterval = 2;
        _mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        _mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
        _mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond);
        _mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);

        _mediaCodec.configure(_mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        recordSurface = _mediaCodec.createInputSurface();

        return _mediaCodec;
    }


    @Override
    public void setCodecCallback() {
        _mediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {

            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                ByteBuffer buffer = codec.getOutputBuffer(index);
//                Log.d(TAG, "size = " + String.valueOf(info.size));
                _presenterCallback.getOutputBufferAvailable(codec, index, info, buffer);

                codec.releaseOutputBuffer(index, false);

            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                _presenterCallback.getOutputFormatChanged(format);
            }
        });
        _mediaCodec.start();

    }

    @Override
    public Surface getSurface() {
        return recordSurface;
    }


    //onSurfaceTextureDestroyed
    @Override
    public void stopRecord() {
        try {
            _mCodec.stop();
            _mCodec.release();
            _mCodec = null;
        } catch (Exception e) {
            e.printStackTrace();
            _mCodec = null;
        }
    }

}
