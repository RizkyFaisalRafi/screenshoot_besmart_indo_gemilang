//package com.rifara.screenshootBesmartIndonesiaGemilang
//
//import android.app.Activity
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.Service
//import android.content.ContentValues
//import android.content.Context
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.PixelFormat
//import android.hardware.display.DisplayManager
//import android.hardware.display.VirtualDisplay
//import android.media.ImageReader
//import android.media.MediaRecorder
//import android.media.MediaScannerConnection
//import android.media.projection.MediaProjection
//import android.media.projection.MediaProjectionManager
//import android.os.Build
//import android.os.Environment
//import android.os.Handler
//import android.os.IBinder
//import android.os.Looper
//import android.provider.MediaStore
//import android.widget.Toast
//import androidx.core.app.NotificationCompat
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class ScreenRecordingService : Service() {
//
//    private var mediaProjectionManager: MediaProjectionManager? = null
//    private var mediaProjection: MediaProjection? = null
//    private var virtualDisplay: VirtualDisplay? = null
//    private var mediaRecorder: MediaRecorder? = null
//    private var imageReader: ImageReader? = null
//
//    private val notificationChannelId = "ScreenRecordServiceChannel"
//    private val notificationId = 12345
//
//    companion object {
//        const val ACTION_START = "ACTION_START"
//        const val ACTION_STOP = "ACTION_STOP"
//        const val ACTION_CAPTURE_SINGLE_FRAME = "ACTION_CAPTURE_SINGLE_FRAME"
//        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
//        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        mediaProjectionManager =
//            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
//        createNotificationChannel()
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (intent != null) {
//            when (intent.action) {
//                ACTION_START -> {
//                    val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
//                    @Suppress("DEPRECATION")
//                    val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
//                    } else {
//                        intent.getParcelableExtra(EXTRA_RESULT_DATA)
//                    }
//
//                    if (resultData != null) {
//                        startForeground(notificationId, createNotification())
//                        startRecording(resultCode, resultData)
//                    }
//                }
//
//                ACTION_STOP -> {
//                    stopRecording()
//                }
//
//                ACTION_CAPTURE_SINGLE_FRAME -> {
//                    val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
//                    @Suppress("DEPRECATION")
//                    val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
//                    } else {
//                        intent.getParcelableExtra(EXTRA_RESULT_DATA)
//                    }
//
//                    if (resultCode == Activity.RESULT_OK && resultData != null) {
//                        startForeground(notificationId, createNotification())
//                        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)
//
//                        // ✨ TAMBAHKAN KODE INI ✨
//                        // Daftarkan callback sebelum menggunakan mediaProjection
//                        val callback = object : MediaProjection.Callback() {
//                            override fun onStop() {
//                                super.onStop()
//                                stopCaptureAndService() // Panggil fungsi pembersih jika berhenti
//                            }
//                        }
//                        mediaProjection?.registerCallback(callback, Handler(Looper.getMainLooper()))
//                        // ✨ --- BATAS AKHIR --- ✨
//
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            startSingleCapture()
//                        }, 100)
//                    } else {
//                        stopSelf()
//                    }
//                }
//            }
//        }
//        return START_NOT_STICKY
//    }
//
//    private fun startSingleCapture() {
//        val displayMetrics = resources.displayMetrics
//        val width = displayMetrics.widthPixels
//        val height = displayMetrics.heightPixels
//
//        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2).apply {
//            setOnImageAvailableListener({ reader ->
//                val image = reader.acquireLatestImage()
//                if (image != null) {
//                    val planes = image.planes
//                    val buffer = planes[0].buffer
//                    val pixelStride = planes[0].pixelStride
//                    val rowStride = planes[0].rowStride
//                    val rowPadding = rowStride - pixelStride * width
//                    val bitmap = Bitmap.createBitmap(
//                        width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
//                    )
//                    bitmap.copyPixelsFromBuffer(buffer)
//                    image.close()
//                    saveBitmap(bitmap)
//                }
//                stopCaptureAndService()
//            }, Handler(Looper.getMainLooper()))
//        }
//
//        virtualDisplay = mediaProjection?.createVirtualDisplay(
//            "SingleScreenCapture", width, height, displayMetrics.densityDpi,
//            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader?.surface, null, null
//        )
//    }
//
//    private fun stopCaptureAndService() {
//        virtualDisplay?.release()
//        imageReader?.close()
//        mediaProjection?.stop()
//
//        val showButtonIntent = Intent(this, FloatingWindowService::class.java).apply {
//            action = "show"
//        }
//        startService(showButtonIntent)
//
//        stopSelf()
//    }
//
//    private fun saveBitmap(bitmap: Bitmap) {
//        val fileName = "screenshot_${System.currentTimeMillis()}.png"
//        var toastMessage = "Gagal menyimpan screenshot!"
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                val resolver = contentResolver
//                val contentValues = ContentValues().apply {
//                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
//                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots")
//                }
//                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//                if (uri != null) {
//                    resolver.openOutputStream(uri).use { outputStream ->
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
//                        toastMessage = "Screenshot disimpan!"
//                    }
//                }
//            } else {
//                @Suppress("DEPRECATION")
//                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Screenshots"
//                val dir = File(imagesDir)
//                if (!dir.exists()) dir.mkdirs()
//                val imageFile = File(dir, fileName)
//                FileOutputStream(imageFile).use { outputStream ->
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                }
//                MediaScannerConnection.scanFile(this, arrayOf(imageFile.toString()), null, null)
//                toastMessage = "Screenshot disimpan!"
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            Handler(Looper.getMainLooper()).post {
//                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun startRecording(resultCode: Int, data: Intent) {
//        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
//
//        // ✨ TAMBAHKAN KODE INI ✨
//        // Daftarkan callback sebelum menggunakan mediaProjection
//        val callback = object : MediaProjection.Callback() {
//            override fun onStop() {
//                super.onStop()
//                stopRecording() // Panggil fungsi stop jika perekaman dihentikan dari luar
//            }
//        }
//        mediaProjection?.registerCallback(callback, Handler(Looper.getMainLooper()))
//        // ✨ --- BATAS AKHIR --- ✨
//
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
//        val fileName = "ScreenRecord-$timeStamp.mp4"
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
//            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
//            }
//        }
//        val resolver = applicationContext.contentResolver
//        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
//        if (uri == null) {
//            stopSelf()
//            return
//        }
//        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            MediaRecorder(this)
//        } else {
//            @Suppress("DEPRECATION")
//            MediaRecorder()
//        }
//        val metrics = resources.displayMetrics
//        val screenWidth = metrics.widthPixels
//        val screenHeight = metrics.heightPixels
//        val screenDensity = metrics.densityDpi
//
//        mediaRecorder?.apply {
//            setVideoSource(MediaRecorder.VideoSource.SURFACE)
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            val parcelFileDescriptor = resolver.openFileDescriptor(uri, "w")
//            if (parcelFileDescriptor != null) {
//                setOutputFile(parcelFileDescriptor.fileDescriptor)
//            } else {
//                stopSelf()
//                return
//            }
//            setVideoSize(screenWidth, screenHeight)
//            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            setVideoEncodingBitRate(512 * 1000)
//            setVideoFrameRate(30)
//            try {
//                prepare()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return
//            }
//        }
//        virtualDisplay = mediaProjection?.createVirtualDisplay(
//            "ScreenRecord", screenWidth, screenHeight, screenDensity,
//            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder?.surface, null, null
//        )
//        mediaRecorder?.start()
//        Toast.makeText(this, "Rekaman layar dengan suara dimulai", Toast.LENGTH_LONG).show()
//    }
//
//    private fun stopRecording() {
//        if (mediaRecorder == null) {
//            return
//        }
//        try {
//            mediaRecorder?.stop()
//            mediaRecorder?.reset()
//            mediaRecorder?.release()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        mediaRecorder = null
//        virtualDisplay?.release()
//        mediaProjection?.stop()
//        Toast.makeText(this, "Rekaman layar disimpan di Galeri", Toast.LENGTH_LONG).show()
//        stopForeground(true)
//        stopSelf()
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                notificationChannelId, "Screen Recording Service Channel", NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(serviceChannel)
//        }
//    }
//
//    private fun createNotification(): Notification {
//        return NotificationCompat.Builder(this, notificationChannelId)
//            .setContentTitle("Merekam Layar")
//            .setContentText("Layanan perekaman layar sedang berjalan.")
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .build()
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onDestroy() {
//        stopRecording()
//        super.onDestroy()
//    }
//}


package com.rifara.screenshootBesmartIndonesiaGemilang

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.CamcorderProfile
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.SparseIntArray
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenRecordingService : Service() {

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var imageReader: ImageReader? = null

    private val notificationChannelId = "ScreenRecordServiceChannel"
    private val notificationId = 12345

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_CAPTURE_SINGLE_FRAME = "ACTION_CAPTURE_SINGLE_FRAME"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"

//        private val ORIENTATIONS = SparseIntArray().apply {
//            append(Surface.ROTATION_0, 90)
//            append(Surface.ROTATION_90, 0)
//            append(Surface.ROTATION_180, 270)
//            append(Surface.ROTATION_270, 180)
//        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_START -> {
                    val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)

                    @Suppress("DEPRECATION")
                    val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                    } else {
                        intent.getParcelableExtra(EXTRA_RESULT_DATA)
                    }

                    if (resultData != null) {
                        startForeground(notificationId, createNotification())
                        startRecording(resultCode, resultData)
                    }
                }

                ACTION_STOP -> {
                    stopRecording()
                }

                ACTION_CAPTURE_SINGLE_FRAME -> {
                    val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)

                    @Suppress("DEPRECATION")
                    val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                    } else {
                        intent.getParcelableExtra(EXTRA_RESULT_DATA)
                    }

                    if (resultCode == Activity.RESULT_OK && resultData != null) {
                        startForeground(notificationId, createNotification())
                        mediaProjection =
                            mediaProjectionManager?.getMediaProjection(resultCode, resultData)
                        val callback = object : MediaProjection.Callback() {
                            override fun onStop() {
                                super.onStop()
                                stopCaptureAndService()
                            }
                        }
                        mediaProjection?.registerCallback(callback, Handler(Looper.getMainLooper()))
                        Handler(Looper.getMainLooper()).postDelayed({
                            startSingleCapture()
                        }, 300)
                    } else {
                        stopSelf()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startSingleCapture() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                if (image != null) {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * width
                    val bitmap = Bitmap.createBitmap(
                        width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)
                    image.close()
                    saveBitmap(bitmap)
                }
                stopCaptureAndService()
            }, Handler(Looper.getMainLooper()))
        }

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "SingleScreenCapture", width, height, displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader?.surface, null, null
        )
    }

    private fun stopCaptureAndService() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()

        val showButtonIntent = Intent(this, FloatingWindowService::class.java).apply {
            action = "show"
        }
        startService(showButtonIntent)

        stopSelf()
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val fileName = "screenshot_${System.currentTimeMillis()}.png"
        var toastMessage = "Gagal menyimpan screenshot!"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/Screenshots"
                    )
                }
                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri).use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
                        toastMessage = "Screenshot disimpan!"
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString() + "/Screenshots"
                val dir = File(imagesDir)
                if (!dir.exists()) dir.mkdirs()
                val imageFile = File(dir, fileName)
                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                MediaScannerConnection.scanFile(this, arrayOf(imageFile.toString()), null, null)
                toastMessage = "Screenshot disimpan!"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private fun startRecording(resultCode: Int, data: Intent) {
//        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
//
//        val callback = object : MediaProjection.Callback() {
//            override fun onStop() {
//                super.onStop()
//                stopRecording()
//            }
//        }
//        mediaProjection?.registerCallback(callback, Handler(Looper.getMainLooper()))
//
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
//        val fileName = "ScreenRecord-$timeStamp.mp4"
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
//            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
//            }
//        }
//        val resolver = applicationContext.contentResolver
//        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
//        if (uri == null) {
//            stopSelf()
//            return
//        }
//        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            MediaRecorder(this)
//        } else {
//            @Suppress("DEPRECATION")
//            MediaRecorder()
//        }
//
//        mediaRecorder?.apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setVideoSource(MediaRecorder.VideoSource.SURFACE)
//
//            val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
//            profile.videoBitRate = 2 * 1024 * 1024
//            setProfile(profile)
//
//            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//
//            @Suppress("DEPRECATION")
//            val rotation = windowManager.defaultDisplay.rotation
//            val orientation = ORIENTATIONS.get(rotation)
//            setOrientationHint(orientation)
//
//            val parcelFileDescriptor = resolver.openFileDescriptor(uri, "w")
//            if (parcelFileDescriptor != null) {
//                setOutputFile(parcelFileDescriptor.fileDescriptor)
//            } else {
//                stopSelf()
//                return
//            }
//
//            try {
//                prepare()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                stopSelf()
//                return
//            }
//        }
//
//        val metrics = resources.displayMetrics
//        virtualDisplay = mediaProjection?.createVirtualDisplay(
//            "ScreenRecord", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
//            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder?.surface, null, null
//        )
//        mediaRecorder?.start()
//        Toast.makeText(this, "Rekaman layar dengan suara dimulai", Toast.LENGTH_LONG).show()
//    }

    private fun startRecording(resultCode: Int, data: Intent) {
        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)

        val callback = object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                stopRecording()
            }
        }
        mediaProjection?.registerCallback(callback, Handler(Looper.getMainLooper()))

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "ScreenRecord-$timeStamp.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            }
        }
        val resolver = applicationContext.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri == null) {
            stopSelf()
            return
        }
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        // ✨ BARIS YANG HILANG ADA DI SINI. KITA TAMBAHKAN KEMBALI. ✨
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val screenDensity = metrics.densityDpi
        // ✨ --- BATAS --- ✨

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            val parcelFileDescriptor = resolver.openFileDescriptor(uri, "w")
            if (parcelFileDescriptor != null) {
                setOutputFile(parcelFileDescriptor.fileDescriptor)
            } else {
                stopSelf()
                return
            }

            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncodingBitRate(5 * 1024 * 1024)
            setVideoFrameRate(30)
            // PENTING: Mengatur ukuran video sesuai resolusi layar
            setVideoSize(screenWidth, screenHeight)

//            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//
//            @Suppress("DEPRECATION")
//            val rotation = windowManager.defaultDisplay.rotation
//            val orientation = ORIENTATIONS.get(rotation)
//            setOrientationHint(orientation)

            try {
                prepare()
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
                return
            }
        }

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecord", screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder?.surface, null, null
        )
        mediaRecorder?.start()
        Toast.makeText(this, "Rekaman layar dengan suara dimulai", Toast.LENGTH_LONG).show()
    }

    private fun stopRecording() {
        if (mediaRecorder == null) return
        try {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        virtualDisplay?.release()
        mediaProjection?.stop()
        Toast.makeText(this, "Rekaman layar disimpan di Galeri", Toast.LENGTH_LONG).show()
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                notificationChannelId,
                "Screen Recording Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                serviceChannel
            )
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Merekam Layar")
            .setContentText("Layanan perekaman layar sedang berjalan.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }
}