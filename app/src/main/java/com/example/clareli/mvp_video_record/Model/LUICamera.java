package com.example.clareli.mvp_video_record.Model;


import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaCodec;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.List;

public interface LUICamera {
    void startPreview(Surface previewSurface);
    void openCamera(int width, int height, String cameraID, CameraManager manager, SurfaceTexture surfaceTexture);
    void closeCamera();
    void createCaptureSession(Surface previewSurface, final Surface recordSurface);
    void closePreviewSession();
    boolean tryToGetAcquire(long timeout);

}
