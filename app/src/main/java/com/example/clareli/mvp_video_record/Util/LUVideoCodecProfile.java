package com.example.clareli.mvp_video_record.Util;

import android.media.MediaCodecInfo;

public class LUVideoCodecProfile {
    String _encodedVideoType;
    int _colorFormat;
    int _videoBitrate;
    int _videoFramePerSecond;//FPS
    int _iFrameInterval;
    int _width;
    int _height;
    public LUVideoCodecProfile(String encodedVideoType, int colorFormat, int videoBitrate, int videoFramePerSecond, int iFrameInterval, int width, int height){
        _encodedVideoType = encodedVideoType;
        _colorFormat = colorFormat;
        _videoBitrate = videoBitrate;
        _videoFramePerSecond = videoFramePerSecond;
        _iFrameInterval = iFrameInterval;
        _width = width;
        _height = height;

    }
    public String getEncodedVideoType(){
        return _encodedVideoType;
    }

    public int getColorFormat(){
        return _colorFormat;
    }

    public int getVideoBitrate(){
        return _videoBitrate;
    }

    public int getVideoFramePerSecond(){
        return _videoFramePerSecond;
    }

    public int getIFrameInterval(){
        return _iFrameInterval;
    }

    public int getWidth(){
        return _width;
    }

    public int getHeight(){
        return _height;
    }

    @Override
    public String toString() {
        return "LUVideoCodecProfile{" +
                "_encodedVideoType='" + _encodedVideoType + '\'' +
                ", _colorFormat=" + _colorFormat +
                ", _videoBitrate=" + _videoBitrate +
                ", _videoFramePerSecond=" + _videoFramePerSecond +
                ", _iFrameInterval=" + _iFrameInterval +
                ", _width=" + _width +
                ", _height=" + _height +
                '}';
    }
}
