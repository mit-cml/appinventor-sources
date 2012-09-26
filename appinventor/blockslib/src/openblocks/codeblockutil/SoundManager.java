// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/** Manages the sounds for StarLogoBlocks */
public class SoundManager
{
  public static boolean DEBUG = false;

  //set to true by default
  private static boolean enableSound = true;

  public static Sound loadSound(String soundFileName)
  {
    URL url = SoundManager.class.getResource(soundFileName);
    if (url == null) {
      System.out.println("Could not find resource " + soundFileName);
      return null;
    }

    AudioInputStream audioInputStream;

    try
    {
      audioInputStream = AudioSystem.getAudioInputStream(url);
    }
    catch(UnsupportedAudioFileException e)
    {
      e.printStackTrace();

      return null;
    }
    catch(IOException e)
    {
      e.printStackTrace();

      return null;
    }

    AudioFormat format = audioInputStream.getFormat();

    if (DEBUG) {
      System.out.println("Loading sound file \"" + url + "\"");
      System.out.println("Format = " + format);
    }

    Clip clip;

    try
    {
      DataLine.Info info = new DataLine.Info(Clip.class, format);

      clip = (Clip)AudioSystem.getLine(info);
      clip.open(audioInputStream);
    }
    catch(LineUnavailableException e)
    {
      System.out.println("Sound failed to play: System sound is not available.");
      return null;
    }
    catch(IOException e)
    {
      e.printStackTrace();

      return null;
    }

    return new Sound(clip);
  }

  /**
   * Sets the ability to enable sound within the entire codeblocks library
   * If enableSound is set to false, no sounds can be played/heard until 
   * enableSound is set to true again.
   * @param enableSound
   */
  public static void setSoundEnabled(boolean enableSound){
    SoundManager.enableSound = enableSound;
  }

  /**
   * Returns true iff sounds are being allowed to play.
   * @return true iff sounds are being allowed to play.
   */
  public static boolean isSoundEnabled(){
    return SoundManager.enableSound;
  }
}