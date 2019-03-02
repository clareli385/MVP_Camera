package com.example.clareli.mvp_video_record.Util;

import android.media.MediaCodecInfo;

import java.util.HashMap;
import java.util.Map;

import static com.example.clareli.mvp_video_record.Model.LUEncodeFinder.avcProfileLevelToString;
import static com.example.clareli.mvp_video_record.Model.LUEncodeFinder.toProfileLevel;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_NAME;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_PROFILE_LEVEL;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_VIDEO_BIT_RATE;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_VIDEO_COLOR_FORMAT;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_VIDEO_FRAME_RATES;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_VIDEO_IFRAME_INTERVAL;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_VIDEO_TYPE;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_HEIGHT;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_WIDTH;


/*2019-02-11,Clare
This class is for read audio codec spec lists after app started
* */
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
                               int videoFrameRates, int iFrameInterval, int width, int height, MediaCodecInfo.CodecProfileLevel profileLevel){
        _encodedName = encodedName;
        _encodedVideoType = encodedVideoType;
        _colorFormat = colorFormat;
        _videoBitrate = videoBitrate;
        _videoFrameRates = videoFrameRates;
        _iFrameInterval = iFrameInterval;
        _width = width;
        _height = height;
        _profileLevel = profileLevel;

    }

    public LUVideoCodecProfile(){

    }

    public void setEncodedName(String encodedName){
        _encodedName = encodedName;
    }

    public void setEncodedVideoType(String videoType){
        _encodedVideoType = videoType;
    }

    public void setColorFormat(int colorFormat){
        _colorFormat = colorFormat;
    }

    public void setVideoBitrate(int videoBitrate){
        _videoBitrate = videoBitrate;
    }

    public void setVideoFrameRates(int videoFrameRates){
        _videoFrameRates = videoFrameRates;
    }

    public void setIFrameInterval(int iFrameInterval){
        _iFrameInterval = iFrameInterval;
    }

    public void setWidth(int width){
        _width = width;
    }

    public void setHeight(int height){
        _height = height;
    }

    public void setProfileLevel(MediaCodecInfo.CodecProfileLevel profileLevel){
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

    public Map<String, String> videoProfileToMap(){
        Map<String, String> audioSelectedCodec = new HashMap<String, String>();
        audioSelectedCodec.put(ENCODEC_NAME, _encodedName);
        audioSelectedCodec.put(ENCODEC_VIDEO_TYPE, _encodedVideoType);
        audioSelectedCodec.put(VIDEO_WIDTH, Integer.toString(_width));
        audioSelectedCodec.put(VIDEO_HEIGHT, Integer.toString(_height));
        audioSelectedCodec.put(ENCODEC_VIDEO_FRAME_RATES, Integer.toString(_videoFrameRates));
        audioSelectedCodec.put(ENCODEC_VIDEO_BIT_RATE, Integer.toString(_videoBitrate));
        audioSelectedCodec.put(ENCODEC_VIDEO_IFRAME_INTERVAL, Integer.toString(_iFrameInterval));
        audioSelectedCodec.put(ENCODEC_VIDEO_COLOR_FORMAT, Integer.toString(_colorFormat));
        audioSelectedCodec.put(ENCODEC_PROFILE_LEVEL, avcProfileLevelToString(_profileLevel));
        return audioSelectedCodec;
    }

    static public LUVideoCodecProfile mapToVideoProfile(Map<String, String> selectedAudioCodec){

        LUVideoCodecProfile tempProfile = new LUVideoCodecProfile(
                selectedAudioCodec.get(ENCODEC_NAME),
                selectedAudioCodec.get(ENCODEC_VIDEO_TYPE),
                Integer.parseInt(selectedAudioCodec.get(ENCODEC_VIDEO_COLOR_FORMAT)),
                Integer.parseInt(selectedAudioCodec.get(ENCODEC_VIDEO_BIT_RATE)),
                Integer.parseInt(selectedAudioCodec.get(ENCODEC_VIDEO_FRAME_RATES)),
                Integer.parseInt(selectedAudioCodec.get(ENCODEC_VIDEO_IFRAME_INTERVAL)),
                Integer.parseInt(selectedAudioCodec.get(VIDEO_WIDTH)),
                Integer.parseInt(selectedAudioCodec.get(VIDEO_HEIGHT)),
                toProfileLevel(selectedAudioCodec.get(ENCODEC_PROFILE_LEVEL))
                );
        return tempProfile;

    }

}
