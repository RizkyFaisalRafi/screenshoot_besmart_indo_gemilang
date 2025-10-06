import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class ContactUsProvider extends ChangeNotifier {
  // Function launch WA
  void launchWhatsApp() async {
    // https://wa.link/w60w0o
    String url = "wa.me/62895412892094";
    final Uri whatsappUrl = Uri(scheme: 'https', path: url);

    if (await canLaunchUrl(whatsappUrl)) {
      await launchUrl(whatsappUrl, mode: LaunchMode.externalApplication);
    } else {
      throw 'Could not launch $whatsappUrl';
    }
  }

  // Function launch PhoneNumber
  void launchPhone() async {
    final Uri url = Uri(scheme: 'tel', path: "0895412892094");

    if (await canLaunchUrl(url)) {
      await launchUrl(url);
    } else {
      throw 'Could not launch $url';
    }
  }

  // Function launch Email
  void launchEmail() async {
    String? encodeQueryParameters(Map<String, String> params) {
      return params.entries
          .map((MapEntry<String, String> e) =>
              '${Uri.encodeComponent(e.key)}=${Uri.encodeComponent(e.value)}')
          .join('&');
    }

    final Uri emailUri = Uri(
      scheme: 'mailto',
      path: 'rizkyfaisalrafi123@gmail.com',
      query: encodeQueryParameters(<String, String>{
        'subject': 'Send your Subject',
        'body': 'Your Description',
      }),
    );

    try {
      await launchUrl(emailUri);
    } catch (e) {
      debugPrint(e.toString());
    }
  }
}
