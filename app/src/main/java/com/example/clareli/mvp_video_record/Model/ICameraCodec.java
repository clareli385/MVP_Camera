package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

public interface ICameraCodec {
    void stopRecord();
    MediaCodec getEncoder();
    MediaFormat createMediaFormat();
    Surface initCodec();
    void record(String dstPath);
}
