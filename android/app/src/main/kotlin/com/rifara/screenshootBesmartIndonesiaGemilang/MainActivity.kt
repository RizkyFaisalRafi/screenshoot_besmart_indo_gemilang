package com.rifara.screenshootBesmartIndonesiaGemilang

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.rifara.screenshot_app/floating_window"
    private var mediaProjectionManager: MediaProjectionManager? = null
    private val REQUEST_MEDIA_PROJECTION = 1

    private var startRecordingResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "startService" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivityForResult(intent, 123)
                    } else {
                        startFloatingWindowService()
                    }
                    result.success(null)
                }

                "stopService" -> {
                    stopFloatingWindowService()
                    result.success(null)
                }

                "startRecording" -> {
                    this.startRecordingResult = result
                    startActivityForResult(
                        mediaProjectionManager?.createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION
                    )
                }

                "stopRecording" -> {
                    val stopIntent = Intent(this, ScreenRecordingService::class.java).apply {
                        action = ScreenRecordingService.ACTION_STOP
                    }
                    startService(stopIntent)
                    result.success(true)
                }

                else -> result.notImplemented()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val serviceIntent = Intent(this, ScreenRecordingService::class.java).apply {
                    action = ScreenRecordingService.ACTION_START
                    putExtra(ScreenRecordingService.EXTRA_RESULT_CODE, resultCode)
                    putExtra(ScreenRecordingService.EXTRA_RESULT_DATA, data)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                startRecordingResult?.success(true)
            } else {
                startRecordingResult?.success(false)
            }
            startRecordingResult = null
        } else if (requestCode == 123) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startFloatingWindowService()
            }
        }
    }

    private fun startFloatingWindowService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFloatingWindowService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        stopService(intent)
    }
}