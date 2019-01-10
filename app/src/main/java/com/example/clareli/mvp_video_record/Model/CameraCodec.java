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
//    private BufferedOutputStream outputStream;
    private MediaCodec _mCodec = null;
    private Surface _encoderSurface;
    private int _textureViewWidth = 1920;
    private int _textureViewHeight = 1080;
    private boolean _isEncode = false;
    private ByteBuffer _inputByteBuffer;
    private PresenterCameraCallback _cameraCallback = null;

    public CameraCodec(PresenterCameraCallback cameraCallback) {
        _cameraCallback = cameraCallback;
    }
    private MediaMuxer _muxer;
    private MediaFormat _mediaFormat = null;
    private int _videoTrackIndex = 0;

    @Override
    public Surface initCodec() {

        try {
            _mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            Log.i(TAG, "_mCodec initialized");

        } catch (IOException e) {
            e.printStackTrace();
        }

        _mCodec.configure(createMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        _encoderSurface = _mCodec.createInputSurface();
        if(_mCodec != null) {
            _mCodec.setCallback(new EncoderCallback());
            _mCodec.start();
        }
        return _encoderSurface;
    }

    @Override
    public MediaFormat createMediaFormat() {
        _mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, _textureViewWidth, _textureViewHeight);
        _mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 500000);//500kbps
        _mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        _mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface); //COLOR_FormatSurface
        _mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        return _mediaFormat;
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




    private class EncoderCallback extends MediaCodec.Callback {
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {
            _inputByteBuffer = _mCodec.getInputBuffer(index);
            Log.i("CLE", "onInputBufferAvailable");
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
            ByteBuffer encodedData = _mCodec.getOutputBuffer(index);
            _videoTrackIndex = _muxer.addTrack(_mediaFormat);

            if(info != null) {
                encodedData.position(info.offset);
                encodedData.limit(info.offset + info.size);
                _muxer.start();
                //save output buffer by _muxer
                Log.i("CLE", "onOutputBufferAvailable");

                if(_isEncode == true)
                    _muxer.writeSampleData(_videoTrackIndex, encodedData, info);
            }

            _mCodec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {
            Log.d(TAG, "Error: " + e);
        }

        @Override
        public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
            Log.d(TAG, "encoder output format changed: " + format);
        }
    }



}
