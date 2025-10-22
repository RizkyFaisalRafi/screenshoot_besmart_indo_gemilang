package com.rifara.screenshootBesmartIndonesiaGemilang

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle

class ScreenshotActivity : Activity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // Langsung minta izin saat Activity dibuat
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Izin BERHASIL, kirim data ke ScreenRecordingService untuk dieksekusi
                val serviceIntent = Intent(this, ScreenRecordingService::class.java).apply {
                    action = ScreenRecordingService.ACTION_CAPTURE_SINGLE_FRAME
                    putExtra(ScreenRecordingService.EXTRA_RESULT_CODE, resultCode)
                    putExtra(ScreenRecordingService.EXTRA_RESULT_DATA, data)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } else {
                // Izin GAGAL/DIBATALKAN, kirim perintah untuk menampilkan kembali tombol
                val showButtonIntent = Intent(this, FloatingWindowService::class.java).apply {
                    action = "show"
                }
                startService(showButtonIntent)
            }
            // Tutup activity transparan ini setelah selesai
            finish()
        }
    }

    companion object {
        private const val REQUEST_CODE = 101
    }
}