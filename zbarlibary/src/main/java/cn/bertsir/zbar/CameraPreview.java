/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.bertsir.zbar;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * <p>QRCode Camera preview, include QRCode recognition.</p>
 */
public class CameraPreview extends FrameLayout implements SurfaceHolder.Callback {

    private CameraManager cameraManager;
    private CameraScanAnalysis previewCallback;
    private SurfaceView surfaceView;
    private boolean isPreviewStart = false;

    public CameraPreview(Context context) {
        this(context, null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        cameraManager = new CameraManager(context);
        previewCallback = new CameraScanAnalysis(context);
    }

    /**
     * Set Scan results callback.
     *
     * @param callback {@link ScanCallback}.
     */
    public void setScanCallback(ScanCallback callback) {
        previewCallback.setScanCallback(callback);
    }

    /**
     * Camera start preview.
     */
    public boolean start() {
        try {
            cameraManager.openDriver();
        } catch (Exception e) {
            Toast.makeText(getContext(), "摄像头权限被拒绝！", Toast.LENGTH_SHORT).show();
            return false;
        }
        previewCallback.onStart();

        if (surfaceView == null) {
            surfaceView = new SurfaceView(getContext());
            addView(surfaceView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            SurfaceHolder holder = surfaceView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        startCameraPreview(surfaceView.getHolder());
        return true;
    }

    /**
     * Camera stop preview.
     */
    public void stop() {
        removeCallbacks(autoFocusTask);
        previewCallback.onStop();

        cameraManager.stopPreview();
        cameraManager.closeDriver();
    }

    private void startCameraPreview(SurfaceHolder holder) {
        try {
            cameraManager.startPreview(holder, previewCallback);
            cameraManager.autoFocus(focusCallback);
            isPreviewStart = true;
        } catch (Exception e) {
            e.printStackTrace();
            //如果异常延迟200ms再试
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cameraManager.autoFocus(focusCallback);
                }
            }, 200);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        cameraManager.stopPreview();
        startCameraPreview(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private Camera.AutoFocusCallback focusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            postDelayed(autoFocusTask, 500);
        }
    };

    private Runnable autoFocusTask = new Runnable() {
        public void run() {
            cameraManager.autoFocus(focusCallback);
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    public void setFlash() {
        cameraManager.setFlash();
    }

    public void setFlash(boolean open) {
        cameraManager.setFlash(open);
    }

    public void setZoom(float zoom){
        cameraManager.setCameraZoom(zoom);
    }

    public void handleZoom(boolean isZoomIn){
        cameraManager.handleZoom(isZoomIn);
    }

    public boolean isPreviewStart(){
        return this.isPreviewStart;
    }
}