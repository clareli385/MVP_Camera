package com.example.clareli.mvp_video_record.Util;

public class LUAudioCodecProfile {
    String _encodedAudioType;
    int _sampleRate;
    int _channelCount;
    int _bitRate;
    int _profileType;
    int _maxInputSize;

    public LUAudioCodecProfile(String encodedAudioType, int sampleRate, int channelCount, int bitRate, int profileType, int maxInputSize){
        _encodedAudioType = encodedAudioType;
        _sampleRate = sampleRate;
        _channelCount = channelCount;
        _bitRate = bitRate;
        _profileType = profileType;
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

    public int getProfileType(){
        return _profileType;
    }

    public int getMaxInputSize(){
        return _maxInputSize;
    }

    @Override
    public String toString() {
        return "LUAudioCodecProfile{" +
                "_encodedAudioType='" + _encodedAudioType + '\'' +
                ", _sampleRate=" + _sampleRate +
                ", _channelCount=" + _channelCount +
                ", _bitRate=" + _bitRate +
                ", _profileType=" + _profileType +
                ", _maxInputSize=" + _maxInputSize +
                '}';
    }
}
