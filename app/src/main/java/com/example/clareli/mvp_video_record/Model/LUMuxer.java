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
    private boolean hasVideoSetted = false;
    private boolean hasAudioSetted = false;


    public LUMuxer(String dstPath, int outputFormat, LUPresenterCallback presenterCallback) {
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
            hasVideoSetted = true;
        }
        if (_mime.startsWith("audio/")) {
            //write Audio format file
            audioTrackIndex = _muxer.addTrack(mediaFormat);
           // hasAudioSetted = true;
        }

        if (hasVideoSetted) {
//            Log.i("LUMuxer", "muxer start!!!");
            startMuxer();
        }

    }


    @Override
    public boolean writeSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo info, int flag) {
        if (flag == 1) {
            currentTrackIndex = videoTrackIndex;
        } else if (flag == 2) {
            currentTrackIndex = audioTrackIndex;
        }
        Log.d("samson", String.valueOf(currentTrackIndex));
        _muxer.writeSampleData(currentTrackIndex, encodedData, info);
        return true;
    }

        @Override
    public boolean isMuxerStarted() {
        return ( hasVideoSetted);
    }

    private void startMuxer() {
        _muxer.start();
    }

    @Override
    public boolean stopMuxer() {
        if (_muxer != null) {

                _muxer.stop();
                _muxer.release();
                _muxer = null;

                hasVideoSetted = false;
                hasAudioSetted = false;
            return true;
        } else {
            _presenterCallback.getMuxerErrorMsg("Stop Muxer error!");
            return false;
        }
    }


}
