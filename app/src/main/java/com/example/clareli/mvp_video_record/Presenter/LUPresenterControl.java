package com.example.clareli.mvp_video_record.Presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseLongArray;
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
import java.util.Objects;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME;

public class LUPresenterControl implements IPresenterControl, IPresenterCallback {
    private final WeakReference<Activity> _messageViewReference;
    private String TAG = "LUPresenterControl";
    private ICamera _camera;
    private LUPresenterCallback _presenterCallback;
    private Object _systemService;
    private LUViewErrorCallback _LU_viewErrorCallback;
    private CameraDevice _cameraDevice;
    private IEncodedVideo _cameraCodec;
    private IMuxer _muxer;
    private SurfaceTexture _previewSurTexture;
    private Surface _previewSurface;
    private LURecordedAudio _recordedAudio;
    private LUEncodedAudio _encodedAudio;
    private AudioRecord _micRecord;

    private int _channelsSampleRate;
    private static final int LAST_FRAME_ID = -1;
    private SparseLongArray mFramesUsCache = new SparseLongArray(2);
    private String _destPath;


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

    @Override
    public void startRecord(String filePath, SurfaceTexture previewSurTexture, int width, int height) {
        _destPath = filePath;
        createMuxer(filePath);
        startVideoRecord(previewSurTexture, width, height);
        startAudioRecord();
    }

    /*  start to do video record
        _camera.createCaptureSession(...) will pass preview , record surface to builder.addTarget() and set Codec Callback
     */
    private void startVideoRecord(SurfaceTexture previewSurTexture, int width, int height) {
        LUVideoCodecProfile videoCodecH264 = new LUVideoCodecProfile("video/avc", MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
                6000000, 30, 1, 1920, 1080);
        //TODO check MJPEG setting
        LUVideoCodecProfile videoCodecMJpeg = new LUVideoCodecProfile("video/mjpeg", MediaCodecInfo.CodecCapabilities.COLOR_FormatCbYCrY,
                6000000, 15, 10, 1920, 1080);
        //TODO modify preview result not callback _cameraDevice
        if (_cameraDevice != null) {
            _camera.closePreviewSession();
            _previewSurTexture = previewSurTexture;
            if (_previewSurTexture == null) {
                _LU_viewErrorCallback.viewShowErrorDialog("preview SurTexture is null fail!");
                return;
            }
            _previewSurTexture.setDefaultBufferSize(width, height);
            _cameraCodec.configuredVideoCodec(videoCodecH264);
            _cameraCodec.startEncode();
            _previewSurface = new Surface(_previewSurTexture);
            _camera.createCaptureSession(_previewSurface, _cameraCodec.getSurface());

        }
    }

    public void startAudioRecord() {
        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        int encodingBit = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, encodingBit) * 1;
        int channelCount = 1;
        LUAudioCodecProfile audioCodecProfileAAC = new LUAudioCodecProfile(MediaFormat.MIMETYPE_AUDIO_AAC,
                44100, channelCount, 80000, MediaCodecInfo.CodecProfileLevel.AACObjectMain,
                //16 * 10240);
                bufferSize);

        _channelsSampleRate = sampleRateInHz * channelCount;

        if (_encodedAudio != null) {
            if (_recordedAudio != null) {
                _micRecord = _recordedAudio.initRecord(sampleRateInHz, channelConfig, encodingBit);
                if (_micRecord != null) {
                    _micRecord.startRecording();
                }
                _encodedAudio.configuredAudioCodec(audioCodecProfileAAC);

                if (_encodedAudio.getCodec() != null) {
                    _encodedAudio.startEncode();
                }
            } else {
                _LU_viewErrorCallback.viewShowErrorDialog("audio Record Start fail!");

            }

        } else {
            _LU_viewErrorCallback.viewShowErrorDialog("audio Record encoded fail!");
        }
    }

    @Override
    public void stopRecord() {
        stopVideoRecord();
        stopAudioRecord();
    }

    /*
    press stop record button will stop codec, muxer and camera preview.
     */
    private void stopVideoRecord() {
        _cameraCodec.stopEncode();
        _camera.startPreview(_previewSurface);
    }

    @Override
    public void stopVideoEncode() {
        _cameraCodec.stopEncode();
    }

    private void stopAudioRecord() {
        _recordedAudio.stopRecord();
        _encodedAudio.stopEncode();
    }

    @Override
    public void createMuxer(String dstPath) {
        int saveOutputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;
        if (_muxer == null) {
            Log.i(TAG, "video initial muxer !!!");
            _muxer = new LUMuxer(dstPath, saveOutputFormat, _presenterCallback);
        }
    }

    @Override
    public void stopMuxer() {
        _messageViewReference.get().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _muxer.stopMuxer();
                _muxer = null;
            }
        });
    }


    @Override
    public void getCameraDevice(CameraDevice mCameraDevice) {
        _cameraDevice = mCameraDevice;
    }


    ///////////////////////Video callback///////////////////////

    /*2019-01-20, Clare
    from LUEncodedVideo.java
    prepare for muxer to write data to file
     */
    @Override
    public void onVideoOutputFormatChanged(final MediaFormat format) {
        //the format include "csd-0" and "csd-1" byte buffers
        Log.i(TAG, "set muxer video format !!!");

        _muxer.setMuxerMediaFormat(format);
    }

    /*2019-01-20, Clare
    from LUEncodedVideo.java
    prepare for muxer to write data to file
        */
    @Override
    public void onVideoOutputBufferAvailable(final MediaCodec.BufferInfo info, final ByteBuffer encodedData) {
        if (_muxer != null && _muxer.isMuxerStarted()) {
            _messageViewReference.get().runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    byte[] b = new byte[encodedData.remaining()];
                    encodedData.get(b);
                    String s = bytesToHex(b);
//                    Log.d("SAMSON", "video_output:"+s);

                    _muxer.writeSampleData(encodedData, info, 1);

                }
            });
        }

    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

   ///////////////////////Audio callback///////////////////////

    @Override
    public void notifyToAccessBuffer(byte[] rowData, int readBytes) {

    }

    @Override
    public void onAudioOutputFormatChanged(MediaFormat format) {
        _muxer.setMuxerMediaFormat(format);

    }

    @Override
    public void onAudioOutputBufferAvailable(MediaCodec codec, int index, final MediaCodec.BufferInfo info, final ByteBuffer encodedData) {

        if(_muxer != null && _muxer.isMuxerStarted()) {
            _messageViewReference.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] b = new byte[encodedData.remaining()];
                    encodedData.get(b);
                    String s = bytesToHex(b);
//                    Log.d("SAMSON", "audio_output:"+s);
                    _muxer.writeSampleData(encodedData, info, 2);
                }
            });
        }


    }

    @Override
    public void onAudioInputBufferAvailable(ByteBuffer inputBuffer, int index) {

        int offset = inputBuffer.position();
        int limit = inputBuffer.limit();
        final AudioRecord micRecord = Objects.requireNonNull(_micRecord);
        int read = 0;
        final boolean eos = micRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED;
        if (eos != true) {
            read = micRecord.read(inputBuffer, limit);

            if (read < 0)
                read = 0;
            long pstTs = calculateFrameTimestamp(read << 3);
            int flags = BUFFER_FLAG_KEY_FRAME;

            if (eos) {
                flags = BUFFER_FLAG_END_OF_STREAM;
            }

            //TODO
            byte[] b = new byte[inputBuffer.remaining()];
            inputBuffer.get(b);
            String s = bytesToHex(b);
            Log.d("SAMSON","_audio_input_pstTs:"+ pstTs);
            _encodedAudio.queueInputBuffer(index, offset, read, pstTs, flags);



        }

    }


    /**
     * Gets presentation time (us) of polled frame.
     * 1 sample = 16 bit
     */
    private long calculateFrameTimestamp(int totalBits) {
        int samples = totalBits >> 4;
        long frameUs = mFramesUsCache.get(samples, -1);
        if (frameUs == -1) {
            frameUs = samples * 1000_000 / _channelsSampleRate;
            mFramesUsCache.put(samples, frameUs);
        }
        long timeUs = SystemClock.elapsedRealtimeNanos() / 1000;
        // accounts the delay of polling the audio sample data
        timeUs -= frameUs;
        long currentUs;
        long lastFrameUs = mFramesUsCache.get(LAST_FRAME_ID, -1);
        if (lastFrameUs == -1) { // it's the first frame
            currentUs = timeUs;
        } else {
            currentUs = lastFrameUs;
        }

        // maybe too late to acquire sample data
        if (timeUs - currentUs >= (frameUs << 1)) {
            // reset
            currentUs = timeUs;
        }
        mFramesUsCache.put(LAST_FRAME_ID, currentUs + frameUs);
        return currentUs;
    }

    @Override
    public void errorCameraCallback(String msg) {
        _LU_viewErrorCallback.viewShowErrorDialog(msg);
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

}
