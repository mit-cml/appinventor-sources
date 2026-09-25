/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for loading, storing, and playing audio for a workspace.
 */
export declare class WorkspaceAudio {
    private parentWorkspace;
    /** Database of pre-loaded sounds. */
    private sounds;
    /** Time that the last sound was played. */
    private lastSound;
    /** Whether the audio is muted or not. */
    private muted;
    /** Audio context used for playback. */
    private readonly context?;
    /**
     * @param parentWorkspace The parent of the workspace this audio object
     *     belongs to if it has one, or the workspace that owns this instance.
     */
    constructor(parentWorkspace: WorkspaceSvg);
    /**
     * Dispose of this audio manager.
     *
     * @internal
     */
    dispose(): void;
    /**
     * Load an audio file.  Cache it, ready for instantaneous playing.
     *
     * @param filenames Single-item array containing the URL for the sound file.
     *     Any items after the first item are ignored.
     * @param name Name of sound.
     */
    load(filenames: string[], name: string): Promise<void>;
    /**
     * Play a named sound at specified volume.  If volume is not specified,
     * use full volume (1).
     *
     * @param name Name of sound.
     * @param opt_volume Volume of sound (0-1).
     */
    play(name: string, opt_volume?: number): Promise<void>;
    /**
     * Plays a beep at the given frequency.
     *
     * @param tone The frequency of the beep to play, in hertz.
     * @param duration The duration of the beep, in seconds. Defaults to 0.2.
     */
    beep(tone: number, duration?: number): Promise<void>;
    /**
     * Plays a standard error beep.
     */
    playErrorBeep(): Promise<void>;
    /**
     * If enabled, plays a tone corresponding to the nesting level of the given
     * node when it differs from the nesting level of the currently focused node.
     * These tones are generally used for accessibility purposes to indicate a
     * scope transition to users who use a screenreader. This method must be
     * called before focus transitions to the given node.
     *
     * @internal
     * @param newNode The soon-to-be-focused node.
     */
    maybePlayScopeChangeAudioCue(newNode: IFocusableNode): void;
    /**
     * Returns whether or not playing sounds is currently allowed.
     *
     * @returns False if audio is muted or a sound has just been played, otherwise
     *     true.
     */
    private isPlayingAllowed;
    /**
     * Prepares to play audio by recording the time of the last play and resuming
     * the audio context.
     */
    private prepareToPlay;
    /**
     * @param muted If true, mute sounds. Otherwise, play them.
     */
    setMuted(muted: boolean): void;
    /**
     * @returns Whether the audio is currently muted or not.
     */
    getMuted(): boolean;
}
//# sourceMappingURL=workspace_audio.d.ts.map