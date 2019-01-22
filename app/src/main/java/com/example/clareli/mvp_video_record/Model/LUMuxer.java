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
            hasAudioSetted = true;
        }

        if (hasVideoSetted && hasAudioSetted) {
            startMuxer();
        }

    }


    @Override
    public boolean writeSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo info, int flag) {
        boolean result = false;
        try {
            _muxerWriteCloseLock.acquire();
            if (flag == 1) {
                currentTrackIndex = videoTrackIndex;
            } else if (flag == 2) {
                currentTrackIndex = audioTrackIndex;
            }
//            Log.d("samson", String.valueOf(currentTrackIndex));
            _muxer.writeSampleData(currentTrackIndex, encodedData, info);
            result = true;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            _muxerWriteCloseLock.release();
            return result;
        }

    }

        @Override
    public boolean isMuxerStarted() {
        return ( hasVideoSetted && hasAudioSetted);
    }

    private void startMuxer() {
        _muxer.start();
    }

    @Override
    public boolean stopMuxer() {
        boolean result = false;
        try {
            _muxerWriteCloseLock.acquire();
            hasVideoSetted = false;
            hasAudioSetted = false;
            result = true;

            if (_muxer != null) {
                _muxer.stop();
                _muxer.release();
                _muxer = null;
                Log.i("SAMSON","stop muxer ok!");
            } else {
                _presenterCallback.getMuxerErrorMsg("Stop Muxer error!");

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            _muxerWriteCloseLock.release();
            return result;
        }


    }


}
