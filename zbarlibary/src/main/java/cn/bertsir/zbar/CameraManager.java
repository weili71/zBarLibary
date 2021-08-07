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
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

import cn.bertsir.zbar.utils.QRUtils;

/**
 * <p>Camera manager.</p>
 */
public final class CameraManager {
    private static final String TAG = "CameraManager";

    private final CameraConfiguration configuration;
    private Context context;

    private Camera camera;

    public CameraManager(Context context) {
        this.context = context;
        this.configuration = new CameraConfiguration(context);
    }

    /**
     * Opens the mCamera driver and initializes the hardware parameters.
     *
     * @throws Exception ICamera open failed, occupied or abnormal.
     */
    public synchronized void openDriver() throws Exception {
        if (camera != null) return;

        camera = Camera.open();
        if (camera == null) throw new IOException("The camera is occupied.");

        configuration.initFromCameraParameters(camera);

        Camera.Parameters parameters = camera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten();
        try {
            configuration.setDesiredCameraParameters(camera, false);

        } catch (RuntimeException re) {
            if (parametersFlattened != null) {
                parameters = camera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    camera.setParameters(parameters);

                    configuration.setDesiredCameraParameters(camera, true);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    /**
     * Camera is opened.
     *
     * @return true, other wise false.
     */
    public boolean isOpen() {
        return camera != null;
    }

    /**
     * Get camera configuration.
     *
     * @return {@link CameraConfiguration}.
     */
    public CameraConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Camera start preview.
     *
     * @param holder          {@link SurfaceHolder}.
     * @param previewCallback {@link Camera.PreviewCallback}.
     * @throws IOException if the method fails (for example, if the surface is unavailable or unsuitable).
     */
    public void startPreview(SurfaceHolder holder, Camera.PreviewCallback previewCallback) throws IOException {
        if (camera != null) {
            //解决nexus5x扫码倒立的情况
            if(android.os.Build.MANUFACTURER.equals("LGE") &&
                    android.os.Build.MODEL.equals("Nexus 5X")) {
                camera.setDisplayOrientation(QRUtils.getInstance().isScreenOrientationPortrait(context) ? 270 : 180);
            }else {
                camera.setDisplayOrientation(QRUtils.getInstance().isScreenOrientationPortrait(context) ? 90 : 0);
            }
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(previewCallback);
            camera.startPreview();
        }
    }


    /**
     * Camera stop preview.
     */
    public void stopPreview() {
        if (camera != null) {
            try {
                camera.stopPreview();
            } catch (Exception ignored) {
                // nothing.
            }
            try {
                camera.setPreviewDisplay(null);
            } catch (IOException ignored) {
                // nothing.
            }
        }
    }

    /**
     * Focus on, make a scan action.
     *
     * @param callback {@link Camera.AutoFocusCallback}.
     */
    public void autoFocus(Camera.AutoFocusCallback callback) {
        if (camera != null)
            try {
                camera.autoFocus(callback);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * set Camera Flash
     */
    public void setFlash(){
        if(camera != null){
            Camera.Parameters parameters = camera.getParameters();
            if(parameters.getFlashMode() == null) {
                return;
            }
            if(parameters.getFlashMode().endsWith(Camera.Parameters.FLASH_MODE_TORCH)){
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            camera.setParameters(parameters);
        }
    }

    /**
     * set Camera Flash
     */
    public void setFlash(boolean open){
        if(camera != null){
            Camera.Parameters parameters = camera.getParameters();
            if(parameters.getFlashMode() == null) {
                return;
            }
            if(!open){
                if(parameters.getFlashMode().endsWith(Camera.Parameters.FLASH_MODE_TORCH)){
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
            }else {
                if(parameters.getFlashMode().endsWith(Camera.Parameters.FLASH_MODE_OFF)){
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }
            camera.setParameters(parameters);
        }
    }

    /**
     * 相机设置焦距
     */
    public void setCameraZoom(float ratio){
        if(camera != null){
            Camera.Parameters parameters = camera.getParameters();
            if(!parameters.isZoomSupported()){
                return;
            }
            int maxZoom = parameters.getMaxZoom();
            if(maxZoom == 0){
                return;
            }
            int zoom = (int) (maxZoom * ratio);
            parameters.setZoom(zoom);
            camera.setParameters(parameters);
        }
    }


    public void handleZoom(boolean isZoomIn) {
        if(camera != null){
            Camera.Parameters params = camera.getParameters();
            if (params.isZoomSupported()) {
                int maxZoom = params.getMaxZoom();
                int zoom = params.getZoom();
                if (isZoomIn && zoom < maxZoom) {
                    zoom++;
                } else if (zoom > 0) {
                    zoom--;
                }
                params.setZoom(zoom);
                camera.setParameters(params);
            } else {
                Log.i(TAG, "zoom not supported");
            }
        }
    }
}
