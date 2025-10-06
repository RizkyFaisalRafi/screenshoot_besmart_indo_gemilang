import 'dart:developer' as dev;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ScreenRecordingProvider extends ChangeNotifier {
  static const _platform = MethodChannel('com.rifara.screenshot_app/floating_window');

  bool _isRecording = false;
  bool get isRecording => _isRecording;

  Future<void> startRecording() async {
    try {
      // Sekarang 'started' akan menerima true/false dari onActivityResult
      final bool? started = await _platform.invokeMethod('startRecording');
      if (started ?? false) {
        _isRecording = true;
        notifyListeners();
        dev.log("Screen recording permission granted and service started.");
      } else {
        dev.log("Screen recording permission denied by user.");
      }
    } on PlatformException catch (e) {
      dev.log("Failed to start recording: '${e.message}'.");
    }
  }

  Future<void> stopRecording() async {
    try {
      // Kode ini sudah benar
      await _platform.invokeMethod('stopRecording');
      _isRecording = false;
      notifyListeners();
      dev.log("Screen recording stopped.");
    } on PlatformException catch (e) {
      dev.log("Failed to stop recording: '${e.message}'.");
    }
  }

  // Method untuk menyinkronkan status dari native (jika diperlukan)
  void updateRecordingStatus(bool status) {
    if (_isRecording != status) {
      _isRecording = status;
      notifyListeners();
    }
  }
}