package com.example.clareli.mvp_video_record.Util;

import android.media.MediaCodecInfo;

import java.util.Arrays;

public class LUVideoCodecInfo {
    String _encodecName;
    String _supportedType;
    int _widthMax;
    int _widthMin;
    int _heightMax;
    int _heightMin;
    int _frameRatesMax;
    int _frameRatesMin;
    int _bitRatesMax;
    int _bitRatesMin;
    //    String[] _profileLevels;
    MediaCodecInfo.CodecProfileLevel[] _profileLevels;
    int[] _colorFormats;

    public LUVideoCodecInfo(String encodecName, String supportedType, int widthMax, int widthMin, int heightMax, int heightMin,
                            int frameRatesMax, int frameRatesMin, int bitRatesMax, int bitRatesMin, MediaCodecInfo.CodecProfileLevel[] profileLevels,//String[] profileLevels,
                            int[] colorFormats) {
        _encodecName = encodecName;
        _supportedType = supportedType;
        _widthMax = widthMax;
        _widthMin = widthMin;
        _heightMax = heightMax;
        _heightMin = heightMin;
        _frameRatesMax = frameRatesMax;
        _frameRatesMin = frameRatesMin;
        _bitRatesMax = bitRatesMax;
        _bitRatesMin = bitRatesMin;
        _profileLevels = Arrays.copyOf(profileLevels, profileLevels.length);
        _colorFormats = Arrays.copyOf(colorFormats, colorFormats.length);

    }

    public String getEncodecName() {
        return _encodecName;
    }

    public String getSupportedType() {
        return _supportedType;
    }

    public int getWidthMax() {
        return _widthMax;
    }

    public int getWidthMin() {
        return _widthMin;
    }

    public int getHeightMax() {
        return _heightMax;
    }

    public int getHeightMin() {
        return _heightMin;
    }

    public int getFrameRatesMax() {
        return _frameRatesMax;
    }

    public int getFrameRatesMin() {
        return _frameRatesMin;
    }

    public int getBitRatesMax() {
        return _bitRatesMax;
    }

    public int getBitRatesMin() {
        return _bitRatesMin;
    }

    //    public String[] getProfileLevels(){
//        return _profileLevels;
//    }
    public MediaCodecInfo.CodecProfileLevel[] getProfileLevels() {
        return _profileLevels;
    }

    public int[] getColorFormats() {
        return _colorFormats;
    }

    @Override
    public String toString() {
        return "LUVideoCodecInfo{" +
                "_encodecName='" + _encodecName + '\'' +
                ", _supportedType='" + _supportedType + '\'' +
                ", _widthMax=" + _widthMax +
                ", _widthMin=" + _widthMin +
                ", _heightMax=" + _heightMax +
                ", _heightMin=" + _heightMin +
                ", _frameRatesMax=" + _frameRatesMax +
                ", _frameRatesMin=" + _frameRatesMin +
                ", _bitRatesMax=" + _bitRatesMax +
                ", _bitRatesMin=" + _bitRatesMin +
                ", _profileLevels=" + Arrays.toString(_profileLevels) +
                ", _colorFormats=" + Arrays.toString(_colorFormats) +
                '}';
    }
}
