package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.example.clareli.mvp_video_record.Presenter.LUPresenterCallback;
import com.example.clareli.mvp_video_record.Util.LUAudioCodecProfile;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LUEncodedAudio implements IEncodedAudio {
    private String TAG = "LUEncodedAudio";
    private LUPresenterCallback _presenterCallback;
    private MediaFormat _audioFormat;
    private MediaCodec _audioCodec;

    public LUEncodedAudio(LUPresenterCallback callback){
        _presenterCallback = callback;
    }

    @Override
    public void configuredAudioCodec(LUAudioCodecProfile audioCodecProfile) {
        _audioFormat = MediaFormat.createAudioFormat(audioCodecProfile.getEncodedAudioType(),
                audioCodecProfile.getSampleRate(), audioCodecProfile.getChannelCount());
        _audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioCodecProfile.getBitRate());
        _audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, audioCodecProfile.getProfileType());
        _audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, audioCodecProfile.getMaxInputSize());
        try {
            _audioCodec = MediaCodec.createEncoderByType(audioCodecProfile.getEncodedAudioType());
            _audioCodec.configure(_audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            setCodecCallback();

        } catch (IOException e) {
            _presenterCallback.getAudioEncodedErrorMsg("Configured Audio Codec Error!");
            e.printStackTrace();
        }

    }

    @Override
    public void startEncode() {
        if(_audioCodec != null)
            _audioCodec.start();
        else
            _presenterCallback.getAudioEncodedErrorMsg("Start Audio Encode Error!");

    }

    @Override
    public void stopEncode() {
        if(_audioCodec != null){
            _audioCodec.stop();
            _audioCodec.release();
            _audioCodec = null;
        } else
            _presenterCallback.getAudioEncodedErrorMsg("Stop Audio Encode Error!");

    }

    private void setCodecCallback() {
        _audioCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                ByteBuffer inputBuffer = codec.getInputBuffer(index);
                Log.d(TAG,"onInputBufferAvailable__inputBuffer:"+inputBuffer.limit());
                _presenterCallback.getAudioInputBufferAvailable(inputBuffer, index);
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                Log.d(TAG,"onOutputBufferAvailable__outputBuffer:"+outputBuffer.limit());
                _presenterCallback.getAudioOutputBufferAvailable(codec, index, info, outputBuffer);
            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                _presenterCallback.getAudioEncodedErrorMsg("Audio Encoder set Callback error!");
            }

            @Override
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                Log.d(TAG,"onOutputFormatChanged<<<<<<");

                _presenterCallback.getAudioOutputFormatChanged(format);
            }
        });

    }

    @Override
    public MediaCodec getCodec() {
        return _audioCodec;
    }

    @Override
    public MediaFormat getFormat() {
        return _audioFormat;
    }

    @Override
    public void queueInputBuffer(int bufferIndex, int sz, long ts) {
        _audioCodec.queueInputBuffer(bufferIndex, 0, sz, ts, 0);
    }

}
