package cn.bertsir.zbar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.QrConfig;
import cn.bertsir.zbar.databinding.ViewScanBinding;


/**
 * Created by Bert on 2017/9/20.
 */

public class ScanView extends FrameLayout {

    private static int CURRENT_TYPE = 1;
    private ArrayList<CornerView> cornerViews;
    private int lineSpeed = 3000;
    private ViewScanBinding binding;

    public ScanView(Context context) {
        super(context);
        initView(context);
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewScanBinding.inflate(LayoutInflater.from(context), this, true);

        cornerViews = new ArrayList<>();
        cornerViews.add(binding.leftTop);
        cornerViews.add(binding.leftBottom);
        cornerViews.add(binding.rightTop);
        cornerViews.add(binding.rightBottom);

        getViewWidthHeight();

    }

    /**
     * 设置扫描速度
     *
     * @param speed
     */
    public void setLineSpeed(int speed) {
        binding.scanLine.setScanAnimatorDuration(speed);
    }


    /**
     * 设置扫描样式
     */
    public void setScanLineStyle(int style) {
        binding.scanLine.setScanStyle(style);
    }

    public void setScannerWidth(int width) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.scan.getLayoutParams();
        params.width = width;
        binding.scan.setLayoutParams(params);
    }

    public void setScannerHeight(int height) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.scan.getLayoutParams();
        params.height = height;
        binding.scan.setLayoutParams(params);
    }

    public void setScannerSize(int width, int height) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.scan.getLayoutParams();
        params.width = width;
        params.height = height;
        binding.scan.setLayoutParams(params);
    }

    public void setType(int type) {
        CURRENT_TYPE = type;

    }

    public void setCornerColor(int color) {
        for (int i = 0; i < cornerViews.size(); i++) {
            cornerViews.get(i).setColor(color);
        }
    }

    public void setCornerWidth(int dp) {
        for (int i = 0; i < cornerViews.size(); i++) {
            cornerViews.get(i).setLineWidth(dp);
        }
    }

    public void setLineColor(int color) {
        binding.scanLine.setScancolor(color);
    }

    public void getViewWidthHeight() {
        binding.scan.post(new Runnable() {
            @Override
            public void run() {
                Symbol.cropWidth = binding.scan.getWidth();
                Symbol.cropHeight = binding.scan.getHeight();
            }
        });
    }


}
