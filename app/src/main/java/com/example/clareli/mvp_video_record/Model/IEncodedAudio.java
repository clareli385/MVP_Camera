package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.example.clareli.mvp_video_record.Util.LUAudioCodecProfile;

public interface IEncodedAudio {
    void configuredAudioCodec(LUAudioCodecProfile audioCodecProfile);
    void startEncode();
    void stopEncode();
    MediaCodec getCodec();
    MediaFormat getFormat();
    void queueInputBuffer(int bufferIndex, int sz, long ts);
}
