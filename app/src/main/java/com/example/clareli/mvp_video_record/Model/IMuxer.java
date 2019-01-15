package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.nio.ByteBuffer;

public interface IMuxer {
    boolean writeSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo info);
    boolean stopMuxer();
}
