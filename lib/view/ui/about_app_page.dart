import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../provider/about_app_provider.dart';

class AboutAppPage extends StatelessWidget {
  const AboutAppPage({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<AboutAppProvider>(context, listen: false);
    return Scaffold(
      appBar: AppBar(
        title: Text(
          'Tentang Aplikasi',
          // AppLocalizations.of(context)!.titleAboutApp,
        ),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Header
            Container(
              padding: const EdgeInsets.all(16.0),
              decoration: const BoxDecoration(
                color: Colors.blueAccent,
                borderRadius: BorderRadius.vertical(
                  bottom: Radius.circular(20.0),
                ),
              ),
              child: Column(
                children: [
                  Image.asset('assets/ss_logo.png', height: 100),
                  const SizedBox(height: 10),
                  Text(
                    'Screenshot Smart',
                    // AppLocalizations.of(context)!.titleDicodingStoryApp,
                    style: const TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 5),
                  Text(
                    'Versi 1.0.0',
                    // AppLocalizations.of(context)!.titleVersionApp,
                    style: const TextStyle(fontSize: 16, color: Colors.white70),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Deskripsi
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0),
              child: Text(
                'Screenshot Smart adalah utilitas canggih yang memungkinkan Anda mengambil tangkapan layar dengan mudah melalui jendela mengambang yang praktis. Dirancang untuk efisiensi, aplikasi ini adalah alat bantu produktivitas esensial Anda.',
                // AppLocalizations.of(context)!.titleContentAboutApp,
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 16),
              ),
            ),
            const SizedBox(height: 20),

            // Informasi Pengembang
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  Text(
                    'Dikembangkan oleh:',
                    // AppLocalizations.of(context)!.titleDevelopedBy,
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 10),
                  const CircleAvatar(
                    radius: 50,
                    backgroundImage: AssetImage(
                      'assets/images/photo_with_almet.png',
                    ),
                  ),
                  const SizedBox(height: 10),
                  Text(
                    'Rizky Faisal Rafi, S.Kom',
                    // AppLocalizations.of(context)!.titleNameDeveloper,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Text(
                    'E-mail: rizkyfaisalrafi123@gmail.com',
                    // AppLocalizations.of(context)!.titleEmailDeveloper,
                    style: TextStyle(color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // Media Sosial
            Text(
              'Ikuti saya di media sosial:',
              // AppLocalizations.of(context)!.titleFollowSocialMedia,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  icon: const Icon(Icons.facebook, color: Colors.blue),
                  onPressed: () {
                    // Tambahkan link ke akun Facebook
                    provider.launchFacebook();
                  },
                ),
                IconButton(
                  icon: const Icon(
                    Icons.dataset_linked_rounded,
                    color: Colors.blue,
                  ),
                  onPressed: () {
                    // Tambahkan link ke akun Linkedin
                    provider.launchLinkedIn();
                  },
                ),
              ],
            ),
            const SizedBox(height: 20),

            // Footer
            Text(
              '© 2025 Rizky Faisal Rafi.',
              // AppLocalizations.of(context)!.footerAboutApp,
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey[600]),
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }
}
