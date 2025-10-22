import 'dart:developer' as dev;
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';
import 'package:pull_to_refresh/pull_to_refresh.dart';
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

  // Controller untuk SmartRefresher
  final RefreshController _refreshController = RefreshController(
    initialRefresh: false,
  );

  @override
  void initState() {
    super.initState();
    // Memeriksa izin setelah frame pertama selesai dibangun
    // untuk memastikan 'context' sudah tersedia.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkAndShowPermissionSheet();
    });
  }

  /// Fungsi yang akan dipanggil saat pengguna melakukan pull-to-refresh.
  void _onRefresh() async {
    // Lakukan pengecekan izin lagi.
    await _checkAndShowPermissionSheet();
    // Beri tahu controller bahwa proses refresh telah selesai.
    _refreshController.refreshCompleted();
  }

  /// Memeriksa status izin dan menampilkan bottom sheet jika diperlukan.
  Future<void> _checkAndShowPermissionSheet() async {
    final systemAlertStatus = await Permission.systemAlertWindow.status;
    final microphoneStatus = await Permission.microphone.status;
    final notificationStatus = await Permission.notification.status;

    // Jika salah satu izin belum diberikan, tampilkan bottom sheet.
    if (!systemAlertStatus.isGranted ||
        !microphoneStatus.isGranted ||
        !notificationStatus.isGranted) {
      if (mounted) {
        // 'mounted' digunakan untuk memastikan widget masih ada di tree.
        _showPermissionBottomSheet();
      }
    }
  }

  /// Menampilkan BottomSheet yang berisi informasi dan tombol permintaan izin.
  void _showPermissionBottomSheet() {
    showModalBottomSheet(
      context: context,
      isDismissible: false,
      // Mencegah sheet ditutup dengan swipe
      enableDrag: false,
      // Mencegah sheet ditutup dengan drag
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        return PopScope(
          canPop: false, // Mencegah sheet ditutup dengan tombol kembali
          child: Container(
            padding: const EdgeInsets.all(24.0),
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  const Icon(
                    Icons.shield_outlined,
                    size: 50,
                    color: Colors.blue,
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    'Izin Diperlukan',
                    style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Aplikasi ini memerlukan beberapa izin untuk dapat berfungsi dengan baik.',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 16, color: Colors.black54),
                  ),
                  const SizedBox(height: 24),
                  _buildPermissionItem(
                    icon: Icons.layers,
                    title: 'Tampil di Atas Aplikasi Lain',
                    subtitle: 'Untuk menampilkan ikon screenshot melayang.',
                  ),
                  _buildPermissionItem(
                    icon: Icons.mic,
                    title: 'Mikrofon',
                    subtitle: 'Untuk merekam audio saat merekam layar.',
                  ),
                  _buildPermissionItem(
                    icon: Icons.notifications,
                    title: 'Notifikasi',
                    subtitle:
                        'Penting untuk menjalankan layanan di latar belakang.',
                  ),
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed: () async {
                      Navigator.pop(context); // Tutup bottom sheet
                      await _requestPermissions(); // Minta izin yang diperlukan
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      foregroundColor: Colors.white,
                      minimumSize: const Size(double.infinity, 50),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    child: const Text(
                      'Berikan Izin',
                      style: TextStyle(fontSize: 16),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  /// Widget helper untuk menampilkan setiap item izin di dalam bottom sheet.
  Widget _buildPermissionItem({
    required IconData icon,
    required String title,
    required String subtitle,
  }) {
    return ListTile(
      leading: Icon(icon, color: Colors.blue.shade700),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
      subtitle: Text(subtitle),
      contentPadding: EdgeInsets.zero,
    );
  }

  /// Fungsi utama untuk meminta semua izin yang diperlukan.
  Future<void> _requestPermissions() async {
    await Permission.systemAlertWindow.request();
    await Permission.microphone.request();
    await Permission.notification.request();
  }

  Future<void> _startService() async {
    // Periksa kembali izin sebelum memulai service
    if (await Permission.systemAlertWindow.isGranted) {
      try {
        await platform.invokeMethod('startService');
      } on PlatformException catch (e) {
        dev.log("Failed to start service: '${e.message}'.");
      }
    } else {
      dev.log("Izin System Alert Window belum diberikan.");
      // Jika izin belum ada, panggil kembali pengecekan untuk menampilkan sheet.
      _checkAndShowPermissionSheet();
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
          body: SmartRefresher(
            controller: _refreshController,
            onRefresh: _onRefresh,
            header: const WaterDropHeader(), // Header animasi saat refresh
            child: Stack(
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
                            style: TextStyle(
                              fontSize: 16,
                              color: Colors.white70,
                            ),
                            textAlign: TextAlign.center,
                          ),
                          const SizedBox(height: 40),

                          /// Test Firebase Crashlytics
                          // TextButton(
                          //   onPressed: () {
                          //     // FirebaseCrashlytics.instance.crash();
                          //     throw Exception();
                          //   },
                          //   child: const Text("Throw Test Exception"),
                          // ),
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
