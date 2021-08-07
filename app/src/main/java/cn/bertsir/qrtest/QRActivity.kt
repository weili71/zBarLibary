package cn.bertsir.qrtest

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.bertsir.qrtest.databinding.ActivityQrBinding
import cn.bertsir.zbar.Qr.ScanResult
import cn.bertsir.zbar.Qr.Symbol
import cn.bertsir.zbar.QrConfig
import cn.bertsir.zbar.QrManager
import cn.bertsir.zbar.ScanCallback
import cn.bertsir.zbar.utils.GetPathFromUri
import cn.bertsir.zbar.utils.QRUtils
import cn.bertsir.zbar.utils.SizeUtil
import com.esafirm.imagepicker.features.*
import com.permissionx.guolindev.PermissionX
//import com.soundcloud.android.crop.Crop
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

    private var flashSwitch: CheckBox?=null
    private lateinit var soundPool: SoundPool
    private lateinit var binding: ActivityQrBinding
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        val window = window
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        options = intent.getSerializableExtra(QrConfig.EXTRA_THIS_CONFIG) as QrConfig
        initParameters()
        initView()
    }

    /**
     * 初始化参数
     */
    private fun initParameters() {
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
        title = options.title
        if (options.scannerWidth == QrConfig.DEFAULT_SCANNER_WIDTH) {
            val defaultSize = SizeUtil.dip2px(this, 300)
            binding.scanView.setScannerWidth(defaultSize)
        } else {
            binding.scanView.setScannerWidth(options.scannerWidth)
        }
        if (options.scannerHeight == QrConfig.DEFAULT_SCANNER_HEIGHT) {
            val defaultSize = SizeUtil.dip2px(this, 300)
            binding.scanView.setScannerHeight(defaultSize)
        } else {
            binding.scanView.setScannerHeight(options.scannerWidth)
        }

        soundPool = SoundPool(10, AudioManager.STREAM_SYSTEM, 5)
        soundPool.load(this, QrConfig.getDingPath(), 1)
        binding.scanView.setType(options.getScanViewType())
        binding.scanView.setCornerColor(options.cornerColor)
        binding.scanView.setLineSpeed(options.getLineSpeed())
        binding.scanView.setLineColor(options.lineColor)
        binding.scanView.setScanLineStyle(options.getLineStyle())
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

    /**
     * 识别结果回调
     */
    private val resultCallback = ScanCallback { result ->
        if (options.isPlaySound) {
            soundPool.play(1, 1f, 1f, 0, 0, 1f)
        }
        if (options.isEnableVibrator) {
            QRUtils.getInstance().getVibrator(applicationContext)
        }
        QrManager.getInstance().getResultCallback().onScanSuccess(result)
        if (!Symbol.looperScan) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        flashSwitch?.isChecked= false
        binding.cameraPreview.apply {
            setScanCallback(resultCallback)
        }.start()

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
//                Crop.REQUEST_CROP -> {
//                    recognitionLocation(uriCropFile)
//                }
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
//        Crop.of(uri, uriCropFile).asSquare().start(this)
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

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_qr, menu)

        flashSwitch = menu.findItem(R.id.flash_switch).actionView as CheckBox
        flashSwitch!!.setOnCheckedChangeListener { button, isChecked ->
            binding.cameraPreview.setFlash(isChecked)
        }

        if (!options.isShowAlbum) {
            menu.findItem(R.id.open_album).isVisible = false
        }
        if (!options.isShowLight) {
            menu.findItem(R.id.flash_switch).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.open_album -> {
                PermissionX.init(this)
                    .permissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .explainReasonBeforeRequest()
                    .onExplainRequestReason { scope, deniedList ->
                        scope.showRequestReasonDialog(deniedList, "获取图片需要相册权限", "去授权", "取消")
                    }
                    .onForwardToSettings { scope, deniedList ->
                        scope.showForwardToSettingsDialog(
                            deniedList,
                            "您需要在“设置”中手动授予必要的权限",
                            "去授权",
                            "取消"
                        )
                    }
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            imagePickerLauncher.launch(createConfig())
                        } else {
                            toast(this, "相机权限被拒绝")
                        }
                    }
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    val imagePickerLauncher = registerImagePicker {
        it.getOrNull(0)?.apply {
            recognitionLocation(uri)
        }
    }

    private fun createConfig(): ImagePickerConfig {
        val returnAfterCapture = false
        val isSingleMode = true
        val useCustomImageLoader = false
        val folderMode = false
        val includeVideo = false
        val onlyVideo = false
        val isExclude = false

//        ImagePickerComponentsHolder.setInternalComponent(
//            CustomImagePickerComponents(this, useCustomImageLoader)
//        )

        return ImagePickerConfig {
            mode = ImagePickerMode.SINGLE
            language = "in" // Set image picker language
            theme = R.style.AppTheme_NoActionBar
            // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
            returnMode = if (returnAfterCapture) ReturnMode.ALL else ReturnMode.NONE
            isFolderMode = folderMode // set folder mode (false by default)
            isIncludeVideo = includeVideo // include video (false by default)
            isOnlyVideo = onlyVideo // include video (false by default)
//            arrowColor = Color.RED // set toolbar arrow up color
            folderTitle = "文件夹" // folder selection title
            imageTitle = "选择图片" // image selection title
            doneButtonText = "完成" // done button text
            showDoneButtonAlways = true // Show done button always or not
            limit = 1 // max images can be selected (99 by default)
            isShowCamera = true // show camera or not (true by default)
            savePath =
                ImagePickerSavePath("Camera") // captured image directory name ("Camera" folder by default)
            savePath = ImagePickerSavePath(
                Environment.getExternalStorageDirectory().path,
                isRelative = false
            ) // can be a full path

//            if (isExclude) {
//                excludedImages = images.toFiles() // don't show anything on this selected images
//            } else {
//                selectedImages = images  // original selected images, used in multi mode
//            }
        }
    }

}