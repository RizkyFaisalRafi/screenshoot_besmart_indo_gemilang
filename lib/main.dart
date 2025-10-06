import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:screenshoot_besmart_indonesia_gemilang/bottomNavBar/main_page.dart';
import 'package:screenshoot_besmart_indonesia_gemilang/view/provider/about_app_provider.dart';
import 'view/provider/contact_us_provider.dart';
import 'view/provider/screen_recording_provider.dart';

void main() {
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
