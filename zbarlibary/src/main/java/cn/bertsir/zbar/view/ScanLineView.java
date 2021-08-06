package cn.bertsir.zbar.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import cn.bertsir.zbar.R;

/**
 * Created by Bert on 2019-09-16.
 * Mail: bertsir@163.com
 */
public class ScanLineView extends View {

    private static final String TAG = "ScanView";

    public static final int styleGrid = 0;//扫描区域的样式
    public static final int styleRadar = 1;
    public static final int styleHybrid = 2;
    public static final int styleLine = 3;


    private Rect frame;//最佳扫描区域的Rect

    private Paint scanPaintGrid;//网格样式画笔
    private Paint scanPaintRadio;//雷达样式画笔
    private Paint scanPaintLine;//线条样式画笔

    private Path boundaryLinePath;//边框path
    private Path gridPath;//网格样式的path

    private LinearGradient linearGradientRadar;//雷达样式的画笔shader
    private LinearGradient linearGradientGrid;//网格画笔的shader
    private LinearGradient linearGradientLine;
    private float gridLineWidth = 2;//网格线的线宽，单位pix
    private int gridDensity = 40;//网格样式的，网格密度，值越大越密集


    private float cornerLineLen = 50f;//根据比例计算的边框长度，从四角定点向临近的定点画出的长度

    private Matrix scanMatrix;//变换矩阵，用来实现动画效果
    private ValueAnimator valueAnimator;//值动画，用来变换矩阵操作

    private int scanAnimatorDuration = 1800;//值动画的时长
    private int scanColor;//扫描颜色

    private int scanStyle = styleGrid;//网格 0：网格，1：纵向雷达 2:综合 3:线
    private float animatedValue;


    public ScanLineView(Context context) {
        this(context, null);
    }

    // This constructor is used when the class is built from an XML resource.
    public ScanLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ScanLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize these once for performance rather than calling them every time in onDraw().
        scanPaintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        scanPaintGrid.setStyle(Paint.Style.STROKE);
        scanPaintGrid.setStrokeWidth(gridLineWidth);

        scanPaintRadio = new Paint(Paint.ANTI_ALIAS_FLAG);
        scanPaintRadio.setStyle(Paint.Style.FILL);
        Resources resources = getResources();
        scanColor = resources.getColor(R.color.common_color);


        scanPaintLine =new Paint();//创建一个画笔
        scanPaintLine.setStyle(Paint.Style.FILL);//设置非填充
        scanPaintLine.setStrokeWidth(10);//笔宽5像素
        scanPaintLine.setAntiAlias(true);//锯齿不显示

        //变换矩阵，用来处理扫描的上下扫描效果
        scanMatrix = new Matrix();
        scanMatrix.setTranslate(0, 30);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        frame = new Rect(left,top,right,bottom);

        initBoundaryAndAnimator();
    }

    private void initBoundaryAndAnimator() {
        if (boundaryLinePath == null) {
            boundaryLinePath = new Path();
            boundaryLinePath.moveTo(frame.left, frame.top + cornerLineLen);
            boundaryLinePath.lineTo(frame.left, frame.top);
            boundaryLinePath.lineTo(frame.left + cornerLineLen, frame.top);
            boundaryLinePath.moveTo(frame.right - cornerLineLen, frame.top);
            boundaryLinePath.lineTo(frame.right, frame.top);
            boundaryLinePath.lineTo(frame.right, frame.top + cornerLineLen);
            boundaryLinePath.moveTo(frame.right, frame.bottom - cornerLineLen);
            boundaryLinePath.lineTo(frame.right, frame.bottom);
            boundaryLinePath.lineTo(frame.right - cornerLineLen, frame.bottom);
            boundaryLinePath.moveTo(frame.left + cornerLineLen, frame.bottom);
            boundaryLinePath.lineTo(frame.left, frame.bottom);
            boundaryLinePath.lineTo(frame.left, frame.bottom - cornerLineLen);
        }

        if (valueAnimator == null) {
            initScanValueAnim(frame.height());
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (frame == null|| boundaryLinePath ==null) {
            return;
        }
        switch (scanStyle) {
            case styleGrid:
                initGriddingPathAndStyle();
                canvas.drawPath(gridPath, scanPaintGrid);
                break;
            case styleRadar:
                initRadarStyle();
                canvas.drawRect(frame, scanPaintRadio);
                break;
            case styleLine:
                initLineStyle();
                canvas.drawLine(0,(frame.height()- Math.abs(animatedValue)),getMeasuredWidth(),
                        (frame.height()- Math.abs(animatedValue)), scanPaintLine);
                break;
            case styleHybrid:
            default:
                initGriddingPathAndStyle();
                initRadarStyle();
                canvas.drawPath(gridPath, scanPaintGrid);
                canvas.drawRect(frame, scanPaintRadio);
                break;

        }

    }

    private void initRadarStyle() {
        if (linearGradientRadar == null) {
            linearGradientRadar = new LinearGradient(0, frame.top, 0, frame.bottom + 0.01f * frame.height(),
                    new int[]{Color.TRANSPARENT, Color.TRANSPARENT, scanColor, Color.TRANSPARENT}, new float[]{0, 0.85f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);
            linearGradientRadar.setLocalMatrix(scanMatrix);
            scanPaintRadio.setShader(linearGradientRadar);
        }
    }

    private void initLineStyle() {
        if (linearGradientLine == null) {
            String line_colors = String.valueOf(Integer.toHexString(scanColor));
            line_colors = line_colors.substring(line_colors.length() - 6, line_colors.length() - 0);
            linearGradientLine = new LinearGradient(0,0,getMeasuredWidth(),0,new int[] {Color.parseColor("#00"+line_colors),
                    scanColor, Color.parseColor("#00"+line_colors),},null, Shader.TileMode.CLAMP);
            linearGradientLine.setLocalMatrix(scanMatrix);
            scanPaintLine.setShader(linearGradientLine);
        }
    }

    private void initGriddingPathAndStyle() {
        if (gridPath == null) {
            gridPath = new Path();
            float wUnit = frame.width() / (gridDensity + 0f);
            float hUnit = frame.height() / (gridDensity + 0f);
            for (int i = 0; i <= gridDensity; i++) {
                gridPath.moveTo(frame.left + i * wUnit, frame.top);
                gridPath.lineTo(frame.left + i * wUnit, frame.bottom);
            }
            for (int i = 0; i <= gridDensity; i++) {
                gridPath.moveTo(frame.left, frame.top + i * hUnit);
                gridPath.lineTo(frame.right, frame.top + i * hUnit);
            }
        }
        if (linearGradientGrid == null) {
            linearGradientGrid = new LinearGradient(0, frame.top, 0, frame.bottom + 0.01f * frame.height(), new int[]{Color.TRANSPARENT, Color.TRANSPARENT, scanColor, Color.TRANSPARENT}, new float[]{0, 0.5f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);
            linearGradientGrid.setLocalMatrix(scanMatrix);
            scanPaintGrid.setShader(linearGradientGrid);

        }
    }

    public void initScanValueAnim(int height) {
        valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(scanAnimatorDuration);
        valueAnimator.setFloatValues(-height, 0);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setRepeatCount(Animation.INFINITE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {



            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(linearGradientGrid == null){
                    initGriddingPathAndStyle();
                }
                if(linearGradientRadar == null){
                    initRadarStyle();
                }

                if(linearGradientLine == null){
                    initLineStyle();
                }

                if (scanMatrix != null ) {
                    animatedValue = (float) animation.getAnimatedValue();
                    scanMatrix.setTranslate(0, animatedValue);
                    linearGradientGrid.setLocalMatrix(scanMatrix);
                    linearGradientRadar.setLocalMatrix(scanMatrix);
                    linearGradientLine.setLocalMatrix(scanMatrix);
                    //mScanPaint.setShader(mLinearGradient); //不是必须的设置到shader即可
                    invalidate();
                }
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    //设定扫描的颜色
    public void setScancolor(int colorValue) {
        this.scanColor = colorValue;
    }

    public void setScanAnimatorDuration(int duration) {
        this.scanAnimatorDuration = duration;
    }


    /*
     * @description 扫描区域的样式
     * @scanStyle
     *
     * */
    public void setScanStyle(int scanStyle) {
        this.scanStyle = scanStyle;
    }

    /*
     * 扫描区域网格的样式
     *  @params strokeWidth：网格的线宽
     * @params density：网格的密度
     * */
    public void setScanGriddingStyle(float strokeWidh, int density) {
        this.gridLineWidth = strokeWidh;
        this.gridDensity = density;
    }
}

