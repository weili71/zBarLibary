package cn.bertsir.qrtest

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import cn.bertsir.qrtest.databinding.ActivityMainBinding
import cn.bertsir.zbar.QrConfig
import cn.bertsir.zbar.QrManager
import cn.bertsir.zbar.utils.QRUtils
import cn.bertsir.zbar.utils.SizeUtil
import cn.bertsir.zbar.view.ScanLineView

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.btScan.setOnClickListener(this)
        binding.ivQr.setOnClickListener(this)
        binding.btMake.setOnClickListener(this)
        binding.ivQr.setOnLongClickListener {
            try {
                val s = QRUtils.getInstance().decodeQRcode(binding.ivQr)
                toast(this, "内容：$s")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@setOnLongClickListener true
        }
    }

    @SuppressLint("NewApi")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_scan -> start()
            R.id.bt_make -> {
                var qrCode: Bitmap? = null
                qrCode = if (binding.cbCreateAddWater.isChecked) {
                    QRUtils.getInstance().createQRCodeAddLogo(
                        binding.etQrContent.text.toString(),
                        BitmapFactory.decodeResource(
                            resources,
                            R.mipmap.ic_launcher
                        )
                    )
                } else {
                    QRUtils.getInstance().createQRCode(binding.etQrContent.text.toString())
                }
                binding.ivQr.setImageBitmap(qrCode)
                toast(this, "长按可识别", Toast.LENGTH_LONG)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun start() {
        var scanType = 0
        var scanViewType = 0
        var screen = 1
        var lineStyle = ScanLineView.styleRadar
        if (binding.rbAll.isChecked()) {
            scanType = QrConfig.TYPE_ALL;
            scanViewType = QrConfig.SCANVIEW_TYPE_QRCODE;
        } else if (binding.rbQrcode.isChecked()) {
            scanType = QrConfig.TYPE_QRCODE;
            scanViewType = QrConfig.SCANVIEW_TYPE_QRCODE;
        } else if (binding.rbBcode.isChecked()) {
            scanType = QrConfig.TYPE_BARCODE;
            scanViewType = QrConfig.SCANVIEW_TYPE_BARCODE;
        }
        if (binding.rbScreenAuto.isChecked()) {
            screen = QrConfig.SCREEN_SENSOR;
        } else if (binding.rbScreenSx.isChecked()) {
            screen = QrConfig.SCREEN_PORTRAIT;
        } else if (binding.rbScreenHx.isChecked()) {
            screen = QrConfig.SCREEN_LANDSCAPE;
        }

        if (binding.rbScanlineRadar.isChecked()) {
            lineStyle = ScanLineView.styleRadar;
        } else if (binding.rbScanlineGrid.isChecked()) {
            lineStyle = ScanLineView.styleGrid;
        } else if (binding.rbScanlineHybrid.isChecked()) {
            lineStyle = ScanLineView.styleHybrid;
        } else if (binding.rbScanlineLine.isChecked()) {
            lineStyle = ScanLineView.styleLine;
        }

        Log.d(TAG, "start: $lineStyle")
        val qrConfig = QrConfig.Builder()
            .setDesText(binding.etQrDes.text.toString()) //扫描框下文字
            .setShowDes(binding.cdShowDes.isChecked) //是否显示扫描框下面文字
            .setShowLight(binding.cbShowFlash.isChecked) //显示手电筒按钮
            .setShowTitle(binding.cbShowTitle.isChecked) //显示Title
            .setShowAlbum(binding.cbShowAlbum.isChecked) //显示从相册选择按钮
            .setNeedCrop(binding.cbCropImage.isChecked) //是否从相册选择后裁剪图片
            .setCornerColor(getColor(R.color.colorPrimary)) //设置扫描框颜色
            .setLineColor(getColor(R.color.colorPrimary)) //设置扫描线颜色
            .setLineSpeed(QrConfig.LINE_MEDIUM) //设置扫描线速度
            .setScanType(scanType) //设置扫码类型（二维码，条形码，全部，自定义，默认为二维码）
            .setScanViewType(scanViewType) //设置扫描框类型（二维码还是条形码，默认为二维码）
            .setCustomBarCodeFormat(QrConfig.BARCODE_PDF417) //此项只有在扫码类型为TYPE_CUSTOM时才有效
            .setPlaySound(binding.cbShowDing.isChecked) //是否扫描成功后bi~的声音
//            .setDingPath(if (binding.cbShowCustomDing.isChecked) R.raw.test else R.raw.qrcode) //设置提示音(不设置为默认的Ding~)
            .setIsOnlyCenter(binding.cbOnlyCenter.isChecked) //是否只识别框中内容(默认为全屏识别)
            .setTitleText(binding.etQrTitle.text.toString()) //设置Tilte文字
            .setTitleBackgroudColor(Color.parseColor("#262020")) //设置状态栏颜色
            .setTitleTextColor(Color.WHITE) //设置Title文字颜色
            .setShowZoom(binding.cbShowZoom.isChecked) //是否开始滑块的缩放
            .setAutoZoom(binding.cbAutoZoom.isChecked) //是否开启自动缩放(实验性功能，不建议使用)
            .setFingerZoom(binding.cbFingerZoom.isChecked) //是否开始双指缩放
            .setDoubleEngine(binding.cbDoubleEngine.isChecked) //是否开启双引擎识别(仅对识别二维码有效，并且开启后只识别框内功能将失效)
            .setScreenOrientation(screen) //设置屏幕方式
            .setOpenAlbumText("选择要识别的图片") //打开相册的文字
            .setLooperScan(binding.cbLoopScan.isChecked) //是否连续扫描二维码
            .setLooperWaitTime(binding.etLoopScanTime.text.toString().toInt() * 1000) //连续扫描间隔时间
            .setScanLineStyle(lineStyle) //扫描线样式
            .setAutoLight(binding.cbAutoLight.isChecked) //自动灯光
            .setShowVibrator(binding.cbHaveVibrator.isChecked) //是否震动提醒
            .setScannerSize(SizeUtil.dip2px(this,300),SizeUtil.dip2px(this,300))
            .create()
        QrManager.getInstance().init(qrConfig).startScan(this@MainActivity) { result ->
            Log.e(TAG, "onScanSuccess: $result")
            toast(this, "内容：${result.getContent()}  类型：${result.getType()}")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}