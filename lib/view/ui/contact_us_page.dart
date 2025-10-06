import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:provider/provider.dart';
import '../../utils/components/spaces.dart';
import '../../utils/design_systems/theme.dart';
import '../provider/contact_us_provider.dart';

class ContactUsPage extends StatefulWidget {
  const ContactUsPage({super.key});

  @override
  State<ContactUsPage> createState() => _ContactUsPageState();
}

class _ContactUsPageState extends State<ContactUsPage> {
  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<ContactUsProvider>(context, listen: false);
    return Scaffold(
      appBar: AppBar(
        title: Text(
          'Hubungi Kami',
          // AppLocalizations.of(context)!.titleContactUs,
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(defaultMargin),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Untuk keluhan, kritik, dan saran dalam penggunaan aplikasi Screenshot Smart, Anda dapat menghubungi:',
              // AppLocalizations.of(context)!.titleComplaintDescription,
              style: fontPoppins.copyWith(fontSize: sizeSmall),
            ),

            const SpaceHeight(8),

            InkWell(
              borderRadius: BorderRadius.circular(15),
              onTap: () {
                provider.launchWhatsApp();
              },
              child: Card(
                child: ListTile(
                  title: Text(
                    '0895412892094',
                    // AppLocalizations.of(context)!.numberDeveloper,
                    style: fontPoppins.copyWith(fontSize: sizeSmall),
                  ),
                  leading: SvgPicture.asset('assets/icons/wa_icon.svg'),
                ),
              ),
            ),

            // Card Number Phone
            // InkWell(
            //   onTap: () {
            //     provider.launchPhone();
            //   },
            //   child: Card(
            //     child: ListTile(
            //       title: Text(
            //         '0895412892094',
            //         // AppLocalizations.of(context)!.numberDeveloper,
            //       ),
            //       leading: SvgPicture.asset(
            //         'assets/icons/contact_us_profil.svg',
            //       ),
            //     ),
            //   ),
            // ),

            const SpaceHeight(4),

            // Card Email
            InkWell(
              borderRadius: BorderRadius.circular(15),
              onTap: () async {
                provider.launchEmail();
              },
              child: Card(
                child: ListTile(
                  title: Text(
                    'rizkyfaisalrafi123@gmail.com',
                    // AppLocalizations.of(context)!.titleEmailDeveloperOnly,
                    style: fontPoppins.copyWith(fontSize: sizeSmall),
                  ),
                  leading: SvgPicture.asset('assets/icons/email_icon.svg'),
                ),
              ),
            ),

            const SpaceHeight(8),

            // Deskripsi Jam Kerja
            Text(
              'Hubungi saya pada saat jam kerja:\n09:00 - 16:00 WIB',
              // AppLocalizations.of(context)!.titleWorkHourDescription,
              style: fontPoppins.copyWith(fontSize: sizeSmall),
            ),
          ],
        ),
      ),
    );
  }
}
