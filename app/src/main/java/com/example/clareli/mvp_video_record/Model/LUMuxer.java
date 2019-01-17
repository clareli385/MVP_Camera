package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.example.clareli.mvp_video_record.Presenter.LUPresenterCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

public class LUMuxer implements IMuxer {
    private MediaMuxer _muxer;
    private int audioTrackIndex = -1;
    private int videoTrackIndex = -1;
    private int currentTrackIndex;
    private String _mime;
    private LUPresenterCallback _presenterCallback;
    private Semaphore _muxerWriteCloseLock = new Semaphore(1);


    public LUMuxer(String dstPath, int outputFormat, LUPresenterCallback presenterCallback){
        try {
            _muxer = new MediaMuxer(dstPath, outputFormat);
            _presenterCallback = presenterCallback;
        } catch (IOException e) {
            _presenterCallback.getMuxerErrorMsg("Muxer initialize error!");
            e.printStackTrace();
        }
    }

    @Override
    public void setMuxerMediaFormat(MediaFormat mediaFormat) {
        _mime = mediaFormat.getString(MediaFormat.KEY_MIME);
        //write Video format file
        if (_mime.startsWith("video/")) {
            videoTrackIndex = _muxer.addTrack(mediaFormat);
            Log.i("LUMuxer", "videoTrackIndex:"+videoTrackIndex);
        }
        if(_mime.startsWith("audio/")){
            //write Audio format file
            audioTrackIndex = _muxer.addTrack(mediaFormat);
            Log.i("LUMuxer", "audioTrackIndex:"+audioTrackIndex);

        }


    }

    @Override
    public boolean writeSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo info) {

        if((info != null) && (_muxer != null)) {
            if (_mime.startsWith("video/")){
                currentTrackIndex = videoTrackIndex;
            } else if(_mime.startsWith("audio/")) {
                currentTrackIndex = audioTrackIndex;
            }
            if(currentTrackIndex < 0){
                _presenterCallback.getMuxerErrorMsg("1.Write Muxer Sample Data error!");
                return false;
            }
            if((encodedData == null) || (info == null)){
                _presenterCallback.getMuxerErrorMsg("2.Write Muxer Sample Data error!");
                return false;
            }
            Log.i("LUMuxer","muxer prepare to write...");
            try {
                _muxerWriteCloseLock.acquire();
                _muxer.writeSampleData(currentTrackIndex, encodedData, info);
                Log.i("LUMuxer","muxer write ok");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                _muxerWriteCloseLock.release();
            }

            return true;
        } else {
            _presenterCallback.getMuxerErrorMsg("3.Write Muxer Sample Data error!");
            return false;
        }
    }

    @Override
    public void startMuxer() {
        _muxer.start();
    }

    @Override
    public boolean stopMuxer() {
        if(_muxer != null) {
            try {
                _muxerWriteCloseLock.acquire();
                _muxer.stop();
                _muxer.release();
                _muxer = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                _muxerWriteCloseLock.release();
            }

            return true;
        } else {
            _presenterCallback.getMuxerErrorMsg("Stop Muxer error!");
            return false;
        }
    }



}
