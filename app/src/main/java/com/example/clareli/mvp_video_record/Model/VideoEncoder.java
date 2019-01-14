package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.example.clareli.mvp_video_record.Presenter.PresenterCameraCallback;


import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoEncoder implements IVideoEncoder {
    private String TAG = "VideoEncoder";
//    private MediaCodec _mCodec;

    private PresenterCameraCallback _presenterCallback;

    private MediaFormat _videoFormat;
    private MediaCodec _videoCodec;
    private Surface recordSurface;

    public VideoEncoder(PresenterCameraCallback cameraCallback) {
        _presenterCallback = cameraCallback;
    }

    @Override
    public MediaCodec initCodec() {

        try { // video/avc is H.264 encode
            _videoCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }

        _videoFormat = MediaFormat.createVideoFormat("video/avc", 1920, 1080);


        int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        int videoBitrate = 8880000;//90000;
        int videoFramePerSecond = 30;   //FPS
        int iframeInterval = 2;
        _videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        _videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
        _videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoFramePerSecond);
        _videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);

        _videoCodec.configure(_videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        recordSurface = _videoCodec.createInputSurface();

        return _videoCodec;
    }

    @Override
    public Surface getSurface() {
        return recordSurface;
    }

    @Override
    public void setCodecCallback() {
        _videoCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {

            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                ByteBuffer buffer = codec.getOutputBuffer(index);
                _presenterCallback.getOutputBufferAvailable(info, buffer);

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
        _videoCodec.start();

    }


    @Override
    public void stopRecord() {
        try {
            _videoCodec.stop();
            _videoCodec.release();
            _videoCodec = null;
        } catch (Exception e) {
            e.printStackTrace();
            _videoCodec = null;
        }
    }

}
