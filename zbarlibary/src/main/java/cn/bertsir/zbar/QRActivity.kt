package cn.bertsir.zbar

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.bertsir.zbar.Qr.Config
import cn.bertsir.zbar.Qr.ScanResult
import cn.bertsir.zbar.Qr.Symbol
import cn.bertsir.zbar.databinding.ActivityQrBinding
import cn.bertsir.zbar.utils.GetPathFromUri
import cn.bertsir.zbar.utils.QRUtils
import cn.bertsir.zbar.utils.SizeUtil
import com.soundcloud.android.crop.Crop
import java.io.File

class QRActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        private const val TAG = "QRActivity"
        const val REQUEST_IMAGE_GET = 1
        const val REQUEST_PHOTO_CUT = 2
        const val RESULT_CANCELED = 401
        const val AUTO_LIGHT_MIN = 10f

        fun start(context: Context, config: QrConfig) {
            val intent = Intent(context, QRActivity::class.java)
            intent.putExtra(QrConfig.EXTRA_THIS_CONFIG, config)
            context.startActivity(intent)
        }
    }

    private lateinit var soundPool: SoundPool
    private lateinit var binding: ActivityQrBinding
    private var textDialog: TextView? = null
    private lateinit var options: QrConfig
    private var uriCropFile: Uri? = null
    private val cropTempPath =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "cropQr.jpg"
    private var progressDialog: AlertDialog? = null
    private var oldDist = 1f

    //用于检测光线
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //        Log.i("zBarLibary", "version: "+BuildConfig.VERSION_NAME);
        options = intent.getSerializableExtra(QrConfig.EXTRA_THIS_CONFIG) as QrConfig
        initParameters()
        initView()
    }

    /**
     * 初始化参数
     */
    private fun initParameters() {
        requestedOrientation = when (options.screenOrientation) {
            QrConfig.SCREEN_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            QrConfig.SCREEN_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            QrConfig.SCREEN_SENSOR -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
            else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        Symbol.scanType = options.scanType
        Symbol.scanFormat = options.customBarCodeFormat
        Symbol.is_only_scan_center = options.isOnlyCenter
        Symbol.is_auto_zoom = options.isAutoZoom
        Symbol.doubleEngine = options.isDoubleEngine
        Symbol.looperScan = options.isLoopScan
        Symbol.looperWaitTime = options.loopWaitTime
        Symbol.screenWidth = QRUtils.getInstance().getScreenWidth(this)
        Symbol.screenHeight = QRUtils.getInstance().getScreenHeight(this)
        if (options.isAutoLight) {
            getSensorManager()
        }
    }

    /**
     * 初始化布局
     */
    private fun initView() {
        //bi~
        if (options.scannerWidth == QrConfig.DEFAULT_SCANNER_WIDTH) {
            val defaultSize = SizeUtil.dip2px(this, 300)
            binding.scanView.setScannerWidth(defaultSize)
        }else{
            binding.scanView.setScannerWidth(options.scannerWidth)
        }
        if (options.scannerHeight == QrConfig.DEFAULT_SCANNER_HEIGHT) {
            val defaultSize = SizeUtil.dip2px(this, 300)
            binding.scanView.setScannerHeight(defaultSize)
        }else{
            binding.scanView.setScannerHeight(options.scannerWidth)
        }

        soundPool = SoundPool(10, AudioManager.STREAM_SYSTEM, 5)
        soundPool.load(this, QrConfig.getDingPath(), 1)
        binding.scanView.setType(options.getScanViewType())
        binding.flash.setImageResource(options.lightImageRes)
        binding.album.setImageResource(options.albumImageRes)
        binding.album.setOnClickListener { fromAlbum() }
        binding.flash.setOnClickListener { binding.cameraPreview.setFlash() }

//        iv_album.visibility = if (options.isShow_light) View.VISIBLE else View.GONE
        binding.flash.visibility = if (options.isShowLight) View.VISIBLE else View.GONE
        binding.album.visibility = if (options.isShowAlbum) View.VISIBLE else View.GONE
        binding.desc.visibility = if (options.isShowDesc) View.VISIBLE else View.GONE
        binding.zoom.visibility = if (options.isShowZoom) View.VISIBLE else View.GONE
        binding.desc.text = options.getDescText()
        binding.scanView.setCornerColor(options.cornerColor)
        binding.scanView.setLineSpeed(options.getLineSpeed())
        binding.scanView.setLineColor(options.lineColor)
        binding.scanView.setScanLineStyle(options.getLineStyle())
        setSeekBarColor(binding.zoom, options.cornerColor)
        binding.zoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.cameraPreview.setZoom(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    /**
     * 获取光线传感器
     */
    private fun getSensorManager() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        }
    }

    private fun setSeekBarColor(seekBar: SeekBar?, color: Int) {
        seekBar!!.thumb.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        seekBar.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    /**
     * 识别结果回调
     */
    private val resultCallback = ScanCallback { result ->
        if (options.isPlaySound) {
            soundPool.play(1, 1f, 1f, 0, 0, 1f)
        }
        if (options.isShowVibrator) {
            QRUtils.getInstance().getVibrator(applicationContext)
        }

        //连续扫描时灯会关闭
//            if (cp != null) {
//                cp.setFlash(false);
//            }
        QrManager.getInstance().getResultCallback().onScanSuccess(result)
        if (!Symbol.looperScan) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.cameraPreview.setScanCallback(resultCallback)
        binding.cameraPreview.start()
        //一般在Resume方法中注册
        /**
         * 第三个参数决定传感器信息更新速度
         * SensorManager.SENSOR_DELAY_NORMAL:一般
         * SENSOR_DELAY_FASTEST:最快
         * SENSOR_DELAY_GAME:比较快,适合游戏
         * SENSOR_DELAY_UI:慢
         */
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        binding.cameraPreview.stop()
        //解除注册
        sensorManager?.unregisterListener(this, sensor)
    }

    override fun onDestroy() {
        super.onDestroy()
        setResult(RESULT_CANCELED) //兼容混合开发
        binding.cameraPreview.apply {
            setFlash(false)
            stop()
        }
        soundPool.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_GET -> {
                    if (options.isNeedCrop) {
                        cropPhoto(data?.data)
                    } else {
                        recognitionLocation(data?.data)
                    }
                }
                Crop.REQUEST_CROP -> {
                    recognitionLocation(uriCropFile)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 从相册选择
     */
    private fun fromAlbum() {
        if (QRUtils.getInstance().isMIUI) { //是否是小米设备,是的话用到弹窗选取入口的方法去选取
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(
                Intent.createChooser(intent, options.openAlbumText),
                REQUEST_IMAGE_GET
            )
        } else { //直接跳到系统相册去选取
            val intent = Intent()
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, options.openAlbumText),
                REQUEST_IMAGE_GET
            )
        }
    }

    /**
     * 识别本地
     *
     * @param uri
     */
    private fun recognitionLocation(uri: Uri?) {
        val imagePath = GetPathFromUri.getPath(this, uri)
        textDialog = showProgressDialog()
        textDialog!!.text = "请稍后..."
        Thread(Runnable {
            try {
                if (TextUtils.isEmpty(imagePath)) {
                    Toast.makeText(applicationContext, "获取图片失败！", Toast.LENGTH_SHORT).show()
                    return@Runnable
                }
                //优先使用zbar识别一次二维码
                val content = QRUtils.getInstance().decodeQRcode(imagePath)
                runOnUiThread {
                    val scanResult = ScanResult()
                    if (!TextUtils.isEmpty(content)) {
                        closeProgressDialog()
                        scanResult.setContent(content)
                        scanResult.setType(ScanResult.CODE_QR)
                        QrManager.getInstance().getResultCallback().onScanSuccess(scanResult)
                        QRUtils.getInstance().deleteTempFile(cropTempPath) //删除裁切的临时文件
                        finish()
                    } else {
                        //尝试用zxing再试一次识别二维码
                        val content = QRUtils.getInstance().decodeQRcodeByZxing(imagePath)
                        if (!TextUtils.isEmpty(content)) {
                            closeProgressDialog()
                            scanResult.setContent(content)
                            scanResult.setType(ScanResult.CODE_QR)
                            QrManager.getInstance().getResultCallback().onScanSuccess(scanResult)
                            QRUtils.getInstance().deleteTempFile(cropTempPath) //删除裁切的临时文件
                            finish()
                        } else {
                            //再试试是不是条形码
                            try {
                                val content = QRUtils.getInstance().decodeBarcode(imagePath)
                                if (!TextUtils.isEmpty(content)) {
                                    closeProgressDialog()
                                    scanResult.setContent(content)
                                    scanResult.setType(ScanResult.CODE_BAR)
                                    QrManager.getInstance().getResultCallback()
                                        .onScanSuccess(scanResult)
                                    QRUtils.getInstance().deleteTempFile(cropTempPath) //删除裁切的临时文件
                                    finish()
                                } else {
                                    Toast.makeText(applicationContext, "识别失败！", Toast.LENGTH_SHORT)
                                        .show()
                                    closeProgressDialog()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(applicationContext, "识别异常！", Toast.LENGTH_SHORT)
                                    .show()
                                closeProgressDialog()
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "识别异常！", Toast.LENGTH_SHORT).show()
                closeProgressDialog()
            }
        }).start()
    }

    /**
     * 裁切照片
     *
     * @param uri
     */
    fun cropPhoto(uri: Uri?) {
        uriCropFile = Uri.parse("file:///$cropTempPath")
        Crop.of(uri, uriCropFile).asSquare().start(this)
    }

    private fun showProgressDialog(): TextView {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        builder.setCancelable(false)
        val view = View.inflate(this, R.layout.dialog_loading, null)
        builder.setView(view)
        val progressBar = view.findViewById(R.id.loading) as ProgressBar
        val tvHint = view.findViewById(R.id.hint) as TextView
        if (Build.VERSION.SDK_INT >= 23) {
            progressBar.indeterminateTintList = getColorStateList(R.color.dialog_pro_color)
        }
        progressDialog = builder.create()
        progressDialog!!.show()
        return tvHint
    }

    private fun closeProgressDialog() {
        try {
            progressDialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (options.isFingerZoom) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = QRUtils.getInstance().getFingerSpacing(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 2) {
                        val newDist = QRUtils.getInstance().getFingerSpacing(event)
                        if (newDist > oldDist) {
                            binding.cameraPreview.handleZoom(true)
                        } else if (newDist < oldDist) {
                            binding.cameraPreview.handleZoom(false)
                        }
                        oldDist = newDist
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val light = event.values[0]
        if (light < AUTO_LIGHT_MIN) { //暂定值
            if (binding.cameraPreview.isPreviewStart) {
                binding.cameraPreview.setFlash(true)
                sensorManager!!.unregisterListener(this, sensor)
                sensor = null
                sensorManager = null
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

}