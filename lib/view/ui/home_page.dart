import 'dart:developer' as dev;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';
import '../provider/screen_recording_provider.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const platform = MethodChannel(
    'com.rifara.screenshot_app/floating_window',
  );

  @override
  void initState() {
    super.initState();
    _requestPermissions();
  }

  // DIUBAH: Menambahkan izin microphone
  // void _requestPermissions() async {
  //   await Permission.systemAlertWindow.request();
  //   // Untuk Android 10 ke bawah
  //   await Permission.storage.request();
  //   // Untuk Android 11 ke atas
  //   await Permission.manageExternalStorage.request();
  //   // Izin untuk merekam suara
  //   await Permission.microphone.request();
  // }

  void _requestPermissions() async {
    // Izin untuk menampilkan jendela mengambang
    await Permission.systemAlertWindow.request();

    // Izin untuk merekam suara
    await Permission.microphone.request();

    // Untuk Android 13+, kita perlu izin notifikasi untuk foreground service
    if (await Permission.notification.isDenied) {
      await Permission.notification.request();
    }
  }

  Future<void> _startService() async {
    if (await Permission.systemAlertWindow.isGranted) {
      try {
        await platform.invokeMethod('startService');
      } on PlatformException catch (e) {
        dev.log("Failed to start service: '${e.message}'.");
      }
    } else {
      dev.log("Izin System Alert Window belum diberikan.");
    }
  }

  Future<void> _stopService() async {
    try {
      await platform.invokeMethod('stopService');
    } on PlatformException catch (e) {
      dev.log("Failed to stop service: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<ScreenRecordingProvider>(
      builder: (context, recorder, child) {
        return Scaffold(
          appBar: AppBar(
            title: const Text(
              "Screenshot Smart",
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            backgroundColor: Colors.grey[50],
            elevation: 0,
          ),
          body: Stack(
            children: [
              Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: [Color(0xFF64B5F6), Color(0xFF1976D2)],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
              ),
              Center(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 24.0),
                  child: SingleChildScrollView(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(
                          Icons.camera_alt_outlined,
                          size: 100,
                          color: Colors.white70,
                        ),
                        const SizedBox(height: 20),
                        const Text(
                          'Screenshot Smart',
                          style: TextStyle(
                            fontSize: 28,
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                          ),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 10),
                        const Text(
                          'Aktifkan fitur untuk mengambil screenshot atau merekam layar kapan saja.',
                          style: TextStyle(fontSize: 16, color: Colors.white70),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 40),
                        _buildServiceButton(
                          onPressed: _startService,
                          icon: Icons.play_circle_outline,
                          label: 'Mulai Jendela Screenshot',
                          backgroundColor: Colors.white,
                          foregroundColor: Colors.blue.shade700,
                        ),
                        const SizedBox(height: 20),

                        recorder.isRecording
                            ? _buildServiceButton(
                                onPressed: recorder.stopRecording,
                                icon: Icons.stop_circle_outlined,
                                label: 'Hentikan Perekaman',
                                backgroundColor: Colors.orange.shade700,
                                foregroundColor: Colors.white,
                              )
                            : _buildServiceButton(
                                onPressed: recorder.startRecording,
                                icon: Icons.videocam_outlined,
                                label: 'Mulai Rekam Layar',
                                backgroundColor: Colors.red.shade400,
                                foregroundColor: Colors.white,
                              ),
                        const SizedBox(height: 20),
                        _buildServiceButton(
                          onPressed: _stopService,
                          icon: Icons.cancel_outlined,
                          label: 'Tutup Jendela Screenshot',
                          backgroundColor: Colors.grey.shade600,
                          foregroundColor: Colors.white,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildServiceButton({
    required VoidCallback onPressed,
    required IconData icon,
    required String label,
    required Color backgroundColor,
    required Color foregroundColor,
  }) {
    return ElevatedButton.icon(
      onPressed: onPressed,
      icon: Icon(icon, size: 24),
      label: Text(label),
      style: ElevatedButton.styleFrom(
        backgroundColor: backgroundColor,
        foregroundColor: foregroundColor,
        minimumSize: const Size(double.infinity, 50),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
        elevation: 5,
      ),
    );
  }
}
