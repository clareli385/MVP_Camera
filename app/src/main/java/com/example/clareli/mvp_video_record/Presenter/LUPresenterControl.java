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
import android.util.Log;
import android.view.Surface;

import com.example.clareli.mvp_video_record.Model.LUEncodedAudio;
import com.example.clareli.mvp_video_record.Model.LUEncodedVideo;
import com.example.clareli.mvp_video_record.Model.LUIEncodedAudio;
import com.example.clareli.mvp_video_record.Model.LUIEncodedVideo;
import com.example.clareli.mvp_video_record.Model.LUICamera;
import com.example.clareli.mvp_video_record.Model.LUCameraClass;
import com.example.clareli.mvp_video_record.Model.LUIMuxer;
import com.example.clareli.mvp_video_record.Model.LUIRecordedAudio;
import com.example.clareli.mvp_video_record.Model.LUMuxer;
import com.example.clareli.mvp_video_record.Model.LURecordedAudio;
import com.example.clareli.mvp_video_record.Util.LUAudioCodecInfo;
import com.example.clareli.mvp_video_record.Util.LUAudioCodecProfile;
import com.example.clareli.mvp_video_record.Util.LUBasicUtility;
import com.example.clareli.mvp_video_record.Model.LUEncodeFinder;
import com.example.clareli.mvp_video_record.Util.LUVideoCodecInfo;
import com.example.clareli.mvp_video_record.Util.LUVideoCodecProfile;
import com.example.clareli.mvp_video_record.View.LUViewCallback;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME;
import static com.example.clareli.mvp_video_record.Util.IConstant.AUDIO_GOOGLE_AAC_ENCODER;
import static com.example.clareli.mvp_video_record.Util.IConstant.AUDIO_TYPE;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_AUDIO_SELECTED_RECORD;
import static com.example.clareli.mvp_video_record.Util.IConstant.ENCODEC_VIDEO_SELECTED_RECORD;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_GOOGLE_H264_ENCODER;
import static com.example.clareli.mvp_video_record.Util.IConstant.VIDEO_TYPE;
import static com.example.clareli.mvp_video_record.Util.LUAudioCodecProfile.mapToAudioProfile;
import static com.example.clareli.mvp_video_record.Util.LUVideoCodecProfile.mapToVideoProfile;

public class LUPresenterControl implements IPresenterControl, IPresenterCallback {
    private final WeakReference<Activity> _messageViewReference;
    private String TAG = "LUPresenterControl";
    private LUICamera _camera;
    private LUPresenterCallback _presenterCallback;
    private Object _systemService;
    private LUViewCallback _viewCallback;
    private CameraDevice _cameraDevice;
    private LUIEncodedVideo _encodedVideo;
    private LUIMuxer _muxer;
    private SurfaceTexture _previewSurTexture;
    private Surface _previewSurface;
    private LUIRecordedAudio _recordedAudio;
    private LUIEncodedAudio _encodedAudio;
    private AudioRecord _micRecord;
    private Queue _audioQueue = new Queue();
    private MediaCodecInfo[] _videoCodecInfos; // video avc codecs
    private MediaCodecInfo[] _audioCodecInfos; // audio aac codecs
    private String selectedVideoCodecName;
    private String selectedAudioCodecName;
    private LUAudioCodecProfile audioCodecProfileAAC;
    private LUVideoCodecProfile videoCodecProfile;  //H264
    private String _selectedVideoFormat;
    private String _selectedAudioFormat;
    private LUBasicUtility _utility;
    private LUEncodeFinder _encodeFinder;

    //constructor
    public LUPresenterControl(Activity activity, LUViewCallback LUViewCallback) {
        _messageViewReference = new WeakReference<>(activity);
        _presenterCallback = new LUPresenterCallback(this);
        _camera = new LUCameraClass(_presenterCallback);
        _viewCallback = LUViewCallback;
        _encodedVideo = new LUEncodedVideo(_presenterCallback);
        _recordedAudio = new LURecordedAudio(_presenterCallback);
        _encodedAudio = new LUEncodedAudio(_presenterCallback);
        _utility = new LUBasicUtility(null, activity);
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
        if((_videoCodecInfos != null)&&(_audioCodecInfos != null)) {
            createMuxer(filePath);
            getPreferenceVideoCodecSettings();
            getPreferenceAudioCodecSettings();

            startVideoRecord(previewSurTexture, width, height);
            startAudioRecord();
        } else {
            Log.i("####","not ready!!!!!!");
        }
    }

    private void getPreferenceAudioCodecSettings() {
        Map<String, String> audioSelectedMap = _utility.loadMapFromPreference(ENCODEC_AUDIO_SELECTED_RECORD);
        if ((audioSelectedMap != null) && (!audioSelectedMap.isEmpty())) {
            audioCodecProfileAAC = mapToAudioProfile(audioSelectedMap);
        }
    }

    private void getPreferenceVideoCodecSettings() {
        Map<String, String> videoSelectedMap = _utility.loadMapFromPreference(ENCODEC_VIDEO_SELECTED_RECORD);
        if ((videoSelectedMap != null) && (!videoSelectedMap.isEmpty())) {
            videoCodecProfile = mapToVideoProfile(videoSelectedMap);
        }
    }

    /*  start to do video record
        _camera.createCaptureSession(...) will pass preview , record surface to builder.addTarget() and set Codec Callback
        In test device Sony XZP android 8.0, using codec VIDEO_GOOGLE_H264_ENCODER =>
        width max:1920, height max:1080, frame rate max:30
     */
    private void startVideoRecord(SurfaceTexture previewSurTexture, int width, int height) {
        int colorFormat;
        int videoBitrate;
        int videoFrameRates;
        if (videoCodecProfile == null) {
            setSelectedVideoCodecName(VIDEO_GOOGLE_H264_ENCODER);
            MediaCodecInfo mediaCodecInfo = getVideoCodecInfo(getSelectedVideoCodecName());
            if (mediaCodecInfo == null) {
                _viewCallback.viewShowErrorDialog("Cannot get Video Codec Info!");
                return;
            }
            LUVideoCodecInfo videoCodecInfo = toVideoCodecInfo(mediaCodecInfo, _selectedVideoFormat);
            colorFormat = videoCodecInfo.getColorFormats()[videoCodecInfo.getColorFormats().length - 1];
            videoBitrate = videoCodecInfo.getBitRatesMax();
            videoFrameRates = 30;

            videoCodecProfile = new LUVideoCodecProfile(getSelectedVideoCodecName(), _selectedVideoFormat,
                    colorFormat, videoBitrate,
                    videoFrameRates, 5, 1920, 1080, videoCodecInfo.getProfileLevels()[videoCodecInfo.getProfileLevels().length - 1]);
            if (isVideoCodecSettingAvailable(videoCodecProfile) == true) {
                Map<String, String> videoSelectedCodec = videoCodecProfile.videoProfileToMap();
                if (_utility.saveMapToPreference(ENCODEC_VIDEO_SELECTED_RECORD, videoSelectedCodec) == false) {
                    _viewCallback.viewShowErrorDialog("Cannot save Video Codec!");
                }

            } else {
                _viewCallback.viewShowErrorDialog("Video Codec settings are invalid!");
            }

        }


        //TODO modify preview result not callback _cameraDevice
        if (_cameraDevice != null) {
            _camera.closePreviewSession();
            _previewSurTexture = previewSurTexture;
            if (_previewSurTexture == null) {
                _viewCallback.viewShowErrorDialog("preview SurTexture is null fail!");
                return;
            }
            _previewSurTexture.setDefaultBufferSize(width, height);
            _encodedVideo.configuredVideoCodec(videoCodecProfile);
            _encodedVideo.startEncode();
            _previewSurface = new Surface(_previewSurTexture);
            _camera.createCaptureSession(_previewSurface, _encodedVideo.getSurface());

        }

    }

    /*2019-2-4, Clare
    channelCount over 2 in Android 8.0, the audio will be destroyed
     */
    public void startAudioRecord() {
        int sampleRateInHz;// = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;//AudioFormat.CHANNEL_IN_MONO;
        int encodingBit = AudioFormat.ENCODING_PCM_16BIT;
        int audio_buffer_times = 1;
        int bufferSize;
        int channelCount = 2;
        int bitRate;
        int lengthSampleRate;
        if (audioCodecProfileAAC == null) {
            setSelectedAudioCodecName(AUDIO_GOOGLE_AAC_ENCODER);
            MediaCodecInfo mediaCodecInfo = getAudioCodecInfo(getSelectedAudioCodecName());
            if (mediaCodecInfo == null) {
                _viewCallback.viewShowErrorDialog("Cannot get Audio Codec Info!");
                return;
            }
            LUAudioCodecInfo audioCodecInfo = toAudioCodecInfo(mediaCodecInfo, _selectedAudioFormat);

            bitRate = audioCodecInfo.getBitRatesMax();
            lengthSampleRate = audioCodecInfo.getSampleRates().length;
            sampleRateInHz = audioCodecInfo.getSampleRates()[lengthSampleRate - 1];
            bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                    channelConfig, encodingBit) * audio_buffer_times;

            audioCodecProfileAAC = new LUAudioCodecProfile(getSelectedAudioCodecName(), _selectedAudioFormat,
                    sampleRateInHz, channelCount, bitRate, MediaCodecInfo.CodecProfileLevel.AACObjectLC, bufferSize);

            if (isAudioCodecSettingAvailable(audioCodecProfileAAC)) {

                Map<String, String> audioSelectedCodec = audioCodecProfileAAC.audioProfileToMap();
                if (_utility.saveMapToPreference(ENCODEC_AUDIO_SELECTED_RECORD, audioSelectedCodec) == false) {
                    _viewCallback.viewShowErrorDialog("Cannot save Audio Codec!");
                }

            } else {
                _viewCallback.viewShowErrorDialog("Audio Codec settings are invalid!");
            }

        } else {
            sampleRateInHz = audioCodecProfileAAC.getSampleRate();
        }

        if (_encodedAudio != null) {
            if (_recordedAudio != null) {
                _micRecord = _recordedAudio.initRecord(sampleRateInHz, channelConfig, encodingBit, audio_buffer_times);
                if (_micRecord != null) {
                    _encodedAudio.configuredAudioCodec(audioCodecProfileAAC);
                    if (_encodedAudio.getCodec() != null) {
                        _encodedAudio.startEncode();
                        _recordedAudio.startRecord();
                        _recordedAudio.getRecordData();

                    }
                }

            } else {
                _viewCallback.viewShowErrorDialog("audio Record Start fail!");

            }

        } else {
            _viewCallback.viewShowErrorDialog("audio Record encoded fail!");
        }

    }

    @Override
    public void stopRecord() {
        stopVideoRecord();
        stopAudioRecord();
        _encodedVideo.releaseEncode();
        _recordedAudio.releaseEncode();
        _encodedAudio.releaseEncode();
    }

    /*
    press stop record button will stop codec, muxer and camera preview.
     */
    private void stopVideoRecord() {
        _encodedVideo.stopEncode();
        _camera.startPreview(_previewSurface);
    }

    @Override
    public void stopVideoEncode() {
        _encodedVideo.stopEncode();
    }

    private void stopAudioRecord() {

        _encodedAudio.stopEncode();
        _recordedAudio.stopRecord();
        signalEndOfStream();

    }


    private void signalEndOfStream() {
        MediaCodec.BufferInfo eos = new MediaCodec.BufferInfo();
        ByteBuffer buffer = ByteBuffer.allocate(0);
        eos.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        _muxer.writeSampleData(buffer, eos, 1);
        _muxer.writeSampleData(buffer, eos, 2);

    }

    @Override
    public void createMuxer(String dstPath) {
        int saveOutputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;
        if (_muxer == null) {

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


    ///////////////////////Video Codec Buffer callback///////////////////////

    /*2019-01-20, Clare
    from LUEncodedVideo.java
    prepare for muxer to write data to file
     */
    @Override
    public void onVideoOutputFormatChanged(final MediaFormat format) {
        //the format include "csd-0" and "csd-1" byte buffers


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

    ///////////////////////Audio Codec Buffer callback///////////////////////

    @Override
    public void notifyToAccessBuffer(byte[] rowData, long presentationTimeStamp, boolean eos) {


        AudioRawData audioRawData = new AudioRawData(rowData, presentationTimeStamp, eos);
        _audioQueue.push(audioRawData);

    }

    @Override
    public void onAudioOutputFormatChanged(MediaFormat format) {
        _muxer.setMuxerMediaFormat(format);

    }

    @Override
    public void onAudioOutputBufferAvailable(final MediaCodec.BufferInfo info, final ByteBuffer encodedData) {

        if (_muxer != null && _muxer.isMuxerStarted()) {
            _messageViewReference.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    _muxer.writeSampleData(encodedData, info, 2);
                }
            });

        }

    }

    /*2019-01-28, Clare
    after audioRecord.stop(), we cannot call audioRecord.getRecordingState()
    * */
    @Override
    public void onAudioInputBufferAvailable(ByteBuffer inputBuffer, int index) {
        int flags = BUFFER_FLAG_KEY_FRAME;
        _recordedAudio.getRecordData();
        if (_audioQueue.isEmpty() == false) {
            AudioRawData audioTempData = (AudioRawData) _audioQueue.pop();
            if (audioTempData != null) {
                if (audioTempData.getEOS()) {
                    flags = BUFFER_FLAG_END_OF_STREAM;
                }
                if ((audioTempData.getRowData() != null) && (audioTempData.getRowData().length > 0)) {
                    inputBuffer.put(audioTempData.getRowData());

                    _encodedAudio.queueInputBuffer(index, 0, audioTempData.getRowData().length, audioTempData.getPresentationTimeStamp(), flags);

                }
            }

        } else {
            MediaCodec.BufferInfo eos = new MediaCodec.BufferInfo();
            ByteBuffer buffer = ByteBuffer.allocate(0);
            long presentationTimeStamp = System.nanoTime() / 1000;
            flags = BUFFER_FLAG_END_OF_STREAM;
            eos.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            _encodedAudio.queueInputBuffer(index, 0, buffer.capacity(), presentationTimeStamp, flags);

        }


    }

    ////////////////find all supported video codecs spec for "video/avc"、"audio/aac" //////////

    @Override
    public void findAllSupportedCodecs() {
        _encodeFinder = new LUEncodeFinder(_presenterCallback);
    }

    @Override
    public void separateCodecs(String formatType) {
        if(formatType.contains(VIDEO_TYPE)){
            _selectedVideoFormat = formatType;
            _videoCodecInfos = _encodeFinder.findEncodersByType(_selectedVideoFormat);
            if(_videoCodecInfos != null)
                _viewCallback.isVideoSettingAllowed(true);
            else
                _viewCallback.isVideoSettingAllowed(false);
        } else if(formatType.contains(AUDIO_TYPE)){
            _selectedAudioFormat  = formatType;
            _audioCodecInfos = _encodeFinder.findEncodersByType(_selectedAudioFormat);
            if(_audioCodecInfos != null)
                _viewCallback.isAudioSettingAllowed(true);
            else
                _viewCallback.isAudioSettingAllowed(false);
        }

//        _encodeFinder.startEncoderFinder(formatType);
    }


    @Override
    public void getVideoEncodeResult(MediaCodecInfo[] infos) {
        if((infos != null) && (infos.length > 0)) {
            _videoCodecInfos = infos;
            _viewCallback.isVideoSettingAllowed(true);
            //just show the original content
            LUEncodeFinder.logCodecInfos(infos, _selectedVideoFormat);
        }
    }

    ////////////////find all supported video codecs spec for "audio/mp4a-latm" //////////
    @Override
    public void getAudioEncodeResult(MediaCodecInfo[] infos) {
        if((infos != null) && (infos.length > 0)) {
            _audioCodecInfos = infos;
            _viewCallback.isAudioSettingAllowed(true);
            //just show the original content
            LUEncodeFinder.logCodecInfos(infos, _selectedAudioFormat);
        }
    }

    private void setSelectedVideoCodecName(String videoCodecName) {
        selectedVideoCodecName = videoCodecName;//VIDEO_GOOGLE_H264_ENCODER;
    }

    private void setSelectedAudioCodecName(String audioCodecName) {
        selectedAudioCodecName = audioCodecName;//AUDIO_GOOGLE_AAC_ENCODER;
    }

    private String getSelectedVideoCodecName() {
        //TODO using default value
        return selectedVideoCodecName;
    }

    private String getSelectedAudioCodecName() {
        //TODO using default value
        return selectedAudioCodecName;
    }

    private MediaCodecInfo getVideoCodecInfo(String codecName) {
        if (codecName == null) return null;
        if (_videoCodecInfos == null) {
            if(_encodeFinder.getEncoderInfos() != null) {
                _videoCodecInfos = _encodeFinder.findEncodersByType(_selectedVideoFormat);
            }
        }
        MediaCodecInfo tempInfo = null;
        for (int i = 0; i < _videoCodecInfos.length; i++) {
            MediaCodecInfo info = _videoCodecInfos[i];
            if (info.getName().equals(codecName)) {
                tempInfo = info;
                break;
            }
        }
        if (tempInfo == null) return null;
        return tempInfo;
    }

    private MediaCodecInfo getAudioCodecInfo(String codecName) {
        if (codecName == null) return null;
        if (_audioCodecInfos == null) {
            if(_encodeFinder.getEncoderInfos() != null) {
                _audioCodecInfos = _encodeFinder.findEncodersByType(_selectedAudioFormat);
            }
        }
        MediaCodecInfo codec = null;
        for (int i = 0; i < _audioCodecInfos.length; i++) {
            MediaCodecInfo info = _audioCodecInfos[i];
            if (info.getName().equals(codecName)) {
                codec = info;
                break;
            }
        }
        if (codec == null) return null;
        return codec;
    }

    private LUVideoCodecInfo toVideoCodecInfo(MediaCodecInfo info, String mimeType) {
        MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(mimeType);
        MediaCodecInfo.VideoCapabilities videoCaps = caps.getVideoCapabilities();
        String codecName = info.getName();
        List tempList = new ArrayList(Arrays.asList(info.getSupportedTypes()));
        tempList.remove("[");
        tempList.remove("]");
        String supportedType = tempList.toString();
        int widthMax = videoCaps.getSupportedWidths().getUpper();
        int widthMin = videoCaps.getSupportedWidths().getLower();
        int heightMax = videoCaps.getSupportedHeights().getUpper();
        int heightMin = videoCaps.getSupportedHeights().getLower();
        int frameRatesMax = videoCaps.getSupportedFrameRates().getUpper();
        int frameRatesMin = videoCaps.getSupportedFrameRates().getLower();
        int bitRatesMax = videoCaps.getBitrateRange().getUpper();
        int bitRatesMin = videoCaps.getBitrateRange().getLower();
        MediaCodecInfo.CodecProfileLevel[] levels = caps.profileLevels;

        int[] colorFormats = caps.colorFormats;

        LUVideoCodecInfo videoCodecInfo = new LUVideoCodecInfo(codecName, supportedType, widthMax, widthMin,
                heightMax, heightMin, frameRatesMax, frameRatesMin, bitRatesMax, bitRatesMin, levels, colorFormats);

        return videoCodecInfo;
    }

    private LUAudioCodecInfo toAudioCodecInfo(MediaCodecInfo info, String mimeType) {
        MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(mimeType);
        MediaCodecInfo.AudioCapabilities audioCaps = caps.getAudioCapabilities();
        int[] sampleRates = Arrays.copyOf(audioCaps.getSupportedSampleRates(), audioCaps.getSupportedSampleRates().length);
        String codecName = info.getName();
        List tempList = new ArrayList(Arrays.asList(info.getSupportedTypes()));
        tempList.remove("[");
        tempList.remove("]");
        String supportedType = tempList.toString();
        int channelMax = audioCaps.getMaxInputChannelCount();
        int bitRatesMax = audioCaps.getBitrateRange().getUpper();
        int bitRatesMin = audioCaps.getBitrateRange().getLower();
        LUAudioCodecInfo audioCodecInfo = new LUAudioCodecInfo(codecName, supportedType, sampleRates,
                bitRatesMax, bitRatesMin, channelMax);
        return audioCodecInfo;

    }

    private boolean isVideoCodecSettingAvailable(LUVideoCodecProfile videoCodecSettings) {
        boolean result = false;
        String codecName = getSelectedVideoCodecName();
        MediaCodecInfo codec = getVideoCodecInfo(codecName);
        if (codec == null) return false;
        MediaCodecInfo.CodecCapabilities capabilities = codec.getCapabilitiesForType(_selectedVideoFormat);
        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
        if (!videoCapabilities.isSizeSupported(videoCodecSettings.getWidth(), videoCodecSettings.getHeight())) {
            _viewCallback.viewShowErrorDialog("Video Codec width and height are not supported!");
            return false;
        }

        if (!videoCapabilities.areSizeAndRateSupported(videoCodecSettings.getWidth(), videoCodecSettings.getHeight(), videoCodecSettings.getVideoFrameRates())) {
            _viewCallback.viewShowErrorDialog("Video Codec frameRates is not supported!");
            return false;
        }
        if (!videoCapabilities.getBitrateRange().contains(videoCodecSettings.getVideoBitrate())) {
            _viewCallback.viewShowErrorDialog("Video Codec bitrate is not supported!");
            return false;
        }
        MediaCodecInfo.CodecProfileLevel[] profiles = capabilities.profileLevels;
        for (int i = 0; i < profiles.length; i++) {
            if (profiles[i] == videoCodecSettings.getProfileLevel()) {
                result = true;
                break;
            }
        }
        if (result == false) {
            _viewCallback.viewShowErrorDialog("Video Codec profileLevel is not supported!");
            return false;
        }

        int[] colorFormats = capabilities.colorFormats;
        for (int colorFormat : colorFormats) {
            if (videoCodecSettings.getColorFormat() == colorFormat) {
                result = true;
                break;
            }
        }

        if (result == false) {
            _viewCallback.viewShowErrorDialog("Video Codec color Formats is not supported!");
        }
        return result;

    }


    public boolean isAudioCodecSettingAvailable(LUAudioCodecProfile audioCodecProfile) {
        boolean result = true;
        String codecName = getSelectedAudioCodecName();
        MediaCodecInfo codec = getAudioCodecInfo(codecName);
        if (codec == null) return false;
        MediaCodecInfo.CodecCapabilities capabilities = codec.getCapabilitiesForType(_selectedAudioFormat);
        MediaCodecInfo.AudioCapabilities audioCapabilities = capabilities.getAudioCapabilities();
        if (!audioCapabilities.isSampleRateSupported(audioCodecProfile.getSampleRate())) {
            _viewCallback.viewShowErrorDialog("Audio Codec sampleRate is not supported!");
            return false;
        }
        if (!audioCapabilities.getBitrateRange().contains(audioCodecProfile.getBitRate())) {
            _viewCallback.viewShowErrorDialog("Audio Codec bitRate is not supported!");
            return false;
        }
        if (audioCodecProfile.getChannelCount() > audioCapabilities.getMaxInputChannelCount()) {
            _viewCallback.viewShowErrorDialog("Audio Codec channel count is over!");
            return false;
        }

        return result;
    }

    //////////////////////////////error msg//////////////////////////////
    @Override
    public void errorCameraCallback(String msg) {
        _viewCallback.viewShowErrorDialog(msg);
    }

    @Override
    public void muxerErrorCallback(String msg) {
        Log.i(TAG, msg);
        _viewCallback.viewShowErrorDialog(msg);
    }

    @Override
    public void encodedVideoErrorCallback(String msg) {
        Log.i(TAG, msg);
        _viewCallback.viewShowErrorDialog(msg);

    }

    @Override
    public void encodedAudioErrorCallback(String msg) {
        Log.i(TAG, msg);
        _viewCallback.viewShowErrorDialog(msg);
    }

    @Override
    public void recordedAudioErrorCallback(String msg) {
        Log.i(TAG, msg);
        _viewCallback.viewShowErrorDialog(msg);
    }

    private class Queue {
        private LinkedList list = new LinkedList();

        public void push(Object obj) {
            list.addLast(obj);
        }

        public Object pop() {
            return list.removeFirst();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }
    }

    private class AudioRawData {
        public byte[] rowData;
        public long presentationTimeStamp;
        public boolean eos;

        public AudioRawData(byte[] rowData, long presentationTimeStamp, boolean eos) {
            this.rowData = rowData;
            this.presentationTimeStamp = presentationTimeStamp;
            this.eos = eos;
        }

        public long getPresentationTimeStamp() {
            return presentationTimeStamp;
        }

        public boolean getEOS() {
            return eos;
        }

        public byte[] getRowData() {
            return rowData;
        }
    }


}
