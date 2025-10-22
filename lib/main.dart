import 'dart:ui';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:screenshootBesmartIndonesiaGemilang/bottomNavBar/main_page.dart';
import 'package:screenshootBesmartIndonesiaGemilang/view/provider/about_app_provider.dart';
import 'firebase_options.dart';
import 'view/provider/contact_us_provider.dart';
import 'view/provider/screen_recording_provider.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  // Pass all uncaught "fatal" errors from the framework to Crashlytics
  // FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterFatalError;
  FlutterError.onError = (errorDetails) {
    FirebaseCrashlytics.instance.recordFlutterFatalError(errorDetails);
  };
  // Pass all uncaught asynchronous errors that aren't handled by the Flutter framework to Crashlytics
  PlatformDispatcher.instance.onError = (error, stack) {
    FirebaseCrashlytics.instance.recordError(error, stack, fatal: true);
    return true;
  };

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ContactUsProvider()),
        ChangeNotifierProvider(create: (_) => AboutAppProvider()),
        ChangeNotifierProvider(create: (_) => ScreenRecordingProvider()),
      ],
      child: MaterialApp(
        // Hilangkan banner debug untuk tampilan yang lebih bersih
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          // Atur warna utama aplikasi
          primarySwatch: Colors.blue,
          // Atur font default agar lebih modern
          fontFamily: 'Roboto',
        ),
        home: MainPage(),
      ),
    );
  }
}
