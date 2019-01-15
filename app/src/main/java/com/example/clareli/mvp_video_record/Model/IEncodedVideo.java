package com.example.clareli.mvp_video_record.Model;

import android.view.Surface;

import com.example.clareli.mvp_video_record.Util.VideoCodecProfile;

public interface IEncodedVideo {
    void configuredVideoCodec(VideoCodecProfile videoCodec);
    Surface getSurface();
    void startEncode();
    void stopEncode();
}
