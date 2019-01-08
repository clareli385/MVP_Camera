package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import com.example.clareli.mvp_video_record.Presenter.PresenterCameraCallback;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraCodec implements ICameraCodec {
    private String TAG = "CameraCodec";
    private BufferedOutputStream outputStream;
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



    //inside onSurfaceTextureAvailable
    @Override
    public void startCodec(String filePath) {
        File outputFile = new File(filePath);

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
            Log.i(TAG, "outputStream initialized");
        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.i(TAG, "_mCodec setCallback");
        isEncode = true;
        if(_mCodec != null) {
            _mCodec.setCallback(new EncoderCallback());
            _mCodec.start();
        }
    }

    //onSurfaceTextureDestroyed
    @Override
    public void stopCodec() {
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

    @Override
    public MediaCodec getEncoder() {
        return null;
    }

    @Override
    public MediaFormat createMediaFormat() {
        return null;
    }

    @Override
    public Surface initCodec() {

        try {
            _mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            Log.i(TAG, "_mCodec initialized");

        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                textureViewWidth, textureViewHeight);

        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 500000);//500kbps
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface); //COLOR_FormatSurface
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        _mCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoderSurface = _mCodec.createInputSurface();
        return mEncoderSurface;
    }

    private class EncoderCallback extends MediaCodec.Callback {
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {
            inputByteBuffer = _mCodec.getInputBuffer(index);
            Log.i("CLE", "EncoderCallback__input ByteBuffer");
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
            ByteBuffer outPutByteBuffer = _mCodec.getOutputBuffer(index);
            byte[] outDate = new byte[info.size];
            outPutByteBuffer.get(outDate);
            Log.d(TAG, " outDate.length : " + outDate.length);
            try {
                outputStream.write(outDate, 0, outDate.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
