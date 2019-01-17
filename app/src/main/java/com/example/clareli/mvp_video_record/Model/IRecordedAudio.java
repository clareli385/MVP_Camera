package com.example.clareli.mvp_video_record.Model;

public interface IRecordedAudio {
    void startRecord(int audioFrequency, int channelConfig, int encodingBit);
    void stopRecord();
    void setCallback(byte[] rowData);
}
