package com.example.clareli.mvp_video_record.Util;

import android.media.MediaCodecInfo;

public class LUVideoCodecProfile {
    String _encodedName;
    String _encodedVideoType;
    int _width;
    int _height;
    int _videoFrameRates;//FPS
    int _videoBitrate;
    int _iFrameInterval;
    int _colorFormat;
    MediaCodecInfo.CodecProfileLevel _profileLevel;

    public LUVideoCodecProfile(String encodedName, String encodedVideoType, int colorFormat, int videoBitrate,
                               int videoFramePerSecond, int iFrameInterval, int width, int height, MediaCodecInfo.CodecProfileLevel profileLevel){
        _encodedName = encodedName;
        _encodedVideoType = encodedVideoType;
        _colorFormat = colorFormat;
        _videoBitrate = videoBitrate;
        _videoFrameRates = videoFramePerSecond;
        _iFrameInterval = iFrameInterval;
        _width = width;
        _height = height;
        _profileLevel = profileLevel;

    }

    public String getEncodedName(){
        return _encodedName;
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

    public int getVideoFrameRates(){
        return _videoFrameRates;
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

    public MediaCodecInfo.CodecProfileLevel getProfileLevel(){
        return _profileLevel;
    }


    @Override
    public String toString() {
        return "LUVideoCodecProfile{" +
                "_encodedName='" + _encodedName + '\'' +
                ", _encodedVideoType='" + _encodedVideoType + '\'' +
                ", _width=" + _width +
                ", _height=" + _height +
                ", _videoFrameRates=" + _videoFrameRates +
                ", _videoBitrate=" + _videoBitrate +
                ", _iFrameInterval=" + _iFrameInterval +
                ", _colorFormat=" + _colorFormat +
                ", _profileLevel=" + _profileLevel +
                '}';
    }
}
