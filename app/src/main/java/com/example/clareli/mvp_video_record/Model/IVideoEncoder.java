package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

public interface IVideoEncoder {
    MediaCodec initCodec();
    Surface getSurface();
    void setCodecCallback();
    void stopRecord();
}
