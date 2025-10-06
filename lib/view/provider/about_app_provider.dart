import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class AboutAppProvider extends ChangeNotifier {
  // Function launch facebook
  void launchFacebook() async {
    // https://www.facebook.com/alrizky.faisalhansome?mibextid=ZbWKwL
    String url = "www.facebook.com/alrizky.faisalhansome?mibextid=ZbWKwL";
    final Uri facebookUrl = Uri(scheme: 'https', path: url);

    if (await canLaunchUrl(facebookUrl)) {
      await launchUrl(facebookUrl, mode: LaunchMode.externalApplication);
    } else {
      throw 'Could not launch $facebookUrl';
    }
  }

  // Function launch linkedin
  void launchLinkedIn() async {
    // https://www.linkedin.com/in/rizky-faisal-rafi-8691a7225
    String url = "www.linkedin.com/in/rizky-faisal-rafi-8691a7225";
    final Uri linkedInUrl = Uri(scheme: 'https', path: url);

    if (await canLaunchUrl(linkedInUrl)) {
      await launchUrl(linkedInUrl, mode: LaunchMode.externalApplication);
    } else {
      throw 'Could not launch $linkedInUrl';
    }
  }
}
