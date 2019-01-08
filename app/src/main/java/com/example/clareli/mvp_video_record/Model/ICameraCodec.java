package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

public interface ICameraCodec {
    void startCodec(String filePath);
    void stopCodec();
    MediaCodec getEncoder();
    MediaFormat createMediaFormat();
    Surface initCodec();
}
