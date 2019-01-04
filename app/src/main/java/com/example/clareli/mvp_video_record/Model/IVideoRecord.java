package com.example.clareli.mvp_video_record.Model;


import com.example.clareli.mvp_video_record.View.AutoFitTextureView;

public interface IVideoRecord {
    void setAudioSource(int micSource);
    void setVideoSource(int surface);
    void setVideoOutputFormat(int outputFormat); //MediaRecorder.OutputFormat.MPEG_4
    void setVideoOutputFile(String outputPath);
    void setVideoEncodingBitRate(int rate);
    void setVideoFrameRate(int rate); //rate = 30 or 60
    void setVideoSize(int width, int height);
    void setSurfaceTextureSize(int width, int height);
    void setVideoEncoder(int videoEncoder); //MediaRecorder.VideoEncoder.H264
    void setAudioEncoder(int audioEncoder); //MediaRecorder.AudioEncoder.AAC
    void setupVideoRecord();
    void startPreview();
    void startRecordingVideo(String filePath);
    void stopRecordingVideo();
    void openCamera(int width, int height);
    void closeCamera();
    void sendTextureView(AutoFitTextureView textureView);
    void startBackgroundThread();
    void stopBackgroundThread();
    void setInitSetting(Object systemService, int rotation, int orientation);
    void configureTransform(int viewWidth, int viewHeight);

}
