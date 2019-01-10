package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.example.clareli.mvp_video_record.Presenter.PresenterCameraCallback;


import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraCodec implements ICameraCodec {
    private String TAG = "CameraCodec";
    private MediaCodec _mCodec = null;

    private boolean _isEncode = false;
    private PresenterCameraCallback _cameraCallback = null;

    private MediaMuxer _muxer;
    private MediaFormat _mediaFormat = null;
    private int _videoTrackIndex = 0;
    private MediaCodec _mediaCodec = null;
    private Surface recordSurface = null;

    public CameraCodec(PresenterCameraCallback cameraCallback) {
        _cameraCallback = cameraCallback;
    }

    @Override
    public MediaCodec initCodec() {

        try {
            _mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }

        _mediaFormat = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
        int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        int videoBitrate = 90000;
        int videoFramePerSecond = 25;
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
    public MediaFormat getMediaFormat() {
        return _mediaFormat;
    }

    @Override
    public void setCodecCallback(final IMuxerOutput muxerOutput) {
        _mediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                Log.d("samson", "input");

            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                Log.d("samson", "output" + String.valueOf(info.size));
                ByteBuffer buffer = codec.getOutputBuffer(index);
                Log.d("samson", "size = " + String.valueOf(info.size));
                muxerOutput.writeSampleData(buffer, info);
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

    }

    @Override
    public Surface getSurface() {
        return recordSurface;
    }

    @Override
    public MediaCodec getEncoder() {
        return _mCodec;
    }

    @Override
    public void record(String dstPath) {
        try {
            _muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            Log.i(TAG, "_mCodec setCallback");
            _isEncode = true;


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //onSurfaceTextureDestroyed
    @Override
    public void stopRecord() {
        try {
            if (_isEncode) {
                _isEncode = false;
            } else {
                _mCodec.stop();
                _mCodec.release();
                _mCodec = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            _mCodec = null;
        }
    }

}
