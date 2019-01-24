package com.example.clareli.mvp_video_record.Model;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


import com.example.clareli.mvp_video_record.Presenter.LUPresenterCallback;

import java.util.concurrent.atomic.AtomicBoolean;

public class LURecordedAudio implements LUIRecordedAudio {
    private String TAG = "LURecordedAudio";
    private AudioRecord _audioRecord;
    private LUPresenterCallback _presenterCallback;
    private int bufferSize = 10240;
//    private Thread _recordThread;
    private long _presentationTimeStamp;
    private boolean eos;
    private boolean _isRecording;


    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
//    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

    public LURecordedAudio(LUPresenterCallback callback) {
        _presenterCallback = callback;
    }

    @Override
    public AudioRecord initRecord(int audioFrequency, int channelConfig, int encodingBit, int audio_buffer_times) {
        bufferSize = AudioRecord.getMinBufferSize(audioFrequency,
                channelConfig, encodingBit) * audio_buffer_times;
        _audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, audioFrequency,
                channelConfig, encodingBit, bufferSize);
        if (_audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            _presenterCallback.getAudioRecordErrorMsg("Bad arguments to AudioRecord");
            return null;
        } else {
            return _audioRecord;
        }

    }

    @Override
    public void startRecord() {
        _isRecording = true;
        _audioRecord.startRecording();
//        recordingInProgress.set(true);
//        _recordThread = new Thread(new RecordingRunnable(), TAG);
//        _recordThread.start();

    }

    @Override
    public void getRecordData() {
        byte[] byteArray = new byte[bufferSize];
        _presentationTimeStamp = System.nanoTime() / 1000;
        eos = _audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED;
        _audioRecord.read(byteArray, 0, bufferSize);
        _presenterCallback.accessAudioRecordBuffer(byteArray, _presentationTimeStamp, eos);
    }


    @Override
    public void stopRecord() {
        _isRecording = false;
        if (_audioRecord != null) {
//            recordingInProgress.set(false);
            _audioRecord.stop();
            _audioRecord.release();
            _audioRecord = null;
        } else {
            _presenterCallback.getAudioRecordErrorMsg("stop Audio Record error!");

        }
//        _recordThread = null;
    }

    private class RecordingRunnable implements Runnable {

        @Override
        public void run() {
            byte[] byteArray = new byte[bufferSize];
            Log.i(TAG, "AudioRecord read:" + byteArray.length);
//            while (recordingInProgress.get()) {
            while (_isRecording){
                int result = _audioRecord.read(byteArray, 0, bufferSize);
//                if (result < 0) {
//                    _presenterCallback.getAudioRecordErrorMsg(getBufferReadFailureReason(result));
//                }

                _presentationTimeStamp = System.nanoTime() / 1000;
                eos = _audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED;
                _audioRecord.read(byteArray, 0, bufferSize);
                _presenterCallback.accessAudioRecordBuffer(byteArray, _presentationTimeStamp, eos);
            }
            byteArray = null;
        }

        private String getBufferReadFailureReason(int errorCode) {
            switch (errorCode) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    return "ERROR_INVALID_OPERATION";
                case AudioRecord.ERROR_BAD_VALUE:
                    return "ERROR_BAD_VALUE";
                case AudioRecord.ERROR_DEAD_OBJECT:
                    return "ERROR_DEAD_OBJECT";
                case AudioRecord.ERROR:
                    return "ERROR";
                default:
                    return "Unknown (" + errorCode + ")";
            }
        }

    }


}
