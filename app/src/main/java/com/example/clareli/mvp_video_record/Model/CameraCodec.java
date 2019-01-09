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
    private Surface mEncoderSurface;
    private int textureViewWidth = 1920;
    private int textureViewHeight = 1080;
    private boolean isEncode = false;
    private ByteBuffer inputByteBuffer;
    private PresenterCameraCallback _cameraCallback = null;

    public CameraCodec(PresenterCameraCallback cameraCallback) {
        _cameraCallback = cameraCallback;
    }
    private MediaMuxer mMuxer;
    private MediaFormat _mediaFormat = null;
    private int videoTrackIndex = 0;

    @Override
    public Surface initCodec() {

        try {
            _mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            Log.i(TAG, "_mCodec initialized");

        } catch (IOException e) {
            e.printStackTrace();
        }

        _mCodec.configure(createMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoderSurface = _mCodec.createInputSurface();
        if(_mCodec != null) {
            _mCodec.setCallback(new EncoderCallback());
            _mCodec.start();
        }
        return mEncoderSurface;
    }

    @Override
    public MediaFormat createMediaFormat() {
        _mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, textureViewWidth, textureViewHeight);
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
            mMuxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            Log.i(TAG, "_mCodec setCallback");
            isEncode = true;


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //onSurfaceTextureDestroyed
    @Override
    public void stopRecord() {
        try {
            if (isEncode) {
                isEncode = false;
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
            inputByteBuffer = _mCodec.getInputBuffer(index);
            Log.i("CLE", "onInputBufferAvailable");
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
            ByteBuffer encodedData = _mCodec.getOutputBuffer(index);
            videoTrackIndex = mMuxer.addTrack(_mediaFormat);

            if(info != null) {
                encodedData.position(info.offset);
                encodedData.limit(info.offset + info.size);
                mMuxer.start();
                //save output buffer by mMuxer
                Log.i("CLE", "onOutputBufferAvailable");

                if(isEncode == true)
                    mMuxer.writeSampleData(videoTrackIndex, encodedData, info);
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
