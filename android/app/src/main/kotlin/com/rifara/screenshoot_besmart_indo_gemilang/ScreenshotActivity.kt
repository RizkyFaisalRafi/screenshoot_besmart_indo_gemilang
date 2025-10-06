package com.rifara.screenshoot_besmart_indonesia_gemilang

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle

class ScreenshotActivity : Activity() {
    companion object {
        const val REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Langsung minta izin saat Activity dibuat
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_CODE
        )
    }

    // Tangkap hasil dari dialog izin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Jika pengguna setuju, kirim data izin ke Service
            val serviceIntent = Intent(this, FloatingWindowService::class.java).apply {
                action = "CAPTURE"
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
            startService(serviceIntent)
        }
        // Langsung tutup activity ini apapun hasilnya
        finish()
    }
}