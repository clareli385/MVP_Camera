package com.example.clareli.mvp_video_record.Model;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import android.util.Log;

import com.example.clareli.mvp_video_record.Presenter.LUPresenterCallback;

import java.util.concurrent.atomic.AtomicBoolean;

public class LURecordedAudio implements IRecordedAudio {
    private String TAG = "LURecordedAudio";
    private AudioRecord _audioRecord;
    private static final int RECORD_AUDIO_BUFFER_TIMES = 1;
    private Thread _recordThread;
    private LUPresenterCallback _presenterCallback;
    private int bufferSize = 10240;
    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

    public LURecordedAudio(LUPresenterCallback callback){
        _presenterCallback = callback;
    }

    @Override
    public void startRecord(int audioFrequency, int channelConfig, int encodingBit) {
        bufferSize = AudioRecord.getMinBufferSize(audioFrequency,
                channelConfig, encodingBit) * RECORD_AUDIO_BUFFER_TIMES;
        _audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, audioFrequency,
                channelConfig, encodingBit, bufferSize);
        _audioRecord.startRecording();
        recordingInProgress.set(true);
        _recordThread = new Thread(new RecordingRunnable(), "AudioRecordThread");
        _recordThread.start();

    }


    @Override
    public void stopRecord() {

        if(_audioRecord != null) {
            recordingInProgress.set(false);
            _audioRecord.stop();
            _audioRecord.release();
            _audioRecord = null;
            _recordThread = null;
        } else{
            _presenterCallback.getAudioRecordErrorMsg("stop Audio Record error!");

        }

    }


    @Override
    public void setCallback(byte[] rowData) {
        Log.d(TAG, "====row Data size:"+rowData.length);
        _presenterCallback.accessAudioRecordBuffer(rowData);
    }

    private class RecordingRunnable implements Runnable{

        @Override
        public void run() {
            byte[] byteArray = new byte[bufferSize];
            Log.i(TAG, "AudioRecord read:"+byteArray.length);
            while (recordingInProgress.get()){
                int result = _audioRecord.read(byteArray, 0, bufferSize);
                if (result < 0) {
                    _presenterCallback.getAudioRecordErrorMsg(getBufferReadFailureReason(result));
                }
                setCallback(byteArray);
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
