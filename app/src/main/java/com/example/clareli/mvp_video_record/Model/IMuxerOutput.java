package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.nio.ByteBuffer;

public interface IMuxerOutput {
    void writeSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo info);
    void stopMuxer();
    MediaMuxer getMuxer();
    void resetMuxerInfo(MediaFormat mediaFormat);
    int getTrackIndex();
}
