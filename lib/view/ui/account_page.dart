import 'package:flutter/material.dart';
import 'package:screenshoot_besmart_indonesia_gemilang/view/ui/about_app_page.dart';
import 'package:screenshoot_besmart_indonesia_gemilang/view/ui/contact_us_page.dart';

class AccountPage extends StatelessWidget {
  const AccountPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          "Pengaturan",
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        backgroundColor: Colors.grey[50],
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16.0),
        children: [
          // Bagian untuk info login/register (dalam pengembangan)
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.blue[50],
              borderRadius: BorderRadius.circular(12),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "Fitur Akun",
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.blue[800],
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  "Login dan registrasi akan segera hadir untuk menyimpan pengaturan Anda di berbagai perangkat.",
                  style: TextStyle(fontSize: 14, color: Colors.blue[700]),
                ),
              ],
            ),
          ),

          const SizedBox(height: 24), // Memberi jarak
          // Menu Bantuan dan Informasi
          _buildSectionTitle(context, "Bantuan & Informasi"),
          const SizedBox(height: 8),
          _buildMenuItem(
            icon: Icons.support_agent_outlined,
            title: "Hubungi Kami",
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const ContactUsPage()),
              );

              // ScaffoldMessenger.of(context).showSnackBar(
              //   const SnackBar(content: Text('Membuka halaman Hubungi Kami...')),
              // );
            },
          ),
          const Divider(),
          _buildMenuItem(
            icon: Icons.info_outline,
            title: "Tentang Aplikasi",
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const AboutAppPage()),
              );

              // ScaffoldMessenger.of(context).showSnackBar(
              //   const SnackBar(content: Text('Membuka halaman Tentang Aplikasi...')),
              // );
            },
          ),
        ],
      ),
    );
  }

  // Helper widget untuk judul bagian
  Widget _buildSectionTitle(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Text(
        title.toUpperCase(),
        style: TextStyle(
          fontWeight: FontWeight.bold,
          color: Colors.grey[600],
          fontSize: 12,
        ),
      ),
    );
  }

  // Helper widget untuk membuat item menu
  Widget _buildMenuItem({
    required IconData icon,
    required String title,
    required VoidCallback onTap,
  }) {
    return ListTile(
      leading: Icon(icon, color: Colors.blue.shade700),
      title: Text(title, style: const TextStyle(fontSize: 16)),
      trailing: const Icon(Icons.chevron_right, color: Colors.grey),
      onTap: onTap,
    );
  }
}
