package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.nio.ByteBuffer;

public interface IMuxer {
    void setMuxerMediaFormat(MediaFormat mediaFormat);
    boolean writeSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo info);
    void startMuxer();
    boolean stopMuxer();
}
