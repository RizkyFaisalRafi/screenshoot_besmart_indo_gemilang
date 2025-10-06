import 'package:flutter/material.dart';
import 'package:persistent_bottom_nav_bar_v2/persistent_bottom_nav_bar_v2.dart';

import '../view/ui/account_page.dart';
import '../view/ui/home_page.dart';

class MainPage extends StatefulWidget {
  const MainPage({super.key});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  // Controller tetap berguna untuk berpindah tab secara programatik
  late PersistentTabController _controller;

  @override
  void initState() {
    super.initState();
    _controller = PersistentTabController(initialIndex: 0);
  }

  @override
  Widget build(BuildContext context) {
    return PersistentTabView(
      controller: _controller,
      tabs: [
        // Tab 1: Beranda
        PersistentTabConfig(
          screen: const HomePage(),
          item: ItemConfig(
            icon: const Icon(Icons.home),
            title: "Beranda",
            activeForegroundColor: Colors.blue,
            inactiveForegroundColor: Colors.grey,
          ),
        ),

        // Tab 2: Akun
        PersistentTabConfig(
          screen: const AccountPage(),
          item: ItemConfig(
            icon: const Icon(Icons.person),
            title: "Akun",
            activeForegroundColor: Colors.blue,
            inactiveForegroundColor: Colors.grey,
          ),
        ),
      ],
      // navBarBuilder adalah cara baru untuk menentukan style
      navBarBuilder: (navBarConfig) =>
          Style4BottomNavBar(navBarConfig: navBarConfig),
      // Konfigurasi tambahan lainnya
      backgroundColor: Colors.white,
      handleAndroidBackButtonPress: true,
      resizeToAvoidBottomInset: true,
      stateManagement: true,
      // hideNavigationBarWhenKeyboardShows: true,
      // popAllScreensOnTapOfSelectedTab: true,
      screenTransitionAnimation: const ScreenTransitionAnimation(
        // animateTabTransition: true,
        curve: Curves.ease,
        duration: Duration(milliseconds: 200),
      ),
    );
  }
}
