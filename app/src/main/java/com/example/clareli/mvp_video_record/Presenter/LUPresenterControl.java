package com.example.clareli.mvp_video_record.Presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.example.clareli.mvp_video_record.Model.LUEncodedAudio;
import com.example.clareli.mvp_video_record.Model.LUEncodedVideo;
import com.example.clareli.mvp_video_record.Model.IEncodedVideo;
import com.example.clareli.mvp_video_record.Model.ICamera;
import com.example.clareli.mvp_video_record.Model.LUCameraClass;
import com.example.clareli.mvp_video_record.Model.IMuxer;
import com.example.clareli.mvp_video_record.Model.LUMuxer;
import com.example.clareli.mvp_video_record.Model.LURecordedAudio;
import com.example.clareli.mvp_video_record.Util.LUAudioCodecProfile;
import com.example.clareli.mvp_video_record.Util.LUVideoCodecProfile;
import com.example.clareli.mvp_video_record.View.LUViewErrorCallback;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class LUPresenterControl implements IPresenterControl, IPresenterCallback {
    private final WeakReference<Activity> _messageViewReference;
    private String TAG = "LUPresenterControl";
    private ICamera _camera;
    private LUPresenterCallback _presenterCallback;
    private Object _systemService;
    private LUViewErrorCallback _LU_viewErrorCallback;
    private CameraDevice _cameraDevice;
    private IEncodedVideo _cameraCodec;
    private IMuxer _videoMuxer;
//    private IMuxer _audioMuxer;
    private SurfaceTexture _previewSurTexture;
    private Surface _previewSurface;
    private String _dstFilePath;
    private LURecordedAudio _recordedAudio;
    private LUEncodedAudio _encodedAudio;
    private ByteBuffer _inputAudioBuffer;
    private AudioSamples audioSamples;
    private long _inputBufferPosition;
    private float _audioRate;
    private MediaFormat _videoFormat;
    private MediaFormat _audioFormat;


    //constructor
    public LUPresenterControl(Activity activity, LUViewErrorCallback LUViewErrorCallback) {
        _messageViewReference = new WeakReference<>(activity);
        _presenterCallback = new LUPresenterCallback(this);
        _camera = new LUCameraClass(_presenterCallback);
        _LU_viewErrorCallback = LUViewErrorCallback;
        _cameraCodec = new LUEncodedVideo(_presenterCallback);
        _recordedAudio = new LURecordedAudio(_presenterCallback);
        _encodedAudio = new LUEncodedAudio(_presenterCallback);
    }


    @Override
    public void openCamera(SurfaceTexture surface, int width, int height) {
        String cameraId = selectCamera();
        CameraManager manager = (CameraManager) _systemService;
        _camera.openCamera(width, height, cameraId, manager, surface);
    }

    /*
    default select index 0 camera
     */
    public String selectCamera() {
        String cameraId = null;
        long timeout = 2500;
        if (_systemService == null)
            _systemService = _messageViewReference.get().getSystemService(Context.CAMERA_SERVICE);

        CameraManager manager = (CameraManager) _systemService;
        try {
            if (_camera.tryToGetAcquire(timeout)) {
                cameraId = manager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return cameraId;
    }

    @Override
    public void closeCamera() {
        _camera.closeCamera();
    }

    /*  start to do video record
        _camera.createCaptureSession(...) will pass preview , record surface to builder.addTarget() and set Codec Callback
     */
    @Override
    public void videoRecordStart(String filePath, SurfaceTexture previewSurTexture, int width, int height) {
        LUVideoCodecProfile videoCodecH264 = new LUVideoCodecProfile("video/avc", MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
                8880000, 30, 5, 1920, 1080);
        //TODO check MJPEG setting
        LUVideoCodecProfile videoCodecMJpeg = new LUVideoCodecProfile("video/mjpeg", MediaCodecInfo.CodecCapabilities.COLOR_FormatCbYCrY,
                6000000, 15, 10, 1920, 1080);

        if (_cameraDevice != null) {
            _camera.closePreviewSession();
            _previewSurTexture = previewSurTexture;
            if (_previewSurTexture == null) {
                //TODO
                _LU_viewErrorCallback.viewShowErrorDialog("preview SurTexture is null fail!");
                return;
            }
            _previewSurTexture.setDefaultBufferSize(width, height);
            _dstFilePath = filePath;
            _cameraCodec.configuredVideoCodec(videoCodecH264);
            _cameraCodec.startEncode();
            _previewSurface = new Surface(_previewSurTexture);
            _camera.createCaptureSession(_previewSurface, _cameraCodec.getSurface());

        }
    }

    public void stopMuxer() {
        _videoMuxer.stopMuxer();
        _videoMuxer = null;
//        _audioMuxer.stopMuxer();
//        _audioMuxer = null;
    }

    /*
    press stop record button will stop codec, muxer and camera preview.
     */
    @Override
    public void videoRecordStop() {
        _cameraCodec.stopEncode();
        _camera.startPreview(_previewSurface);
    }

    @Override
    public void stopVideoEncode() {
        _cameraCodec.stopEncode();
    }

    @Override
    public void audioRecordStart(String filePath) {

    }

    public void setupAudioRecord() {
        int audioFrequency = 4100;
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        int encodingBit = AudioFormat.ENCODING_PCM_16BIT;
        int bitPerSample = 8;
        LUAudioCodecProfile audioCodecProfileAAC = new LUAudioCodecProfile(MediaFormat.MIMETYPE_AUDIO_AAC,
                16000, 1, 128 * 100, MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                16 * 10240);
        if (encodingBit == AudioFormat.ENCODING_PCM_16BIT)
            bitPerSample = 16;
        calculateInputRate(bitPerSample, 16000, 1);

        if (_encodedAudio != null) {
            if (_recordedAudio != null)
                _recordedAudio.startRecord(audioFrequency, channelConfig, encodingBit);
            else {
                _LU_viewErrorCallback.viewShowErrorDialog("audio Record Start fail!");

            }
            _encodedAudio.configuredAudioCodec(audioCodecProfileAAC);
            if (_encodedAudio.getCodec() != null) {
                _encodedAudio.startEncode();

                _audioFormat = _encodedAudio.getFormat();
                //must wait video and audio format done, then start muxer

//                int saveOutputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;
//                if (_audioMuxer == null) {
//                    _audioMuxer = new LUMuxer(_dstFilePath, saveOutputFormat, _presenterCallback);
//                }
//                _audioMuxer.setMuxerMediaFormat(_audioFormat);
//                _audioMuxer.startMuxer();

            }
        } else {
            _LU_viewErrorCallback.viewShowErrorDialog("audio Record encoded fail!");
        }
    }

    @Override
    public void audioRecordStop() {
        _recordedAudio.stopRecord();
        _encodedAudio.stopEncode();
        stopMuxer();
    }

    @Override
    public void errorCameraCallback(String msg) {
        _LU_viewErrorCallback.viewShowErrorDialog(msg);
    }

    @Override
    public void errorDecoderCallback() {

    }


    @Override
    public void getCameraDevice(CameraDevice mCameraDevice) {
        _cameraDevice = mCameraDevice;
    }


    /*from LUEncodedVideo.java
    prepare for muxer to write data to file
     */
    @Override
    public void onVideoOutputFormatChanged(MediaFormat format) {
        //the format include "csd-0" and "csd-1" byte buffers
        Log.i(TAG, "onVideoOutputFormatChanged....");
        //setup video muxer
        _videoFormat = format;
        int saveOutputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;
        if (_videoMuxer == null) {
            _videoMuxer = new LUMuxer(_dstFilePath, saveOutputFormat, _presenterCallback);
        }
        _videoMuxer.setMuxerMediaFormat(format);
        _videoMuxer.startMuxer();

        setupAudioRecord();
    }

    /*from LUEncodedVideo.java
    prepare for muxer to write data to file
     */
    @Override
    public void onVideoOutputBufferAvailable(MediaCodec.BufferInfo info, ByteBuffer encodedData) {
        Log.i(TAG, "info_presentationTimeUs:" + info.presentationTimeUs + ", offset:" + info.offset);
        if(_videoMuxer != null) {
            if (_videoMuxer.writeSampleData(encodedData, info) == false) {

            }
        }

    }

    @Override
    public void muxerErrorCallback(String msg) {
        Log.i(TAG, msg);
        _LU_viewErrorCallback.viewShowErrorDialog(msg);
    }

    @Override
    public void encodedVideoErrorCallback(String msg) {
        Log.i(TAG, msg);
        _LU_viewErrorCallback.viewShowErrorDialog(msg);

    }

    @Override
    public void encodedAudioErrorCallback(String msg) {
        Log.i(TAG, msg);
        _LU_viewErrorCallback.viewShowErrorDialog(msg);
    }

    @Override
    public void recordedAudioErrorCallback(String msg) {
        Log.i(TAG, msg);
        _LU_viewErrorCallback.viewShowErrorDialog(msg);
    }

    @Override
    public void notifyToAccessBuffer(byte[] rowData) {
        audioSamples = new AudioSamples();
        audioSamples.bytes = rowData;
        Log.i(TAG, "----Audio Record data:" + rowData.length);
    }

    @Override
    public void onAudioOutputFormatChanged(MediaFormat format) {
        Log.i(TAG, "********onAudioOutputFormatChanged");

    }

    @Override
    public void onAudioOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info, ByteBuffer encodedData) {
        Log.i(TAG, "..onAudioOutputBufferAvailable_encodedData:" + encodedData.limit());
        if (encodedData != null) {
//            if (_audioMuxer != null) {
//                if (_audioMuxer.writeSampleData(encodedData, info) == false) {
//                    _LU_viewErrorCallback.viewShowErrorDialog("Audio output write to Muxer error!");
//
//                }
//            } else
//                _LU_viewErrorCallback.viewShowErrorDialog("Audio record muxer write data fail!");
        }

    }

    @Override
    public void onAudioInputBufferAvailable(ByteBuffer inputBuffer, int index) {
        if (audioSamples != null) {
            _inputAudioBuffer = inputBuffer;
            int sz = Math.min(inputBuffer.capacity(), audioSamples.bytes.length - audioSamples.offset);
            long ts = getPresentationTimestampUs(_inputBufferPosition);

            inputBuffer.put(audioSamples.bytes, audioSamples.offset, sz);
            _encodedAudio.queueInputBuffer(index, sz, ts);
            _inputBufferPosition += sz;
            audioSamples.offset += sz;
            Log.i(TAG, "....Presenter__onAudioInputBufferAvailable__inputBuffer size:" + _inputAudioBuffer.limit());
        }
    }

    private class AudioSamples {
        byte bytes[];
        int offset;
    }

    private void calculateInputRate(int bitPerSample, int sampleRate, int channelCount) {
        _audioRate = bitPerSample;
        _audioRate *= sampleRate;
        _audioRate *= channelCount;
        _audioRate *= 1e-6; // -> us
        _audioRate /= 8; // -> bytes

        Log.v(TAG, "Rate: " + _audioRate);
    }


    private long getPresentationTimestampUs(long position) {
        return (long) (position / _audioRate);
    }

}
