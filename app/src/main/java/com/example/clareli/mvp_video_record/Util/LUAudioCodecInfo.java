package com.example.clareli.mvp_video_record.Util;

import java.util.Arrays;


/*2019-02-11,Clare
This class is for read audio codec spec lists after app started
* */
public class LUAudioCodecInfo {
    String _encodecName;
    String _supportedType;
    int[] _sampleRates;
    int _bitRatesMax;
    int _bitRatesMin;
    int _channelMax;

    public LUAudioCodecInfo(String encodecName, String supportedType, int[] sampleRates, int bitRatesMax, int bitRatesMin, int channelMax){
        _encodecName = encodecName;
        _supportedType = supportedType;
        _sampleRates = Arrays.copyOf(sampleRates, sampleRates.length);
        _bitRatesMax = bitRatesMax;
        _bitRatesMin = bitRatesMin;
        _channelMax = channelMax;
    }

    public String getEncodecName(){
        return _encodecName;
    }

    public String getSupportedType(){
        return _supportedType;
    }

    public int[] getSampleRates(){
        return _sampleRates;
    }

    public int getBitRatesMax(){
        return _bitRatesMax;
    }

    public int getBitRatesMin(){
        return _bitRatesMin;
    }

    public int getChannelMax(){
        return _channelMax;
    }

    @Override
    public String toString() {
        return "LUAudioCodecInfo{" +
                "_encodecName='" + _encodecName + '\'' +
                ", _supportedType='" + _supportedType + '\'' +
                ", _sampleRates=" + Arrays.toString(_sampleRates) +
                ", _bitRatesMax=" + _bitRatesMax +
                ", _bitRatesMin=" + _bitRatesMin +
                ", _channelMax=" + _channelMax +
                '}';
    }
}
