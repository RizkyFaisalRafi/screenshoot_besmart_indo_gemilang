package com.rifara.screenshoot_besmart_indonesia_gemilang

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues // <-- TAMBAHKAN INI
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore // <-- TAMBAHKAN INI
import androidx.core.app.NotificationCompat
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenRecordingService : Service() {

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private val notificationChannelId = "ScreenRecordServiceChannel"
    private val notificationId = 12345

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
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
                    val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                    if (resultData != null) {
                        startForeground(notificationId, createNotification())
                        startRecording(resultCode, resultData)
                    }
                }

                ACTION_STOP -> {
                    stopRecording()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startRecording(resultCode: Int, data: Intent) {
        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)

        // Persiapkan MediaStore untuk menyimpan video
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "ScreenRecord-$timeStamp.mp4"

        // 1. Buat metadata untuk file video
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            // Simpan di dalam folder Movies
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            }
        }

        // 2. Daftarkan file baru ke MediaStore dan dapatkan URI-nya
        val resolver = applicationContext.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri == null) {
            // Gagal membuat file di MediaStore, hentikan proses
            stopSelf()
            return
        }

        // 3. Inisialisasi MediaRecorder
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val screenDensity = metrics.densityDpi

        mediaRecorder?.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.MIC) // Pastikan ini aktif jika ingin ada suara
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            // 4. Gunakan FileDescriptor dari URI yang didapat dari MediaStore
            val parcelFileDescriptor = resolver.openFileDescriptor(uri, "w")
            if (parcelFileDescriptor != null) {
                setOutputFile(parcelFileDescriptor.fileDescriptor)
            } else {
                // Gagal mendapatkan file descriptor, hentikan
                stopSelf()
                return
            }

            setVideoSize(screenWidth, screenHeight)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC) // Pastikan ini aktif jika ingin ada suara
            setVideoEncodingBitRate(512 * 1000)
            setVideoFrameRate(30)
            try {
                prepare()
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf() // Hentikan jika gagal prepare
                return
            }
        }

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecord",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface,
            null,
            null
        )

        mediaRecorder?.start()

        Toast.makeText(this, "Rekaman layar dengan suara dimulai", Toast.LENGTH_LONG).show()

    }

    private fun stopRecording() {
        if (mediaRecorder == null) {
            return
        }

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
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Merekam Layar")
            .setContentText("Layanan perekaman layar sedang berjalan.")
            .setSmallIcon(R.mipmap.ic_launcher) // Pastikan Anda memiliki ikon ini
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }
}