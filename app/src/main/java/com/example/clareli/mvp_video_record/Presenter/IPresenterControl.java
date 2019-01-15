package com.example.clareli.mvp_video_record.Presenter;


import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewErrorCallback;

public interface IPresenterControl {
    void videoPreviewStart(AutoFitTextureView textureView, IViewErrorCallback iViewErrorCallback);
    void closeCamera();
    void videoRecordStart(String filePath);
    void videoRecordStop();
}
