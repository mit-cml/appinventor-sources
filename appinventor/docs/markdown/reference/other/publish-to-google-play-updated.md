---
title: Uploading Your Apps to Google Play
layout: documentation
---

# Uploading Your Apps to Google Play (Updated for 2025)

> This page replaces the previous Google Play publishing guide, which referenced APK-based uploads. Updated for Android App Bundles (AAB) in 2025.

Applications built with MIT App Inventor can be published on **Google Play**.  
You’ll need to register as a [Google Play Developer](https://play.google.com/console/signup) and pay a one-time $25 fee.

---

## Before You Begin

> **Important:** As of **August 2021**, all new apps uploaded to Google Play must use **Android App Bundles (.aab)** instead of traditional APKs.  
> Existing apps that were already published as APKs can continue to update using APK for now, but new apps *must* be submitted as `.aab`.

If your exported App Inventor build is an **APK**, you can use **Android Studio** to convert it to an **App Bundle** before uploading.  
Learn more: [Use Android App Bundles](https://developer.android.com/guide/app-bundle)

---

## Versioning Your App

Every app published on Google Play must define a **VersionCode** and a **VersionName**.

In the MIT App Inventor Designer view:
- **VersionCode** → an integer (1, 2, 3, …). It must increase with each new release.  
- **VersionName** → a string such as “1.0”, “1.1”, “2.0”, etc.

When updating your app on Google Play:
- Increment **VersionCode**
- Update **VersionName**
before exporting a new build.

---

## Exporting and Preparing Your Build

1. In App Inventor, click **Build → App (provide QR code for .apk)** or **Build → App (save .apk to my computer)**.  
2. This exports your app as an **APK** file.  
3. To publish on Google Play:
   - Import the APK into **Android Studio**.  
   - Follow Android Studio’s wizard to generate an **Android App Bundle (.aab)** file.  
   - Save the `.aab` to your computer.

---

## Publishing to Google Play

1. Go to [Google Play Console](https://play.google.com/console/).  
2. Click **Create App**, then enter app name, language, and category.  
3. Under **Production → Create New Release**, upload your **.aab** file.  
4. Complete:
   - App details and screenshots  
   - Content rating questionnaire  
   - Privacy policy link  
   - Target devices and permissions  
5. Ensure your app targets **Android API Level 33 (Android 13)** or higher.  
6. Click **Review and Publish** when all checks pass.

---

## App Signing

Google Play now requires **Play App Signing**.  
When you upload an AAB, Google securely manages your signing key and automatically generates optimized APKs for users’ devices.

You can still download and back up your **MIT App Inventor keystore** from the *Projects → Download Keystore* menu.  
Store it safely if you ever migrate or rebuild your project.

---

## Backups

Once published, users depend on your app’s maintenance.  
Always back up your project’s source code (`.aia` file):

1. In **My Projects**, check your project.  
2. Click **Project → Export selected project (.aia)**  
3. Save it securely — it contains your app’s source.

MIT App Inventor makes best efforts to protect projects, but **backups are your responsibility.**

---

## Note on Large Apps

If your app exceeds **150 MB**, use **Play Asset Delivery** or **Play Feature Delivery**.  
These allow modular delivery of additional assets without exceeding size limits.

---

## License

This documentation is licensed under a  
[Creative Commons Attribution-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-sa/4.0/).

© 2012–2025 Massachusetts Institute of Technology
