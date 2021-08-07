package cn.bertsir.zbar;

import android.graphics.Color;

import androidx.annotation.DrawableRes;

import java.io.Serializable;

import cn.bertsir.zbar.view.ScanLineView;

/**
 * Created by Bert on 2017/9/22.
 */

public class QrConfig implements Serializable {


    public static final int LINE_FAST = 1000;
    public static final int LINE_MEDIUM = 2000;
    public static final int LINE_SLOW = 3000;


    public int CORNER_COLOR = Color.parseColor("#ff5f00");
    public int LINE_COLOR = Color.parseColor("#ff5f00");

    public int TITLE_BACKGROUND_COLOR = Color.parseColor("#ff5f00");
    public int TITLE_TEXT_COLOR = Color.parseColor("#ffffff");

    public boolean showTitle = true;
    public boolean showLight = true;
    public boolean showAlbum = true;
    public boolean showDesc = true;
    public boolean needCrop = true;
    public boolean showZoom = false;
    public boolean autoZoom = false;
    public boolean fingerZoom = false;
    public boolean onlyCenter = false;
    public boolean playSound = true;
    public boolean doubleEngine = false;
    public boolean loopScan = false;
    public boolean showVibrator = false;
    public String title = "扫描二维码";
    public String descText = "(识别二维码)";
    public String openAlbumText = "选择要识别的图片";
    public int lineSpeed = LINE_FAST;
    public int lineStyle = ScanLineView.styleHybrid;
    public int cornerWidth = 10;
    public int loopWaitTime = 0;

    public int backImgRes = R.drawable.scanner_back_img;
    public int flashImgRes = R.drawable.scanner_light;
    public int albumImgRes = R.drawable.scanner_album;

    public static final int DEFAULT_SCANNER_WIDTH = -1;
    public static final int DEFAULT_SCANNER_HEIGHT = -1;

    public int scannerWidth = DEFAULT_SCANNER_WIDTH;
    public int scannerHeight = DEFAULT_SCANNER_WIDTH;

    public boolean autoLight = false;


    public static int dingPath = R.raw.qrcode;//默认声音
    public int customBarCodeFormat = -1;

    public static final int TYPE_QRCODE = 1;//扫描二维码
    public static final int TYPE_BARCODE = 2;//扫描条形码（UPCA）
    public static final int TYPE_ALL = 3;//扫描全部类型码
    public static final int TYPE_CUSTOM = 4;//扫描用户定义类型码

    public static final int SCANVIEW_TYPE_QRCODE = 1;//二维码框
    public static final int SCANVIEW_TYPE_BARCODE = 2;//条形码框
    public static final int SCREEN_PORTRAIT = 1;//屏幕纵向
    public static final int SCREEN_LANDSCAPE = 2;//屏幕横向
    public static final int SCREEN_SENSOR = 3;//屏幕自动

    public int scanType = TYPE_QRCODE;//默认只扫描二维码
    public int scanViewType = SCANVIEW_TYPE_QRCODE;//默认为二维码扫描框

    public final static int REQUEST_CAMERA = 99;
    public final static String EXTRA_THIS_CONFIG = "extra_this_config";

    public int SCREEN_ORIENTATION = SCREEN_PORTRAIT;

    /**
     * EAN-8.
     */
    public static final int BARCODE_EAN8 = 8;
    /**
     * UPC-E.
     */
    public static final int BARCODE_UPCE = 9;
    /**
     * ISBN-10 (from EAN-13).
     */
    public static final int BARCODE_ISBN10 = 10;
    /**
     * UPC-A.
     */
    public static final int BARCODE_UPCA = 12;
    /**
     * EAN-13.
     */
    public static final int BARCODE_EAN13 = 13;
    /**
     * ISBN-13 (from EAN-13).
     */
    public static final int BARCODE_ISBN13 = 14;
    /**
     * Interleaved 2 of 5.
     */
    public static final int BARCODE_I25 = 25;
    /**
     * DataBar (RSS-14).
     */
    public static final int BARCODE_DATABAR = 34;
    /**
     * DataBar Expanded.
     */
    public static final int BARCODE_DATABAR_EXP = 35;
    /**
     * Codabar.
     */
    public static final int BARCODE_CODABAR = 38;
    /**
     * Code 39.
     */
    public static final int BARCODE_CODE39 = 39;
    /**
     * PDF417.
     */
    public static final int BARCODE_PDF417 = 57;

    /**
     * Code 93.
     */
    public static final int BARCODE_CODE93 = 93;
    /**
     * Code 128.
     */
    public static final int BARCODE_CODE128 = 128;


    public int getScannerWidth() {
        return scannerWidth;
    }

    public int getScannerHeight() {
        return scannerHeight;
    }

    public int getScanType() {
        return scanType;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public int getCornerColor() {
        return CORNER_COLOR;
    }

    public int getLineColor() {
        return LINE_COLOR;
    }

    public int getTitleBackgroundColor() {
        return TITLE_BACKGROUND_COLOR;
    }

    public int getTitleTextColor() {
        return TITLE_TEXT_COLOR;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public boolean isShowLight() {
        return showLight;
    }

    public boolean isShowAlbum() {
        return showAlbum;
    }

    public boolean isShowDesc() {
        return showDesc;
    }

    public boolean isNeedCrop() {
        return needCrop;
    }

    public String getTitle() {
        return title;
    }

    public String getDescText() {
        return descText;
    }

    public String getOpenAlbumText() {
        return openAlbumText;
    }

    public int getLineSpeed() {
        return lineSpeed;
    }

    public int getLineStyle() {
        return lineStyle;
    }

    public int getCornerWidth() {
        return cornerWidth;
    }

    public int getCustomBarCodeFormat() {
        return customBarCodeFormat;
    }

    public int getScanViewType() {
        return scanViewType;
    }

    public boolean isOnlyCenter() {
        return onlyCenter;
    }

    public static int getDingPath() {
        return dingPath;
    }

    public boolean isShowZoom() {
        return showZoom;
    }

    public boolean isAutoZoom() {
        return autoZoom;
    }

    public boolean isFingerZoom() {
        return fingerZoom;
    }

    public int getScreenOrientation() {
        return SCREEN_ORIENTATION;
    }

    public boolean isDoubleEngine() {
        return doubleEngine;
    }

    public boolean isLoopScan() {
        return loopScan;
    }

    public int getLoopWaitTime() {
        return loopWaitTime;
    }

    public boolean isAutoLight() {
        return autoLight;
    }


    public boolean isEnableVibrator() {
        return showVibrator;
    }

    public int getBackImgRes() {
        return backImgRes;
    }

    public int getLightImageRes() {
        return flashImgRes;
    }

    public int getAlbumImageRes() {
        return albumImgRes;
    }


    public static class Builder {
        private QrConfig watcher;

        public Builder() {
            watcher = new QrConfig();
        }

        public Builder setScannerWidth(int width) {
            watcher.scannerWidth = width;
            return this;
        }

        public Builder setScannerHeight(int height) {
            watcher.scannerHeight = height;
            return this;
        }

        public Builder setScannerSize(int width, int height) {
            watcher.scannerWidth = width;
            watcher.scannerHeight = height;
            return this;
        }

        public Builder setLineSpeed(int speed) {
            watcher.lineSpeed = speed;
            return this;
        }

        public Builder setLineColor(int color) {
            watcher.LINE_COLOR = color;
            return this;
        }

        public Builder setCornerColor(int color) {
            watcher.CORNER_COLOR = color;
            return this;
        }

        public Builder setCornerWidth(int dp) {
            watcher.cornerWidth = dp;
            return this;
        }

        public Builder setDesText(String text) {
            watcher.descText = text;
            return this;
        }

        public Builder setTitleText(String text) {
            watcher.title = text;
            return this;
        }

        public Builder setShowTitle(boolean show) {
            watcher.showTitle = show;
            return this;
        }

        public Builder setShowLight(boolean show) {
            watcher.showLight = show;
            return this;
        }

        public Builder setShowAlbum(boolean show) {
            watcher.showAlbum = show;
            return this;
        }

        public Builder setShowDes(boolean show) {
            watcher.showDesc = show;
            return this;
        }

        public Builder setNeedCrop(boolean crop) {
            watcher.needCrop = crop;
            return this;
        }

        public Builder setTitleBackgroudColor(int color) {
            watcher.TITLE_BACKGROUND_COLOR = color;
            return this;
        }

        public Builder setTitleTextColor(int color) {
            watcher.TITLE_TEXT_COLOR = color;
            return this;
        }

        public Builder setScanType(int type) {
            watcher.scanType = type;
            return this;
        }

        public Builder setPlaySound(boolean play) {
            watcher.playSound = play;
            return this;
        }

        public Builder setCustomBarCodeFormat(int format) {
            watcher.customBarCodeFormat = format;
            return this;
        }

        public Builder setScanViewType(int type) {
            watcher.scanViewType = type;
            return this;
        }

        public Builder setIsOnlyCenter(boolean isOnlyCenter) {
            watcher.onlyCenter = isOnlyCenter;
            return this;
        }

        public Builder setDingPath(int ding) {
            watcher.dingPath = ding;
            return this;
        }

        @Deprecated
        public Builder setShowZoom(boolean zoom) {
            watcher.showZoom = zoom;
            return this;
        }

        public Builder setAutoZoom(boolean auto) {
            watcher.autoZoom = auto;
            return this;
        }

        public Builder setFingerZoom(boolean auto) {
            watcher.fingerZoom = auto;
            return this;
        }


        public Builder setScreenOrientation(int SCREEN_ORIENTATION) {
            watcher.SCREEN_ORIENTATION = SCREEN_ORIENTATION;
            return this;
        }

        public Builder setDoubleEngine(boolean open) {
            watcher.doubleEngine = open;
            return this;
        }

        public Builder setOpenAlbumText(String text) {
            watcher.openAlbumText = text;
            return this;
        }

        public Builder setLooperScan(boolean looper) {
            watcher.loopScan = looper;
            return this;
        }

        public Builder setLooperWaitTime(int time) {
            watcher.loopWaitTime = time;
            return this;
        }

        public Builder setScanLineStyle(int style) {
            watcher.lineStyle = style;
            return this;
        }

        public Builder setAutoLight(boolean light) {
            watcher.autoLight = light;
            return this;
        }

        public Builder setShowVibrator(boolean vibrator) {
            watcher.showVibrator = vibrator;
            return this;
        }

        public Builder setBackImageRes(@DrawableRes int res) {
            watcher.backImgRes = res;
            return this;
        }

        public Builder setLightImageRes(@DrawableRes int res) {
            watcher.flashImgRes = res;
            return this;
        }

        public Builder setAlbumImageRes(@DrawableRes int res) {
            watcher.albumImgRes = res;
            return this;
        }

        public QrConfig create() {
            return watcher;
        }
    }

}
