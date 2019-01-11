package com.example.clareli.mvp_video_record.Model;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MuxerOutput implements IMuxerOutput {
    private MediaMuxer _muxer;
    private int videoTrackIndex = -1;

    public MuxerOutput(String dstPath, MediaFormat mediaFormat){
        try {
            _muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            videoTrackIndex = _muxer.addTrack(mediaFormat);
            _muxer.start();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
    @Override
    public void writeSampleData(ByteBuffer encodedData, MediaCodec.BufferInfo info) {

        if((info != null) && (_muxer != null)) {
            _muxer.writeSampleData(videoTrackIndex, encodedData, info);
        }
    }

    @Override
    public void stopMuxer() {
        if(_muxer != null) {
            _muxer.stop();
            _muxer.release();
            _muxer = null;
        }
    }

}
