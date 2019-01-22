package com.example.clareli.mvp_video_record.Model;

import android.media.AudioRecord;

public interface IRecordedAudio {
    AudioRecord initRecord(int audioFrequency, int channelConfig, int encodingBit);
    void startRecord();
    void stopRecord();
}
