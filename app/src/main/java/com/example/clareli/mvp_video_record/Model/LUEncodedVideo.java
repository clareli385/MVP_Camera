package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.example.clareli.mvp_video_record.Presenter.LUPresenterCallback;
import com.example.clareli.mvp_video_record.Util.LUVideoCodecProfile;


import java.io.IOException;
import java.nio.ByteBuffer;

public class LUEncodedVideo implements IEncodedVideo {
    private String TAG = "LUEncodedVideo";

    private LUPresenterCallback _presenterCallback;

    private MediaFormat _videoFormat;
    private MediaCodec _videoEncoder;
    private Surface _recordSurface;


    public LUEncodedVideo(LUPresenterCallback cameraCallback) {
        _presenterCallback = cameraCallback;
    }

    @Override
    public void configuredVideoCodec(LUVideoCodecProfile videoCodec) {
        try { // video/avc is H.264 encode
            _videoEncoder = MediaCodec.createEncoderByType(videoCodec.getEncodedVideoType());
            _videoFormat = MediaFormat.createVideoFormat(videoCodec.getEncodedVideoType(), videoCodec.getWidth(), videoCodec.getHeight());
            _videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, videoCodec.getColorFormat());
            _videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoCodec.getVideoBitrate());
            _videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoCodec.getVideoFramePerSecond());
            _videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoCodec.getIFrameInterval());

            _videoEncoder.configure(_videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            _recordSurface = _videoEncoder.createInputSurface();
            setCodecCallback();

        } catch (IOException e) {
            _presenterCallback.getVideoEncodedErrorMsg("configured Video Codec error!");
            e.printStackTrace();
        }

    }

    @Override
    public Surface getSurface() {
        return _recordSurface;
    }

    /*2019-01-20, clare
    _cameraCodec setCodecCallback will be called by android system and feedback result to
    onVideoOutputFormatChanged() and onVideoOutputBufferAvailable()
     */
    private void setCodecCallback() {
        _videoEncoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {

            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                ByteBuffer buffer = codec.getOutputBuffer(index);
                if(buffer != null)
                    _presenterCallback.getVideoOutputBufferAvailable(info, buffer);

                codec.releaseOutputBuffer(index, false);

            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                _presenterCallback.getVideoEncodedErrorMsg("Set Codec Callback error!");

            }

            @Override
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                _presenterCallback.getVideoOutputFormatChanged(format);
            }
        });

    }

    @Override
    public void startEncode() {
        if (_videoEncoder != null) {
            _videoEncoder.start();
        } else {
            _presenterCallback.getVideoEncodedErrorMsg("Start Encoded error!");
        }
    }


    @Override
    public void stopEncode() {
        try {
            _videoEncoder.stop();
            _videoEncoder.release();
            _videoEncoder = null;
        } catch (Exception e) {
            _videoEncoder = null;
            _presenterCallback.getVideoEncodedErrorMsg("Stop Encoded error!");
            e.printStackTrace();
        }
    }

}
