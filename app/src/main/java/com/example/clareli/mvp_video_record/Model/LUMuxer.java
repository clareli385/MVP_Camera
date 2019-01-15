package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.example.clareli.mvp_video_record.Presenter.LUPresenterCameraCallback;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LUMuxer implements IMuxer {
    private MediaMuxer _muxer;
    private int _trackIndex = -1;
    private String _mime;
    private LUPresenterCameraCallback _presenterCallback;

    public LUMuxer(String dstPath, MediaFormat mediaFormat, int outputFormat, LUPresenterCameraCallback presenterCallback){
        try {
            _muxer = new MediaMuxer(dstPath, outputFormat);
            _mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            _presenterCallback = presenterCallback;
            //write Video format file
            if (_mime.startsWith("video/")) {
                _trackIndex = _muxer.addTrack(mediaFormat);
            } else {
             //write Audio format file

            }

            _muxer.start();
        } catch (IOException e) {
            _presenterCallback.getMuxerErrorMsg("Muxer initialize error!");
            e.printStackTrace();
        }
    }
    @Override
    public boolean writeSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo info) {

        if((info != null) && (_muxer != null)) {
            _muxer.writeSampleData(_trackIndex, encodedData, info);
            return true;
        } else {
            _presenterCallback.getMuxerErrorMsg("Write Muxer Sample Data error!");
            return false;
        }
    }

    @Override
    public boolean stopMuxer() {
        if(_muxer != null) {
            _muxer.stop();
            _muxer.release();
            _muxer = null;
            return true;
        } else {
            _presenterCallback.getMuxerErrorMsg("Stop Muxer error!");
            return false;
        }
    }

}
