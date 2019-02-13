package com.example.clareli.mvp_video_record.Util;


import java.util.HashMap;
import java.util.Map;

import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_AUDIO_BIT_RATE;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_AUDIO_CHANNEL_COUNT;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_AUDIO_MAX_INPUT_SIZE;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_AUDIO_SAMPLE_RATE;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_AUDIO_TYPE;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_NAME;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_PROFILE_LEVEL;

/*2019-02-11,Clare
This class is for selected audio codec.
* */
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

    public LUAudioCodecProfile(){

    }

    public void set_encodecName(String encodecName){
        _encodecName = encodecName;
    }

    public void setEncodedAudioType(String encodedAudioType){
        _encodedAudioType = encodedAudioType;
    }

    public void setSampleRate(int sampleRate){
        _sampleRate = sampleRate;
    }

    public void setChannelCount(int channelCount){
        _channelCount = channelCount;
    }

    public void setBitRate(int bitRate){
        _bitRate = bitRate;
    }

    public void setProfileLevel(int profileLevel){
        _profileLevel = profileLevel;
    }

    public void setMaxInputSize(int maxInputSize){
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

    public Map<String, String> audioProfileToMap(){
        Map<String, String> audioSelectedCodec = new HashMap<String, String>();
        audioSelectedCodec.put(ENCODEC_NAME, _encodecName);
        audioSelectedCodec.put(ENCODEC_AUDIO_TYPE, _encodedAudioType);
        audioSelectedCodec.put(ENCODEC_AUDIO_SAMPLE_RATE, String.valueOf(_sampleRate));
        audioSelectedCodec.put(ENCODEC_AUDIO_CHANNEL_COUNT, String.valueOf(_channelCount));
        audioSelectedCodec.put(ENCODEC_AUDIO_BIT_RATE, String.valueOf(_bitRate));
        audioSelectedCodec.put(ENCODEC_AUDIO_MAX_INPUT_SIZE, String.valueOf(_maxInputSize));
        audioSelectedCodec.put(ENCODEC_PROFILE_LEVEL, String.valueOf(_profileLevel));
        return audioSelectedCodec;
    }

    static public LUAudioCodecProfile mapToAudioProfile(Map<String, String> audioSelectedCodec){

        LUAudioCodecProfile tempProfile = new LUAudioCodecProfile(
                audioSelectedCodec.get(ENCODEC_NAME),
                audioSelectedCodec.get(ENCODEC_AUDIO_TYPE),
                Integer.parseInt(audioSelectedCodec.get(ENCODEC_AUDIO_SAMPLE_RATE)),
                Integer.parseInt(audioSelectedCodec.get(ENCODEC_AUDIO_CHANNEL_COUNT)),
                Integer.parseInt(audioSelectedCodec.get(ENCODEC_AUDIO_BIT_RATE)),
                Integer.parseInt(audioSelectedCodec.get(ENCODEC_PROFILE_LEVEL)),
                Integer.parseInt(audioSelectedCodec.get(ENCODEC_AUDIO_MAX_INPUT_SIZE)));
        return tempProfile;

    }

}
