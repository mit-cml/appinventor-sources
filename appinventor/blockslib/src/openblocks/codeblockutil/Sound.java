// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import javax.sound.sampled.Clip;

public class Sound
{
    private Clip clip;

    public Sound(Clip clip)
    {
	this.clip = clip;
    }

    public void play()
    {
    	if(SoundManager.isSoundEnabled()){
			clip.setFramePosition(0);
			clip.loop(0);
    	}
    }
}