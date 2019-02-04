package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.example.clareli.mvp_video_record.Presenter.LUPresenterCallback;
import com.example.clareli.mvp_video_record.Util.LUAudioCodecProfile;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LUEncodedAudio implements LUIEncodedAudio {
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
        _audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, audioCodecProfile.getProfileLevel());
        _audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, audioCodecProfile.getMaxInputSize());
        try {
            _audioCodec = MediaCodec.createEncoderByType(audioCodecProfile.getEncodedAudioType());
            setCodecCallback();
            _audioCodec.configure(_audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);


        } catch (IOException e) {
            _audioCodec = null;
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

        } else
            _presenterCallback.getAudioEncodedErrorMsg("Stop Audio Encode Error!");

    }

    @Override
    public void releaseEncode() {
        _audioCodec.release();
        _audioCodec = null;
    }

    private void setCodecCallback() {
        _audioCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                ByteBuffer inputBuffer = codec.getInputBuffer(index);
                inputBuffer.clear();
                if(inputBuffer != null) {

                    _presenterCallback.getAudioInputBufferAvailable(inputBuffer, index);
                }
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                if(outputBuffer != null) {

                    _presenterCallback.getAudioOutputBufferAvailable(info, outputBuffer);
                }

                codec.releaseOutputBuffer(index, false);

            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                _presenterCallback.getAudioEncodedErrorMsg("Audio Encoder set Callback error!");
            }

            @Override
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
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
    public void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) {
        _audioCodec.queueInputBuffer(index, offset, size, presentationTimeUs, flags);
    }



}
