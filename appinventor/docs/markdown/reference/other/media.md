---
title: Accessing Images and Sounds
layout: documentation
---

Applications built with App Inventor can access sound, image, and video sources from three different kinds of locations:

Application assets

: The sources labeled *Media* shown in the designer — part of the application's *assets* — are packaged with the application. Anyone who installs your application will have them, as part of the application. You also specify them in the designer, which is convenient. You can also specify these in programs by their file name: just use the file name without any special prefix. For example, if you have an image asset named *kitty.png*, you can use it as an image: just set the `Picture` property of an image component to the text *kitty.png*. You can similarly use files names for sound (Sound or Player) or video (VideoPlayer).

  Assets are the most convenient to use, but the space for them is limited to a few megabytes, because they must be packaged with the application. They are good for small images and short audio selections. Bit you would probably not use them for complete songs or videos.

The device storage

: You can access files on your phone's SD (secure digital) card using file names that begin with */sdcard*. You could play a song on your SDCard by setting the source of a `Player` component to

  /sdcard/Music/Blondie/The Best of Blondie/Heart of Glass.mp3

  and starting the `Player` (assuming of course, that the song file is on the SDCard). Make sure to specify the complete file name, including the "mp3".

  The Android system also includes an alternative way to designe SDCard files as URLs. Here you prefix the file name with *file:///sdcard* and use "URL encoding" for special characters. For example, a space is "%20". So you could designate the same file by setting the player source to

  file:///sdcard/Music/Blondie/The%20Best%20of%20Blondie/Heart%20of%20Glass.mp3

  Note that you'll want to use a `Player` component for this, not `Sound`. A complete song like this is too large for `Sound` to handle.

  Images and videos can be designated similarly.

  App Inventor doesn't (yet) include any way to store files on the SD card. It also doesn't (yet) include a way to list the files on the SDCard. You'll have to use other applications or the Android phone file manager for that.

  Using the SD Card provides a lot more space for media than trying to package things as assets. The drawback is that users won't automatically get them by installing your application.

URLs and the Web

: You can access files on Web using URLs, starting with *http://*, for example, setting the picture property of an image to

  http://www.google.com/images/srpr/nav_logo14.png

  and similarly for music and videos. Make sure you use the link that points to the actual file, not to players for the files, which is much more common on the Web, especially for music and videos.

Other content URLs

: The Android system also uses URLs to access various places that media is stored on the phone. For example, the images in the photo gallery can be accessed with file names beginning *content://media/external/images/media*, as you can see by using the `ImagePicker` and examining the resulting image path.
