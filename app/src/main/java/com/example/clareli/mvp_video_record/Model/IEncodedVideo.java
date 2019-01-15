package com.example.clareli.mvp_video_record.Model;

import android.view.Surface;

public interface IEncodedVideo {
    void configuredVideoCodec(String encodedVideoType, int colorFormat, int videoBitrate, int videoFramePerSecond, int iFrameInterval, int width, int height);
    Surface getSurface();
    void startEncode();
    void stopEncode();
}
