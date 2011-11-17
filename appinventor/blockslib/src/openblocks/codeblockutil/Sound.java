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