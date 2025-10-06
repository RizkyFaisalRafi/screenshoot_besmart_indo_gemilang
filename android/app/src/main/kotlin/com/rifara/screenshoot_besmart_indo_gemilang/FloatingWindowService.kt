//package com.rifara.screenshoot_besmart_indo_gemilang
package com.rifara.screenshoot_besmart_indonesia_gemilang

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.OutputStream
import kotlin.math.abs
import java.io.File
import java.io.FileOutputStream
import android.media.MediaScannerConnection

class FloatingWindowService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f

            private val screenWidth = resources.displayMetrics.widthPixels
            private val screenHeight = resources.displayMetrics.heightPixels

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val xDiff = event.rawX - initialTouchX
                        val yDiff = event.rawY - initialTouchY
                        if (kotlin.math.abs(xDiff) < 10 && kotlin.math.abs(yDiff) < 10) {
                            // ✅ --- LOGIKA BARU DI SINI --- ✅
                            // Cek apakah kita sudah punya izin (mediaProjection)
                            if (mediaProjection == null) {
                                // Jika BELUM, minta izin terlebih dahulu
                                floatingView.visibility = View.INVISIBLE
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val intent = Intent(this@FloatingWindowService, ScreenshotActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                }, 100)
                            } else {
                                // Jika SUDAH, langsung ambil screenshot
                                floatingView.visibility = View.INVISIBLE
                                startCapture()
                            }
                            // ---------------------------------
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = (initialX + (event.rawX - initialTouchX).toInt()).coerceIn(0, screenWidth - floatingView.width)
                        params.y = (initialY + (event.rawY - initialTouchY).toInt()).coerceIn(0, screenHeight - floatingView.height)
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "CAPTURE") {
            val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED)
            @Suppress("DEPRECATION")
            val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("data", Intent::class.java)
            } else {
                intent.getParcelableExtra("data")
            }

            if (data != null && resultCode == Activity.RESULT_OK) {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

                // Beri jeda 150 milidetik sebelum mengambil gambar
                // untuk memberi waktu dialog izin hilang sepenuhnya.
                Handler(Looper.getMainLooper()).postDelayed({
                    startCapture()
                }, 250)
            } else {
                // ✅ PERBAIKAN 2: TAMPILKAN KEMBALI JIKA PENGGUNA MEMBATALKAN
                floatingView.visibility = View.VISIBLE
            }
        } else {
            startForeground(1, createNotification())
        }
        return START_NOT_STICKY
    }

    private fun startCapture() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        mediaProjection?.createVirtualDisplay(
            "ScreenCapture", width, height, displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader?.surface, null, null
        )

        // ✅ --- PERBAIKAN UTAMA DI SINI --- ✅
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width

                val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(buffer)

                image.close()

                // Pindahkan logika ini ke dalam listener
                saveBitmap(bitmap)
                stopCapture() // Hentikan setelah gambar disimpan

                // Tampilkan kembali tombol setelah semuanya selesai
                Handler(Looper.getMainLooper()).post {
                    if (::floatingView.isInitialized) {
                        floatingView.visibility = View.VISIBLE
                    }
                }
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val fileName = "screenshot_${System.currentTimeMillis()}.png"
        var toastMessage = "Gagal menyimpan screenshot!" // Pesan default

        try {
            // Cek versi Android untuk menentukan metode penyimpanan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Metode untuk Android 10 (Q) ke atas
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots")
                }

                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri).use { outputStream ->
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            toastMessage = "Screenshot disimpan!"
                        }
                    }
                }

            } else {
                // Metode lama untuk Android 9 (Pie) ke bawah
                @Suppress("DEPRECATION")
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Screenshots"
                val dir = File(imagesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val imageFile = File(dir, fileName)
                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                // PENTING: Beri tahu galeri tentang file baru!
                MediaScannerConnection.scanFile(this, arrayOf(imageFile.toString()), null, null)
                toastMessage = "Screenshot disimpan!"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Tampilkan pesan hasil di Main Thread
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopCapture() {
        mediaProjection?.stop()
        mediaProjection = null
        imageReader?.close()
        imageReader = null
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel("FloatingWindowChannel", "Floating Window Service", NotificationManager.IMPORTANCE_DEFAULT)
            getSystemService(NotificationManager::class.java).createNotificationChannel(serviceChannel)
        }
        return NotificationCompat.Builder(this, "FloatingWindowChannel")
            .setContentTitle("Screenshot Service Aktif")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCapture()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}