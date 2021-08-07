package cn.bertsir.qrtest

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cn.bertsir.qrtest.databinding.ActivityMainBinding
import cn.bertsir.zbar.QrConfig
import cn.bertsir.zbar.QrManager
import cn.bertsir.zbar.utils.QRUtils
import cn.bertsir.zbar.utils.SizeUtil
import cn.bertsir.zbar.view.ScanLineView
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.scan.setOnClickListener{
            start()
        }
        binding.make.setOnClickListener{
            var qrCode: Bitmap? = null
            qrCode = if (binding.createQrCodeWithLogo.isChecked) {
                QRUtils.getInstance().createQRCodeAddLogo(
                    binding.qrContent.text.toString(),
                    BitmapFactory.decodeResource(
                        resources,
                        R.mipmap.app_icon
                    )
                )
            } else {
                QRUtils.getInstance().createQRCode(binding.qrContent.text.toString())
            }
            binding.qr.setImageBitmap(qrCode)
            toast(this, "长按可识别")
        }
        binding.qr.setOnLongClickListener {
            try {
                val s = QRUtils.getInstance().decodeQRcode(binding.qr)
                toast(this, "内容：$s")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@setOnLongClickListener true
        }
    }

    private fun start() {
        var scanType = 0
        var scanViewType = 0
        var screen = 1
        var lineStyle = ScanLineView.styleRadar

        when {
            binding.rbAll.isChecked -> {
                scanType = QrConfig.TYPE_ALL;
                scanViewType = QrConfig.SCANVIEW_TYPE_QRCODE;
            }
            binding.rbQrcode.isChecked -> {
                scanType = QrConfig.TYPE_QRCODE;
                scanViewType = QrConfig.SCANVIEW_TYPE_QRCODE;
            }
            binding.rbBcode.isChecked -> {
                scanType = QrConfig.TYPE_BARCODE;
                scanViewType = QrConfig.SCANVIEW_TYPE_BARCODE;
            }
        }

        when {
            binding.rbScreenAuto.isChecked -> {
                screen = QrConfig.SCREEN_SENSOR;
            }
            binding.rbScreenSx.isChecked -> {
                screen = QrConfig.SCREEN_PORTRAIT;
            }
            binding.rbScreenHx.isChecked -> {
                screen = QrConfig.SCREEN_LANDSCAPE;
            }
        }

        when {
            binding.rbScanlineRadar.isChecked -> {
                lineStyle = ScanLineView.styleRadar;
            }
            binding.rbScanlineGrid.isChecked -> {
                lineStyle = ScanLineView.styleGrid;
            }
            binding.rbScanlineHybrid.isChecked -> {
                lineStyle = ScanLineView.styleHybrid;
            }
            binding.rbScanlineLine.isChecked -> {
                lineStyle = ScanLineView.styleLine;
            }
        }

        val qrConfig = QrConfig.Builder()
            .setDesText(binding.qrDesc.text.toString()) //扫描框下文字
            .setShowDes(binding.showDesc.isChecked) //是否显示扫描框下面文字
            .setShowLight(binding.showFlash.isChecked) //显示手电筒按钮
            .setShowTitle(binding.showTitle.isChecked) //显示Title
            .setShowAlbum(binding.showAlbum.isChecked) //显示从相册选择按钮
            .setNeedCrop(binding.cropImage.isChecked) //是否从相册选择后裁剪图片
            .setCornerColor(ContextCompat.getColor(this,R.color.colorPrimary)) //设置扫描框颜色
            .setLineColor(ContextCompat.getColor(this,R.color.colorPrimary)) //设置扫描线颜色
            .setLineSpeed(QrConfig.LINE_MEDIUM) //设置扫描线速度
            .setScanType(scanType) //设置扫码类型（二维码，条形码，全部，自定义，默认为二维码）
            .setScanViewType(scanViewType) //设置扫描框类型（二维码还是条形码，默认为二维码）
            .setCustomBarCodeFormat(QrConfig.BARCODE_PDF417) //此项只有在扫码类型为TYPE_CUSTOM时才有效
            .setPlaySound(binding.showDing.isChecked) //是否扫描成功后bi~的声音
//            .setDingPath(if (binding.cbShowCustomDing.isChecked) R.raw.test else R.raw.qrcode) //设置提示音(不设置为默认的Ding~)
            .setIsOnlyCenter(binding.onlyCenter.isChecked) //是否只识别框中内容(默认为全屏识别)
            .setTitleText(binding.qrTitle.text.toString()) //设置Tilte文字
            .setTitleBackgroudColor(Color.parseColor("#262020")) //设置状态栏颜色
            .setTitleTextColor(Color.WHITE) //设置Title文字颜色
            .setAutoZoom(binding.autoZoom.isChecked) //是否开启自动缩放(实验性功能，不建议使用)
            .setFingerZoom(binding.fingerZoom.isChecked) //是否开始双指缩放
            .setDoubleEngine(binding.doubleEngine.isChecked) //是否开启双引擎识别(仅对识别二维码有效，并且开启后只识别框内功能将失效)
            .setScreenOrientation(screen) //设置屏幕方式
            .setOpenAlbumText("选择要识别的图片") //打开相册的文字
            .setLooperScan(binding.loopScan.isChecked) //是否连续扫描二维码
            .setLooperWaitTime(binding.loopScanTime.text.toString().toInt() * 1000) //连续扫描间隔时间
            .setScanLineStyle(lineStyle) //扫描线样式
            .setAutoLight(binding.autoLight.isChecked) //自动灯光
            .setShowVibrator(binding.haveVibrator.isChecked) //是否震动提醒
            .setScannerSize(SizeUtil.dip2px(this, 300), SizeUtil.dip2px(this, 300))
            .create()

        QrManager.getInstance().init(qrConfig).registerCallback(this@MainActivity) { result ->
            Log.e(TAG, "onScanSuccess: $result")
            toast(this, "内容：${result.getContent()}  类型：${result.getType()}")
        }

        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "扫描二维码需要相机权限", "去授权", "取消")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "您需要在“设置”中手动授予必要的权限", "去授权", "取消")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    val intent = Intent(this, QRActivity::class.java)
                    intent.putExtra(QrConfig.EXTRA_THIS_CONFIG, qrConfig)
                    startActivity(intent)
//                    QRActivity.start(this,qrConfig)
                } else {
                    toast(this, "相机权限被拒绝")
                }
            }

    }

    companion object {
        private const val TAG = "MainActivity"
    }
}