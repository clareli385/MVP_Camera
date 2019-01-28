package com.example.clareli.mvp_video_record.Model;

import android.view.Surface;

import com.example.clareli.mvp_video_record.Util.LUVideoCodecProfile;

public interface LUIEncodedVideo {
    void configuredVideoCodec(LUVideoCodecProfile videoCodec);
    Surface getSurface();
    void startEncode();
    void stopEncode();
    void releaseEncode();

}
