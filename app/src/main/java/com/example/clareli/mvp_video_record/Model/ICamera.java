package com.example.clareli.mvp_video_record.Model;


import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaCodec;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.List;

public interface ICamera {
    void startPreview(Surface previewSurface);
    void openCamera(int width, int height, String cameraID, CameraManager manager, SurfaceTexture surfaceTexture);
    void closeCamera();
    void startBackgroundThread();
    void stopBackgroundThread();
    void createCaptureSession(Surface previewSurface, final Surface recordSurface, String filePath);
    void closePreviewSession();
    boolean tryToGetAcquire();

}
