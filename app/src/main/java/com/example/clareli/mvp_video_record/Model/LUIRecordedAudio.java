package com.example.clareli.mvp_video_record.Model;

import android.media.AudioRecord;

public interface LUIRecordedAudio {
    AudioRecord initRecord(int audioFrequency, int channelConfig, int encodingBit, int audio_buffer_times);
    void startRecord();
    void stopRecord();
    void getRecordData();
}
