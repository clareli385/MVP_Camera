package com.example.clareli.mvp_video_record.Util;

import android.media.MediaCodecInfo;

public class LUAudioCodecProfile {
    String _encodecName;
    String _encodedAudioType;
    int _sampleRate;
    int _channelCount;
    int _bitRate;
    int _maxInputSize;
    int _profileLevel;

    public LUAudioCodecProfile(String encodecName, String encodedAudioType, int sampleRate, int channelCount, int bitRate,
                               int profileLevel, int maxInputSize){
        _encodecName = encodecName;
        _encodedAudioType = encodedAudioType;
        _sampleRate = sampleRate;
        _channelCount = channelCount;
        _bitRate = bitRate;
        _profileLevel = profileLevel;
        _maxInputSize = maxInputSize;

    }

    public String getEncodedAudioType(){
        return _encodedAudioType;
    }

    public int getSampleRate(){
        return _sampleRate;
    }

    public int getChannelCount(){
        return _channelCount;
    }

    public int getBitRate(){
        return _bitRate;
    }

    public int getProfileLevel(){
        return _profileLevel;
    }

    public int getMaxInputSize(){
        return _maxInputSize;
    }


    @Override
    public String toString() {
        return "LUAudioCodecProfile{" +
                "_encodecName='" + _encodecName + '\'' +
                ", _encodedAudioType='" + _encodedAudioType + '\'' +
                ", _sampleRate=" + _sampleRate +
                ", _channelCount=" + _channelCount +
                ", _bitRate=" + _bitRate +
                ", _maxInputSize=" + _maxInputSize +
                ", _profileLevel=" + _profileLevel +
                '}';
    }
}
